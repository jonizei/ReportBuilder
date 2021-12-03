/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.builder;

import com.github.jonizei.reportbuilder.builder.ReportBuilder.PageColor;
import com.github.jonizei.reportbuilder.utils.PaperCategory;
import com.github.jonizei.reportbuilder.utils.Utilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a pdf file in the given source folder
 *
 * @author Joni
 * @version 2021-12-03
 */
public class PdfFileEntry extends FileEntry {
    
    /**
     * List of pdf page entries
     */
    private List<PdfPageEntry> pages;
    
    /**
     * Constructor that takes file path, filename and file extension
     * as a parameter
     * 
     * @param path
     * @param name
     * @param extension 
     */
    public PdfFileEntry(String path, String name, String extension) {
        super(path, name, extension);
        pages = new ArrayList<>();
    }
    
    /**
     * Creates new pdf page with given parameters
     * Adds the new pdf page to the pages list
     * Sorts the list after addition
     * 
     * @param pageNumber Number of the pdf page
     * @param height PaperCategory class that contains page's height information
     * @param width PaperCategory class that contains page's width information
     * @param pageColor enum that contains information if page is colored or not
     */
    public void addPage(int pageNumber, PaperCategory height, PaperCategory width, PageColor pageColor) {
        pages.add(new PdfPageEntry(this, pageNumber, height, width, pageColor));
        Collections.sort(pages);
    }
    
    /**
     * Adds new pdf page to pages list
     * Sorts the list after addition
     * 
     * @param page PdfPageEntry class that contains pdf file information
     */
    public void addPage(PdfPageEntry page) {
        pages.add(page);
        Collections.sort(pages);
    }
    
    /**
     * Returns the list of pdf pages
     * 
     * @return List of PdfPageEntry
     */
    public List<PdfPageEntry> getPages() {
        return this.pages;
    }
    
    /**
     * Returns true or false if this page contains annotations
     * 
     * @return boolean  
     */
    public boolean hasPagesWithAnnotations() {
        return this.pages.stream()
                .anyMatch(v -> v.hasAnnotations());
    }
    
    /**
     * Converts this class's information to an string array
     * 
     * @return String array
     */
    public String[] toStringArray() {
        
        String paperSizes = Utilities.getDistinctPaperSizes(pages, true).stream()
                .map(v -> Utilities.countPagesByPaperSize(pages, v, PageColor.ALL) + " " + v)
                .collect(Collectors.joining(","));
        
        int coloredCount = (int)pages.stream().filter(v -> v.getPageColor() == PageColor.COLORED).count();
        int grayscaleCount = (int)pages.stream().filter(v -> v.getPageColor() == PageColor.GRAYSCALE).count();
        
        String unknownPaperSizes = pages.stream()
                .filter(v -> v.getPaperHeightCategory().isUnknown() || v.getPaperWidthCategory().isUnknown())
                .map(v -> v.getSizeName())
                .collect(Collectors.joining(","));
        
        String errorPages = pages.stream()
                .filter(v -> v.getPageColor() == PageColor.ERROR)
                .map(v -> "" + v.getPageNumber())
                .collect(Collectors.joining(","));
        
        return new String[] {
            getFullname()
            , "" + pages.size()
            , paperSizes
            , "" + coloredCount
            , "" + grayscaleCount
            , unknownPaperSizes
            , errorPages
            , hasPagesWithAnnotations() ? "KYLLÄ" : "EI"
        };
    }
}
