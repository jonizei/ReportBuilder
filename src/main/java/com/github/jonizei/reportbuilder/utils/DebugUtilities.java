/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This class is used for debugging
 * 
 * @author Joni
 */
public class DebugUtilities {

    private static final int MAX_LEVEL = 5;
    private static final int MIN_LEVEL = 0;

    /**
     * List of debug results
     */
    private List<Pair<Integer, String>> logs;

    /**
     * Boolean which tells if debugging is enabled
     */
    private boolean isDebug;

    private int debugLevel;
    
    /**
     * Constructor for DebugUtilities
     * 
     * @param level
     */
    public DebugUtilities(int level) {
        this.logs = new ArrayList<>();
        setDebugLevel(level);
    }

    public List<Pair<Integer, String>> getLogs() {
        return this.logs;
    }

    public void setDebugLevel(int level) {
        level = level < MIN_LEVEL ? MIN_LEVEL : level;
        level = level > MAX_LEVEL ? MAX_LEVEL : level;

        this.debugLevel = level;
    }

    public int getDebugLevel() {
        return this.debugLevel;
    }
    
    /**
     * Adds new log text to a list
     * 
     * @param text 
     */
    public void addLog(int level, String text, Object... args) {
        level = level <= MIN_LEVEL ? MIN_LEVEL + 1 : level;
        logs.add(new Pair<>(level, String.format(text, args)));
    }

    public void addTimeLog(int level, String text, ElapsedTime elapsedTime) {
        this.addLog(level, text + " (Time): %dm %ds %,.0fms"
                , elapsedTime.getMinutes(), elapsedTime.getSeconds(), elapsedTime.getMilliSeconds());
    }

    private boolean isDebug() {
        return this.debugLevel > 0;
    }
    
    public void writeLogs(String filename) {
        if(isDebug()) {
            try (BufferedWriter logWriter = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(new File(filename + ".txt"), false), StandardCharsets.UTF_8))) {
                for(Pair<Integer, String> log : logs) {
                    if((int)log.key <= this.debugLevel) {
                        logWriter.write(String.format("%s\n", log.value));
                    }
                }

                logWriter.flush();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
