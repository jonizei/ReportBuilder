/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

/**
 *
 * @author Joni
 */
public class PaperCategory {
    
    private String name;
    private int sizeMm;
    private int sizePx;
    private boolean isUnknown = false;
    
    public PaperCategory() {
        
    }
    
    public PaperCategory(String name, int sizeMm, int sizePx, boolean isUnknown) {
        setName(name);
        setPaperSizeMm(sizeMm);
        setPaperSizePx(sizePx);
        setUnknown(isUnknown);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setPaperSizeMm(int height) {
        this.sizeMm = height;
    }
    
    public void setPaperSizePx(int height) {
        this.sizePx = height;
    }
    
    public int getPaperSizeMm() {
        return this.sizeMm;
    }
    
    public int getPaperSizePx() {
        return this.sizePx;
    }
    
    public void setUnknown(boolean isUnknown) {
        this.isUnknown = isUnknown;
    }
    
    public boolean isUnknown() {
        return this.isUnknown;
    }
    
    public boolean equals(PaperCategory category) {
        return category.getPaperSizeMm() == getPaperSizeMm();
    }
    
}
