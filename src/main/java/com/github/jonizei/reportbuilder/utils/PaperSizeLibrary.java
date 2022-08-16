/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains all the paper sizes used in this program
 * 
 * @author Joni
 * @version 2021-12-03
 */
public class PaperSizeLibrary {
    
    /**
     * Instance of PaperSizeLibrary
     */
    private static PaperSizeLibrary instance;
    
    /**
     * List of all paper height categories
     */
    private List<PaperCategory> heightList;
    
    /**
     * List of all paper width categories
     */
    private List<PaperCategory> widthList;
    
    /**
     * List of all paper names
     */
    private List<NamedPaper> namedPapers;

    private List<PaperSize> paperSizes;

    /**
     * Class that contains necessary information for a single named paper size
     */
    public class NamedPaper {
        
        /**
         * Named of the paper size
         */
        private String name;
        
        /**
         * Paper height in millimetres
         */
        private int heightMm;
        
        /**
         * Paper width in millimetres 
         */
        private int widthMm;
        
        /**
         * Constructor of NamedPaper
         */
        public NamedPaper() {
            
        }
        
        /**
         * Constructor of NamedPaper
         * 
         * @param name Name of the paper size
         * @param heightMm Paper height in millimetres
         * @param widthMm Paper width in millimetres
         */
        public NamedPaper(String name, int heightMm, int widthMm) {
            this.name = name;
            this.heightMm = heightMm;
            this.widthMm = widthMm;
        }
        
        /**
         * Set name of the paper size
         * 
         * @param name Name of the paper size
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Set paper height in millimetres
         * 
         * @param heightMm Paper height in millimetres
         */
        public void setHeightMm(int heightMm) {
            this.heightMm = heightMm;
        }
        
        /**
         * Set paper width in millimetres
         * 
         * @param widthMm Paper width in millimetres
         */
        public void setWidthMm(int widthMm) {
            this.widthMm = widthMm;
        }
        
        /**
         * Get name of the paper size
         * 
         * @return Name of the paper size
         */
        public String getName() {
            return this.name;
        }
        
        /**
         * Get height of the paper in millimetres
         * 
         * @return Height of the paper in millimetres
         */
        public int getHeightMm() {
            return this.heightMm;
        }
        
        /**
         * Get width of the paper in millimetres
         * 
         * @return Width of the paper in millimetres
         */
        public int getWidthMm() {
            return this.widthMm;
        }
        
    }

    static class AreaComparator implements Comparator<PaperSize> {
        public int compare(PaperSize a, PaperSize b) {
            return a.compareTo(b);
        }
    }

    static class HeightComparator implements  Comparator<PaperSize> {
        public int compare(PaperSize a, PaperSize b) { return a.getHeightCategory().getPaperSizePx() - b.getHeightCategory().getPaperSizePx(); }
    }
    
    /**
     * Private constructor of PaperSizeLibrary
     */
    private PaperSizeLibrary() {
        heightList = new ArrayList<>();
        widthList = new ArrayList<>();
        namedPapers = new ArrayList<>();
        paperSizes = new ArrayList<>();
    }
    
    /**
     * Static method that returns PaperSizeLibrary
     * instance if it exists. If not then it creates
     * a new instance
     * 
     * @return Instance of PaperSizeLibrary
     */
    public static PaperSizeLibrary getInstance() {
        
        if(instance == null) {
            return new PaperSizeLibrary();
        }
        
        return instance;
    }
    
    /**
     * Adds new paper height category to the list
     * 
     * @param height PaperCategory object
     */
    public void addPaperHeightCategory(PaperCategory height) {
        heightList.add(height);
    }
    
    /**
     * Adds new paper width category to the list
     * 
     * @param height PaperCategory object
     */
    public void addPaperWidthCategory(PaperCategory height) {
        widthList.add(height);
    }
    
    /**
     * Adds array of paper height categories to the list
     * 
     * @param heightArray Array of paper height categories
     */
    public void addPaperHeightArray(PaperCategory[] heightArray) {
        heightList.addAll(Arrays.asList(heightArray));
    }
    
    /**
     * Adds array of paper width categories to the list
     * 
     * @param widthArray Array of paper width categories
     */
    public void addPaperWidthArray(PaperCategory[] widthArray) {
        widthList.addAll(Arrays.asList(widthArray));
    }
    
    /**
     * Adds new named paper size to named paper list
     * 
     * @param name Name of the paper size
     * @param heightMm Paper height in millimetres
     * @param widthMm Paper width in millimetres
     */
    public void addNamedPaper(String name, int heightMm, int widthMm) {
        namedPapers.add(new NamedPaper(name, heightMm, widthMm));
    }
    
    /**
     * Adds array of named paper sizes to named paper list
     * 
     * @param namedPapersArray Array of named paper sizes
     */
    public void addNamedPaperArray(NamedPaper[] namedPapersArray) {
        namedPapers.addAll(Arrays.asList(namedPapersArray));
    }
    
    /**
     * Returns list of paper height categories
     * 
     * @return List of paper height categories
     */
    public List<PaperCategory> getPaperHeightCategories() {
        return this.heightList;
    }
    
    /**
     * Returns list of paper width categories
     * 
     * @return List of paper width categories
     */
    public List<PaperCategory> getPaperWidthCategories() {
        return this.widthList;
    }
    
    /**
     * Returns list of named paper sizes
     * 
     * @return List of named paper sizes
     */
    public List<NamedPaper> getNamedPapers() {
        return this.namedPapers;
    }
    
    /**
     * Find named paper size with given values
     * 
     * @param heightMm Paper height in millimetres
     * @param widthMm Paper width in millimetres
     * @param heightOffset Offset to categorize various paper heights
     * @param widthOffset Offset to categorize various paper widths
     * @return Name of paper size
     */
    public String findPaperName(int heightMm, int widthMm, int heightOffset, int widthOffset) {
        Optional<String> name = namedPapers.stream()
                .filter(v -> heightMm < v.getHeightMm() + heightOffset && heightMm > v.getHeightMm() - heightOffset)
                .filter(v -> widthMm < v.getWidthMm() + widthOffset && widthMm > v.getWidthMm() - widthOffset)
                .map(v -> v.getName())
                .findFirst();
        return name.isPresent() ? name.get() : "";
    }
    
    /**
     * Returns smallest paper width category from the list
     * 
     * @return Smallest paper width category
     */
    public PaperCategory getMinWidth() {
        return this.widthList.stream()
                .min((v1, v2) -> Integer.compare(v1.getPaperSizeMm(), v2.getPaperSizeMm()))
                .get();
    }
    
    /**
     * Converts pixels to millimetres
     * 
     * @param pixel Pixel to be converted
     * @return Pixels in millimetres
     */
    public static int pixelToMm(int pixel) {
        double temp = pixel * 25.4 / 72;
        return (int)temp;
    }
    
    /**
     * Converts millimetres to pixels
     * 
     * @param mm Millimetres to be converted
     * @return Millimetres in pixels
     */
    public static int mmToPixel(int mm) {
        double temp = mm * 72 / 25.4;
        return (int)temp;
    }

    public List<PaperSize> getPaperSizes() {
        Collections.sort(paperSizes, new HeightComparator());
        return paperSizes;
    }

    public List<PaperSize> getPaperSizesSortArea() {
        Collections.sort(paperSizes, new AreaComparator());
        return paperSizes;
    }

    public void setPaperSizes(List<PaperSize> paperSizes) {
        this.paperSizes = paperSizes;
    }
    
}
