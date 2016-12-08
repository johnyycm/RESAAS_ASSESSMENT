package com.example.chenminyao.resaas_assessment;

/**
 * Created by chenminyao on 2016-12-07.
 */

public class Listing {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    String name;
    String address;

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    String userRating;
}
