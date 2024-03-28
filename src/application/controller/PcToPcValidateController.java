package application.controller;

import application.model.ViewSwitcher;
import application.model.WindowsFileSystem;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class PcToPcValidateController {

    private WindowsFileSystem windowsFileSystem;
    private File selectedOriginalDirectory;
    private File selectedCopiedDirectory;
    private DropShadow dropShadow;
    private ObservableList<HBox> formattedDirectoryDetailList;

    @FXML private AnchorPane root,animationRoot;
    @FXML private ImageView originalButton,copiedButton;
    @FXML private VBox originalContainer,copiedContainer;
    @FXML private JFXListView<HBox> listView;


    public void initialize() {
        formattedDirectoryDetailList = FXCollections.observableArrayList();
        ViewSwitcher.getInstance().setViewDraggable(ViewSwitcher.getInstance().getCurrentStage(),root,animationRoot,Color.WHITE);
        windowsFileSystem = new WindowsFileSystem();
        dropShadow = new DropShadow();
        dropShadow.setColor(Color.WHITE);
        dropShadow.setWidth(20);
        dropShadow.setHeight(20);
    }

    @FXML
    public void onBackToMainButtonClick() {
        ViewSwitcher.getInstance().switchBackToMainView();
    }

    @FXML
    public void doubleClickOnRoot(MouseEvent event) {
        if ( event.getClickCount() == 2 ) {
            Stage currentStage = ViewSwitcher.getInstance().getCurrentStage();
            if ( !currentStage.isMaximized() ) {
                currentStage.setMaximized(true);
            } else currentStage.setMaximized(false);
        }
    }

    @FXML
    public void clickOnSelectButtons(MouseEvent event) {
        ImageView currentButton = (ImageView) event.getSource();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if ( currentButton == originalButton ) {
            selectedOriginalDirectory = selectedDirectory;
            if ( selectedOriginalDirectory != null ) {
                originalContainer.getStyleClass().add("containers");
                initializeListView();
            } else {
                originalContainer.getStyleClass().removeIf( s -> s.equals("containers"));
            }
        } else if ( currentButton == copiedButton ) {
            selectedCopiedDirectory = selectedDirectory;
            if ( selectedCopiedDirectory != null ) {
                copiedContainer.getStyleClass().add("containers");
                initializeListView();
            } else {
                copiedContainer.getStyleClass().removeIf( s -> s.equals("containers"));
            }
        }
    }

    @FXML
    public void clickOnValidateButton() {
        if ( formattedDirectoryDetailList.size() != 0 ) {
            windowsFileSystem.validateFormattedDirectories(formattedDirectoryDetailList);
        }
    }

    private void initializeListView() {
        if ( selectedOriginalDirectory != null && selectedCopiedDirectory != null ) {
            formattedDirectoryDetailList = windowsFileSystem.getFormattedDirectoriesDetailLists(
                                                selectedOriginalDirectory,selectedCopiedDirectory);
            listView.setItems(formattedDirectoryDetailList);
        } else {
            formattedDirectoryDetailList.clear();
        }
    }



}