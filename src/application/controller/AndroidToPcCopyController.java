package application.controller;

import application.model.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRadioButton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


public class AndroidToPcCopyController {

    private final String androidDefaultPath = "sdcard";

    private HBox sourceViewCellContainer,destViewCellContainer;

    private DropShadow dropShadow;
    private int[] sprinkleSize;

    private DeviceManipulator deviceManipulator;
    private AndroidFileSystem androidFileSystem;
    private WindowsFileSystem windowsFileSystem;

    @FXML private AnchorPane androidToPcCopyRoot;
    @FXML private HBox connectedDeviceBox;
    @FXML private VBox optionContainer;
    @FXML private Label deviceNameLabel, deviceStatusLabel , deviceProductLabel , androidPathLabel , pcPathLabel;
    @FXML private JFXListView<HBox> sourcePathListView,destPathListView;
    @FXML private ImageView sourceBackButton,destBackButton,copyLogo;
    @FXML private JFXButton backToMainButton,copyButton;
    @FXML private JFXRadioButton androidToPcOption,pcToAndroidOption;


    public void initialize() {
        sprinkleSize = new int[]{2,6};
        copyButton.setVisible(false);
        androidFileSystem = new AndroidFileSystem();
        windowsFileSystem = new WindowsFileSystem();
        destPathListView.setItems(windowsFileSystem.getPathList());
        deviceManipulator = new DeviceManipulator(deviceNameLabel,deviceStatusLabel,deviceProductLabel,connectedDeviceBox);
        sourcePathListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dropShadow = new DropShadow();
        dropShadow.setWidth(30);
        dropShadow.setHeight(30);
        dropShadow.setColor(Color.valueOf("#00D141"));
        sourcePathListView.setEffect(dropShadow);
        destPathListView.setEffect(dropShadow);
        optionContainer.setEffect(dropShadow);
        androidFileSystem.setCurrentPath(androidDefaultPath);
        androidFileSystem.setPreviousPath(androidDefaultPath);
        androidPathLabel.setText(androidFileSystem.getCurrentPath());
        deviceManipulator.start_Android_Device_Info_Checker_Thread(1000);
        start_Android_To_Pc_Copy_View_Monitor_Thread();
        start_UI_Color_Thread();

        sourcePathListView.setOnMouseClicked((e) -> {
            setVisibilityCopyButtonUsingAI();
            if ( e.getClickCount() == 2 ) {
                sourceViewCellContainer = sourcePathListView.getSelectionModel().getSelectedItem();
                String tempCurrentPath = androidFileSystem.getCurrentPath()+"/"+
                        androidFileSystem.getExtractedFileNameFromContainer(sourceViewCellContainer);
                if ( androidFileSystem.isDirectory(tempCurrentPath) ) {
                    androidFileSystem.setCurrentPath(tempCurrentPath);
                    sourcePathListView.getItems().clear();
                    sourcePathListView.getItems().addAll(androidFileSystem.getListFromPath(
                            androidFileSystem.getCurrentPath()
                    ));
                    androidFileSystem.increasePreviousPath(androidFileSystem.getExtractedFileNameFromContainer(sourceViewCellContainer));
                }
                androidPathLabel.setText(androidFileSystem.getCurrentPath());
            }
            AnimationStyles.sprinkleFlyingEffect(androidToPcCopyRoot,1500,1,0,sprinkleSize,Color.GREEN);
        });

        destPathListView.setOnMouseClicked( ( e ) -> {
            setVisibilityCopyButtonUsingAI();
            destViewCellContainer = destPathListView.getSelectionModel().getSelectedItem();
            windowsFileSystem.setSelectedItem(windowsFileSystem.getExtractedFileNameFromContainer(destViewCellContainer));
            if ( windowsFileSystem.getCurrentPath() != null ) {
                if ( windowsFileSystem.getCurrentPath().equals("home") ) windowsFileSystem.setCurrentPath(
                        windowsFileSystem.getExtractedFileNameFromContainer(destViewCellContainer)
                );
            }

            if ( e.getClickCount() == 2 ) {
                if ( windowsFileSystem.isDirectory(destViewCellContainer) ) {

                    windowsFileSystem.increaseCurrentPath(windowsFileSystem.getExtractedFileNameFromContainer(destViewCellContainer));
                    windowsFileSystem.syncListFromPath(windowsFileSystem.getCurrentPath());

                }
            }
            pcPathLabel.setText(windowsFileSystem.getCurrentPath());
            AnimationStyles.sprinkleFlyingEffect(androidToPcCopyRoot,1500,1,0,sprinkleSize,Color.GREEN);
        });
    }

    private void start_Android_To_Pc_Copy_View_Monitor_Thread() {
        new Thread(() -> {
            while ( true ) {
                    // if view is showing and thread is not running...
                if ( ViewSwitcher.getInstance().getAndroidToPcCopyStage().isShowing() &&
                        !deviceManipulator.getDeviceStatusCheckerThread().isAlive() ) {
                    deviceManipulator.start_Android_Device_Info_Checker_Thread(1000);
                } else {
                        // if source and dest view is empty
                    if ( deviceManipulator.isConnected() && sourcePathListView.getItems().size() == 0 ) {
                        Platform.runLater(() -> {
                            addItemsToListView(sourcePathListView,androidFileSystem.getListFromPath(androidFileSystem.getCurrentPath()));
//                            addItemsToListView(destPathListView,windowsFileSystem.getAllAvailableDrives());
                        });
                    }


                }
                SystemCommandManipulator.delayTime(2500);
            }
        }).start();
    }

    private void start_UI_Color_Thread() {
        new Thread( () -> {
            SystemCommandManipulator.delayTime(1000);
            while ( true ) {
                if ( deviceManipulator.isConnected() ) {
                    disableItems(false);
                    if ( dropShadow.getColor() == Color.RED ) dropShadow.setColor(Color.valueOf("#009A30"));
                    copyButton.getStyleClass().removeIf( style -> style.equals("redBackground") ||
                            style.equals("orangeBackground"));
                    backToMainButton.getStyleClass().removeIf( style -> style.equals("redBackground") ||
                            style.equals("orangeBackground"));
                } else {
                    disableItems(true);
                    if ( ViewSwitcher.getInstance().getCopyStage().isShowing() ) {
                        ViewSwitcher.getInstance().showCopyCancelledView("Cancelled",Color.RED);
                    }
                    if ( deviceManipulator.getDeviceStatus().equalsIgnoreCase("unauthorized") ) {
//                        System.out.println("Orange");
                        dropShadow.setColor(Color.valueOf("#F29D00"));
                        copyButton.getStyleClass().add("orangeBackground");
                        backToMainButton.getStyleClass().add("orangeBackground");
                    } else {
                        dropShadow.setColor(Color.RED);
                        copyButton.getStyleClass().removeIf(s -> s.equals("orangeBackground"));
                        backToMainButton.getStyleClass().removeIf(s -> s.equals("orangeBackground"));
                        copyButton.getStyleClass().add("redBackground");
                        backToMainButton.getStyleClass().add("redBackground");
                    }
                }
                SystemCommandManipulator.delayTime(500);
            }
        }).start();
    }

    private <T> void addItemsToListView(ListView<T> listView, ObservableList<T> items) {
        for ( T item : items ) {
            Platform.runLater(() -> listView.getItems().add(item));
        }
    }

    private void setVisibilityCopyButtonUsingAI() {
        if ( sourcePathListView.getSelectionModel().getSelectedItem() != null &&
            destPathListView.getSelectionModel().getSelectedItem() != null ) {
            copyButton.setVisible(true);
        } else {
            copyButton.setVisible(false);
        }
    }

    private void disableItems(boolean isDisable) {
        if ( isDisable ) {
            sourcePathListView.setDisable(true);
            destPathListView.setDisable(true);
            copyButton.setDisable(true);
            sourceBackButton.setDisable(true);
            destBackButton.setDisable(true);
            optionContainer.setDisable(true);
            copyLogo.setVisible(false);

        } else {
            sourcePathListView.setDisable(false);
            destPathListView.setDisable(false);
            copyButton.setDisable(false);
            sourceBackButton.setDisable(false);
            destBackButton.setDisable(false);
            optionContainer.setDisable(false);
            copyLogo.setVisible(true);
        }
    }

    @FXML
    public void onBackToMainButtonClick() {
        ViewSwitcher.getInstance().switchBackToMainView();
        deviceManipulator.stop_Android_Device_Status_Checker_Thread();
    }

    @FXML
    public void hoverOnBackButtons(MouseEvent event) {
        AnimationStyles.scaleEffect((ImageView)event.getSource(),200,1,false,1,1,1.3,1.3);
    }

    @FXML
    public void hoverExitOnBackButtons(MouseEvent event) {
        AnimationStyles.scaleEffect((ImageView)event.getSource(),200,1,false,1.3,1.3,1,1);
    }

    @FXML
    public void clickOnBackButtons(MouseEvent event) {
        ImageView currentBackButton = (ImageView) event.getSource();
        if ( currentBackButton == sourceBackButton ) {
            new Thread( () -> {
                androidFileSystem.decreasePreviousPath();
                androidFileSystem.decreaseCurrentPath();
                Platform.runLater(() -> {
                    sourcePathListView.getItems().clear();
                });
                addItemsToListView(sourcePathListView,androidFileSystem.getListFromPath(
                        androidFileSystem.getCurrentPath()));
            }).start();

        } else {
            windowsFileSystem.decreaseCurrentPath();
            windowsFileSystem.syncListFromPath(windowsFileSystem.getCurrentPath());
        }
        androidPathLabel.setText(androidFileSystem.getPreviousPath());
        pcPathLabel.setText(windowsFileSystem.getCurrentPath());

//        DropShadow dropShadow = (DropShadow) sourcePathListView.getEffect();
//        Color color = dropShadow.getColor();
//        System.out.println(color.getRed());
    }

    @FXML
    public void clickOnAndroidToPcOption() {
        sourcePathListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        destPathListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @FXML
    public void clickOnPcToAndroidOption() {
        sourcePathListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        destPathListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    public void clickOnCopyButton() {
        if ( androidToPcOption.isSelected() ) {
            androidFileSystem.copyFromAndroidToPc(sourcePathListView.getSelectionModel().getSelectedItems(),
                                                windowsFileSystem.getCurrentPath()+"\\"+
                        windowsFileSystem.getExtractedFileNameFromContainer(destPathListView.getSelectionModel().getSelectedItem()));
        } else {
            System.out.println("Clicked");
            androidFileSystem.copyFromPcToAndroid(destPathListView.getSelectionModel().getSelectedItems(),
                                                androidFileSystem.getCurrentPath()+"/"+
                    androidFileSystem.getExtractedFileNameFromContainer(sourcePathListView.getSelectionModel().getSelectedItem()) ,
                    windowsFileSystem.getCurrentPath());
        }
    }

}
