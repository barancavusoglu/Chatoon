package com.bcmobileappdevelopment.chatoon.GsonResponse;

public class LoginResponse {


    /**
     * user : {"ID":11,"AccountKey":"ABC","Email":"ABC","ProfilePicURL":"https://firebasestorage.googleapis.com/v0/b/votidea-ef6e2.appspot.com/o/images%2Fturk%2Fchatoon_logo3-8.png?alt=media&token=9f449611-aa81-4ae5-8b62-f13de505f1ac","Gender":" ","Age":25,"IsActive":true,"IsEmailApproved":false,"IsBanned":false,"Country":"Turkey","IsFacebookApproved":false,"LookingGender":"","LookingAgeRange":"25-25","FlagCode":"turk","AccountType":"Anonymous","LookingFlagCode":"turk","Username":"Anonymous#1005","FacebookID":"ABC"}
     * isSuccess : true
     * message : anonymous_login
     * token : 6eP4nloOUVFMHtdqRB2hGPKMWumyiFMY6vsqZ8CLhYE=
     */

    private UserBean user;
    private boolean isSuccess;
    private String message;
    private String token;

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class UserBean {
        /**
         * ID : 11
         * AccountKey : ABC
         * Email : ABC
         * ProfilePicURL : https://firebasestorage.googleapis.com/v0/b/votidea-ef6e2.appspot.com/o/images%2Fturk%2Fchatoon_logo3-8.png?alt=media&token=9f449611-aa81-4ae5-8b62-f13de505f1ac
         * Gender :
         * Age : 25
         * IsActive : true
         * IsEmailApproved : false
         * IsBanned : false
         * Country : Turkey
         * IsFacebookApproved : false
         * LookingGender :
         * LookingAgeRange : 25-25
         * FlagCode : turk
         * AccountType : Anonymous
         * LookingFlagCode : turk
         * Username : Anonymous#1005
         * FacebookID : ABC
         */

        private int ID;
        private String AccountKey;
        private String Email;
        private String ProfilePicURL;
        private String Gender;
        private int Age;
        private boolean IsActive;
        private boolean IsEmailApproved;
        private boolean IsBanned;
        private String Country;
        private boolean IsFacebookApproved;
        private String LookingGender;
        private String LookingAgeRange;
        private String FlagCode;
        private String AccountType;
        private String LookingFlagCode;
        private String Username;
        private String FacebookID;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public String getAccountKey() {
            return AccountKey;
        }

        public void setAccountKey(String AccountKey) {
            this.AccountKey = AccountKey;
        }

        public String getEmail() {
            return Email;
        }

        public void setEmail(String Email) {
            this.Email = Email;
        }

        public String getProfilePicURL() {
            return ProfilePicURL;
        }

        public void setProfilePicURL(String ProfilePicURL) {
            this.ProfilePicURL = ProfilePicURL;
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

        public boolean isIsActive() {
            return IsActive;
        }

        public void setIsActive(boolean IsActive) {
            this.IsActive = IsActive;
        }

        public boolean isIsEmailApproved() {
            return IsEmailApproved;
        }

        public void setIsEmailApproved(boolean IsEmailApproved) {
            this.IsEmailApproved = IsEmailApproved;
        }

        public boolean isIsBanned() {
            return IsBanned;
        }

        public void setIsBanned(boolean IsBanned) {
            this.IsBanned = IsBanned;
        }

        public String getCountry() {
            return Country;
        }

        public void setCountry(String Country) {
            this.Country = Country;
        }

        public boolean isIsFacebookApproved() {
            return IsFacebookApproved;
        }

        public void setIsFacebookApproved(boolean IsFacebookApproved) {
            this.IsFacebookApproved = IsFacebookApproved;
        }

        public String getLookingGender() {
            return LookingGender;
        }

        public void setLookingGender(String LookingGender) {
            this.LookingGender = LookingGender;
        }

        public String getLookingAgeRange() {
            return LookingAgeRange;
        }

        public void setLookingAgeRange(String LookingAgeRange) {
            this.LookingAgeRange = LookingAgeRange;
        }

        public String getFlagCode() {
            return FlagCode;
        }

        public void setFlagCode(String FlagCode) {
            this.FlagCode = FlagCode;
        }

        public String getAccountType() {
            return AccountType;
        }

        public void setAccountType(String AccountType) {
            this.AccountType = AccountType;
        }

        public String getLookingFlagCode() {
            return LookingFlagCode;
        }

        public void setLookingFlagCode(String LookingFlagCode) {
            this.LookingFlagCode = LookingFlagCode;
        }

        public String getUsername() {
            return Username;
        }

        public void setUsername(String Username) {
            this.Username = Username;
        }

        public String getFacebookID() {
            return FacebookID;
        }

        public void setFacebookID(String FacebookID) {
            this.FacebookID = FacebookID;
        }
    }
}
