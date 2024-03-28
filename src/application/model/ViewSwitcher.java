package application.model;

import application.Main;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ViewSwitcher {
    private final Stage mainStage,androidToPcCopyStage,pcToPcCopyStage,pcToPcValidateStage,
            initializeStage,copyCompleteStage,copyCanceledStage;
    private Stage copyStage;
    private final String appIconPath;
    private final String androidToPcCopyViewPath;
    private final String pcToPcCopyViewPath;
    private final String pcToPcValidateViewPath;
    private final String initializeViewPath;
    private final String copyCompleteViewPath;
    private final String copyCancelViewPath;
    private final String minimizeIconPath;
    private final String closeIconPath;
    private static final ViewSwitcher viewSwitcher = new ViewSwitcher();
    private final double stageMinWidth = 1280;
    private final double stageMinHeight = 720;

    private double xOffSet;
    private double yOffSet;

    private ViewSwitcher() {
        mainStage = Main.mainStage;
        androidToPcCopyStage = new Stage();
        pcToPcCopyStage = new Stage();
        pcToPcValidateStage = new Stage();
        copyStage = new Stage();
        initializeStage = new Stage();
        copyCompleteStage = new Stage();
        copyCanceledStage = new Stage();
        initializeStage.initModality(Modality.APPLICATION_MODAL);
        copyCompleteStage.initModality(Modality.APPLICATION_MODAL);
        appIconPath = "assets/photos/app_icon.png";
        androidToPcCopyViewPath = "view/AndroidToPcCopyView.fxml";
        pcToPcCopyViewPath = "view/PcToPcCopyView.fxml";
        pcToPcValidateViewPath = "view/PcToPcValidateView.fxml";
        initializeViewPath = "view/InitializeView.fxml";
        copyCompleteViewPath = "view/CopyCompleteView.fxml";
        copyCancelViewPath = "view/CopyCancelView.fxml";
        minimizeIconPath = "assets/photos/minimize.png";
        closeIconPath = "assets/photos/close.png";

    }

    public Stage getMainStage() {
        return mainStage;
    }

    public Stage getAndroidToPcCopyStage() {
        return androidToPcCopyStage;
    }

    public Stage getPcToPcCopyStage() {
        return pcToPcCopyStage;
    }

    public Stage getPcToPcValidateStage() {
        return pcToPcValidateStage;
    }

    public Stage getInitializeStage() {
        return initializeStage;
    }

    public Stage getCopyCompleteStage() {
        return copyCompleteStage;
    }

    public Stage getCopyCanceledStage() {
        return copyCanceledStage;
    }

    public Stage getCopyStage() {
        return copyStage;
    }

    public static ViewSwitcher getInstance() {
        return viewSwitcher;
    }

    private void viewLoader(Stage showStage,String viewPath,Stage hideStage,boolean isTransparent,
                            boolean useSwitchEffect,boolean sceneToTransparent) {
        try {
            if ( showStage.getScene() == null ) {
                Parent root = FXMLLoader.load(new Main().getClass().getResource(viewPath));
                Scene scene = new Scene(root);
                if ( sceneToTransparent ) {
                    scene.setFill(Color.TRANSPARENT);
                    showStage.initStyle(StageStyle.TRANSPARENT);
                }
                showStage.setOnCloseRequest((e) -> {
                    e.consume();
                    System.exit(0);
                });

//                showStage.setMinWidth(stageMinWidth);
//                showStage.setMinHeight(stageMinHeight);
                if ( showStage.getIcons().size() == 0 ) {
                    showStage.getIcons().add(new Image(new Main().getClass().getResource(appIconPath).toString()));
                }
                showStage.sizeToScene();

                if ( isTransparent ) {
                    scene.setFill(Color.TRANSPARENT);
                    showStage.initStyle(StageStyle.TRANSPARENT);
                }
                showStage.setScene(scene);
                if ( useSwitchEffect ) {
                    switchViewWithEffect(showStage,hideStage);
                } else {
                    showStage.show();
                    hideView(hideStage);
                }
            } else {
                if ( useSwitchEffect ) {
                    switchViewWithEffect(showStage,hideStage);
                } else {
                    showStage.show();
                    hideView(hideStage);
                }
            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void delayTime(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void switchViewWithEffect(Stage showStage,Stage hideStage) {
        new Thread(() -> {
            AnimationStyles.fadeEffect(hideStage.getScene().getRoot(),200,1 , false , 1,0);
            delayTime(200);
            Platform.runLater(showStage::show);
            AnimationStyles.fadeEffect(showStage.getScene().getRoot(),500,1 , false , 0,1);
            Platform.runLater(() -> hideView(hideStage));
        }).start();
    }

    private void hideView(Stage stage) {
        stage.getScene().getWindow().hide();
    }

    public Stage getCurrentStage() {
        if ( androidToPcCopyStage.isShowing() ) return androidToPcCopyStage;
        else if ( pcToPcCopyStage.isShowing() ) return pcToPcCopyStage;
        else return pcToPcValidateStage;
    }

    public void switchBackToMainView() {
        switchViewWithEffect(mainStage,getCurrentStage());
    }

    public void switchFromMainToAndroidToPcCopyView() {
        viewLoader(androidToPcCopyStage,androidToPcCopyViewPath,mainStage,false,true,false);
    }

    public void switchFromMainToPcToPcCopyView() {
        viewLoader(pcToPcCopyStage,pcToPcCopyViewPath,mainStage,false,true,true);
    }

    public void switchFromMainToPcToPcValidateView() {
        viewLoader(pcToPcValidateStage,pcToPcValidateViewPath,mainStage,false,true,true);
    }

    public void showInitializeView(Color sprinkleColor) {
        try {
            Parent root = FXMLLoader.load(new Main().getClass().getResource(initializeViewPath));
            Platform.runLater(() -> {
                initializeStage.setScene(new Scene(root));
                initializeStage.setTitle("Initializing");
                initializeStage.setResizable(false);
                initializeStage.sizeToScene();
                initializeStage.show();
                AnimationStyles.sprinkleFlyingEffect(root,1000,300,500,new int[]{2,7},sprinkleColor);
            });
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    public void showCopyingView(Task<?> progressTask, String borderColor,Color sprinkleColor) {
        Platform.runLater(() -> {
            initializeStage.hide();
            initializeStage.close();
            getCurrentStage().setIconified(true);
            copyStage = new Stage();
            ImageView minimizeIcon = new ImageView(new Image(new Main().getClass().getResource(minimizeIconPath).toString()));
            minimizeIcon.setFitWidth(20);
            minimizeIcon.setFitHeight(20);
            ImageView closeIcon = new ImageView(new Image(new Main().getClass().getResource(closeIconPath).toString()));
            closeIcon.setFitWidth(15);
            closeIcon.setFitHeight(15);

            HBox minimizeIconContainer = new HBox();
            minimizeIconContainer.setPrefWidth(20);
            minimizeIconContainer.setPrefHeight(20);
            minimizeIconContainer.setAlignment(Pos.CENTER);
            minimizeIconContainer.getChildren().add(minimizeIcon);

            HBox closeIconContainer = new HBox();
            closeIconContainer.setPrefWidth(20);
            closeIconContainer.setPrefHeight(20);
            closeIconContainer.setAlignment(Pos.CENTER);
            closeIconContainer.getChildren().add(closeIcon);

//            minimizeIconContainer.setOnMouseClicked( e -> {
//                copyStage.setIconified(true);
//                getCurrentStage().setIconified(true);
//            });
//            closeIconContainer.setOnMouseClicked( e -> {
//                AndroidFileSystem.fileCopyInterruptFlag = true;
//                AndroidFileSystem.directoryCopyInterruptFlag = true;
//                copyStage.close();
//            });
            copyStage.setOnCloseRequest(e -> {
                e.consume();
                AndroidFileSystem.fileCopyInterruptFlag = true;
                AndroidFileSystem.directoryCopyInterruptFlag = true;
                WindowsFileSystem.directoryCopyInterruptFlag = true;
                copyStage.close();
            });
            try {
                double prefWidth = 800;
                double prefHeight = 170;
                double progressPadding = 20;
                StackPane mainRoot = new StackPane();
                AnchorPane animationRoot = new AnchorPane();
                Pane firstComponentGap = new Pane();
                HBox.setHgrow(firstComponentGap,Priority.ALWAYS);

                VBox copyComponentsRoot = new VBox();
                HBox firstComponent = new HBox();
                HBox firstRightComponent = new HBox();
                HBox secondComponent = new HBox();
                HBox thirdComponent = new HBox();
                Label copyLabel = new Label("Copying");
                JFXProgressBar progressBar = new JFXProgressBar();
                Text itemRemainingText = new Text("Item remaining: ");
                Label itemRemainingLabel = new Label("0");

                firstComponent.setPrefWidth(prefWidth);
                firstComponent.setPrefHeight(50);
                firstComponent.setAlignment(Pos.CENTER_LEFT);
                firstComponent.setSpacing(10);
                firstComponent.setPadding(new Insets(0,0,0,20));
                firstRightComponent.setPrefWidth(100);
                firstRightComponent.setSpacing(20);
                firstRightComponent.setAlignment(Pos.CENTER);
                secondComponent.setPrefWidth(prefWidth);
                secondComponent.setPrefHeight(80);
                secondComponent.setAlignment(Pos.CENTER);
                secondComponent.setPadding(new Insets(0,progressPadding,0,progressPadding));
                thirdComponent.setPrefWidth(prefWidth);
                thirdComponent.setPrefHeight(50);
                thirdComponent.setSpacing(10);
                thirdComponent.setAlignment(Pos.TOP_LEFT);
                thirdComponent.setPadding(new Insets(0,0,0,20));
                animationRoot.setPrefWidth(prefWidth);
                animationRoot.setPrefHeight(prefHeight);

                progressBar.setPrefWidth( prefWidth-(progressPadding*2) );
                progressBar.setPrefHeight(30);
                progressBar.getStylesheets().add(new Main().getClass().getResource("assets/stylesheets/progressBarStyles.css").toExternalForm());
//                mainRoot.setStyle("-fx-background-color: white; -fx-border-color: "+borderColor+";" +
//                        "-fx-border-radius: 20px; -fx-border-width: 2px; -fx-background-radius: 20px");
                copyLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-font-family: Roboto;");
//                percentLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-font-family: Roboto;");
                itemRemainingText.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: Roboto;");
                itemRemainingLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: Roboto;");
                copyComponentsRoot.setStyle("-fx-background-color: transparent; -fx-background-radius: 20px; -fx-border-radius: 20px;");
                animationRoot.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-border-radius: 20px;");


                thirdComponent.getChildren().addAll(itemRemainingText,itemRemainingLabel);
                secondComponent.getChildren().add(progressBar);
                firstRightComponent.getChildren().addAll(minimizeIconContainer,closeIconContainer);
//                firstComponent.getChildren().addAll(copyLabel,firstComponentGap,firstRightComponent);
                firstComponent.getChildren().addAll(copyLabel);
                copyComponentsRoot.getChildren().addAll(firstComponent,secondComponent,thirdComponent);
                mainRoot.getChildren().addAll(animationRoot,copyComponentsRoot);

                progressBar.progressProperty().bind(progressTask.progressProperty());
                itemRemainingLabel.textProperty().bind(progressTask.messageProperty());
//                copyLabel.textProperty().bind(progressTask.titleProperty());

                AnimationStyles.sprinkleFlyingEffect(animationRoot,3000,1000,1500,new int[]{2,4},sprinkleColor);

                Scene scene = new Scene(mainRoot,prefWidth,prefHeight);
                scene.setFill(Color.TRANSPARENT);
                copyStage.setScene(scene);
                copyStage.initModality(Modality.APPLICATION_MODAL);
//                copyStage.initStyle(StageStyle.TRANSPARENT);
                copyStage.setResizable(false);
                copyStage.sizeToScene();
                copyStage.setTitle("Copy from android to pc");
                copyStage.show();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        });
    }

    public void showCopyCompleteView(String status,Color sprinkleColor) {
        try {
            Parent root = FXMLLoader.load(new Main().getClass().getResource(copyCompleteViewPath));
            Platform.runLater(() -> {
                copyStage.hide();
                copyStage.close();
                copyCompleteStage.setScene(new Scene(root));
                copyCompleteStage.setTitle(status);
                copyCompleteStage.setResizable(false);
                copyCompleteStage.sizeToScene();
                copyCompleteStage.show();
                AnimationStyles.sprinkleFlyingEffect(root,6000,1000,2000,sprinkleColor);
            });
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void showCopyCancelledView(String status,Color sprinkleColor) {
        try {
            Parent root = FXMLLoader.load(new Main().getClass().getResource(copyCancelViewPath));
            Platform.runLater(() -> {
                AndroidFileSystem.fileCopyInterruptFlag = true;
                AndroidFileSystem.directoryCopyInterruptFlag = true;
                copyStage.hide();
                copyStage.close();
                copyCanceledStage.setScene(new Scene(root));
                copyCanceledStage.setTitle(status);
                copyCanceledStage.setResizable(false);
                copyCanceledStage.sizeToScene();
                copyCanceledStage.show();
                AnimationStyles.sprinkleFlyingEffect(root,6000,1000,2000,sprinkleColor);
            });
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void setViewDraggable(Stage stage,Parent root,AnchorPane animationRoot,Color sprinkleColor) {
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffSet = event.getSceneX();
                yOffSet = event.getSceneY();
                AnimationStyles.sprinkleFlyingEffect(animationRoot,3500,1,0,sprinkleColor);
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX()-xOffSet);
                stage.setY(event.getScreenY()-yOffSet);
            }
        });
    }
}
