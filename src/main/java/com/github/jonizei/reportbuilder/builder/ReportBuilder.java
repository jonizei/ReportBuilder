/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.builder;

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
import java.util.List;
import java.util.stream.Collectors;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;

/**
 *
 * @author Joni
 */
public class ReportBuilder {
    
    private PaperSizeLibrary sizeLibrary;
    private PdfPageCropper pageCropper;
    private boolean ignoreColor;
    
    public enum PageColor {
        ALL
        , ERROR
        , GRAYSCALE
        , COLORED
    }
    
    private int heightThreshold;
    private int widthThreshold;
    
    private static int CROP_MARGIN = 0;
    
    private List<FileEntry> fileEntries;
    
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
    }
    
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
                    pdfFileEntry = new PdfFileEntry(fileEntry.getPath(), fileEntry.getName(), fileEntry.getExtension());
                    processAllPdfPages(file, pdfFileEntry);
                    fileEntries.add(pdfFileEntry);
                }
                else {
                    fileEntries.add(fileEntry);
                }
                
                currentFile++;
                System.out.println(String.format("Tiedostoja käsitelty: %d/%d", currentFile, maxFiles));
            }
        }
        
    }
    
    private Image[] pdfDocumentToImages(File pdfFile) throws FileNotFoundException, IOException {
        PDFDocument document = new PDFDocument();
        document.load(pdfFile);
        SimpleRenderer renderer = new SimpleRenderer();
        renderer.setResolution(72);
        List<Image> images = new ArrayList<>();
        try {
            images = renderer.render(document);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        Image[] arrImages = new Image[images.size()];
        return images.toArray(arrImages);
    }
    
    private void processAllPdfPages(File originalFile, PdfFileEntry fileEntry) throws IOException {
        
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
    
    private String[] getArrayOfAnnotations(PDPage page) throws IOException {
        List<PDAnnotation> anList = page.getAnnotations();
        return anList.stream().map(v -> v.getSubtype())
                .toArray(String[]::new);
    }
    
    private PageColor getPageColor(BufferedImage img) {
       
        int[] pxArray = imageToIntArray(img);
        boolean isGrayscale = processPxArray(pxArray);
        img.flush();
        
        return isGrayscale ? PageColor.GRAYSCALE : PageColor.COLORED;
    }
    
    private boolean processPxArray(int[] pxArray) {
        
        boolean isGrayscale = true;
        
        for(int i = 0; i < pxArray.length && isGrayscale; i+=5) {
            if(!Utilities.isGrayscale(new Color(pxArray[i]))) {
                isGrayscale = false;
            }
        }
        
        return isGrayscale;
    }
    
    private int[] imageToIntArray(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        int[] pxArray = img.getRGB(0, 0, width, height, null, 0, width);
        List<Integer> pxList = Arrays.stream(pxArray).boxed().collect(Collectors.toList());
        Collections.shuffle(pxList);
        pxArray = pxList.stream().mapToInt(i -> i).toArray();
        
        return pxArray;
    }
    
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
                    reportWriter.write(String.format("%s", fileEntry.getFullname()));
                }
            }
            
            reportWriter.flush();
            reportWriter.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void build(String path, String reportName) {
        writeSimpleReport(path + reportName);
        writeExtentedReport(path + reportName + "_extented");
        writeDetailedReport(path + reportName + "_pageDetails");
        System.out.println("Raportti luotu onnistuneesti!");
        System.out.println(path + reportName + ".txt");
        System.out.println(path + reportName + "_extented.txt");
        System.out.println(path + reportName + "_pageDetails.csv");
    }
}
