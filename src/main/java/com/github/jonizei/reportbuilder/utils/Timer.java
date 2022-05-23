package com.github.jonizei.reportbuilder.utils;

import java.util.HashMap;

public class Timer {

    private HashMap<String, Long> startTimes;

    public Timer() {
        this.startTimes = new HashMap<String, Long>();
    }

    public void start(String id) {
        this.startTimes.put(id, System.nanoTime());
    }

    public ElapsedTime stop(String id) {
        if(this.startTimes.containsKey(id)) {
            long startTime = this.startTimes.get(id);
            long finishTime = System.nanoTime();
            long elapsedTime = finishTime - startTime;
            double milliSeconds = elapsedTime * 0.0000001;
            double timeInSeconds = elapsedTime * 0.000000001;
            int minutes = (int) Math.floor(timeInSeconds / 60);
            int seconds = (int) timeInSeconds - (minutes * 60);
            return new ElapsedTime(minutes, seconds, milliSeconds);
        }

        return new ElapsedTime(-1, -1,-1);
    }
}
