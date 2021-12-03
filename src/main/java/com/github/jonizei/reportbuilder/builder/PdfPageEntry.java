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
    
    /**
     * Parent file where page belongs to
     */
    private PdfFileEntry parent;
    
    /**
     * Pdf page number
     */
    private int pageNumber;
    
    /**
     * PaperCategory that contains height in millimetres and pixels
     */
    private PaperCategory height;
    
    /**
     * PaperCategory that contains width in millimetres and pixels
     */
    private PaperCategory width;
    
    /**
     * PaperCategory that contains exact height in millimetres and pixels
     */
    private PaperCategory exactHeight;
    
    /**
     * PaperCategory that contains exact width in millimetres and pixels
     */
    private PaperCategory exactWidth;
    
    /**
     * PaperCategory that contains original height in millimetres and pixels
     */
    private PaperCategory originalHeight;
    
    /**
     * PaperCategory that contains original width in millimetres and pixels
     */
    private PaperCategory originalWidth;
    
    /**
     * Enum that contains the page's color state
     */
    private PageColor pageColor;
    
    /**
     * Name of the paper size (For example. A4)
     */
    private String paperName;
    
    /**
     * Boolean which tells if page contains annotations
     */
    private boolean hasAnnotations;
    
    /**
     * Array of strings containing names of annotation types that are 
     * found in the pdf page
     */
    private String[] arrayOfAnnotations;
    
    /**
     * Constructor of PdfPageEntry
     * 
     * @param parent Pdf parent file
     * @param pageNumber Pdf page number
     */
    public PdfPageEntry(PdfFileEntry parent, int pageNumber) {
        setParent(parent);
        setPageNumber(pageNumber);
    }
    
    /**
     * Constructor of PdfPageEntry
     * 
     * @param parent Pdf parent file
     * @param pageNumber Pdf page number
     * @param height Height of the pdf page
     * @param width Width of the pdf page
     * @param pageColor Color state of the pdf page
     */
    public PdfPageEntry(PdfFileEntry parent, int pageNumber, PaperCategory height, PaperCategory width, PageColor pageColor) {
        setParent(parent);
        setPageNumber(pageNumber);
        setPaperHeightCategory(height);
        setPaperWidthCategory(width);
        setPageColor(pageColor);
    }
    
    /**
     * Set value for parent attribute.
     * 
     * @param parent Pdf parent file
     */
    public void setParent(PdfFileEntry parent) {
        this.parent = parent;
    }
    
    /**
     * Set value for pdf page number
     * 
     * @param pageNumber Pdf page number
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    /**
     * Set height of the pdf page
     * 
     * @param height Height in millimetres and pixels
     */
    public void setPaperHeightCategory(PaperCategory height) {
        this.height = height;
    }
    
    /** 
     * Set width of the pdf page
     * 
     * @param width Width in millimetres and pixels
     */
    public void setPaperWidthCategory(PaperCategory width) {
        this.width = width;
    }
    
    /**
     * Set exact height of the pdf page
     * 
     * @param height Height in millimetres and pixels
     */
    public void setExactPaperHeightCategory(PaperCategory height) {
        this.exactHeight = height;
    }
    
    /**
     * Set exact width of the pdf page
     * 
     * @param width Width in millimetres and pixels
     */
    public void setExactPaperWidthCategory(PaperCategory width) {
        this.exactWidth = width;
    }
    
    /**
     * Set original height of the pdf page
     * 
     * @param height Height in millimetres and pixels
     */
    public void setOriginalPaperHeightCategory(PaperCategory height) {
        this.originalHeight = height;
    }
    
    /**
     * Set original width of the pdf page
     * 
     * @param width Width in millimetres and pixels
     */
    public void setOriginalPaperWidthCategory(PaperCategory width) {
        this.originalWidth = width;
    }
    
    /**
     * Set name of the paper size
     * 
     * @param paperName Name of the paper size
     */
    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }
    
    /**
     * Set array of annotation types
     * 
     * @param annotations String array of annotation types
     */
    public void setAnnotations(String[] annotations) {
        this.arrayOfAnnotations = annotations;
    }
    
    /**
     * Returns string array of annotation types
     * 
     * @return string array of annotation types
     */
    public String[] getAnnotations() {
        return this.arrayOfAnnotations;
    }
    
    /**
     * Checks if page has annotations based on count of the items
     * inside array of annotation types. If count is more than zero
     * then page contains annotations otherwise not.
     * 
     * @return Boolean which tells if page has annotations or not
     */
    public boolean hasAnnotations() {
        return this.arrayOfAnnotations.length > 0;
    }
    
    /**
     * Return pdf parent file
     * 
     * @return Pdf parent file
     */
    public PdfFileEntry getParent() {
        return this.parent;
    }
    
    /**
     * Returns pdf page number
     * 
     * @return Pdf page number
     */
    public int getPageNumber() {
        return this.pageNumber;
    }
    
   /**
    * Get paper height
    * 
    * @return Paper height in millimetres and pixels
    */
    public PaperCategory getPaperHeightCategory() {
        return this.height;
    }
    
    /**
     * Get paper width
     * 
     * @return Paper width in millimetres and pixels
     */
    public PaperCategory getPaperWidthCategory() {
        return this.width;
    }
    
    /**
     * Get exact paper height
     * 
     * @return Exact paper height in millimetres and pixels
     */
    public PaperCategory getExactPaperHeightCategory() {
        return this.exactHeight;
    }
    
    /**
     * Get exact paper width
     * 
     * @return Exact paper width in millimetres and pixels
     */
    public PaperCategory getExactPaperWidthCategory() {
        return this.exactWidth;
    }
    
    /**
     * Get original paper height
     * 
     * @return Original paper height in millimetres and pixels
     */
    public PaperCategory getOriginalPaperHeightCategory() {
        return this.originalHeight;
    }
    
    /**
     * Get original paper width
     * 
     * @return Original paper width in millimetres and pixels
     */
    public PaperCategory getOriginalPaperWidthCategory() {
        return this.originalWidth;
    }
    
    /**
     * Set pdf page's color state
     * 
     * @param color Page color state
     */
    public void setPageColor(PageColor color) {
        this.pageColor = color;
    }
    
    /** 
     * Get pdf page's color state
     * 
     * @return Page color state
     */
    public PageColor getPageColor() {
        return this.pageColor;
    }
    
    /**
     * Get name of the paper size
     * 
     * @return Name of the paper size (For example. A4)
     */
    public String getPaperName() {
        return this.paperName;
    }
    
    /**
     * Get name of the paper size.
     * If paper size doesn't have a name then return
     * height and width as a string (100x200)
     * 
     * @return Name of the paper size
     */
    public String getSizeName() {
        if(!this.paperName.equals("")) {
            return this.paperName;
        }
        
        return String.format("%sx%s", height.getName(), width.getName());
    }
    
    /**
     * Converts this class's information to a string array
     * 
     * @return String array
     */
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

    /**
     * Compare two pdf pages using their measurements
     * 
     * @param pageEntry Comparable pdf page
     * @return Either height difference or width difference of the two pages
     */
    @Override
    public int compareTo(PdfPageEntry pageEntry) {
        int result = this.height.getPaperSizeMm() - pageEntry.getPaperHeightCategory().getPaperSizeMm();
        return result == 0 ? 
                this.width.getPaperSizeMm() - pageEntry.getPaperWidthCategory().getPaperSizeMm() : result;
    }
}
