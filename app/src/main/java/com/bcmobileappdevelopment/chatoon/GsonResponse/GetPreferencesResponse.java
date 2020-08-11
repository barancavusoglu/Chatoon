package com.bcmobileappdevelopment.chatoon.GsonResponse;

import java.util.List;

public class GetPreferencesResponse {

    private boolean isSuccess;
    private String message;
    private List<PreferencesBean> preferences;

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

    public List<PreferencesBean> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<PreferencesBean> preferences) {
        this.preferences = preferences;
    }

    public static class PreferencesBean {
        /**
         * ID : 1
         * Preference : Version
         * Value : 1
         * şuan tam konsantre değilim sende esneyip duruyon kapatıyom ben :DD piç yok yok bende meyve falan yiyecem yine bakak
         * öptüm say gödmüm by
         * np knk
         * mvc
         * mvc
         * mvc
         * mvc
         * kotlin kotlin
         * bbc bbc bbc
         * Julio Gomes vs Piper Perri
         * Dredd vs Piper Perri
         * Angelina Diamanti
         * hmm...
         * dur dur
         * dur dur
         * Alena Lam Lam -> süper -> bu amatör star
         * bende bende
         * aynen
         * :DD
         * :DDDD
         * neyse isimler var göstercem
         * daşşak kiremit
         * gg wp :D çıktım
         * afiyet olsn
         */

        private int ID;
        private String Preference;
        private String Value;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public String getPreference() {
            return Preference;
        }

        public void setPreference(String Preference) {
            this.Preference = Preference;
        }

        public String getValue() {
            return Value;
        }

        public void setValue(String Value) {
            this.Value = Value;
        }
    }
}
