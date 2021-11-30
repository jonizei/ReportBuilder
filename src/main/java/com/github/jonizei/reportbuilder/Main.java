/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder;

import com.github.jonizei.reportbuilder.builder.ReportBuilder;
import com.github.jonizei.reportbuilder.builder.SettingsReader;
import com.github.jonizei.reportbuilder.utils.Utilities;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 *
 * @author Joni
 */
public class Main {
    
    public static void main(String[] args) {
        
        Scanner scan = new Scanner(System.in);
        SettingsReader reader = new SettingsReader();
        
        long startTime = System.nanoTime();
        
        if(reader.readXml("./settings.xml")) {
            if(!reader.getSourcePath().equals("") && !reader.getDestinationPath().equals("") && !reader.getReportName().equals("")) {
                if(!isPathValid(reader.getSourcePath())) {
                    System.out.printf("Lähdekansio ei ole olemassa: %s\n", reader.getSourcePath());
                }
                else if(!isPathValid(reader.getDestinationPath())) {
                    System.out.printf("Päätekansio ei ole olemassa: %s\n", reader.getDestinationPath());
                }
                else {
                    try {
                        System.out.println("Luodaan raportti kansiosta: " + reader.getSourcePath());
                        Utilities.setPaperSizeLibrary(reader.getPaperSizeLibrary());
                        Utilities.setColorThreshold(reader.getColorThreshold());
                        ReportBuilder builder = new ReportBuilder(
                                reader.getPaperSizeLibrary()
                                , reader.ignoreColor()
                                , reader.getHeightThreshold()
                                , reader.getWidthThreshold()
                        );
                        builder.load(reader.getSourcePath());
                        builder.build(reader.getDestinationPath(), reader.getReportName());
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else System.out.println("Tarvittavia tietoja puuttuu. Päivitä settings.xml tiedosto.");
        }
        else System.out.println("Tiedostoa ei löydy: settings.xml");
        
        long finishTime = System.nanoTime();
        long elapsedTime = finishTime - startTime;
        double timeInSeconds = elapsedTime * 0.000000001;
        int minutes = (int) Math.floor(timeInSeconds / 60);
        int seconds = (int) timeInSeconds - (minutes * 60);
        
        DecimalFormat df = new DecimalFormat("#.##");
        
        System.out.printf("Aikaa kului: %d minuuttia %d sekuntia\n", minutes, seconds);
        
        System.out.println("Paina mitä tahansa näppäintä jatkaaksesi...");
        scan.nextLine();
    }
    
    private static boolean isPathValid(String path) {
        
        File directory = new File(path);
        if(directory.exists()) {
            return true;
        }
        
        return false;
    }
    
}
