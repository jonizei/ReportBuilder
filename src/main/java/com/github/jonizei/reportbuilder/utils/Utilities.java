/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

import com.github.jonizei.reportbuilder.builder.FileEntry;
import com.github.jonizei.reportbuilder.builder.PdfFileEntry;
import com.github.jonizei.reportbuilder.builder.PdfPageEntry;
import com.github.jonizei.reportbuilder.builder.ReportBuilder;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Joni
 */
public class Utilities {
    
    private static PaperSizeLibrary sizeLibrary;
    private static int COLOR_THRESHOLD = 0;
    
    public static void setPaperSizeLibrary(PaperSizeLibrary library) {
        sizeLibrary = library;
    }
    
    public static void setColorThreshold(int threshold) {
        COLOR_THRESHOLD = threshold;
    }
    
    public static List<String> getDistinctPaperSizes(List<PdfPageEntry> pageList, boolean ignoreUnknown) {
        
        if(ignoreUnknown) {
            return pageList.stream()
                    .filter(e -> 
                            !e.getPaperHeightCategory().isUnknown() && !e.getPaperWidthCategory().isUnknown())
                    .map(e -> e.getSizeName())
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        return pageList.stream()
                    .map(e -> e.getSizeName())
                    .distinct()
                    .collect(Collectors.toList());
        
    }
    
    public static int countPagesByPaperSize(List<PdfPageEntry> pageList, String sizeName, ReportBuilder.PageColor color) {
        if(color == ReportBuilder.PageColor.ALL) {
           return pageList.stream()
                .filter(e -> e.getSizeName().equals(sizeName))
                .collect(Collectors.toList()).size(); 
        }
        else {
            return pageList.stream()
                .filter(e -> e.getSizeName().equals(sizeName) && e.getPageColor() == color)
                .collect(Collectors.toList()).size();
        }
    }
    
    public static List<PdfPageEntry> mergeAllPdfPages(List<FileEntry> fileEntries) {
        
        List<PdfPageEntry> allPages = new ArrayList<>();
        for(FileEntry entry : fileEntries) {
            if(entry instanceof PdfFileEntry) {
                PdfFileEntry pdfEntry = (PdfFileEntry) entry;
                allPages.addAll(pdfEntry.getPages());
            }
        }
        
        return allPages;
    }
    
    public static boolean isGrayscale(Color color) {
        int rgDiff = Math.abs(color.getRed() - color.getGreen());
        int gbDiff = Math.abs(color.getGreen() - color.getBlue());
        int brDiff = Math.abs(color.getBlue() - color.getRed());
        
        boolean isGrayscale = rgDiff <= COLOR_THRESHOLD && gbDiff <= COLOR_THRESHOLD && brDiff <= COLOR_THRESHOLD;
        
        return isGrayscale;
    }
    
    public static PaperCategory findStrictHeightCategory(int height, int heightOffset) {
        
        PaperCategory foundCategory = null;
        for(PaperCategory category : sizeLibrary.getPaperHeightCategories()) {
            if(height < (category.getPaperSizePx() + heightOffset) && height > (category.getPaperSizePx() - heightOffset)) {
                foundCategory = category;
                break;
            }
        }
        
        return foundCategory;
    }
    
    public static PaperCategory findPaperCategory(List<PaperCategory> categories, int size, int offset) {
        
        PaperCategory foundCategory = null;
        
        for(PaperCategory category : categories) {
                if(size <= (category.getPaperSizePx() + offset)) {
                    foundCategory = category;
                    break;
                }
            }
        
        return foundCategory;
    }
    
    public static PaperCategory findClosestCategory(int width, int height, int widthOffset, int heightOffset) {
        PaperCategory closestCategory = null;
        PaperCategory foundCategory = Utilities.findPaperCategory(sizeLibrary.getPaperHeightCategories(), height, heightOffset);
        PaperCategory foundCategory2 = Utilities.findPaperCategory(sizeLibrary.getPaperHeightCategories(), width, widthOffset);
        
        if(foundCategory != null && foundCategory2 != null) {
            if(foundCategory.equals(foundCategory2)) {
                closestCategory = height < width ? foundCategory : foundCategory2;
            }
            else {
                closestCategory = foundCategory.getPaperSizeMm() < foundCategory2.getPaperSizeMm() ? foundCategory : foundCategory2;
            }
        }
        else if(foundCategory != null && foundCategory2 == null) {
            closestCategory = foundCategory;
        }
        else if(foundCategory == null && foundCategory2 != null) {
            closestCategory = foundCategory2;
        }
        
        return closestCategory;
    }
    
    public static int getLongestString(String[] array) {
        return Arrays.asList(array).stream()
                        .mapToInt(e -> e.length())
                        .max().getAsInt();
    }
    
    public static List<String[]> createTable(String[] headerRow, List<String[]> rows) {
        
        rows.add(0, headerRow);
        List<String[]> table = new ArrayList<>();
        
                    
        for(int i = 0; i < headerRow.length; i++) {
            String[] col = new String[rows.size()];
            for(int j = 0; j < rows.size(); j++) {
                col[j] = rows.get(j)[i];
            }
            table.add(col);
        }
        
        
        return table;
    }
    
    public static String[] valuesToConstLength(String[] array, int padding) {
        
        int maxLength = getLongestString(array);
        
        for(int i = 0; i < array.length; i++) {
            array[i] = StringUtils.rightPad(array[i], maxLength + padding, " ");
        }
        
        return array;
    }
    
    public static List<String[]> createRowsByPaperSize(List<PdfPageEntry> allPages, boolean ignoreColor) {
        List<String> allPaperSizes = getDistinctPaperSizes(allPages, false);
        List<String[]> rows = new ArrayList<>();
        List<String> unknownPaperSizes = allPages.stream()
                .filter(v -> v.getPaperHeightCategory().isUnknown() || v.getPaperWidthCategory().isUnknown())
                .map(v -> v.getSizeName())
                .collect(Collectors.toList());
        
        for(String paperSize : allPaperSizes) {
            String[] row = ignoreColor ? new String[] {
                paperSize
                , "" + countPagesByPaperSize(allPages, paperSize, ReportBuilder.PageColor.GRAYSCALE)
                , unknownPaperSizes.contains(paperSize) ? "Tuntematon koko" : ""
            } : new String[] {
                paperSize
                , "" + countPagesByPaperSize(allPages, paperSize, ReportBuilder.PageColor.COLORED)
                , "" + countPagesByPaperSize(allPages, paperSize, ReportBuilder.PageColor.GRAYSCALE)
                , unknownPaperSizes.contains(paperSize) ? "Tuntematon koko" : ""
            };
            rows.add(row);
        }
        
        return rows;
    }
    
    public static int arrayMax(int[] array) {
        
        if(array.length > 0) {
            int max = array[0];
            for(int i = 0; i < array.length; i++) {
                max = max < array[i] ? array[i] : max;
            }
            
            return max;
        }
        
        return 0;
    }
    
    public static int arrayMin(int[] array) {
        
        if(array.length > 0) {
            int min = array[0];
            for(int i = 0; i < array.length; i++) {
                min = min > array[i] ? array[i] : min;
            }
            
            return min;
        }
        
        return 0;
    }
    
    public static String[] distinctArray(String[] array) {
        return Arrays.asList(array).stream()
                .distinct()
                .toArray(String[]::new);
    }
    
}
