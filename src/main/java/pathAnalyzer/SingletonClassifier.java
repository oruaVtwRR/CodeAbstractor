package pathAnalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.PathHelper;
import pattern.PathCollection;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingletonClassifier {

    private static SingletonClassifier instance;
    private List<String> importantPaths;
    private List<String> importantPathsMasked;

    private SingletonClassifier() {
        ObjectMapper objectMapper = new ObjectMapper();
        URI fileURI;
        try{
            fileURI = SingletonClassifier.class.getResource( "/pattern/pathsSingleton.json" ).toURI();
            PathCollection singletonImportantPaths = objectMapper.readValue(
                    new File(fileURI),
                    PathCollection.class);
            importantPaths = new ArrayList<>(singletonImportantPaths.getPaths());
            importantPathsMasked = new ArrayList<>(PathHelper.maskPaths(importantPaths));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static SingletonClassifier getInstance() {
        if(instance == null){
            instance = new SingletonClassifier();
        }
        return instance;
    }

    public boolean isSingleton(List<String> pathsFromSample){
        List<String> maskedPathsOfSample = new ArrayList<>(PathHelper.maskPaths(pathsFromSample));

        AtomicBoolean isSingleton = new AtomicBoolean(true);

        importantPathsMasked.forEach(importantPath -> {
            if(!maskedPathsOfSample.contains(importantPath)){
                isSingleton.set(false);
            }
        });


        if(isSingleton.get()){
            //check in details
            //get all nodes
            HashMap<String, List<Integer>> importantParts = PathHelper.getFittingIndices(importantPaths);

            for(Map.Entry<String, List<Integer>> entry: importantParts.entrySet()){
                for(Integer v: entry.getValue()){

                    List<String> currentCluster = PathHelper.getCluster(importantPaths, entry.getKey(), v);
                    List<String> t = PathHelper.getListOfPossibleIndexValuesForCluster(pathsFromSample, currentCluster, entry.getKey(), v);
                    if(t.size() == 0){
                        isSingleton.set(false);
                    }
                }
            }
        }
        return isSingleton.get();
    }

    public List<String> getImportantPaths(){
        return this.importantPaths;
    }

}
