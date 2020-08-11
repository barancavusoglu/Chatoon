package com.bcmobileappdevelopment.chatoon.HelperClass;

import java.util.List;

public class Countries {

    private List<CountriesBean> countries;

    public List<CountriesBean> getCountries() {
        return countries;
    }

    public void setCountries(List<CountriesBean> countries) {
        this.countries = countries;
    }

    public static class CountriesBean {
        /**
         * ID : 1
         * Name : Afghanistan
         * CountryCode : AFG
         */

        private int ID;
        private String Name;
        private String CountryCode;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public String getName() {
            return Name;
        }

        public void setName(String Name) {
            this.Name = Name;
        }

        public String getCountryCode() {
            return CountryCode;
        }

        public void setCountryCode(String CountryCode) {
            this.CountryCode = CountryCode;
        }
    }
}
