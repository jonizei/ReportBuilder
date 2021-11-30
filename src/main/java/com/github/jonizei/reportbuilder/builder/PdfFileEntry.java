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
 *
 * @author Joni
 */
public class PdfFileEntry extends FileEntry {
    
    private List<PdfPageEntry> pages;
    
    public PdfFileEntry(String path, String name, String extension) {
        super(path, name, extension);
        pages = new ArrayList<>();
    }
    
    public void addPage(int pageNumber, PaperCategory height, PaperCategory width, PageColor pageColor) {
        pages.add(new PdfPageEntry(this, pageNumber, height, width, pageColor));
        Collections.sort(pages);
    }
    
    public void addPage(PdfPageEntry page) {
        pages.add(page);
        Collections.sort(pages);
    }
    
    public List<PdfPageEntry> getPages() {
        return this.pages;
    }
    
    public boolean hasPagesWithAnnotations() {
        return this.pages.stream()
                .anyMatch(v -> v.hasAnnotations());
    }
    
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
