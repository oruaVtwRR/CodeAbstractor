package common;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class FileHelper {

    public static synchronized List<File> getAllFilesInAllSubdirectoriesRecursive(String rootFolder, String filePostFix){
        List<File> outputList = new ArrayList<>();
        File[] files = new File(rootFolder).listFiles();
        if(files != null){
            for ( File file : files) {
                outputList.addAll(getFilesRecursive(file, filePostFix));
            }
        }
        return outputList;
    }

    public static synchronized List<String> getPathsFromFile(File file) throws IOException {
        List<String> pathsFromFile = new ArrayList<>();

        if(file.exists() && !file.isDirectory()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    pathsFromFile.add(line);
                }
            }

        }
        return pathsFromFile;
    }

    public static synchronized String returnAbstractFilePath(String javaFilePath, String className, String abstractFileExtension, String javaRootPath, String abstractionsRootPath){
        String s = javaFilePath.replace(javaRootPath, abstractionsRootPath);

        StringBuilder javaFileName = new StringBuilder();
        String lastPart = "";
        String[] parts = s.split("/");

        for(int i = 0; i < parts.length; i++) {
            if(i < parts.length - 1){
                javaFileName.append(parts[i]).append("/");
            } else {
                lastPart = parts[i];
            }
        }

        //lastpart is the .java-file
        String fileName = lastPart.replace(CommonStrings.POSTFIX_JAVA, CommonStrings.EMPTYSTRING);
        fileName += fileName + CommonStrings.SEPARATOR_PATH_PARTS + className + abstractFileExtension;

        //attach fileName
        return javaFileName.append(fileName).toString();
    }

    public static synchronized String returnJavaFilePath(String abstractionsPath, String javaRootPath, String abstractionsRootPath){
        String s = abstractionsPath.replace(abstractionsRootPath, javaRootPath);

        StringBuilder javaFileName = new StringBuilder();
        String lastPart = "";
        String[] parts = s.split("/");

        for(int i = 0; i < parts.length; i++) {
            if(i < parts.length - 1){
                javaFileName.append(parts[i]).append("/");
            } else {
                lastPart = parts[i];
            }
        }

        //attach fileName
        return javaFileName.append(returnJavaFileName(lastPart, false)).toString();
    }

    public static synchronized String returnJavaFileName(String abstractionsFileName, boolean fullPathIfAvailable){
        StringBuilder javaFileName = new StringBuilder();
        String[] parts = abstractionsFileName.split("/");

        for(int i = 0; i < parts.length; i++){
            if(i < parts.length - 1){
                javaFileName.append(parts[i]).append("/");
            }
        }

        javaFileName.append(returnClassName(abstractionsFileName)).append(CommonStrings.POSTFIX_JAVA);

        if(fullPathIfAvailable){
            return javaFileName.toString();
        } else {
            String[] tempParts = javaFileName.toString().split("/");
            return tempParts[tempParts.length - 1];
        }
    }

    public static synchronized String returnClassName(String abstractionsFileName){
        String[] parts = abstractionsFileName.split("/");
        String lastPart = parts[parts.length - 1];
        String[] className = lastPart.split(CommonStrings.SEPARATOR_PATH_PARTS);

        if(className.length == 3){
            return className[0];
        } else if(className.length == 5){
            return className[0] + CommonStrings.SEPARATOR_PATH_PARTS + className[1];
        }else {
            throw new IllegalArgumentException();
        }
    }

    private static List<File> getFilesRecursive(File pFile, String filePostfix) {
        List<File> outputList = new ArrayList<>();
        if(pFile.isDirectory()){
            File[] files = pFile.listFiles();
            if(files != null){
                for ( File file : files) {
                    outputList.addAll(getFilesRecursive(file, filePostfix));
                }
            }
        }
        else {
            if(pFile.getName().endsWith(filePostfix)){
                outputList.add(pFile);
            }
        }
        return outputList;
    }

    /**
     * this method switches all assignments of the nodes of nodeType (and related node types)
     * All nodes that had indexA will have indexB and vice versa afterwards
     * @param filePath
     * @param nodeType
     * @param changeAllRelatedNodeTypes
     * @param indexA
     * @param indexB
     */
    public static synchronized  void switchIndexOfNodeType(String filePath, String nodeType, boolean changeAllRelatedNodeTypes, int indexA, int indexB){
        //getAll related NodeTypes
        List<String> allNodeTypesToChange = changeAllRelatedNodeTypes ? NodeHelper.getAllRelatedNodeTypes(nodeType) : new ArrayList<>(Collections.singleton(nodeType));
        for(String nType: allNodeTypesToChange){
            String a = nType + CommonStrings.SEPARATOR_INDEX + indexA + CommonStrings.SEPARATOR_PATH_PARTS;
            String b = nType + CommonStrings.SEPARATOR_INDEX + indexB + CommonStrings.SEPARATOR_PATH_PARTS;
            String temp = nType + CommonStrings.SEPARATOR_INDEX + 99999 + CommonStrings.SEPARATOR_PATH_PARTS;
            modifyFile(filePath, a, temp);
            modifyFile(filePath, b, a);
            modifyFile(filePath, temp, b);
        }
    }

    public static synchronized void modifyFile(String filePath, String oldString, String newString)
    {
        File fileToBeModified = new File(filePath);
        String oldContent = "";
        BufferedReader reader = null;
        FileWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(fileToBeModified));
            //Reading all the lines of input text file into oldContent
            String line = reader.readLine();
            while (line != null) {
                oldContent = oldContent + line + System.lineSeparator();
                line = reader.readLine();
            }
            //Replacing oldString with newString in the oldContent
            String newContent = oldContent.replaceAll(oldString, newString);
            //Rewriting the input text file with newContent
            writer = new FileWriter(fileToBeModified);
            writer.write(newContent);
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert reader != null;
                assert writer != null;
                reader.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
