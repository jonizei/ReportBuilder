/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Joni
 */
public class PaperSizeLibrary {
    
    private static PaperSizeLibrary instance;
    private List<PaperCategory> heightList;
    private List<PaperCategory> widthList;
    private List<NamedPaper> namedPapers;
    
    public class NamedPaper {
        
        private String name;
        private int heightMm;
        private int widthMm;
        
        public NamedPaper() {
            
        }
        
        public NamedPaper(String name, int heightMm, int widthMm) {
            this.name = name;
            this.heightMm = heightMm;
            this.widthMm = widthMm;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setHeightMm(int heightMm) {
            this.heightMm = heightMm;
        }
        
        public void setWidthMm(int widthMm) {
            this.widthMm = widthMm;
        }
        
        public String getName() {
            return this.name;
        }
        
        public int getHeightMm() {
            return this.heightMm;
        }
        
        public int getWidthMm() {
            return this.widthMm;
        }
        
    }
    
    private PaperSizeLibrary() {
        heightList = new ArrayList<>();
        widthList = new ArrayList<>();
        namedPapers = new ArrayList<>();
    }
    
    public static PaperSizeLibrary getInstance() {
        
        if(instance == null) {
            return new PaperSizeLibrary();
        }
        
        return instance;
    }
    
    public void addPaperHeightCategory(PaperCategory height) {
        heightList.add(height);
    }
    
    public void addPaperWidthCategory(PaperCategory height) {
        widthList.add(height);
    }
    
    public void addPaperHeightArray(PaperCategory[] heightArray) {
        heightList.addAll(Arrays.asList(heightArray));
    }
    
    public void addPaperWidthArray(PaperCategory[] widthArray) {
        widthList.addAll(Arrays.asList(widthArray));
    }
    
    public void addNamedPaper(String name, int heightMm, int widthMm) {
        namedPapers.add(new NamedPaper(name, heightMm, widthMm));
    }
    
    public void addNamedPaperArray(NamedPaper[] namedPapersArray) {
        namedPapers.addAll(Arrays.asList(namedPapersArray));
    }
    
    public List<PaperCategory> getPaperHeightCategories() {
        return this.heightList;
    }
    
    public List<PaperCategory> getPaperWidthCategories() {
        return this.widthList;
    }
    
    public List<NamedPaper> getNamedPapers() {
        return this.namedPapers;
    }
    
    public String findPaperName(int heightMm, int widthMm, int heightOffset, int widthOffset) {
        Optional<String> name = namedPapers.stream()
                .filter(v -> heightMm < v.getHeightMm() + heightOffset && heightMm > v.getHeightMm() - heightOffset)
                .filter(v -> widthMm < v.getWidthMm() + widthOffset && widthMm > v.getWidthMm() - widthOffset)
                .map(v -> v.getName())
                .findFirst();
        return name.isPresent() ? name.get() : "";
    }
    
    public PaperCategory getMinWidth() {
        return this.widthList.stream()
                .min((v1, v2) -> Integer.compare(v1.getPaperSizeMm(), v2.getPaperSizeMm()))
                .get();
    }
    
    public static int pixelToMm(int pixel) {
        double temp = pixel * 25.4 / 72;
        return (int)temp;
    }
    
    public static int mmToPixel(int mm) {
        double temp = mm * 72 / 25.4;
        return (int)temp;
    }
    
}
