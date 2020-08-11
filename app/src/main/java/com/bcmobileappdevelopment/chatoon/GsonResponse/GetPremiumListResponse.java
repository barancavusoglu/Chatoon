package com.bcmobileappdevelopment.chatoon.GsonResponse;

import java.util.List;

public class GetPremiumListResponse {

    private boolean isSuccess;
    private String message;
    private List<PremiumListBean> premiumList;

    public boolean isIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PremiumListBean> getPremiumList() {
        return premiumList;
    }

    public void setPremiumList(List<PremiumListBean> premiumList) {
        this.premiumList = premiumList;
    }

    public static class PremiumListBean {

        private int ID;
        private int UserID;
        private String Username;
        private String ProfilePicURL;
        private String FlagCode;
        private String Gender;
        private int Age;
        private String Date;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public int getUserID() {
            return UserID;
        }

        public void setUserID(int UserID) {
            this.UserID = UserID;
        }

        public String getUsername() {
            return Username;
        }

        public void setUsername(String Username) {
            this.Username = Username;
        }

        public String getProfilePicURL() {
            return ProfilePicURL;
        }

        public void setProfilePicURL(String ProfilePicURL) {
            this.ProfilePicURL = ProfilePicURL;
        }

        public String getFlagCode() {
            return FlagCode;
        }

        public void setFlagCode(String FlagCode) {
            this.FlagCode = FlagCode;
        }

        public String getGender() {
            return Gender;
        }

        public void setGender(String Gender) {
            this.Gender = Gender;
        }

        public int getAge() {
            return Age;
        }

        public void setAge(int Age) {
            this.Age = Age;
        }

        public String getDate() {
            return Date;
        }

        public void setDate(String Date) {
            this.Date = Date;
        }
    }
}
