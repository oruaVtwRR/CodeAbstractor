package parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import common.CommonStrings;
import common.NodeHelper;
import common.PathHelper;
import resolver.NodeIndexResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassParser extends Thread {

    private final File file;
    private final String className;
    private final NodeIndexResolver nodeIndexResolver;
    HashMap<String, Integer> classInstanceIndexMap;
    HashMap<String, Integer> methodInstanceIndexMap;
    HashMap<String, Integer> objectInstanceIndexMap;

    public ClassParser(File file, String className){
        this.file = file;
        this.className = className;
        this.nodeIndexResolver = new NodeIndexResolver();
        classInstanceIndexMap = new HashMap<>();
        methodInstanceIndexMap = new HashMap<>();
        objectInstanceIndexMap = new HashMap<>();
    }

    /**
     *
     * use to process all classes contained in the file
     * @param file
     */
    public ClassParser(File file){
        this(file, CommonStrings.EMPTYSTRING);
    }

    public void run() {
        TypeSolver typeSolver = new ReflectionTypeSolver();
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        try{
            System.out.println(file.getAbsolutePath());
            CompilationUnit compilationUnit = StaticJavaParser.parse(file);
            classInstanceIndexMap = collectQualifiedClassNamesWithIndex(compilationUnit);
            methodInstanceIndexMap = collectQualifiedMethodNamesWithIndex(compilationUnit);
            objectInstanceIndexMap = collectQualifiedObjectsWithIndex(compilationUnit);

            for(ClassOrInterfaceDeclaration classDeclaration: compilationUnit.findAll(ClassOrInterfaceDeclaration.class)){
                if(className.equals(CommonStrings.EMPTYSTRING) || classDeclaration.getNameAsString().equals(className)){
                    try{
                        String mName = classDeclaration.getNameAsString();

                        System.out.println(mName + ":" + file.getAbsolutePath());

                        List<Node> leafNodes = getAllLeafNodes(classDeclaration);
                        List<List<Node>> nodePaths = getNodePaths(leafNodes, classDeclaration);
                        List<String> flattenedPaths = new ArrayList<>();

                        for(List<Node> path: nodePaths){
                            StringBuilder currentStringPath = new StringBuilder();
                            for(Node node: path){
                                //check if constant
                                String constantPart = NodeHelper.getConstantName(node);
                                String  nodeAsString;

                                if(!constantPart.equals(CommonStrings.EMPTYSTRING)){
                                    //if it is constant, done with the node
                                    nodeAsString = constantPart;
                                } else {
                                    //more complex resolution
                                    int index = getInstanceIndexOfNode(node);

                                    if(index == -1){
                                        index = nodeIndexResolver.getNodeIndexHashed(node);
                                    }

                                    //get Class as String
                                    String[] parts = node.getClass().getTypeName().split("\\.");
                                    nodeAsString = parts[parts.length-1] + CommonStrings.SEPARATOR_INDEX + index;
                                }
                                //add the part to the StringPath
                                if(currentStringPath.length() == 0){
                                    //first part of the path
                                    currentStringPath.append(nodeAsString);
                                } else{
                                    //all but the first part
                                    currentStringPath.append(CommonStrings.SEPARATOR_PATH_PARTS).append(nodeAsString);
                                }
                            }
                            //add the String path to the list of Paths
                            flattenedPaths.add(currentStringPath.toString());
                        }

                        //the Parser is storing hte abstractions in a similar directory structure as the original files parsed
                        //it replaces part of the original file path to store it to a new destination
                        String outputFile = file.getParent().replace("/testRepos/","/outputFolder/20210424/unseen/");
                        String fileName = file.getName().replace(CommonStrings.POSTFIX_JAVA,CommonStrings.EMPTYSTRING) + CommonStrings.SEPARATOR_PATH_PARTS + mName;
                        printPathsToFile(flattenedPaths, outputFile, fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException | ParseProblemException e){
            System.out.println("Could not process file: " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (Exception e){
            System.out.println("Problem in file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        System.out.println("done");
    }

    /**
     *
     * @param node node of the AST, for which the nodeIndex should be returned
     * @return the index of the Instance, reflecting the node passed to the method, default is -1 iff no Instance if available
     */
    private int getInstanceIndexOfNode(Node node){
        if(node instanceof ClassOrInterfaceDeclaration){
            return classInstanceIndexMap.getOrDefault(NodeHelper.getQualifiedNameClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration)node), -1);
        }
        else if(node instanceof ClassOrInterfaceType){
            return classInstanceIndexMap.getOrDefault(NodeHelper.getQualifiedNameClassOrInterfaceType((ClassOrInterfaceType)node), -1);
        }
        else if(node instanceof MethodDeclaration){
            return methodInstanceIndexMap.getOrDefault(NodeHelper.getQualifiedNameMethodDeclaration((MethodDeclaration)node), -1);
        }
        else if(node instanceof MethodCallExpr){
            return methodInstanceIndexMap.getOrDefault(NodeHelper.getQualifiedNameMethodCallExpr((MethodCallExpr)node), -1);
        }
        else if(node instanceof ObjectCreationExpr){
            return objectInstanceIndexMap.getOrDefault(NodeHelper.getQualifiedSignatureObjectCreation((ObjectCreationExpr) node), -1);
        }
        else if(node instanceof ConstructorDeclaration){
            return objectInstanceIndexMap.getOrDefault(NodeHelper.getQualifiedSignatureConstructor((ConstructorDeclaration) node), -1);
        }
        return -1;
    }

    /**
     *
     * @param paths List of paths (one String per path) to be printed to files (masked and not masked)
     * @param outputDir dir where to print the files to
     * @param fileName name of the original source code file
     * @throws IOException throws an exception if the file cannot be written or the directories needed cannot be created
     */
    private void printPathsToFile(List<String> paths, String outputDir, String fileName) throws IOException {
        File file = new File(outputDir + "/" + fileName + "_paths" + CommonStrings.POSTFIX_PATHS);
        File maskedF = new File(outputDir + "/" + fileName + "_paths" + CommonStrings.POSTFIX_PATHS_MASKED);
        File directory = new File(String.valueOf(outputDir));

        if(!directory.exists()){
            Files.createDirectories(Paths.get(outputDir));
        }

        PrintWriter out = new PrintWriter(file);
        PrintWriter outMasked = new PrintWriter(maskedF);

        paths.forEach(currentPath -> {
            String maskedPath = PathHelper.maskPath(currentPath);
            outMasked.println(maskedPath);
            out.println(currentPath);
        });

        out.close();
        outMasked.close();
    }

    /**
     *
     * @param leafNodes all the leaf nodes (start points for the paths)
     * @param rootNode node which is the most top level node of the paths (e.g., )
     * @return a list of paths, one path per leafnode, chained from the leafnode upwards to the root node of the tree
     */
    private List<List<Node>> getNodePaths(List<Node> leafNodes, Node rootNode) {
        List<List<Node>> pathList = new ArrayList<>();

        for(Node leaf: leafNodes){
            List<Node> path = new ArrayList<>();
            Node currentNode = leaf;

            while(currentNode.getParentNode().isPresent() && currentNode != rootNode){
                path.add(currentNode);
                if(currentNode.getParentNode().isPresent()){
                    currentNode = currentNode.getParentNode().get();
                }
            }
            path.add(currentNode);
            pathList.add(path);
        }
        return pathList;
    }

    public HashMap<String, Integer> collectQualifiedClassNamesWithIndex(CompilationUnit compilationUnit){
        HashMap<String, Integer> outputHashMap = new HashMap<>();
        AtomicInteger instanceIndex = new AtomicInteger(1);
        //classes first
        //classes
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classOrInterfaceDeclaration -> {
            try{
                String currentQualifiedName = NodeHelper.getQualifiedNameClassOrInterfaceDeclaration(classOrInterfaceDeclaration);
                if(!outputHashMap.containsKey(currentQualifiedName)){
                    outputHashMap.put(currentQualifiedName, instanceIndex.getAndIncrement());
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
        //class usage
        compilationUnit.findAll(ClassOrInterfaceType.class).forEach(classType -> {
            try{
                String currentQualifiedName = NodeHelper.getQualifiedNameClassOrInterfaceType(classType);
                if(!outputHashMap.containsKey(currentQualifiedName)){
                    outputHashMap.put(currentQualifiedName, instanceIndex.getAndIncrement());
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        });
        return outputHashMap;
    }

    public HashMap<String, Integer> collectQualifiedMethodNamesWithIndex(CompilationUnit compilationUnit){
        HashMap<String, Integer> outputHashMap = new HashMap<>();
        AtomicInteger instanceIndex = new AtomicInteger(1);
        //declarations first
        compilationUnit.findAll(MethodDeclaration.class).forEach(methodDeclaration -> {
            String currentQualifiedName = NodeHelper.getQualifiedNameMethodDeclaration(methodDeclaration);//methodDeclaration.resolve().getQualifiedSignature();
            if(!outputHashMap.containsKey(currentQualifiedName)){
                outputHashMap.put(currentQualifiedName, instanceIndex.getAndIncrement());
            }
        });
        //method usage
        compilationUnit.findAll(MethodCallExpr.class).forEach(methodCallExpr -> {
            String currentQualifiedName = NodeHelper.getQualifiedNameMethodCallExpr(methodCallExpr);//methodCallExpr.resolve().getQualifiedSignature();
            if(!outputHashMap.containsKey(currentQualifiedName)){
                outputHashMap.put(currentQualifiedName, instanceIndex.getAndIncrement());
            }
        });

        return outputHashMap;
    }

    public HashMap<String, Integer> collectQualifiedObjectsWithIndex(CompilationUnit compilationUnit){
        HashMap<String, Integer> outputHashMap = new HashMap<>();
        AtomicInteger instanceIndex = new AtomicInteger(1);
        //declarations first
        compilationUnit.findAll(ConstructorDeclaration.class).forEach(constructorDeclaration -> {
            String currentQualifiedName = NodeHelper.getQualifiedSignatureConstructor(constructorDeclaration);//methodDeclaration.resolve().getQualifiedSignature();
            if(!outputHashMap.containsKey(currentQualifiedName)){
                outputHashMap.put(currentQualifiedName, instanceIndex.getAndIncrement());
            }
        });
        //method usage
        compilationUnit.findAll(ObjectCreationExpr.class).forEach(objectCreationExpr -> {
            String currentQualifiedName = NodeHelper.getQualifiedSignatureObjectCreation(objectCreationExpr);//methodCallExpr.resolve().getQualifiedSignature();
            if(!outputHashMap.containsKey(currentQualifiedName)){
                outputHashMap.put(currentQualifiedName, instanceIndex.getAndIncrement());
            }
        });

        return outputHashMap;
    }

    private List<Node> getAllLeafNodes(Node rootNode){
        List<Node> leafNodes = new ArrayList<>();
        List<Node> nodesToProcess = new ArrayList<>(rootNode.getChildNodes());

        while(nodesToProcess.size() > 0){
            Node currentNode = nodesToProcess.get(0);
            //remove currentNode from the list to be processed
            nodesToProcess.remove(0);

            if(currentNode.getChildNodes().size() > 0){
                nodesToProcess.addAll(currentNode.getChildNodes());
            } else {
                leafNodes.add(currentNode);
            }
        }
        return leafNodes;
    }
}

