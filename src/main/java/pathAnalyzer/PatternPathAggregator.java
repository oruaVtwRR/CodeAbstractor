package pathAnalyzer;

import common.CommonStrings;
import common.FileHelper;
import common.PathHelper;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PatternPathAggregator {

    public static void main(String[] args) {
        String rootPath = "";

        List<File> files = FileHelper.getAllFilesInAllSubdirectoriesRecursive(
                rootPath,
                CommonStrings.POSTFIX_PATHS);


        //information from table
        pathsInPercentageFiles(files, 100);

        //information from table
        analyzeIncrementThresholds(files);
    }

    /**
     * Produces output as shown in the evaluation of the paper (threshold )
     * @param files
     */
    private static void analyzeIncrementThresholds(List<File> files){
        List<String> currentlyKnownPath = new ArrayList<>();

        for(int i = 100; i >= 10; i-=10){
            List<String> paths = pathsInPercentageFiles(files, i);
            System.out.println(i + "%: " + paths.size());

            for(String path : paths) {
                if(!currentlyKnownPath.contains(path)){
                    currentlyKnownPath.add(path);
                    System.out.println(path);
                }
            }
        }
    }

    /**
     *
     * @param files all the files (containing paths) to check for paths
     * @param percentage value between 0 and 1
     */
    private static List<String> pathsInPercentageFiles(List<File> files, int percentage) {
        //key is the path, int is a counter over all files

        final int[] pathCounter = new int[files.size()];
        Set<String> uniquePaths = new HashSet<>();
        Set<String> maskedPaths = new HashSet<>();

        HashMap<String, Integer> occurrences = new HashMap<>();
        AtomicInteger totalFiles = new AtomicInteger();

        files.forEach(file -> {
            try {
                List<String> pathsOfFile = FileHelper.getPathsFromFile(file);
                pathCounter[totalFiles.getAndIncrement()] = pathsOfFile.size();

                //remove duplicates to only consider one occurrence per file (masked paths can be duplicated by just removing the index values)
                Set<String> pathsWithoutDuplicates = new HashSet<>(pathsOfFile);

                pathsWithoutDuplicates.forEach(path -> {
                    uniquePaths.add(path);
                    int occurancesBeforeAdding = occurrences.getOrDefault(path, 0);
                    maskedPaths.add(PathHelper.maskPath(path));
                    occurrences.put(path, occurancesBeforeAdding + 1);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //print all the paths that are in more than percentage files
        int threshold = (totalFiles.get() * percentage) / 100;
        List<String> outputList = new ArrayList<>();
        occurrences.forEach((key, value) -> {
            if (value >= threshold) {
                outputList.add(key);
            }
        });

        int min = pathCounter[0];
        int max = pathCounter[0];
        int sum = pathCounter[0];

        for(int i = 1; i < totalFiles.get(); i++){
            min = Math.min(min, pathCounter[i]);
            max = Math.max(max, pathCounter[i]);
            sum += pathCounter[i];
        }


        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("AllPaths: " + sum);
        System.out.println("Avg: " + (sum / totalFiles.get()));
        System.out.println("unique concrete paths: " + uniquePaths.size());
        System.out.println("unique masked paths: " + maskedPaths.size());

        return outputList;
    }
}
