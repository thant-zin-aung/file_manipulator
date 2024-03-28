package application;

import application.model.FileSystem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    public static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("view/mainView.fxml")));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Blacksky File Manipulator");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.getIcons().add(new Image(getClass().getResource("assets/photos/app_icon.png").toString()));
        mainStage = primaryStage;
        mainStage.setOnCloseRequest((e) -> {
            e.consume();
            System.exit(0);
        });
        FileSystem.checkADB();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
