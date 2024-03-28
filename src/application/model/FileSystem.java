package application.model;

import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

public interface FileSystem {
    String greenDirectoryIconPath = "assets/photos/greenDirectory.png";
    String fileIconPath = "assets/photos/fileIcon.png";
    String validIconPath = "assets/photos/valid.png";
    String invalidIconPath = "assets/photos/invalid.png";
    public void setSelectedItem(String selectedItem);
    public String getSelectedItem();
    public ObservableList<HBox> getListFromPath(String path);
    public static void checkADB() {
        try {
            Runtime.getRuntime().exec("adb --version");
        } catch ( Exception e ) {
            SystemCommandManipulator.executeSystemCommand("cmd /c msg * [Warning] Need to install adb first!");
        }
    }
    public static String getLastFileNameFromPath(String filePath,boolean isAndroid) {
        char delimiter = isAndroid ? '/' : '\\';
        int lastIndex = 0;
        for ( int rId = filePath.length() -1 ; rId >= 0 ; rId-- ) {
            if ( filePath.charAt(rId) == delimiter ) {
                lastIndex = ++rId;
                break;
            }
        }
        StringBuilder tempFilePath = new StringBuilder();
        for ( int id = lastIndex ; id < filePath.length() ; id++ ) {
            tempFilePath.append(filePath.charAt(id));
        }
        return tempFilePath.toString();
    }
}
