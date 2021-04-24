package pattern;

import common.CommonStrings;
import common.FileHelper;
import pathAnalyzer.SingletonClassifier;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SingletonCrawler {

    public static void main(String[] args) {
        String pathToLookForAbstractions = "";

        List<File> files = FileHelper.getAllFilesInAllSubdirectoriesRecursive(
                pathToLookForAbstractions,
                CommonStrings.POSTFIX_PATHS);

        AtomicInteger matchCounter = new AtomicInteger();

        files.forEach(file -> {
            try{
                List<String> pathsOfFile = FileHelper.getPathsFromFile(file);
                if(SingletonClassifier.getInstance().isSingleton(pathsOfFile)){
                    System.out.println(file.getAbsolutePath());
                    matchCounter.getAndIncrement();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        });

        System.out.println(matchCounter + " matches");
    }
}
