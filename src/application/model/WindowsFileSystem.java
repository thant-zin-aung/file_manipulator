package application.model;

import application.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;

public class WindowsFileSystem implements FileSystem {

    public static boolean directoryCopyInterruptFlag = false;

    private final long oneGigabyteInByte = 1073741824;
    private String selectedItem;
    private String currentPath;
    private final ObservableList<String> availableDriveList;
    private final ObservableList<HBox> pathList;
    private final ObservableList<String> directoryList,fileList;
    private File pathFile;
    private int totalFilesToCopy;
    private int progressLevel;

    public WindowsFileSystem() {
        totalFilesToCopy = 0;
        pathList = FXCollections.observableArrayList();
        availableDriveList = FXCollections.observableArrayList();
        directoryList = FXCollections.observableArrayList();
        fileList = FXCollections.observableArrayList();
        syncAllAvailableDrives();
    }

    @Override
    public void setSelectedItem(String selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    public String getSelectedItem() {
        return this.selectedItem;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public ObservableList<HBox> getPathList() {
        return pathList;
    }

    @Override
    public ObservableList<HBox> getListFromPath(String path) {
        return null;
    }

    public void syncListFromPath(String path) {

        if ( path.equals("home") ) {
            pathList.clear();
            syncAllAvailableDrives();
            return;
        }

        pathFile = new File(path);
        // if there's files in this path
        if ( pathFile.listFiles() != null ) {
            directoryList.clear();
            fileList.clear();
            String trimmedPathFileName;
            for ( File file : pathFile.listFiles() ) {
                trimmedPathFileName =file.getAbsolutePath().replace("    \\","");
                file = new File(trimmedPathFileName);
                if ( file.isDirectory() ) {
                    directoryList.add(file.getAbsolutePath());
                } else {
                    fileList.add(file.getAbsolutePath());
                }
            }

            directoryList.sort( ( d1 , d2 ) -> d1.compareTo(d2) );
            fileList.sort( (f1 , f2 ) -> f1.compareTo(f2) );
            pathList.clear();
            addFilesToPathList(directoryList,fileList);
        } else {
            Platform.runLater(() -> pathList.clear());
            for ( HBox cellContainer : getAllAvailableDrives() ) {
                Platform.runLater(() -> pathList.add(cellContainer));
            }
        }

//        return pathList;
    }

    public void syncAllAvailableDrives() {

        availableDriveList.clear();
        for ( String drive : SystemCommandManipulator.executeSystemCommand("wmic logicaldisk get name").split("\n") ) {
            if ( drive.contains("Name") || drive.equalsIgnoreCase("") ) continue;
            drive = drive.replace("    ","");
            if ( drive.equalsIgnoreCase("C:") ) drive+="\\Users";
            File tempFile = new File(drive);
            if ( tempFile.isDirectory() ) {
                availableDriveList.add(drive);
                pathList.add(createAndGetPathListContainer(drive,greenDirectoryIconPath));
            }

        }
    }

    public boolean isDirectory ( HBox container ) {
        Label fileName = (Label) container.getChildren().get(1);
        File file = null;
        if ( this.getCurrentPath() == null || this.getCurrentPath().equals("") || isCurrentPathMatchWithSelectedItem() ) {
            file = new File(fileName.getText());
        } else {
            file = new File(this.getCurrentPath()+"\\"+fileName.getText());
        }
        return file.isDirectory();
    }

    public boolean isCurrentPathMatchWithSelectedItem() {
        return this.getCurrentPath().equals(this.getSelectedItem());
    }

    public String getExtractedFileNameFromContainer( HBox container ) {
        Label fileName = (Label) container.getChildren().get(1);
        return fileName.getText();
    }

    public ObservableList<HBox> getAllAvailableDrives() {
        ObservableList<HBox> tempAvailableDrives = FXCollections.observableArrayList();
        for ( String drive : availableDriveList ) {
            tempAvailableDrives.add(createAndGetPathListContainer(drive,greenDirectoryIconPath));
        }
        return tempAvailableDrives;
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
        Label fileNameLabel = new Label(filterOnlyFileNameFromPath(fileName));
        container.getChildren().addAll(iconContainer,fileNameLabel);
        return container;
    }

    private String filterOnlyFileNameFromPath(String path) {
        if ( path.equalsIgnoreCase("C:\\Users")) return path;
        StringBuilder finalPath = new StringBuilder();
        int startIndex = 0;
        for ( int rId = path.length()-1 ; rId >= 0 ; rId-- ) {
            if ( path.charAt(rId) == '\\' ) {
                startIndex = ++rId;
                break;
            }
        }
        if ( startIndex != 0 ) {
            for ( int id = startIndex ; id < path.length() ; id++ ) {
                finalPath.append(path.charAt(id));
            }
            return finalPath.toString();
        }
        return path;
    }

    public void increaseCurrentPath(String selectedItem) {
        if ( this.getCurrentPath() == null || this.getCurrentPath().equals("") ) {
            this.setCurrentPath(selectedItem);
        } else {
            if ( !isCurrentPathMatchWithSelectedItem() ) {
                this.setCurrentPath(this.getCurrentPath()+"\\"+selectedItem);
            }
        }
    }

    public void decreaseCurrentPath() {
        if ( this.getCurrentPath().contains("\\") ) {
            int lastIndex = 0;
            for ( int rId = this.getCurrentPath().length() -1 ; rId >= 0 ; rId-- ) {
                if ( this.getCurrentPath().charAt(rId) == '\\' ) {
                    lastIndex = rId;
                    break;
                }
            }

            StringBuilder tempCurrentPath = new StringBuilder();
            for ( int id = 0 ; id < lastIndex ; id++ ) {
                tempCurrentPath.append(this.getCurrentPath().charAt(id));
            }
            this.setCurrentPath(tempCurrentPath.toString());
        } else {
            this.setCurrentPath("home");
        }
    }

    public double getTotalSpace(String drivePath) {
        return getSpace(drivePath,2);
    }
    public double getFreeSpace(String drivePath) {
        return getSpace(drivePath,1);
    }
    public double getDirectorySize(File directory) {
        double size = (double)FileUtils.sizeOfDirectory(directory)/oneGigabyteInByte;
        return Double.parseDouble(String.format("%.2f",size));
    }
    private double getSpace(String drivePath,int spacePosition) {
        String driveLetter = getDriveLetterFromDrivePath(drivePath);
        int spaceCounter = 0;
        String spaceInString = "0";
        long actualSpace = 0;
        String[] filteredDrive = null;
        String[] filteredDriveString = SystemCommandManipulator.executeSystemCommand("wmic logicaldisk get size, freespace, caption").split("\n");

        for ( String driveString : filteredDriveString ) {
            if ( driveString.contains(driveLetter) ) {
                filteredDrive = driveString.split(" ");
                break;
            }
        }

        for ( String space : filteredDrive ) {
            if ( !space.equals("") ) {
                if ( spaceCounter == spacePosition ) {
                    spaceInString = space;
                    break;
                } else {
                    ++spaceCounter;
                }
            }
        }
        actualSpace = Long.parseLong(spaceInString)/oneGigabyteInByte;
        return Double.parseDouble(String.valueOf(actualSpace));

    }
    private String getDriveLetterFromDrivePath(String drivePath) {
        String driveLetter = "";
        driveLetter += String.valueOf(drivePath.charAt(0)) + String.valueOf(drivePath.charAt(1));
        return driveLetter;
    }

    public void copyDirectory(File sourceDirectory, File destDirectory) {
        Thread initializeThread = new Thread(() -> initializeBeforeCopy(sourceDirectory) );
        Thread copyAnimationThread = new Thread(() -> {
            int maxProgressLevel = totalFilesToCopy;
            Task<ProgressBar> task = new Task<ProgressBar>() {
                @Override
                protected ProgressBar call() throws Exception {
                    while ( progressLevel != maxProgressLevel && !directoryCopyInterruptFlag) {
//                        System.out.println("Progress level - "+androidProgressLevel);
//                        System.out.println("Total files - "+maxProgressLevel);
                        updateProgress(progressLevel,maxProgressLevel);
                        updateMessage(String.valueOf(totalFilesToCopy));
                    }
                    if ( directoryCopyInterruptFlag ) {
                        ViewSwitcher.getInstance().showCopyCancelledView("Cancelled",Color.RED);
                    } else ViewSwitcher.getInstance().showCopyCompleteView("Completed",Color.ORANGE);

                    return null;
                }
            };
            ViewSwitcher.getInstance().showCopyingView(task,"orange",Color.ORANGE);
            task.run();

        });
        Thread copyThread = new Thread(() -> {
            copyAlgorithm(sourceDirectory,destDirectory);
        });

        Thread mainCopyThread = new Thread(() -> {
            try {

                initializeThread.start();
                initializeThread.join();
                copyAnimationThread.start();
                copyThread.start();

            } catch ( Exception e ) {
                e.printStackTrace();
            }
        });
        mainCopyThread.start();
//        System.out.println("Total file - "+totalFilesToCopy);
    }

    private void initializeBeforeCopy(File sourceDirectory) {
        System.out.println("Initializing...");
        totalFilesToCopy = 0;
        progressLevel = 0;
        directoryCopyInterruptFlag = false;
        totalFileCounter(sourceDirectory);
        ViewSwitcher.getInstance().showInitializeView(Color.ORANGE);

    }

    private void copyAlgorithm(File sourceDirectory,File destDirectory) {
        if ( sourceDirectory.isDirectory() ) {
            File currentDestDirectory = new File(destDirectory.getAbsolutePath()+"\\"+sourceDirectory.getName());
            // if current source directory is not exist in dest ...
            if ( !currentDestDirectory.exists() ) {
                currentDestDirectory.mkdir();
            }
            for ( File directory : sourceDirectory.listFiles() ) {
                if ( directory.isDirectory() ) {
                    copyAlgorithm(directory,currentDestDirectory);
                } else {
                    if ( !new File(currentDestDirectory.getAbsolutePath()+"\\"+directory.getName()).exists() ) {
                        SystemCommandManipulator.executeSystemCommand("cmd /c copy /Y \""+directory.getAbsolutePath()+"\" \""+
                                currentDestDirectory.getAbsolutePath()+"\"");
                        System.out.println("Copied the file...");
                        ++progressLevel;
                        --totalFilesToCopy;
                    } else {
                        System.out.println("Skip the file...");
                        ++progressLevel;
                        --totalFilesToCopy;
                    }
                }
            }
        }
//        else {
//            if ( !new File(destDirectory.getAbsolutePath()+"\\"+sourceDirectory.getName()).exists() ) {
//                SystemCommandManipulator.executeSystemCommand("copy /Y \""+sourceDirectory.getAbsolutePath()+"\" \""+
//                        currentDestDirectory.getAbsolutePath()+"\"");
//            } else {
//                System.out.println("Skip the file...");
//            }
//
//        }
    }

    private void totalFileCounter(File sourceDirectory) {
        if ( sourceDirectory.listFiles() != null ) {
            for ( File directory :sourceDirectory.listFiles() ) {
                if ( directoryCopyInterruptFlag ) break;
                if ( directory.isDirectory() ) {
                    totalFileCounter(directory);
                } else {
                    ++totalFilesToCopy;
                }
            }
        } else {
            System.out.println("Empty directory - "+sourceDirectory.getAbsolutePath());
        }
    }

    public ObservableList<HBox> getFormattedDirectoriesDetailLists(File originalDirectory,File copiedDirectory) {
        ObservableList<HBox> formattedDirectoryDetailList = FXCollections.observableArrayList();
        ObservableList<File> originalFiles = FXCollections.observableArrayList();
        ObservableList<File> copiedFiles = FXCollections.observableArrayList();

        if ( originalDirectory.listFiles() == null || copiedDirectory.listFiles() == null ) return null;

        originalFiles.addAll(Arrays.asList(originalDirectory.listFiles()));
        copiedFiles.addAll(Arrays.asList(copiedDirectory.listFiles()));

        for ( int id = 0 ; id < originalFiles.size() ; id++ ) {
            if ( id > copiedFiles.size() ) {
                formattedDirectoryDetailList.add(createAndGetHBox(originalFiles.get(id),new File("No directory")));
            } else {
                formattedDirectoryDetailList.add(createAndGetHBox(originalFiles.get(id),copiedFiles.get(id)));
            }
        }

        return formattedDirectoryDetailList;
    }

    private HBox createAndGetHBox(File originalFile,File copiedFile) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(40);
        Label originalDirectoryName = new Label(originalFile.getName());
        Label originalDirectorySpace = new Label(String.valueOf(getDirectorySize(originalFile)));
        Label copiedDirectoryName = new Label(copiedFile.getName());
        Label copiedDirectorySpace = new Label(String.valueOf(getDirectorySize(copiedFile)));
        ImageView isMatchIcon = new ImageView();
        isMatchIcon.setFitWidth(20);
        isMatchIcon.setFitHeight(20);
        container.getChildren().addAll(originalDirectoryName,originalDirectorySpace,isMatchIcon,copiedDirectorySpace,copiedDirectoryName);
        return container;

    }

    public void validateFormattedDirectories(ObservableList<HBox> list) {
        double originalSpace = 0;
        double copiedSpace = 0;
        Label originalSpaceLabel,copiedSpaceLabel;
        ImageView isMatchIcon = null;

        for ( HBox container : list ) {
            originalSpaceLabel = (Label) container.getChildren().get(1);
            originalSpace = Double.parseDouble(originalSpaceLabel.getText());
            copiedSpaceLabel = (Label) container.getChildren().get(3);
            copiedSpace = Double.parseDouble(copiedSpaceLabel.getText());
            isMatchIcon =  (ImageView) container.getChildren().get(2);
            if ( originalSpace == copiedSpace ) {
                isMatchIcon.setImage(new Image(new Main().getClass().getResource(validIconPath).toString()));
            } else {
                isMatchIcon.setImage(new Image(new Main().getClass().getResource(invalidIconPath).toString()));
            }

        }
    }
}
