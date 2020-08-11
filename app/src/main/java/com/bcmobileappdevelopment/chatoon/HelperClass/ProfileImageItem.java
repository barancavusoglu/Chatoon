package com.bcmobileappdevelopment.chatoon.HelperClass;

public class ProfileImageItem {

    public ProfileImageItem(String imageName){
        this.setImageName(imageName);
    }
    private String filePath ="";
    private String fileName="";
    private String fileURL="";
    private Boolean isFilled= false;
    private String imageName = "";

    public String getImageName() {
        return imageName;
    }

    private void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getFilled() {
        return isFilled;
    }

    public void setFilled(Boolean filled) {
        isFilled = filled;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }
}
