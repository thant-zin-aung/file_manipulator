package application.controller;

import application.model.AnimationStyles;
import application.model.ViewSwitcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainController {

    private HBox currentTag;
    private Stage currentStage;

    @FXML private AnchorPane mainViewRoot;

    @FXML private VBox mainTagsContainer,copyTagsContainer,validateTagsContainer;

    @FXML
    private HBox copyTag,validateTag,exitTag, androidToPcCopyTag, pcToPcCopyTag,pcToPcValidateTag,backToMainTag,backToMainTag2;
    public void initialize() {

    }

    @FXML
    public void hoverOnTags(MouseEvent event) {
        currentStage = new Stage();
        currentTag = (HBox) event.getSource();
        DropShadow dropShadow = new DropShadow();
        dropShadow.setWidth(85);
        dropShadow.setHeight(50);
        currentTag.setEffect(dropShadow);
        if ( currentTag == copyTag ) dropShadow.setColor(Color.valueOf("#23B4FF"));
        else if ( currentTag == validateTag ) dropShadow.setColor(Color.valueOf("#06E3A2"));
        else if ( currentTag == exitTag ) dropShadow.setColor(Color.valueOf("#E70000"));
        else dropShadow.setColor(Color.BLACK);
        AnimationStyles.scaleEffect(currentTag,200,1,false,1,1,1.1,1.1);
    }
    @FXML
    public void hoverExitFromTags(MouseEvent event) {
        currentTag = (HBox) event.getSource();
        currentTag.setEffect(null);
        AnimationStyles.scaleEffect(currentTag,200,1,false,1.1,1.1,1,1);
    }

    @FXML
    public void clickOnTags(MouseEvent event) {
        currentTag = (HBox) event.getSource();
//        AnimationStyles.scaleEffect(currentTag,200,2 , true , 1.1 , 1.1 , 1 , 1);



        // Exit from program thread
        if ( currentTag == exitTag) {
            System.exit(0);
        } else if ( currentTag == copyTag ) {

            copyTagsContainer.setVisible(true);
            switchSlideToNext(mainTagsContainer,copyTagsContainer);

        } else if ( currentTag == validateTag ) {

            validateTagsContainer.setVisible(true);
            switchSlideToNext(mainTagsContainer,validateTagsContainer);

        } else if ( currentTag == backToMainTag ) {

            switchSlideToBack(mainTagsContainer,copyTagsContainer);
            new Thread(() -> {
                delayTime(1000);
                Platform.runLater(() -> {
                    copyTagsContainer.setVisible(false);
                });
            }).start();

        } else if ( currentTag == backToMainTag2 ) {

            switchSlideToBack(mainTagsContainer,validateTagsContainer);
            new Thread(() -> {
                delayTime(1000);
                Platform.runLater(() -> {
                    validateTagsContainer.setVisible(false);
                });
            }).start();

        } else if ( currentTag == androidToPcCopyTag) {
            ViewSwitcher.getInstance().switchFromMainToAndroidToPcCopyView();
        } else if ( currentTag == pcToPcCopyTag) {
            ViewSwitcher.getInstance().switchFromMainToPcToPcCopyView();
        } else if ( currentTag == pcToPcValidateTag ) {
            ViewSwitcher.getInstance().switchFromMainToPcToPcValidateView();
        }
    }


    private void switchSlideToNext(Node firstTag,Node secondTag) {
        double translateDuration = 600;
        double fadeDuration = 600;
        int cycleCount = 1;
        boolean isAutoReverse = false;
        double translateToX = -500;
        double translateFromX = 500;
        AnimationStyles.translateAndFadeParallelEffect(firstTag,translateDuration , fadeDuration, cycleCount , isAutoReverse,0,translateToX,1,0);
        AnimationStyles.translateAndFadeParallelEffect(secondTag,translateDuration, fadeDuration, cycleCount , isAutoReverse , translateFromX , 0,0,1);
    }
    private void switchSlideToBack(Node firstTag,Node secondTag) {
        double translateDuration = 600;
        double fadeDuration = 600;
        int cycleCount = 1;
        boolean isAutoReverse = false;
        double translateToX = 500;
        double translateFromX = -500;
        AnimationStyles.translateAndFadeParallelEffect(firstTag,translateDuration , fadeDuration, cycleCount , isAutoReverse,translateFromX,0,0,1);
        AnimationStyles.translateAndFadeParallelEffect(secondTag,translateDuration, fadeDuration, cycleCount , isAutoReverse , 0 , translateToX,1,0);
    }
    private void delayTime(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
