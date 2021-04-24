package common;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PathHelper {

    public static synchronized String maskPath(String path) {
        return path.replaceAll("\\d", CommonStrings.EMPTYSTRING);
    }

    public static synchronized List<String> maskPaths(List<String> paths) {
        List<String> outputList = new ArrayList<>();
        paths.forEach(path -> outputList.add(maskPath(path)));

        return outputList;
    }

    public static synchronized String maskPathWithoutNodeType(String path, String nodeType) {
        if(path.contains(nodeType)){
            List<String> nodeTypes = new ArrayList<>();
            nodeTypes.add(nodeType);
            return maskPathWithoutCluster(path, nodeTypes);
        } else {
            return maskPath(path);
        }
    }

    public static synchronized String maskPathWithoutCluster(String path, List<String> nodeTypes) {
        String[] parts = path.split(CommonStrings.SEPARATOR_PATH_PARTS);
        StringBuilder output = new StringBuilder();

        for(String part: parts){
            if(!output.toString().isEmpty()){
                output.append(CommonStrings.SEPARATOR_PATH_PARTS);
            }
            AtomicBoolean mask = new AtomicBoolean(true);
            nodeTypes.forEach(nodeType -> {
                if(part.contains(nodeType)){
                    mask.set(false);
                }
            });
            String partToAppend = mask.get() ? part.replaceAll("\\d", CommonStrings.EMPTYSTRING) : part;
            output.append(partToAppend);
        }
        return output.toString();
    }

    public static synchronized List<String> maskPathWithoutCluster(List<String> paths, List<String> nodeTypes) {
        List<String> outputList = new ArrayList<>();

        paths.forEach(path -> {
            outputList.add(maskPathWithoutCluster(path, nodeTypes));
        });

        return outputList;
    }

    public static synchronized List<String> getAllConcretePaths(File file, List<String> maskedPaths){
        try{
            return getAllConcretePaths(
                    FileHelper.getPathsFromFile(file),
                    maskedPaths);

        } catch(Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static synchronized List<String> getAllConcretePaths(List<String> concretePaths, List<String> maskedPaths){
        Set<String> paths = new HashSet<>();

        maskedPaths.forEach(mPath -> {
            List<String> allPaths = getAllConcretePaths(concretePaths, mPath);
            paths.addAll(allPaths);
        });

        return new ArrayList<>(paths);
    }

    public static synchronized List<String> getAllConcretePaths(List<String> concretePaths, String maskedPath){
        List<String> paths = new ArrayList<>();
        concretePaths.forEach(path -> {
            if(maskedPath.equals(PathHelper.maskPath(path))){
                paths.add(path);
            }
        });
        return paths;
    }

    /**
     *
     * @param paths
     * @return
     */
    public static synchronized HashMap<String, List<Integer>> getFittingIndices(List<String> paths){
        HashMap<String, List<Integer>> fittingIds = new HashMap<>();

        paths.forEach(path -> {
            String[] pathParts = path.split(CommonStrings.SEPARATOR_PATH_PARTS);
            for(String part: pathParts){
                String[] nodeParts = part.split(CommonStrings.SEPARATOR_INDEX);
                String nodeType = nodeParts[0];
                int id = nodeParts.length > 1 ? Integer.parseInt(nodeParts[1]) : -1;

                if(id >= 0){
                    List<Integer> seenIds = fittingIds.getOrDefault(nodeType, new ArrayList<>());
                    if(!seenIds.contains(id)){
                        seenIds.add(id);
                        fittingIds.put(nodeType, seenIds);
                    }
                }
            }
        });
        return fittingIds;
    }

    public static synchronized List<String> getAllPathsAffectedByType(String nodeType, List<String> allPaths){
        List<String> allRelatedTypes = NodeHelper.getAllRelatedNodeTypes(nodeType);
        List<String> output = new ArrayList<>();

        allRelatedTypes.forEach(type -> {
            allPaths.forEach(path -> {
                if(path.contains(type)){
                    if(!output.contains(path)){
                        output.add(path);
                    }
                }
            });
        });
        return output;
    }

    public static synchronized String changeIndex(String path, String nodeType, int oldIndex, int newIndex, int defaultIndex){

        List<String> importantNodeTypes = NodeHelper.getAllRelatedNodeTypes(nodeType);
        String[] parts = path.split(CommonStrings.SEPARATOR_PATH_PARTS);
        String[] returnParts = new String[parts.length];

        importantNodeTypes.forEach(currentNodeType -> {
            if(path.contains(currentNodeType)){
                for(int i = 0; i < parts.length; i++){
                    //for(String part: parts){
                    if(parts[i].contains(currentNodeType)){
                        if(oldIndex == Integer.parseInt(parts[i].split(CommonStrings.SEPARATOR_INDEX)[1])){
                            returnParts[i] = currentNodeType + CommonStrings.SEPARATOR_INDEX + newIndex;
                        } else if (newIndex == Integer.parseInt(parts[i].split(CommonStrings.SEPARATOR_INDEX)[1])){
                            returnParts[i] = currentNodeType + CommonStrings.SEPARATOR_INDEX + defaultIndex;
                        }
                    }
                }
            }
        });
        //fill all the parts that are unchanged
        StringBuilder outputString = new StringBuilder();

        for(int i = 0; i < returnParts.length; i++){
            if(i > 0){
                outputString.append(CommonStrings.SEPARATOR_PATH_PARTS);
            }
            String currentPart = null ==  returnParts[i] ? parts[i] : returnParts[i];
            outputString.append(currentPart);
        }
        return outputString.toString();

    }

    /**
     *
     * @param pathsToCheck
     * @param contextNode
     * @param indexOfContextNode
     * @return
     */
    public static synchronized List<String> getCluster(List<String> pathsToCheck, String contextNode, int indexOfContextNode){
        Set<String> contextPaths = new HashSet<>();

        String searchString = contextNode + CommonStrings.SEPARATOR_INDEX + indexOfContextNode;

        for(String path: pathsToCheck){
            if(path.endsWith(searchString)) {
                contextPaths.add(path);
            }
            if(path.contains(searchString + CommonStrings.SEPARATOR_PATH_PARTS)){
                contextPaths.add(path);
            }
        }

        return new ArrayList<>(contextPaths);
    }

    /**
     * Method to available all contextnodes before using the actual method
     * @param pathsToCheck
     * @param clusterOfPaths
     * @param contextNode
     * @param indexOfContextNode
     * @return
     */
    public static synchronized List<String> getListOfPossibleIndexValuesForCluster(List<String> pathsToCheck, List<String> clusterOfPaths, String contextNode, int indexOfContextNode){
        List<String> maskedPathsToCheck = maskPaths(pathsToCheck);
        List<String> maskCluster = maskPaths(clusterOfPaths);
        List<String> candidateList = new ArrayList<>();

        //checks that every path has a potential match
        for(String p: maskCluster){
            if(!maskedPathsToCheck.contains(p)){
                //cluster is not contained
                return new ArrayList<>();
            }
        }

        //cluster might be contained
        //for(String maskedClusterPath: maskCluster){

        for(int i = 0; i < clusterOfPaths.size(); i++){
            String currentMaskedPath = maskPath(clusterOfPaths.get(i));
            String[] pathParts = clusterOfPaths.get(i).split(CommonStrings.SEPARATOR_PATH_PARTS);

            for(int j = 0; j < pathParts.length; j++){
                if((contextNode + CommonStrings.SEPARATOR_INDEX + indexOfContextNode).equals(pathParts[j])){

                    //get all related paths of set to check
                    List<String> pathsToCheckForCurrentPath = getAllConcretePaths(pathsToCheck, currentMaskedPath);
                    Set<String> candidateListTemp = new HashSet<>();

                    for(String currentPathToCheck: pathsToCheckForCurrentPath){
                        String candadate = currentPathToCheck.split(CommonStrings.SEPARATOR_PATH_PARTS)[j];
                        candidateListTemp.add(candadate);
                    }

                    //iff i == 0, allow new candidates to be added
                    //otherwise candidates(need to be removed if not contained
                    if(i == 0){
                        candidateList.addAll(candidateListTemp);
                    } else {
                        List<String> candidatesToRemove = new ArrayList<>();
                        candidateList.forEach(candidate -> {
                            if(!candidateListTemp.contains(candidate)){
                                candidatesToRemove.add(candidate);
                            }
                        });
                        candidateList.removeAll(candidatesToRemove);
                    }
                }
            }
        }
        return candidateList;
    }
}
