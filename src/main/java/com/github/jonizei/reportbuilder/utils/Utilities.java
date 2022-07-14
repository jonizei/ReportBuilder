/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

import com.github.jonizei.reportbuilder.Main;
import com.github.jonizei.reportbuilder.builder.FileEntry;
import com.github.jonizei.reportbuilder.builder.PdfFileEntry;
import com.github.jonizei.reportbuilder.builder.PdfPageEntry;
import com.github.jonizei.reportbuilder.builder.ReportBuilder;
import java.awt.Color;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.ghost4j.document.PDFDocument;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class contains utility methods used in this program
 * 
 * @author Joni
 * @version 2021-12-03
 */
public class Utilities {

    private static final String GIT_USERNAME = "jonizei";
    private static final String GIT_REPO = "ReportBuilder";
    private static final String GIT_API_URL = createGitApiUrl();
    
    /**
     * Class that contains all the paper sizes used in this program
     */
    private static PaperSizeLibrary sizeLibrary;
    
    /**
     * Threshold value used in color checking
     */
    private static int COLOR_THRESHOLD = 0;

    public static String createGitApiUrl() {
        return "https://api.github.com/repos/" + GIT_USERNAME + "/" + GIT_REPO + "/tags";
    }
    
    /**
     * Static method to set PaperSizeLibrary instance
     * 
     * @param library PaperSizeLibrary instance
     */
    public static void setPaperSizeLibrary(PaperSizeLibrary library) {
        sizeLibrary = library;
    }
    
    /**
     * Static method to set the color threshold
     * 
     * @param threshold Color threshold
     */
    public static void setColorThreshold(int threshold) {
        COLOR_THRESHOLD = threshold;
    }
    
    /**
     * Static method that removes all paper size duplicates from the given 
     * pdf page list
     * 
     * @param pageList List of pdf pages
     * @param ignoreUnknown Should unknown paper sizes to be ignored
     * @return List of pdf pages with no paper size duplicates
     */
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
    
    /**
     * Static method that counts all pdf pages with the same paper size
     * 
     * @param pageList List of pdf pages
     * @param sizeName Name of the paper size
     * @param color Only pages with this color state will be counted
     * @return Count of the pdf pages with same size
     */
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
    
    /**
     * Static method that merges all the pdf pages from every pdf file to 
     * a single list
     * 
     * @param fileEntries List of files
     * @return Merged list of all pdf pages of every pdf file
     */
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
    
    /** 
     * Static method that checks if color is grayscale or not
     * 
     * @param color Color of the pixel
     * @return Boolean value which tells if color is grayscale or not
     */
    public static boolean isGrayscale(Color color) {
        int rgDiff = Math.abs(color.getRed() - color.getGreen());
        int gbDiff = Math.abs(color.getGreen() - color.getBlue());
        int brDiff = Math.abs(color.getBlue() - color.getRed());
        
        boolean isGrayscale = rgDiff <= COLOR_THRESHOLD && gbDiff <= COLOR_THRESHOLD && brDiff <= COLOR_THRESHOLD;
        
        return isGrayscale;
    }
    
    /**
     * Statc method that tries to find height category where given height will fit
     * 
     * @param height Height of the pdf page
     * @param heightOffset Offset to modify the strictness of the search
     * @return Found paper category if finds any
     */
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
    
    /**
     * Static method that finds the first paper category where given size fits
     * 
     * @param categories Categories that will be searched
     * @param size width or height of the paper
     * @param offset Offset to modify the strictness of the search
     * @return Found paper category if finds any
     */
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
    
    /**
     * Static method finds the closest height category for the paper size
     * 
     * @param width Width of the paper
     * @param height Height of the paper
     * @param widthOffset Width offset to modify the strictness of the search
     * @param heightOffset height offset to modify the strictness of the search
     * @return Closest height category
     */
    public static PaperCategory findClosestHeightCategory(int width, int height, int heightOffset) {
        PaperCategory closestCategory = null;
        PaperCategory foundCategory = Utilities.findPaperCategory(sizeLibrary.getPaperHeightCategories(), height, heightOffset);
        PaperCategory foundCategory2 = Utilities.findPaperCategory(sizeLibrary.getPaperHeightCategories(), width, heightOffset);
        
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

    /**
     * Tries to find closest height category using image width.
     *
     * @param imgWidth
     * @param imgHeight
     * @param heightOffset
     * @return Found PaperCategory or null
     */
    public static PaperCategory findClosestHeightCategoryByImageWidth(int imgWidth, int imgHeight, int heightOffset) {
        PaperCategory heightCategory = findStrictHeightCategory(imgWidth, heightOffset);
        PaperCategory closestHeightCategory = findClosestHeightCategory(imgWidth, imgHeight, heightOffset);

        if (heightCategory != null && closestHeightCategory != null) {
            if (closestHeightCategory.getPaperSizePx() < heightCategory.getPaperSizePx()) {
                heightCategory = closestHeightCategory;
            }
        } else if (closestHeightCategory != null) {
            heightCategory = closestHeightCategory;
        }

        return heightCategory;
    }

    /**
     * Tries to find the closest height category using image height and image width
     * and selects the smaller one.
     *
     * @param imgWidth
     * @param imgHeight
     * @param heightOffset
     * @return Found PaperCategory or null
     */
    public static PaperCategory findHeightCategory(int imgWidth, int imgHeight, int heightOffset) {
        PaperCategory heightCategory = findStrictHeightCategory(imgHeight, heightOffset);
        PaperCategory heightCategoryByImgWidth = findClosestHeightCategoryByImageWidth(imgWidth, imgHeight, heightOffset);

        if (heightCategory != null && heightCategoryByImgWidth != null) {
            if (heightCategoryByImgWidth.getPaperSizePx() < heightCategory.getPaperSizePx()) {
                heightCategory = heightCategoryByImgWidth;
            }
        } else if(heightCategoryByImgWidth != null) {
            heightCategory = heightCategoryByImgWidth;
        }

        return heightCategory;
    }
    
    /**
     * Static method that finds the longest string from a string array
     * 
     * @param array Array of strings
     * @return Width of the longest string
     */
    public static int getLongestString(String[] array) {
        return Arrays.asList(array).stream()
                        .mapToInt(e -> e.length())
                        .max().getAsInt();
    }
    
    /**
     * Static method that creates a table (list of string arrays)
     * from string array containing headers and list string arrays
     * 
     * @param headerRow Header names of the table
     * @param rows Table rows
     * @return Table (List of string arrays)
     */
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
    
    /**
     * Static method that creates all the strings in the array to the same
     * length with padding
     * 
     * @param array Array of strings
     * @param padding Number of empty spaces
     * @return Array of strings with same lengths
     */
    public static String[] valuesToConstLength(String[] array, int padding) {
        
        int maxLength = getLongestString(array);
        
        for(int i = 0; i < array.length; i++) {
            array[i] = StringUtils.rightPad(array[i], maxLength + padding, " ");
        }
        
        return array;
    }
    
    /**
     * Static method that creates a table (list of string arrays) from
     * pdf pages and create distinct rows using paper sizes
     * 
     * @param allPages Pdf pages to be added to the table
     * @param ignoreColor Should page color be ignored or not
     * @return Table with distinct paper size rows (list of string arrays)
     */
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
    
    /**
     * Static method that returns highest value from given integer array
     * 
     * @param array Array of integers
     * @return Highest value from given integer array
     */
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
    
    /**
     * Static method that returns lowest integer value from given integer array
     * 
     * @param array Array of integers
     * @return Lowest value from given integer array
     */
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
    
    /**
     * Static method that returns distinct values from array of strings
     * 
     * @param array Array of strings
     * @return Array of strings with distinct values
     */
    public static String[] distinctArray(String[] array) {
        return Arrays.asList(array).stream()
                .distinct()
                .toArray(String[]::new);
    }
    
    /**
     * Converts exception's stacktrace to string.
     * 
     * @param ex Exception
     * @return Exception stacktrace as a string
     */
    public static String convertStackTraceToString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void checkProgramVersion() {

        String version = Main.class.getPackage().getImplementationVersion();
        System.out.println("Version: " + version);

        try {
            String responseJson = loadTagsFromGithubAsJson();
            JSONArray responseArray = new JSONArray(responseJson);
            for(int i = 0; i < responseArray.length(); i++) {
               JSONObject tmp = responseArray.getJSONObject(i);
               System.out.println(tmp.get("name"));
            }
        } catch (MalformedURLException ex) {
            System.out.println("Github verkko-osoite on virheellinen.");
        } catch (ProtocolException ex) {
            System.out.println("Virheellinen http protokolla.");
        } catch (IOException ex) {
            System.out.println("Http pyyntö epäonnistui.");
        }
    }

    public static String loadTagsFromGithubAsJson() throws MalformedURLException, ProtocolException, IOException {
        URL url = new URL(GIT_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int status = connection.getResponseCode();
        StringBuffer content = new StringBuffer();

        if(status == 200) {
            BufferedReader httpReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            String inputLine = "";
            while((inputLine = httpReader.readLine()) != null) {
                content.append(inputLine);
            }
            httpReader.close();
        }

        connection.disconnect();
        return content.toString();
    }

    public static boolean ghostCanReadPdf(File pdfFile) {
        boolean canRead = true;

        try {
            PDFDocument document = new PDFDocument();
            document.load(pdfFile);
        } catch(Exception ex) {
            canRead = false;
        }

        return canRead;
    }
    
}
