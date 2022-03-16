/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for debugging
 * 
 * @author Joni
 */
public class DebugUtilities {
    
    /**
     * List of debug results
     */
    public List<String> logs;
    
    /**
     * Boolean which tells if debugging is enabled
     */
    public boolean isDebug;
    
    /**
     * Constructor for DebugUtilities
     * 
     * @param isDebug 
     */
    public DebugUtilities(boolean isDebug) {
        this.logs = new ArrayList<>();
        this.isDebug = isDebug;
    }
    
    /**
     * Adds new log text to a list
     * 
     * @param text 
     */
    public void addLog(String text) {
        logs.add(text);
    }
    
    public void writeLogs(String filename) {
        if(isDebug) {
            try (BufferedWriter logWriter = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(new File(filename + ".txt"), false), StandardCharsets.UTF_8))) {
                for(String log : logs) {
                    logWriter.write(String.format("%s\n", log));
                }

                logWriter.flush();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Calculates colored pixels in a pdf page
     * Saves result to a log list
     * 
     * @param filename Name of the pdf file
     * @param pageNum Page number of the pdf file
     * @param image Pdf page as an image
     */
    public void calculateColorPixels(String filename, int pageNum, BufferedImage image) {
        if(isDebug) {
            int pixelCounter = 0;
            for(int i = 0; i < image.getHeight(); i++) {
                int[] pixelArray = image.getRGB(0, i, image.getWidth(), 1, null, 0, image.getWidth());
                for(int j = 0; j < pixelArray.length; j++) {
                    pixelCounter += Utilities.isGrayscale(new Color(pixelArray[j])) ? 0 : 1;
                }
            }
            addLog(String.format("%s : %d : %d", filename, pageNum, pixelCounter));
        }
    }
    
}
