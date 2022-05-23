package com.github.jonizei.reportbuilder.utils;

public class ElapsedTime {
    private int minutes;
    private int seconds;

    private double milliSeconds;

    public ElapsedTime(int minutes, int seconds, double milliSeconds) {
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliSeconds = milliSeconds;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public double getMilliSeconds() {
        return this.milliSeconds;
    }
}
