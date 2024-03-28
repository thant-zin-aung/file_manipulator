package application.controller;

import application.Main;
import application.model.AnimationStyles;
import application.model.SystemCommandManipulator;
import application.model.ViewSwitcher;
import application.model.WindowsFileSystem;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class PcToPcCopyController {

    private double selectedDirectorySize;
    @FXML private AnchorPane root,animationRoot;
    @FXML private ImageView selectSourceButton,selectDestButton;
    @FXML private ProgressIndicator sourceProgressIndicator,destProgressIndicator;
    @FXML private JFXProgressBar beforeInitProgressBar,afterInitProgressBar;
    @FXML private Label sourceSpaceLabel,destSpaceLabel;

    private WindowsFileSystem windowsFileSystem;
    private File sourceDirectory,destDirectory;

    private Stage pcToPcCopyStage;
    public void initialize() {
        windowsFileSystem = new WindowsFileSystem();
        // Set Pc to Pc copy Stage to draggable
        ViewSwitcher.getInstance().setViewDraggable(ViewSwitcher.getInstance().getPcToPcCopyStage(),root,animationRoot,Color.ORANGE);
        setProgressStateToOriginal();

    }

    @FXML
    public void onBackToMainButtonClick() {
        ViewSwitcher.getInstance().switchBackToMainView();
    }

    @FXML
    public void clickOnCopyButton() {
        windowsFileSystem.copyDirectory(sourceDirectory,destDirectory);
    }

    @FXML
    public void hoverOnSelectButtons(MouseEvent event) {
        ImageView currentButton = (ImageView) event.getSource();
        AnimationStyles.scaleEffect(currentButton,300,1,false,1,1,1.1,1.1);

    }
    @FXML
    public void hoverExitFromSelectButtons(MouseEvent event) {
        ImageView currentButton = (ImageView) event.getSource();
        AnimationStyles.scaleEffect(currentButton,300,1,false,1.1,1.1,1,1);
    }

    @FXML
    public void clickOnSelectButtons(MouseEvent e) {
        ImageView currentButton = ( ImageView) e.getSource();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(ViewSwitcher.getInstance().getCurrentStage());
        if ( currentButton == selectSourceButton ) {
            refreshProgressStates(selectedDirectory,sourceProgressIndicator,sourceSpaceLabel);
            sourceDirectory = selectedDirectory;
        } else if ( currentButton == selectDestButton ) {
//            refreshProgressStates(selectedDirectory,destProgressIndicator,destSpaceLabel,beforeInitProgressBar,afterInitProgressBar);
            new Thread(() -> refreshProgressStates(selectedDirectory,destProgressIndicator,destSpaceLabel,beforeInitProgressBar,afterInitProgressBar)).start();
            destDirectory = selectedDirectory;
        }
    }

    @FXML
    private void setProgressStateToOriginal() {
        sourceProgressIndicator.setProgress(0);
        destProgressIndicator.setProgress(0);
        beforeInitProgressBar.setProgress(0);
        afterInitProgressBar.setProgress(0);
        sourceSpaceLabel.setText("0.0 GB");
        destSpaceLabel.setText("0.0 GB");

//        refreshProgress(sourceProgressIndicator,0,0);
//        refreshProgress(destProgressIndicator,0,0);
//        refreshProgress(beforeInitProgressBar,0,0);
//        refreshProgress(afterInitProgressBar,0,0);
//        refreshLabel(sourceSpaceLabel,0.00);
//        refreshLabel(destSpaceLabel,0.00);
    }

    private void refreshProgress(Control progress,double currentProgress,double maxProgress) {
        Task<Control> task = new Task<Control>() {
            @Override
            protected Control call() throws Exception {
                new Thread(() -> {
                    for ( int id = 0 ; id < currentProgress ; id++ ) {
                        updateProgress(id,maxProgress);
                        SystemCommandManipulator.delayTime(2);
                    }
                }).start();
                return null;
            }
        };
        if ( progress instanceof ProgressBar ) {
            JFXProgressBar currentProgressBar = (JFXProgressBar) progress;
            Platform.runLater(() -> currentProgressBar.progressProperty().bind(task.progressProperty()) );
        } else if ( progress instanceof ProgressIndicator ) {
            ProgressIndicator currentProgressIndicator = (ProgressIndicator) progress;
            Platform.runLater(() -> currentProgressIndicator.progressProperty().bind(task.progressProperty()) );
        }
        task.run();
    }

    private void refreshLabel(Label label,double freeSpace) {
        Task<Control> task = new Task<Control>() {
            @Override
            protected Control call() throws Exception {
                new Thread(() -> {
                    for ( int id = 0 ; id < freeSpace ; id++ ) {
                        updateMessage(id+".00 GB");
                        SystemCommandManipulator.delayTime(2);
                    }
                    updateMessage(freeSpace+" GB");
                }).start();
                return null;
            }
        };
        Platform.runLater(() -> label.textProperty().bind(task.messageProperty()));
        task.run();
    }

    private void refreshProgressStates(File selectedDirectory,ProgressIndicator progressIndicator,Label label) {
        if ( selectedDirectory == null ) {
            selectedDirectorySize = 0;
            refreshProgress(progressIndicator,0,0);
            refreshLabel(label,0.00);
        } else {
            refreshProgress(progressIndicator,
                    windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()) -
                            windowsFileSystem.getFreeSpace(selectedDirectory.getAbsolutePath()),
                    windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()));
            refreshLabel(label,windowsFileSystem.getFreeSpace(selectedDirectory.getAbsolutePath()));
            selectedDirectorySize = windowsFileSystem.getDirectorySize(selectedDirectory);
        }
    }
    private void refreshProgressStates(File selectedDirectory,ProgressIndicator progressIndicator,Label label,ProgressBar ... progressBars) {
        if ( selectedDirectory == null ) {
            refreshProgress(progressIndicator,0,0);
            refreshProgress(progressBars[0],0,0);
            refreshProgress(progressBars[1],0,0);
            refreshLabel(label,0.00);
        } else {
            System.out.println("Selected directory size - "+selectedDirectorySize);
            refreshProgress(progressIndicator,
                    windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()) -
                            windowsFileSystem.getFreeSpace(selectedDirectory.getAbsolutePath()),
                    windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()));
            refreshProgress(progressBars[0],
                    windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()) -
                            windowsFileSystem.getFreeSpace(selectedDirectory.getAbsolutePath()),
                    windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()));
            refreshProgress(progressBars[1],
                    ( windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()) -
                            windowsFileSystem.getFreeSpace(selectedDirectory.getAbsolutePath()) ) +
                            selectedDirectorySize,
                    windowsFileSystem.getTotalSpace(selectedDirectory.getAbsolutePath()));
            refreshLabel(label,windowsFileSystem.getFreeSpace(selectedDirectory.getAbsolutePath()));
        }
    }


}
