package application.model;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AndroidFileSystem implements FileSystem {

    public static boolean fileCopyInterruptFlag = false;
    public static boolean directoryCopyInterruptFlag = false;

    private ObservableList<HBox> pathList;
    private ObservableList<String> directoryList;
    private ObservableList<String> fileList;
    private final String[] escapeCharacters = new String[]{"(",")"," ","&","'"};
    private String currentPath;
    private String previousPath;
    private String selectedItem;
    private int androidToPcTotalFiles;
    private int pcToAndroidTotalFiles;
    private int androidProgressLevel;
    private int pcProgressLevel;
    private String borderColor;


    public AndroidFileSystem() {
        borderColor = "#00BA3C";
        pathList = FXCollections.observableArrayList();
        directoryList = FXCollections.observableArrayList();
        fileList = FXCollections.observableArrayList();
        androidToPcTotalFiles = 0;
        androidProgressLevel = 0;
        pcToAndroidTotalFiles = 0;
        pcProgressLevel = 0;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }

    @Override
    public void setSelectedItem(String selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    public String getSelectedItem() {
        return this.selectedItem;
    }

    public void increasePreviousPath(String selectedFileName) {
        if ( !this.previousPath.equalsIgnoreCase(this.currentPath) && characterCounter(this.currentPath,'/') > 1) {
            this.previousPath = this.currentPath.replace("/"+selectedFileName,"");
        }
    }

    public void decreasePreviousPath() {
        this.previousPath = androidDecreasePathAlgorithm(this.previousPath);
    }

    public void decreaseCurrentPath() {
        this.currentPath = androidDecreasePathAlgorithm(this.currentPath);
    }

    private String androidDecreasePathAlgorithm(String path) {
        int totalSlashCount = characterCounter(path,'/');
        if ( totalSlashCount == 0 ) return path;
        int slashCounter = 0;

        StringBuilder finalTempPath = new StringBuilder();
        for ( int id = 0 ; id < path.length() ; id ++ ) {
            if ( path.charAt(id) == '/' ) {
                ++slashCounter;
                if ( slashCounter == totalSlashCount ) break;
            }
            finalTempPath.append(path.charAt(id));
        }
        return finalTempPath.toString();
    }


    public String getPreviousPath() {
        return previousPath;
    }

    @Override
    public ObservableList<HBox> getListFromPath(String path) {
        pathList = FXCollections.observableArrayList();
        directoryList = FXCollections.observableArrayList();
        fileList = FXCollections.observableArrayList();
        path = addSlashToPath(path);
        path = "adb shell ls \""+path+"\"";
        for ( String readLine : SystemCommandManipulator.executeSystemCommand(path).split("\n") ) {
            if ( readLine.isEmpty() ) continue;
            if ( isDirectory(readLine) ) {
                directoryList.add(readLine);
            } else {
                fileList.add(readLine);
            }
        }
        directoryList.sort((f1,f2) ->  f1.compareTo(f2) );
        fileList.sort((f1,f2) ->  f1.compareTo(f2));


        addFilesToPathList(directoryList,fileList);

        return pathList;
    }

    public String getExtractedFileNameFromContainer( HBox container ) {
        Label fileName = (Label) container.getChildren().get(1);
        return fileName.getText();
    }

    public boolean isDirectory(String filePath) {
        if ( filePath.contains(".torrent") ) return false;
        boolean isDirectory = false;
        String tempExt = "";
        for ( int id=filePath.length()-1 ; id >= 0 ; id-- ) {
            tempExt += filePath.charAt(id);
            if ( filePath.charAt(id) == '.' ) break;
        }
        if ( tempExt.length() > 5 || !tempExt.contains(".") ) isDirectory = true;
//        System.out.println(filePath + " - "+ isDirectory);
        return isDirectory;
    }

    private boolean isExist(String fullFilePath) {
        String fileName = FileSystem.getLastFileNameFromPath(fullFilePath,true);
        String filePath = fullFilePath.replace("/"+fileName,"");
        ObservableList<String> fileList = FXCollections.observableArrayList();
        fileList.addAll(SystemCommandManipulator.executeSystemCommand("adb shell ls \""+addSlashToPath(filePath)+"\"").split("\n"));
        return fileList.contains(fileName);
    }

    private void addFilesToPathList(ObservableList<String> directoryList,ObservableList<String> fileList) {
        for ( String directory : directoryList ) pathList.add(createAndGetPathListContainer(directory, greenDirectoryIconPath));
        for ( String file : fileList ) pathList.add(createAndGetPathListContainer(file,fileIconPath));
    }

    private HBox createAndGetPathListContainer(String fileName,String iconPath) {
        HBox container = new HBox();
        container.setSpacing(10);
        ImageView iconContainer = new ImageView(new Image(new Main().getClass().getResource(iconPath).toString()));
        iconContainer.setFitWidth(20);
        iconContainer.setFitHeight(20);
        Label fileNameLabel = new Label(fileName);
        container.getChildren().addAll(iconContainer,fileNameLabel);
        return container;
    }

    private String addSlashToPath(String path) {
        StringBuilder convertedPath = new StringBuilder();
        for ( int id = 0 ; id < path.length() ; id++ ) {
            for ( int extId = 0 ; extId < escapeCharacters.length ; extId++ ) {
                if ( String.valueOf(path.charAt(id)).equals(escapeCharacters[extId]) ) {
                    convertedPath.append("\\");
                }
            }
            convertedPath.append(path.charAt(id));
        }
        return convertedPath.toString();
    }

    private String removeSlashFromPath(String path) {
        return "";
    }

    private int characterCounter(String path,char character) {
        int countedCharNum = 0;
        for ( int id = 0 ; id < path.length() ; id++ ) {
            if ( path.charAt(id) == character ) {
                ++countedCharNum;
            }
        }
        return countedCharNum;
    }

    public void copyFromAndroidToPc(ObservableList<HBox> selectedAndroidFiles , String pcCurrentPath) {

        Thread androidInitializeThread = new Thread(() -> initializeAndroidToPcCopyTask(selectedAndroidFiles) );

        ObservableList<File> sourceFileNames = FXCollections.observableArrayList();
        ObservableList<File> sourceFolderNames = FXCollections.observableArrayList();

        Thread androidDeciderThread = new Thread(() -> {
            System.out.println("Deciding...");
            File tempDirectoryChecker;
            for ( HBox file : selectedAndroidFiles ) {
                tempDirectoryChecker = new File(getExtractedFileNameFromContainer(file));
//            System.out.println("File name - "+this.getCurrentPath()+"\\"+tempDirectoryChecker.getName());
//            System.out.println("Is directory - "+isDirectory(tempDirectoryChecker.getAbsolutePath()));
                if ( isDirectory(tempDirectoryChecker.getAbsolutePath() ) ) {
                    sourceFolderNames.add(tempDirectoryChecker);
                } else sourceFileNames.add(tempDirectoryChecker);
            }
        });

        Thread androidCopyAnimationThread = new Thread(() -> {
            double maxProgressLevel = androidToPcTotalFiles;
            Task<Double> progressTask = new Task<Double>() {
                @Override
                protected Double call() throws Exception {
                    while ( androidProgressLevel != maxProgressLevel && !fileCopyInterruptFlag && !directoryCopyInterruptFlag) {
                        System.out.println("Progress level - "+androidProgressLevel);
                        System.out.println("Total files - "+maxProgressLevel);
                        updateProgress(androidProgressLevel,maxProgressLevel);
                        updateMessage(String.valueOf(androidToPcTotalFiles));
                    }
                    if ( fileCopyInterruptFlag || directoryCopyInterruptFlag ) {
                        ViewSwitcher.getInstance().showCopyCancelledView("Cancelled",Color.RED);
                    } else ViewSwitcher.getInstance().showCopyCompleteView("Completed",Color.GREEN);
                    return null;
                }
            };
            ViewSwitcher.getInstance().showCopyingView(progressTask,borderColor,Color.GREEN);
            progressTask.run();
        });

        // File copying thread
        Thread androidFileThread = new Thread( () -> {
            try {
                System.out.println("Copying files...");
                // Copying files from android to pc
                File tempPcFile;
                String tempAndroidFile;
                for ( File file : sourceFileNames ) {
                    if ( fileCopyInterruptFlag ) throw new InterruptedException("Thread Stopped");
                    // Check if file is exist on pc
                    tempPcFile = new File(pcCurrentPath+"\\"+file.getName());
                    tempAndroidFile = this.getCurrentPath()+"/"+file;
                    if ( tempPcFile.exists() ) {
                        System.out.println("File already exists...");
                        --androidToPcTotalFiles;
                        ++androidProgressLevel;
                        System.out.println("Total android to pc file - "+androidToPcTotalFiles);
                    } else {
                        SystemCommandManipulator.executeSystemCommand("adb pull \""+tempAndroidFile+"\" \""+tempPcFile+"\"");
                        --androidToPcTotalFiles;
                        ++androidProgressLevel;
                        System.out.println("Total android to pc file - "+androidToPcTotalFiles);
                    }
                }
            } catch ( InterruptedException ie ) {
                System.out.println(ie.getMessage());
            }
        });

        // Folder copying thread
        Thread androidDirectoryThread = new Thread( () -> {
            try {
                System.out.println("Copying directories...");
                String tempPcFile;
                String tempAndroidFile;
                ObservableList<String> filesFromDirectory = FXCollections.observableArrayList();
                for ( File folder : sourceFolderNames ) {
                    if ( directoryCopyInterruptFlag ) throw new InterruptedException();
                    tempPcFile = pcCurrentPath+"\\"+folder.getName();
                    tempAndroidFile = this.getCurrentPath()+"/"+folder.getName();
                    File tempAndroidPcFile = new File(tempAndroidFile);
                    if ( !tempAndroidPcFile.exists() ) tempAndroidPcFile.mkdir();

                    try {
                        androidToPcCopyAlgorithm(tempAndroidFile,tempPcFile);
                    } catch ( IOException ioe ) {
                        ioe.printStackTrace();
                    }
                }
            } catch ( InterruptedException ie ) {
                System.out.println(ie.getMessage());
            }
        });

        startCopyThread(androidInitializeThread, androidDeciderThread,androidCopyAnimationThread,
                androidFileThread, androidDirectoryThread);
    }

    public void copyFromPcToAndroid(ObservableList<HBox> selectedPcFiles , String selectedAndroidDirectory , String currentPcPath) {

        Thread pcInitializeThread = new Thread(() -> initializePcToAndroidCopyTask(selectedPcFiles,currentPcPath));

        ObservableList<File> sourceFileNames = FXCollections.observableArrayList();
        ObservableList<File> sourceDirectoryNames = FXCollections.observableArrayList();

        Thread pcDeciderThread = new Thread(() -> {
            File tempDirectoryChecker;
            for ( HBox file : selectedPcFiles ) {

                tempDirectoryChecker = new File(currentPcPath+"\\"+getExtractedFileNameFromContainer(file));
                if ( tempDirectoryChecker.isDirectory()) {
                    sourceDirectoryNames.add(tempDirectoryChecker);
                } else sourceFileNames.add(tempDirectoryChecker);
            }
        });

        // Copy animation pc to android thread
        Thread pcCopyAnimationThread = new Thread(() -> {
            double maxProgressLevel = pcToAndroidTotalFiles;
            Task<Double> progressTask = new Task<Double>() {
                @Override
                protected Double call() throws Exception {
                    while ( pcProgressLevel != maxProgressLevel && !fileCopyInterruptFlag && !directoryCopyInterruptFlag ) {
                        System.out.println("Progress level - "+pcProgressLevel);
                        System.out.println("TOtal files - "+maxProgressLevel);
                        updateProgress(pcProgressLevel,maxProgressLevel);
                        updateMessage(String.valueOf(pcToAndroidTotalFiles));
                    }
                    if ( fileCopyInterruptFlag || directoryCopyInterruptFlag ) {
                        ViewSwitcher.getInstance().showCopyCancelledView("Cancelled",Color.RED);
                    } else ViewSwitcher.getInstance().showCopyCompleteView("Completed",Color.GREEN);
                    return null;
                }
            };
            ViewSwitcher.getInstance().showCopyingView(progressTask,borderColor, Color.GREEN);
            progressTask.run();
        });

        // File copying thread
        Thread pcFileThread = new Thread(() -> {
            try {
                // Copying files from pc to android
                for ( File file : sourceFileNames ) {
                    if ( fileCopyInterruptFlag ) throw new InterruptedException();
                    if ( !isExist(selectedAndroidDirectory+"/"+file.getName()) ) {
                        // copy file
                        SystemCommandManipulator.executeSystemCommand("adb push \""+currentPcPath+"\\"+file.getName()+"\" \""+
                                selectedAndroidDirectory+"\"");
                        System.out.println("Copied file");
                        --pcToAndroidTotalFiles;
                        ++pcProgressLevel;
                    } else {
                        System.out.println("Skipped file to copy");
                        --pcToAndroidTotalFiles;
                        ++pcProgressLevel;
                    }
                }
            } catch ( InterruptedException ie ) {
                System.out.println(ie.getMessage());
            }
        });

        // Directory copying thread
        Thread pcDirectoryThread = new Thread(() -> {
            try {
                for ( File directory : sourceDirectoryNames ) {
                    if ( directoryCopyInterruptFlag ) throw new InterruptedException();
                    // If there's files in current directory

                    String tempCurrentPcPath = currentPcPath+"\\"+directory.getName();
                    String tempCurrentAndroidPath = selectedAndroidDirectory+"/"+directory.getName();
                    if ( directory.listFiles() != null ) {
//                System.out.println("Creating directory - "+tempCurrentAndroidPath);
                        // Creating directory in android...
                        SystemCommandManipulator.executeSystemCommand("adb shell mkdir \""+addSlashToPath(tempCurrentAndroidPath)+"\"");
                        for ( File file : directory.listFiles() ) {
                            if ( directoryCopyInterruptFlag ) throw new InterruptedException();
                            pcToAndroidCopyAlgorithm(tempCurrentPcPath+"\\"+file.getName(),tempCurrentAndroidPath+
                                    "/"+file.getName());
                        }
                    } else {
                        SystemCommandManipulator.executeSystemCommand("adb shell mkdir \""+addSlashToPath(tempCurrentAndroidPath)+"\"");
                    }
                }
            } catch ( InterruptedException ie ) {
                System.out.println(ie.getMessage());
            }
        });

        startCopyThread(pcInitializeThread, pcDeciderThread, pcCopyAnimationThread ,pcFileThread, pcDirectoryThread);
    }

    private void startCopyThread(Thread initializeThread, Thread deciderThread, Thread copyAnimationThread,
                                 Thread fileThread, Thread directoryThread) {
        Thread copyThread = new Thread( () -> {
            try {
                initializeThread.start();
                initializeThread.join();

                deciderThread.start();
                deciderThread.join();

                copyAnimationThread.start();
                fileThread.start();
                directoryThread.start();

            } catch ( Exception e ) {
                e.printStackTrace();
            }
        });

        copyThread.start();
    }

    private void initializeAndroidToPcCopyTask(ObservableList<HBox> selectedAndroidFiles) {
        ViewSwitcher.getInstance().showInitializeView(Color.GREEN);
        fileCopyInterruptFlag = false;
        directoryCopyInterruptFlag = false;
        androidProgressLevel=0;
        androidToPcTotalFiles=0;
        System.out.println("Initializing...");
        ObservableList<File> sourceFileNames = FXCollections.observableArrayList();
        ObservableList<File> sourceFolderNames = FXCollections.observableArrayList();

        File tempDirectoryChecker;
        for ( HBox file : selectedAndroidFiles ) {
            tempDirectoryChecker = new File(getExtractedFileNameFromContainer(file));
//            System.out.println("File name - "+this.getCurrentPath()+"\\"+tempDirectoryChecker.getName());
//            System.out.println("Is directory - "+isDirectory(tempDirectoryChecker.getAbsolutePath()));
            if ( isDirectory(tempDirectoryChecker.getAbsolutePath() ) ) {
                sourceFolderNames.add(tempDirectoryChecker);
            } else sourceFileNames.add(tempDirectoryChecker);
        }

        // File copying thread
        Thread fileThread = new Thread( () -> {
            // Copying files from android to pc
            for ( File file : sourceFileNames ) ++androidToPcTotalFiles;
        });

        // Folder copying thread
        Thread directoryThread = new Thread( () -> {
            androidFileCounter(sourceFolderNames,this.getCurrentPath());
        });

        try {
            fileThread.start();
            directoryThread.start();
            fileThread.join();
            directoryThread.join();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void initializePcToAndroidCopyTask(ObservableList<HBox> selectedPcFiles,String currentPcPath) {
        ViewSwitcher.getInstance().showInitializeView(Color.GREEN);
        fileCopyInterruptFlag = false;
        directoryCopyInterruptFlag = false;
        pcProgressLevel = 0;
        pcToAndroidTotalFiles = 0;
        System.out.println("Initializing...");
        ObservableList<File> sourceFileNames = FXCollections.observableArrayList();
        ObservableList<File> sourceFolderNames = FXCollections.observableArrayList();

        File tempDirectoryChecker;
        for ( HBox file : selectedPcFiles ) {
            tempDirectoryChecker = new File(currentPcPath+"\\"+getExtractedFileNameFromContainer(file));
            if ( tempDirectoryChecker.isDirectory() ) {
                sourceFolderNames.add(tempDirectoryChecker);
            } else sourceFileNames.add(tempDirectoryChecker);
        }

        // File copying thread
        Thread fileThread = new Thread( () -> {
            // Copying files from android to pc
            for ( File file : sourceFileNames ) {
                ++pcToAndroidTotalFiles;
                System.out.println("Counting\nPc to android size - "+pcToAndroidTotalFiles);
            }
        });

        // Folder copying thread
        Thread directoryThread = new Thread( () -> {
            pcFileCounter(sourceFolderNames,currentPcPath);
        });

        try {
            fileThread.start();
            directoryThread.start();
            fileThread.join();
            directoryThread.join();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void androidToPcCopyAlgorithm (String currentAndroidDirectory,String currentPcDirectory) throws IOException,InterruptedException {
        Path currentPcPath = Paths.get(currentPcDirectory);
        if ( !Files.exists(currentPcPath) ) Files.createDirectory(currentPcPath);
        ObservableList<String> filesFromDirectory = FXCollections.observableArrayList();
        filesFromDirectory.addAll(SystemCommandManipulator.executeSystemCommand("adb shell ls \""+
                addSlashToPath(currentAndroidDirectory)+"\"").split("\n"));
        // if current directory has files...
        if ( filesFromDirectory.size() > 0 ) {
            for ( String fileFromDirectory : filesFromDirectory ) {
                if ( directoryCopyInterruptFlag ) throw new InterruptedException();
                String nextAndroidDirectoryPath = currentAndroidDirectory+"/"+fileFromDirectory;
                String nextPcDirectoryPath = currentPcDirectory+"\\"+fileFromDirectory;
                File tempNextPcDirectoryPath = new File(nextPcDirectoryPath);
                if ( isDirectory(nextAndroidDirectoryPath) ) {
                    if ( !tempNextPcDirectoryPath.exists() ) tempNextPcDirectoryPath.mkdir();
                    ObservableList<String> filesFromNextDirectory = FXCollections.observableArrayList();
                    filesFromNextDirectory.addAll(SystemCommandManipulator.executeSystemCommand("adb shell ls \""+
                            addSlashToPath(nextAndroidDirectoryPath)+"\"").split("\n"));
                    // if next directory has files...
                    if ( filesFromNextDirectory.size() > 0 && !filesFromNextDirectory.get(filesFromNextDirectory.size()-1).isEmpty() ) {
                        androidToPcCopyAlgorithm(nextAndroidDirectoryPath,nextPcDirectoryPath);
                    }
                } else {
                    if ( !tempNextPcDirectoryPath.exists() ) {
                        SystemCommandManipulator.executeSystemCommand("adb pull \""+nextAndroidDirectoryPath+"\" \""+
                                nextPcDirectoryPath+"\"");
                        System.out.println("Copied file");
                        --androidToPcTotalFiles;
                        ++androidProgressLevel;
                        System.out.println("Total android to pc file - "+androidToPcTotalFiles);
                    } else {
                        System.out.println("Skip file");
                        --androidToPcTotalFiles;
                        ++androidProgressLevel;
                        System.out.println("Total android to pc file - "+androidToPcTotalFiles);
                    }
                }
            }
        }
    }

    private void pcToAndroidCopyAlgorithm ( String currentPcDirectory,String currentAndroidDirectory) throws InterruptedException {

        File tempCurrentPcDirectory = new File(currentPcDirectory);
//        System.out.println("Creating folder in android - "+currentAndroidDirectory);
        // Creating folder in android...

        if (isDirectory(currentAndroidDirectory)) {
            SystemCommandManipulator.executeSystemCommand("adb shell mkdir \""+addSlashToPath(currentAndroidDirectory)+"\"");
        } else {
            // Copy files from current directory...
            if ( !isExist(currentAndroidDirectory) ) {
                SystemCommandManipulator.executeSystemCommand("adb push \""+currentPcDirectory+"\" \""+
                        "/"+currentAndroidDirectory.replace(FileSystem.getLastFileNameFromPath(currentAndroidDirectory,true),"")+"\"");
            }
            --pcToAndroidTotalFiles;
            ++pcProgressLevel;
        }
        // check if there are files in the current directory...
        if ( tempCurrentPcDirectory.listFiles() != null ) {
            File tempCurrentFile;
            for ( File fileFromCurrentDirectory : tempCurrentPcDirectory.listFiles() ) {
                if ( directoryCopyInterruptFlag ) throw new InterruptedException();
                tempCurrentFile = new File(currentPcDirectory+"\\"+fileFromCurrentDirectory.getName());

                if ( tempCurrentFile.isDirectory() ) {
                    pcToAndroidCopyAlgorithm(currentPcDirectory+"\\"+tempCurrentFile.getName(),
                            currentAndroidDirectory+"/"+tempCurrentFile.getName());
                } else {
                    if ( !isExist(currentAndroidDirectory+"/"+fileFromCurrentDirectory.getName()) ) {
                        // Copy files from current directory...
                        SystemCommandManipulator.executeSystemCommand("adb push \""+currentPcDirectory+"\\"+tempCurrentFile.getName()+"\" \""+
                                currentAndroidDirectory+"/"+tempCurrentFile.getName()+"\"");
                        --pcToAndroidTotalFiles;
                        ++pcProgressLevel;
//                        System.out.println("Copied file - "+currentAndroidDirectory+"/"+fileFromCurrentDirectory.getName());
                        System.out.println("Copied file");
                    } else {
                        --pcToAndroidTotalFiles;
                        ++pcProgressLevel;
//                        System.out.println("Skip file - "+currentAndroidDirectory+"/"+fileFromCurrentDirectory.getName());
                        System.out.println("Skipped file");
                    }
                }
            }
        }
    }

    private void androidFileCounter(ObservableList<File> directories,String currentPath) {
        System.out.println("Running counter");
        String tempAndroidFile;
        for ( File directory : directories ) {
            tempAndroidFile = currentPath+"/"+directory.getName();
            if ( isDirectory(tempAndroidFile) ) {
                ObservableList<String> nextDirectory = FXCollections.observableArrayList();
                nextDirectory.addAll(SystemCommandManipulator.executeSystemCommand("adb shell ls \""+addSlashToPath(tempAndroidFile)+"\"").split("\n"));
                if ( nextDirectory.size() > 0 && !nextDirectory.get(nextDirectory.size()-1).equals("") ) {
                    ObservableList<File> nextDirectoryFiles = FXCollections.observableArrayList();
                    for ( String file : nextDirectory ) {
                        nextDirectoryFiles.add(new File(file));
                    }
                    androidFileCounter(nextDirectoryFiles,tempAndroidFile);
                }
            } else {
                ++androidToPcTotalFiles;
            }
        }
        System.out.println("Total androidtopc files - "+androidToPcTotalFiles);
    }

    private void pcFileCounter(ObservableList<File> directories,String currentPcPath) {
        System.out.println("Running Counter");
        File currentTempDirectory;
        for ( File directory : directories ) {
            currentTempDirectory = new File(currentPcPath+"\\"+directory.getName());
            System.out.println("Name - "+currentTempDirectory.getName());
            if ( currentTempDirectory.isDirectory() ) {
                if ( currentTempDirectory.listFiles() != null ) {
                    ObservableList<File> tempNextDirectories = FXCollections.observableArrayList();
                    tempNextDirectories.addAll(currentTempDirectory.listFiles());
                    pcFileCounter(tempNextDirectories,currentPcPath+"\\"+currentTempDirectory.getName());
                }
            } else {
                if ( !currentTempDirectory.getName().equalsIgnoreCase("desktop.ini") ) {
                    ++pcToAndroidTotalFiles;
                    System.out.println("Total pc to android files - "+pcToAndroidTotalFiles);
                }
            }
        }
    }

}
