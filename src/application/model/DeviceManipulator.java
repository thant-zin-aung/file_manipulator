package application.model;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class DeviceManipulator {
    private String deviceName;
    private String deviceStatus;
    private String deviceProductName;
    private boolean isConnected;
    private Thread deviceStatusCheckerThread;
    private boolean androidDeviceStatusCheckerFlag;
    private Label deviceNameLabel,deviceStatusLabel,deviceProductLabel;
    private HBox connectedDeviceBox;

    public DeviceManipulator(Label deviceNameLabel,Label deviceStatusLabel,Label deviceProductLabel,HBox connectedDeviceBox) {
        this.deviceNameLabel = deviceNameLabel;
        this.deviceStatusLabel = deviceStatusLabel;
        this.deviceProductLabel = deviceProductLabel;
        this.connectedDeviceBox = connectedDeviceBox;
        androidDeviceStatusCheckerFlag = true;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceProductName() { return deviceProductName; }

    public void setDeviceProductName(String deviceProductName ) { this.deviceProductName = deviceProductName; }

    public Thread getDeviceStatusCheckerThread() {
        return deviceStatusCheckerThread;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void start_Android_Device_Info_Checker_Thread(int checkingSpeed) {
        androidDeviceStatusCheckerFlag = true;
        deviceStatusCheckerThread = new Thread(() -> {
            while (androidDeviceStatusCheckerFlag) {
                if ( isAndroidDeviceConnected(SystemCommandManipulator.executeSystemCommand("adb devices -l")) ) {
                    this.setConnected(true);
//                    System.out.println("Connected");
                } else {
                    this.setConnected(false);
//                    System.out.println("Disconnected");
                }
                SystemCommandManipulator.delayTime(checkingSpeed);
            }

        });
        deviceStatusCheckerThread.start();
    }

    public void stop_Android_Device_Status_Checker_Thread() {
        androidDeviceStatusCheckerFlag = false;
    }

    private void setNameLabelProduct(String deviceName,String status,String product) {
        deviceNameLabel.setText(this.getDeviceName());
        deviceStatusLabel.setText(this.getDeviceStatus());
        deviceProductLabel.setText(this.getDeviceProductName());
    }

    private boolean isAndroidDeviceConnected(String deviceInfo) {
        if ( deviceInfo.contains("unauthorized" ) ) {
            System.out.println("Unauthorized yes");
            this.deviceName = "Unknown";
            this.deviceStatus = "Unauthorized";
            this.deviceProductName = "Unknown";
            Platform.runLater(() -> {
                connectedDeviceBox.setStyle("-fx-background-color: #F29D00");
                setNameLabelProduct(this.getDeviceName(),this.getDeviceStatus(),getDeviceProductName());
            });
        }
        else if ( !deviceInfo.contains("model") ) {
            this.deviceName = "Unknown";
            this.deviceStatus = "Disconnected";
            this.deviceProductName = "Unknown";
            Platform.runLater(() -> {
                connectedDeviceBox.setStyle("-fx-background-color: red");
                setNameLabelProduct(this.getDeviceName(),this.getDeviceStatus(),getDeviceProductName());
            });
        }
        else {
            this.deviceName = androidDeviceInfoExtractor(deviceInfo,"model:");
            this.deviceStatus="Connected";
            this.deviceProductName = androidDeviceInfoExtractor(deviceInfo,"product:");
            Platform.runLater(() -> {
                connectedDeviceBox.setStyle("-fx-background-color: #00D141");
                setNameLabelProduct(this.getDeviceName(),this.getDeviceStatus(),getDeviceProductName());
            });
        }
//        androidDeviceInfoExtractor(deviceInfo,"model:");
        return deviceInfo.contains("model");
    }

    private String androidDeviceInfoExtractor(String deviceInfo,String searchStr) {
        String deviceName = "";
        String[] infos = deviceInfo.split(" ");
        for ( String info:infos ) {
            if ( info.contains(searchStr)) {
                deviceName = info.substring(searchStr.length());
            }
        }
        return deviceName;
    }
}
