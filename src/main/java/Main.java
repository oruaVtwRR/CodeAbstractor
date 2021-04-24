import common.CommonStrings;
import common.FileHelper;
import parser.ClassParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        String pathToLookForCodeSamples = "";
        int amountOfThreads = 4;

        List<File> files = getFilesFromRootFolder(
                pathToLookForCodeSamples,
                CommonStrings.POSTFIX_JAVA);


        ExecutorService service = Executors.newFixedThreadPool(amountOfThreads);

        files.forEach(file -> {
            service.submit(new ClassParser(file));
        });

        service.shutdown();
    }

    private static List<File> getFilesFromRootFolder(String rootPath, String filePostFix){
        //TODO put those file into the resource or remove them from the set to test
        List<File> files = FileHelper.getAllFilesInAllSubdirectoriesRecursive(rootPath, filePostFix);

        List<String> filesToSkip = new ArrayList<>();
        //files not beeing able to be parsed by the java parser
        filesToSkip.add("from_larg_dataset/training/opennetworkinglab__flowvisor/src/org/flowvisor/config/SliceImpl.java");
        filesToSkip.add("from_larg_dataset/training/beanshell__beanshell/src/main/java/bsh/ClassGenerator.java");
        filesToSkip.add("from_larg_dataset/training/rapidminer__rapidminer-studio/src/main/java/com/rapidminer/gui/search/GlobalSearchPanel.java");
        filesToSkip.add("from_larg_dataset/training/langurmonkey__gaiasky/core/src/gaia/cu9/ari/gaiaorbit/util/gravwaves/RelativisticEffectsManager.java");
        filesToSkip.add("/from_larg_dataset/training/langurmonkey__gaiasky/core/src/gaia/cu9/ari/gaiaorbit/script/EventScriptingInterface.java");
        filesToSkip.add("from_larg_dataset/training/powercrystals__MineFactoryReloaded/src/powercrystals/minefactoryreloaded/MineFactoryReloadedCore.java");
        filesToSkip.add("/from_larg_dataset/training/newbiechen1024__NovelReader/app/src/main/java/com/example/newbiechen/ireader/model/local/update/Update2Helper.java");
        filesToSkip.add("/from_larg_dataset/training/Stonekity__Shop/Shop_AS/Shop/Shop-app/src/main/java/com/stone/shop/base/util/HttpClientWrapper.java");
        filesToSkip.add("/from_larg_dataset/training/timolson__cointrader/src/main/java/org/cryptocoinpartners/util/Injector.java");

        //remove the blacklisted files from the samples
        //that are not possible to be parsed (e.g., java parser cannot resolve parts that are in the class beeing extended
        for(int i = filesToSkip.size() - 1; i >= 0; i--){
            int index = -1;
            for(int j = 0; j < files.size(); j++){
                if(files.get(j).getAbsolutePath().endsWith(filesToSkip.get(i))){
                    index = j;
                    break;
                }
            }
            if(index > -1){
                files.remove(index);
            }
        }

        return files;
    }
}
