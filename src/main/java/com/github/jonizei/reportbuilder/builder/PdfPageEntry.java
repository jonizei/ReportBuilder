/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.builder;

import com.github.jonizei.reportbuilder.builder.ReportBuilder.PageColor;
import com.github.jonizei.reportbuilder.utils.PaperCategory;
import com.github.jonizei.reportbuilder.utils.Utilities;
import java.util.Arrays;

/**
 * This class contains all necessary information of a pdf file
 * 
 * @author Joni
 * @version 2021-12-03
 */
public class PdfPageEntry implements Comparable<PdfPageEntry> {
    
    private PdfFileEntry parent;
    private int pageNumber;
    private PaperCategory height;
    private PaperCategory width;
    private PaperCategory exactHeight;
    private PaperCategory exactWidth;
    private PaperCategory originalHeight;
    private PaperCategory originalWidth;
    private PageColor pageColor;
    private String paperName;
    private boolean hasAnnotations;
    private String[] arrayOfAnnotations;
    
    public PdfPageEntry(PdfFileEntry parent, int pageNumber) {
        setParent(parent);
        setPageNumber(pageNumber);
    }
    
    public PdfPageEntry(PdfFileEntry parent, int pageNumber, PaperCategory height, PaperCategory width, PageColor pageColor) {
        setParent(parent);
        setPageNumber(pageNumber);
        setPaperHeightCategory(height);
        setPaperWidthCategory(width);
        setPageColor(pageColor);
    }
    
    public void setParent(PdfFileEntry parent) {
        this.parent = parent;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public void setPaperHeightCategory(PaperCategory height) {
        this.height = height;
    }
    
    public void setPaperWidthCategory(PaperCategory width) {
        this.width = width;
    }
    
    public void setExactPaperHeightCategory(PaperCategory height) {
        this.exactHeight = height;
    }
    
    public void setExactPaperWidthCategory(PaperCategory width) {
        this.exactWidth = width;
    }
    
    public void setOriginalPaperHeightCategory(PaperCategory height) {
        this.originalHeight = height;
    }
    
    public void setOriginalPaperWidthCategory(PaperCategory width) {
        this.originalWidth = width;
    }
    
    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }
    
    public void setAnnotations(String[] annotations) {
        this.arrayOfAnnotations = annotations;
    }
    
    public String[] getAnnotations() {
        return this.arrayOfAnnotations;
    }
    
    public boolean hasAnnotations() {
        return this.arrayOfAnnotations.length > 0;
    }
    
    public PdfFileEntry getParent() {
        return this.parent;
    }
    
    public int getPageNumber() {
        return this.pageNumber;
    }
    
    public PaperCategory getPaperHeightCategory() {
        return this.height;
    }
    
    public PaperCategory getPaperWidthCategory() {
        return this.width;
    }
    
    public PaperCategory getExactPaperHeightCategory() {
        return this.exactHeight;
    }
    
    public PaperCategory getExactPaperWidthCategory() {
        return this.exactWidth;
    }
    
    public PaperCategory getOriginalPaperHeightCategory() {
        return this.originalHeight;
    }
    
    public PaperCategory getOriginalPaperWidthCategory() {
        return this.originalWidth;
    }
    
    public void setPageColor(PageColor color) {
        this.pageColor = color;
    }
    
    public PageColor getPageColor() {
        return this.pageColor;
    }
    
    public String getPaperName() {
        return this.paperName;
    }
    
    public String getSizeName() {
        if(!this.paperName.equals("")) {
            return this.paperName;
        }
        
        return String.format("%sx%s", height.getName(), width.getName());
    }
    
    public String[] toStringArray() {
        
        String sizeStr = paperName.equals("") ? getSizeName() : paperName;
        String coloredStr = pageColor == PageColor.ERROR ?  "NONE" 
                : pageColor == PageColor.COLORED ? "1" : "0";
        
        return new String[] {
            parent.getPath()
            , parent.getFullname()
            , "" + pageNumber
            , "" + originalHeight.getPaperSizeMm()
            , "" + originalWidth.getPaperSizeMm()
            , "" + exactHeight.getPaperSizeMm()
            , "" + exactWidth.getPaperSizeMm()
            , "" + height.getPaperSizeMm()
            , "" + width.getPaperSizeMm()
            , sizeStr
            , coloredStr
            , String.join(", ", Utilities.distinctArray(this.arrayOfAnnotations))
        };
    }

    @Override
    public int compareTo(PdfPageEntry pageEntry) {
        int result = this.height.getPaperSizeMm() - pageEntry.getPaperHeightCategory().getPaperSizeMm();
        return result == 0 ? 
                this.width.getPaperSizeMm() - pageEntry.getPaperWidthCategory().getPaperSizeMm() : result;
    }
}
