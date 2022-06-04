/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.builder;

import com.github.jonizei.reportbuilder.utils.DebugUtilities;
import com.github.jonizei.reportbuilder.utils.PaperCategory;
import com.github.jonizei.reportbuilder.utils.PaperSizeLibrary;
import com.github.jonizei.reportbuilder.utils.PdfPageCropper;
import com.github.jonizei.reportbuilder.utils.Utilities;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.set.SynchronizedSet;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import org.apache.pdfbox.rendering.PDFRenderer;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;
import org.ghost4j.converter.PDFConverter;
import org.ghost4j.converter.PSConverter;
import org.ghost4j.document.PSDocument;

/**
 * This class processes all the files in the given source folder
 * 
 * @author Joni
 * @version 2022-01-03
 */
public class ReportBuilder {
    
    /**
     * Class that contains all the paper sizes used in this program
     */
    private PaperSizeLibrary sizeLibrary;
    
    /**
     * Class that is used to crop all the pdf pages
     */
    private PdfPageCropper pageCropper;
    
    /**
     * Boolean which enables or disables pdf page color checking
     */
    private boolean ignoreColor;
    
    /**
     * Enum that is used for telling the color state of the pdf page
     */
    public enum PageColor {
        ALL
        , ERROR
        , GRAYSCALE
        , COLORED
    }
    
    /**
     * Height threshold. Used when assigning a fixed paper size 
     * to a pdf page
     */
    private int heightThreshold;
    
    /** 
     * Width threshold. Used when assigning a fixed paper size
     * to a pdf page
     */
    private int widthThreshold;
    
    /**
     * Margin used in page cropping
     */
    private static int CROP_MARGIN = 0;
    
    /**
     * List of all the files in the given source folder
     */
    private List<FileEntry> fileEntries;
    
    /**
     * List of all errors happened during runtime
     */
    private HashMap<String, String> errorLogs;
    
    private DebugUtilities debugUtils;
    
    /**
     * Constructor assigns given parameters to class
     * variables.
     * 
     * Disables ghost4j library's logging.
     * 
     * @param sizeLibrary
     * @param ignoreColor
     * @param heightThreshold
     * @param widthThreshold
     */
    public ReportBuilder(PaperSizeLibrary sizeLibrary
            , boolean ignoreColor
            , int heightThreshold
            , int widthThreshold) {
        this.sizeLibrary = sizeLibrary;
        this.ignoreColor = ignoreColor;
        this.heightThreshold = heightThreshold;
        this.widthThreshold = widthThreshold;
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        this.pageCropper = new PdfPageCropper();
        this.errorLogs = new HashMap<>();
        this.debugUtils = new DebugUtilities(false);
    }
    
    /**
     * Loads the folder in the given path.
     * If folder contains sub folders it also loads them.
     * 
     * @param path
     * @throws IOException 
     */
    public void load(String path) throws IOException {
        File folder = new File(path);
        
        if(folder.isDirectory()) {
            fileEntries = new ArrayList<>();
            List<File> subfolders = findAllSubFolders(folder);
            loadFolder(folder);
            
            for(File dir : subfolders) {
                System.out.println("Käsitellään alikansiota");
                loadFolder(dir);
            }
        }
        else System.out.println("Kansiota ei löydy");

    }
    
    /**
     * Finds all sub folders inside given folder.
     * This method uses recursion to follow down the folder path.
     * 
     * @param folder
     * @return List of found sub folders
     */
    private List<File> findAllSubFolders(File folder) {
        
        List<File> subfolders = new ArrayList<>();
        
        for(File file : folder.listFiles()) {
            if(file.isDirectory()) {
                subfolders.add(file);
                subfolders.addAll(findAllSubFolders(file));
            }
        }
        
        return subfolders;
    }
    
    /**
     * Loads all the files from given folder.
     * Iterates through all the files and separates the pdf files
     * from other files.
     * 
     * Creates a new FileEntry instance for every file. If file is a 
     * pdf file it creates PdfFileEntry instance.
     * 
     * When it founds pdf file it processes all the pages of the file.
     * 
     * @param folder
     * @throws IOException 
     */
    private void loadFolder(File folder) throws IOException {
        
        File[] listOfFiles = folder.listFiles();

        int maxFiles = listOfFiles.length;
        int currentFile = 0;
        FileEntry fileEntry = null; 
        PdfFileEntry pdfFileEntry = null;

        for(File file : listOfFiles) {
            if(file.isFile()) {
                System.out.println(String.format("Tiedostoa käsitellään: %s", file.getName()));

                fileEntry = new FileEntry(folder.getPath(), file.getName());

                if(fileEntry.getExtension().equals("pdf")) {
                    
                    try {
                        pdfFileEntry = new PdfFileEntry(fileEntry.getPath(), fileEntry.getName(), fileEntry.getExtension());
                        processAllPdfPages(file, pdfFileEntry);
                        fileEntries.add(pdfFileEntry);
                    } catch(Exception ex) {
                        System.out.println(String.format("Ongelma tiedoston käsittelyssä: %s", file.getName()));
                        this.errorLogs.put(file.getName(), Utilities.convertStackTraceToString(ex));
                    }
                    
                }
                else {
                    fileEntries.add(fileEntry);
                }
                
                currentFile++;
                System.out.println(String.format("Tiedostoja käsitelty: %d/%d", currentFile, maxFiles));
            }
        }
        
    }
    
    /**
     * Renders all pages of the pdf file to images for processing.
     * Used 72 dpi for image resolution.
     * 
     * @param pdfFile
     * @return Array of Image class
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private Image[] pdfDocumentToImages(File pdfFile) throws FileNotFoundException, IOException {

        if(!Utilities.ghostCanReadPdf(pdfFile)) {
            return pdfDocumentToImagesWithPdfBox(pdfFile);
        }

        PDFDocument document = new PDFDocument();

        SimpleRenderer renderer = new SimpleRenderer();
        renderer.setResolution(72);

        document.load(pdfFile);
        
        List<Image> images = new ArrayList<>();
        
        try {
            images = renderer.render(document);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        Image[] arrImages = new Image[images.size()];
        return images.toArray(arrImages);
    }

    private Image[] pdfDocumentToImagesWithPdfBox(File pdfFile) {

        List<BufferedImage> buffImages = new ArrayList<>();

        try {
            PDDocument document = Loader.loadPDF(pdfFile);
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage pageImg = null;

            int numberOfPages = document.getNumberOfPages();
            for(int i = 0; i < numberOfPages; i++) {
                pageImg = renderer.renderImageWithDPI(i, 72);
                buffImages.add(pageImg);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        List<Image> images = buffImages.stream()
                .map(bImg -> (Image)bImg)
                .collect(Collectors.toList());
        return images.toArray(new Image[images.size()]);
    }
    
    /** 
     * Iterates trough all the pdf pages and processes every page.
     * Creates PdfPageEntry of every processed pdf page that contains 
     * all the information of the page.
     * 
     * @param originalFile File class for handling the pdf file
     * @param fileEntry PdfFileEntry that contains necessary pdf file
     * information
     * @throws IOException 
     */
    private void processAllPdfPages(File originalFile, PdfFileEntry fileEntry) throws Exception {
        
        PDDocument document = Loader.loadPDF(originalFile);

        Image[] allPages = pdfDocumentToImages(originalFile);
        
        for(int i = 0; i < allPages.length; i++) {
            BufferedImage pageImg = (BufferedImage) allPages[i];
            
            PdfPageEntry newPage = processPdfPage(fileEntry, pageImg, i+1);
            newPage.setAnnotations(getArrayOfAnnotations(document.getPage(i)));

            fileEntry.addPage(newPage);

            pageImg.flush();
            pageImg = null;
            System.out.println(String.format("Sivuja käsitelty: %d/%d", i+1, allPages.length));
        }
        
        document.close();
    }
    
    /**
     * Processes a single pdf page.
     * Saves page's measurements to PdfPageEntry instance and the crops the page
     * and after that it saves cropped measurements to the same instance.
     * 
     * If color check (ignoreColor) is enabled then it check the page's color
     * state and saves it to the PdfPageEntry instance otherwise it sets color
     * state to a default GRAYSCALE.
     * 
     * @param parent File where the pdf pages belongs to
     * @param pageImg Image of pdf page
     * @param pageNumber Number of the pdf page
     * @return Processed page instance (PdfPageEntry)
     * @throws IOException 
     */
    private PdfPageEntry processPdfPage(PdfFileEntry parent, BufferedImage pageImg, int pageNumber) throws IOException {
        PdfPageEntry pageEntry = new PdfPageEntry(parent, pageNumber);
        
        pageEntry.setOriginalPaperHeightCategory(new PaperCategory("", PaperSizeLibrary.pixelToMm(pageImg.getHeight()), pageImg.getHeight(), false));
        pageEntry.setOriginalPaperWidthCategory(new PaperCategory("", PaperSizeLibrary.pixelToMm(pageImg.getWidth()), pageImg.getWidth(), false));
        
        pageImg = pageCropper.cropPage(pageImg, CROP_MARGIN);
        
        pageEntry.setExactPaperHeightCategory(new PaperCategory("", PaperSizeLibrary.pixelToMm(pageImg.getHeight()), pageImg.getHeight(), false));
        pageEntry.setExactPaperWidthCategory(new PaperCategory("", PaperSizeLibrary.pixelToMm(pageImg.getWidth()), pageImg.getWidth(), false));
        
        addPaperCategories(pageEntry, pageImg.getWidth(), pageImg.getHeight());
        
        if(!ignoreColor) {
            try {
                pageEntry.setPageColor(getPageColor(pageImg));
            } catch(OutOfMemoryError err) {
                System.out.println("Sivua ei voitu käsitellä. Sivun koko on liian suuri.");
                System.out.println("Lisätiedot löytyy raportista.");
                pageEntry.setPageColor(PageColor.ERROR);
            }
        }
        else pageEntry.setPageColor(PageColor.GRAYSCALE);
        
        return pageEntry;
    }
    
    /**
     * Returns an array of all the annotations that the pdf page contains.
     * If there weren't any annotation then it will return an empty array.
     * The array contains only the types of the annotations
     * 
     * @param page Pdf page that will be checked
     * @return String array of annotation types
     * @throws IOException 
     */
    private String[] getArrayOfAnnotations(PDPage page) throws IOException {
        List<PDAnnotation> anList = page.getAnnotations();
        return anList.stream().map(v -> v.getSubtype())
                .toArray(String[]::new);
    }
    
    /**
     * Converts given image to an pixel array and then
     * it iterates trough the array to check for colored pixels
     * 
     * @param img Image to be processed
     * @return enum that tells image's color state (GRAYSCALE OR COLORED)
     */
    private PageColor getPageColor(BufferedImage img) {
       
        int[] pxArray = imageToIntArray(img);
        boolean isGrayscale = processPxArray(pxArray);
        img.flush();
        
        return isGrayscale ? PageColor.GRAYSCALE : PageColor.COLORED;
    }
    
    /** 
     * Iterates trough every fifth of the pixel array to check colored pixels.
     * If colored pixel is found then it breaks the loop and
     * returns true. Otherwise it will go through the entire loop
     * and return false.
     * 
     * @param pxArray An array of pixels
     * @return Boolean which tells if colored pixel is found or not
     */
    private boolean processPxArray(int[] pxArray) {
        
        boolean isGrayscale = true;
        
        for(int i = 0; i < pxArray.length && isGrayscale; i+=5) {
            if(!Utilities.isGrayscale(new Color(pxArray[i]))) {
                isGrayscale = false;
            }
        }
        
        return isGrayscale;
    }
    
    /**
     * Creates int array from BufferedImage and shuffles the pixels
     * to break concentrated pixel chuncks to speed up the search.
     * 
     * @param img Image to be converted
     * @return Shuffled int array
     */
    private int[] imageToIntArray(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        int[] pxArray = img.getRGB(0, 0, width, height, null, 0, width);
        List<Integer> pxList = Arrays.stream(pxArray).boxed().collect(Collectors.toList());
        Collections.shuffle(pxList);
        pxArray = pxList.stream().mapToInt(i -> i).toArray();
        
        return pxArray;
    }
    
    /**
     * Tries to find the best possible paper category for the given pdf page
     * using the page's measurements. 
     * If paper category does not exist it will create a custom paper category
     * for the page.
     * 
     * After finding suitable paper category it will save the category to given
     * PdfPageEntry
     * 
     * @param pdfEntry Entry of current pdf page
     * @param imgHeight Height of the page image
     * @param imgWidth Width of the page image
     */
    private void addPaperCategories(PdfPageEntry pdfEntry, int imgHeight, int imgWidth) {
       
        int heightOffset = PaperSizeLibrary.mmToPixel(heightThreshold);
        int widthOffset = PaperSizeLibrary.mmToPixel(widthThreshold);
         
        PaperCategory heightCategory = Utilities.findStrictHeightCategory(imgHeight, heightOffset);
        heightCategory = heightCategory == null ? Utilities.findStrictHeightCategory(imgWidth, heightOffset) : heightCategory;
        heightCategory = heightCategory == null ? Utilities.findClosestCategory(imgWidth, imgHeight, widthOffset, heightOffset) : heightCategory;
        
        PaperCategory widthCategory = null;
        
        if(heightCategory == null) {
            heightCategory = new PaperCategory("" + PaperSizeLibrary.pixelToMm(imgHeight), PaperSizeLibrary.pixelToMm(imgHeight), imgHeight, true);
            widthCategory = new PaperCategory("" + PaperSizeLibrary.pixelToMm(imgWidth), PaperSizeLibrary.pixelToMm(imgWidth), imgWidth, true);
        }
        else {
            int hDiff = Math.abs(heightCategory.getPaperSizePx() - imgHeight);
            int wDiff = Math.abs(heightCategory.getPaperSizePx() - imgWidth);
            
            int width = hDiff <= wDiff ? imgWidth : imgHeight;
            
            widthCategory = Utilities.findPaperCategory(sizeLibrary.getPaperWidthCategories(), width, widthOffset);
            widthCategory = widthCategory == null 
                    ? new PaperCategory("" + PaperSizeLibrary.pixelToMm(imgWidth), PaperSizeLibrary.pixelToMm(imgWidth), imgWidth, true) : widthCategory;
            heightCategory = widthCategory.isUnknown() 
                    ? new PaperCategory("" + PaperSizeLibrary.pixelToMm(imgHeight), PaperSizeLibrary.pixelToMm(imgHeight), imgHeight, true) : heightCategory;
        }
        
        pdfEntry.setPaperName(sizeLibrary.findPaperName(heightCategory.getPaperSizeMm(), widthCategory.getPaperSizeMm(), heightOffset, widthOffset));
        pdfEntry.setPaperHeightCategory(heightCategory);
        pdfEntry.setPaperWidthCategory(widthCategory);
        
    }
    
    /**
     * Creates a report to a text file to given path using only 
     * minimal amount information: 
     * - Size
     * - GRAYSCALE and/or COLORED depending on if color check is enabled
     * - Comments
     * 
     * @param path Path to the destination folder
     */
    private void writeSimpleReport(String path) {
        
        List<PdfPageEntry> allPages = Utilities.mergeAllPdfPages(fileEntries);
        Collections.sort(allPages);
        
        List<FileEntry> otherFiles = fileEntries.stream()
            .filter(e -> !(e instanceof PdfFileEntry))
            .collect(Collectors.toList());
        
        String[] headerRow = ignoreColor ? new String[] {
            "Koko"
            , "Mustavalkoinen"
            , "Huomioitavaa"
        } : new String[] {
            "Koko"
            , "Värillinen"
            , "Mustavalkoinen"
            , "Huomioitavaa"
        };

        List<String[]> allRows = Utilities.createRowsByPaperSize(allPages, ignoreColor);
        
        int pdfFileCount = (int) fileEntries.stream()
                .filter(e -> e instanceof PdfFileEntry)
                .count();
        
        int pdfPageCount = allPages.size();
        
        List<PdfFileEntry> errorFiles = fileEntries.stream()
                .filter(v -> v instanceof PdfFileEntry)
                .map(v -> (PdfFileEntry) v)
                .filter(v -> v.getPages().stream()
                .filter(e -> e.getPageColor() == PageColor.ERROR).count() > 0)
                .collect(Collectors.toList());
        
        writeTxtReport(path, headerRow, allRows, otherFiles, errorFiles, pdfFileCount, pdfPageCount);
       
    }
    
    /**
     * Creates an extended report to a text file.
     * Information:
     * - File name
     * - Page count
     * - All the page sizes the file contains
     * - Color state information
     * - Custom sizes the pdf file contains
     * - Pages that failed during the process
     * - Does page contain annotations
     * 
     * @param path Path to the destination folder
     */
    private void writeExtentedReport(String path) {
        
        List<PdfFileEntry> pdfFiles = fileEntries.stream()
            .filter(e -> e instanceof PdfFileEntry)
            .map(e -> (PdfFileEntry)e)
            .collect(Collectors.toList());
        
        List<FileEntry> otherFiles = fileEntries.stream()
            .filter(e -> !(e instanceof PdfFileEntry))
            .collect(Collectors.toList());
        
        int pdfFileCount = pdfFiles.size();
        int pdfPageCount = Utilities.mergeAllPdfPages(fileEntries).size();
        
        String[] headerRow = new String[] {
            "Tiedoston nimi"
            , "Sivumäärä"
            , "Koot"
            , "Värilliset"
            , "Mustavalkoiset"
            , "Muut koot"
            , "Epäonnistuneet sivut"
            , "Sisältää merkintöjä"
        };
        
        List<String[]> allRows = pdfFiles.stream()
                .map(v -> v.toStringArray())
                .collect(Collectors.toList());
        
        writeTxtReport(path, headerRow, allRows, otherFiles, null, pdfFileCount, pdfPageCount);
        
    }
    
    /**
     * Converts all pdf pages to list of a string arrays and then creates
     * detailed report to csv file with the created list.
     * 
     * @param path Path to destination folder
     */
    public void writeDetailedReport(String path) {
        
        List<PdfPageEntry> allPages = Utilities.mergeAllPdfPages(fileEntries);
        List<String[]> allRows = allPages.stream()
                .map(v -> v.toStringArray())
                .collect(Collectors.toList());
        
        String[] headerRow = new String[] {
            "Tiedosto polku"
            , "Tiedoston nimi"
            , "Sivu"
            , "Alkuperäinen korkeus"
            , "Alkuperäinen leveys"
            , "Rajattu korkeus"
            , "Rajattu leveys"
            , "Laskutus korkeus"
            , "Laskutus leveys"
            , "Laskutus koko"
            , "Värillinen"
            , "Sisältää merkintöjä"
        };
        
        int pdfCount = fileEntries.stream()
            .filter(e -> e instanceof PdfFileEntry)
            .map(e -> (PdfFileEntry)e)
            .collect(Collectors.toList()).size();
        
        int pdfPageCount = Utilities.mergeAllPdfPages(fileEntries).size();
        
        writeCsvReport(path, headerRow, allRows, pdfCount, pdfPageCount);
    }
    
    /**
     * Writes report to a csv file using given list of string arrays.
     * Adds BOM to the csv file so it can be opened with MS excel.
     * 
     * @param path Path to destination folder
     * @param headerRow String array that contains all the required headers
     * @param allRows List that contains all pdf page entries as string arrays
     * @param pdfCount Count of all pdf files.
     * @param pageCount Count of all the pdf pages of all the pdf files combined
     */
    private void writeCsvReport(String path, String[] headerRow, List<String[]> allRows, int pdfCount, int pageCount) {
        
        String headerCsv = String.join(";", Arrays.asList(headerRow));
        List<String> csvRows = allRows.stream()
                .map(v -> String.join(";", Arrays.asList(v)))
                .collect(Collectors.toList());
        
        byte[] BOM = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String BOMStr = new String(BOM);
        
        try {
            Writer reportWriter = new OutputStreamWriter(new FileOutputStream(new File(path + ".csv"), false), StandardCharsets.UTF_8);
            
            reportWriter.write(BOMStr);
            
            reportWriter.write(String.format("PDF;%d\n", pdfCount));
            reportWriter.write(String.format("Sivumäärä;%d\n", pageCount));
            reportWriter.write("\n");
            
            reportWriter.write(String.format("%s\n", headerCsv));
            
            for(String item : csvRows) {
                reportWriter.write(String.format("%s\n", item));
            }
            
            reportWriter.flush();
            reportWriter.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Writes a report to a text file using given list of string arrays.
     * 
     * 
     * @param path Path to destination folder
     * @param headerRow String array that contains all required headers
     * @param allRows List that contains all pdf file entries as string arrays
     * @param otherFiles Other files that was found from the given folder
     * @param errorFiles List of all the pdf files that failed the process
     * @param fileCount Count of all the pdf files
     * @param pageCount Count of all the pdf pages of all the pdf files combined
     */
    private void writeTxtReport(String path, String[] headerRow, List<String[]> allRows, List<FileEntry> otherFiles, List<PdfFileEntry> errorFiles, int fileCount, int pageCount) {
        
        List<String[]> table = Utilities.createTable(headerRow, allRows);
        table = table.stream().map(v -> Utilities.valuesToConstLength(v , 3)).collect(Collectors.toList());
        
        
        try {
            BufferedWriter reportWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File(path + ".txt"), false), StandardCharsets.UTF_8));
            
            reportWriter.write(String.format("PDF: %d\nSivumäärä: %d\n\n", fileCount, pageCount));
            
            for(int i = 0; i < table.get(0).length; i++) {
                    for(String[] col : table) {
                        reportWriter.write(String.format("%s", col[i]));
                    }
                    reportWriter.write("\n");
            }
            
            if(errorFiles != null) {
                if(errorFiles.size() > 0) {
                    reportWriter.write("\nEpäonnistuneet sivut:\n");
                    for(PdfFileEntry errFile : errorFiles) {
                        reportWriter.write(String.format("%s Sivut: %s\n"
                                , errFile.getName()
                                , errFile.getPages().stream()
                                .filter(v -> v.getPageColor() == PageColor.ERROR)
                                .map(v -> "" + v.getPageNumber())
                                .collect(Collectors.joining(","))));
                    }
                }
            }
            
            if(otherFiles.size() > 0) {
                reportWriter.write("\nMuut tiedostot:\n");
                for(FileEntry fileEntry : otherFiles) {
                    reportWriter.write(String.format("%s\n", fileEntry.getFullname()));
                }
            }
            
            reportWriter.flush();
            reportWriter.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    /**
     * Creates a text file for all the errors that
     * happened during runtime.
     * 
     * Writes names of the files that failed.
     * Writes all the exception messages.
     * 
     * @param path Full path to the error log file
     */
    private void writeErrorLogs(String path)
    {
        try {
            BufferedWriter reportWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File(path + ".txt"), false), StandardCharsets.UTF_8));
            
            reportWriter.write("Epäonnistuneet tiedostot:\n");
            
            for(String key : this.errorLogs.keySet()) {
                reportWriter.write(String.format("%s\n", key));
            }
            
            reportWriter.write("\n");
            
            for(String key : this.errorLogs.keySet()) {
                reportWriter.write(String.format("%s:\n", key));
                reportWriter.write(String.format("%s\n\n", this.errorLogs.get(key)));
            }
            
            reportWriter.flush();
            reportWriter.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Creates three different reports from the files.
     * Simple report: Contains minimal amount of information
     * Extented report: Little more specific than simple report
     * Detailed report: Csv file that contains all the collected information
     * 
     * @param path Path to destination file
     * @param reportName Name of the report
     */
    public void build(String path, String reportName) {
        writeSimpleReport(path + reportName);
        writeExtentedReport(path + reportName + "_extented");
        writeDetailedReport(path + reportName + "_pageDetails");
        
        System.out.println("Raportti luotu onnistuneesti!");
        System.out.println(path + reportName + ".txt");
        System.out.println(path + reportName + "_extented.txt");
        System.out.println(path + reportName + "_pageDetails.csv");
        
        if(this.errorLogs.size() > 0) {
            System.out.println("Joitain tiedostoja ei voitu käsitellä.");
            System.out.println("Tiedot tallennettu:");
            writeErrorLogs(path + reportName + "_errorLogs");
            System.out.println(path + reportName + "_errorLogs.txt");
        }
        
        debugUtils.writeLogs(path + "debug_log");
    }
}
