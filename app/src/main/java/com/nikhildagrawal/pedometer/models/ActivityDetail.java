package com.nikhildagrawal.pedometer.models;

public class ActivityDetail {

    private int noOfSteps;
    private String date;


    public ActivityDetail(int noOfSteps, String date) {
        this.noOfSteps = noOfSteps;
        this.date = date;
    }

    public int getNoOfSteps() {
        return noOfSteps;
    }

    public void setNoOfSteps(int noOfSteps) {
        this.noOfSteps = noOfSteps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
