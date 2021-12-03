/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

/**
 * This class contains all necessary information for single paper category
 * 
 * @author Joni
 * @version 2021-12-03
 */
public class PaperCategory {
    
    /**
     * Name of the paper category
     */
    private String name;
    
    /**
     * Paper size in millimetres
     */
    private int sizeMm;
    
    /**
     * Paper size in pixels
     */
    private int sizePx;
    
    /**
     * Boolean which tells if paper size is unknown or not
     */
    private boolean isUnknown = false;
    
    /**
     * Constructor of PaperCategory
     */
    public PaperCategory() {
        
    }
    
    /**
     * Constructor of PaperCategory
     * 
     * @param name Name of the paper category
     * @param sizeMm Paper size in millimetres
     * @param sizePx Paper size in pixels
     * @param isUnknown Is paper size unknown or not
     */
    public PaperCategory(String name, int sizeMm, int sizePx, boolean isUnknown) {
        setName(name);
        setPaperSizeMm(sizeMm);
        setPaperSizePx(sizePx);
        setUnknown(isUnknown);
    }
    
    /**
     * Set name of the paper category
     * 
     * @param name Name of the paper category
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get name of the paper category
     * 
     * @return Name of the paper category
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Set paper size in millimetres
     * 
     * @param height Paper size in millimetres
     */
    public void setPaperSizeMm(int height) {
        this.sizeMm = height;
    }
    
    /**
     * Set paper size in pixels
     * 
     * @param height Paper size in pixels
     */
    public void setPaperSizePx(int height) {
        this.sizePx = height;
    }
    
    /**
     * Get paper size in millimetres
     * 
     * @return Paper size in millimetres
     */
    public int getPaperSizeMm() {
        return this.sizeMm;
    }
    
    /**
     * Get paper size in pixels
     * 
     * @return Paper size in pixels
     */
    public int getPaperSizePx() {
        return this.sizePx;
    }
    
    /**
     * Set paper size unknown boolean value
     * 
     * @param isUnknown Paper size state
     */
    public void setUnknown(boolean isUnknown) {
        this.isUnknown = isUnknown;
    }
    
    /**
     * Gets state of the paper category
     * 
     * @return Paper category state
     */
    public boolean isUnknown() {
        return this.isUnknown;
    }
    
    /**
     * Compare two paper categories together
     * Comparison is done with using the paper size
     * 
     * @param category Comparable paper category
     * @return Boolean which tells if paper categories are the same
     */
    public boolean equals(PaperCategory category) {
        return category.getPaperSizeMm() == getPaperSizeMm();
    }
    
}
