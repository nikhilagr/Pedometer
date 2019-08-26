package com.nikhildagrawal.pedometer.models;

public class ActivityDetail {

    private int noOfSteps;
    private String fromDateTime;
    private String toDateTime;

    public ActivityDetail(int noOfSteps, String fromDateTime, String toDateTime) {
        this.noOfSteps = noOfSteps;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
    }

    public int getNoOfSteps() {
        return noOfSteps;
    }

    public void setNoOfSteps(int noOfSteps) {
        this.noOfSteps = noOfSteps;
    }

    public String getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(String fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public String getToDateTime() {
        return toDateTime;
    }

    public void setToDateTime(String toDateTime) {
        this.toDateTime = toDateTime;
    }
}
