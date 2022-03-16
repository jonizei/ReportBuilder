/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.builder;

import com.github.jonizei.reportbuilder.utils.PaperCategory;
import com.github.jonizei.reportbuilder.utils.PaperSizeLibrary;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class reads the settings.xml file that contains all necessary
 * information for this program
 * 
 * @author Joni
 * @version 2021-12-03
 */
public class SettingsReader {
    
    /**
     * Multiplier for converting millimetres to pixels
     * with 72 dpi resolution
     */
    private static int PX_FACTOR = 3;
    
    /**
     * Path to a source folder
     */
    private String sourcePath;
    
    /**
     * Path to a destination folder
     */
    private String destinationPath;
    
    /**
     * Name of the report
     */
    private String reportName;
    
    /**
     * Boolean that enables or disables color state checking
     */
    private boolean ignoreColor;
    
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
     * Color threshold. Used when checking if pixel is colored or grayscale
     */
    private int colorThreshold;
    
    /**
     * Boolean which tells if debugging is enabled
     */
    private boolean enableDebug;
    
    /**
     * Class that contains all the paper sizes used in this program
     */
    private PaperSizeLibrary sizeLibrary;
    
    /**
     * Constructor of SettingsReader
     */
    public SettingsReader() {
        this.sourcePath = "";
        this.destinationPath = "";
        this.reportName = "";
        this.ignoreColor = false;
        this.heightThreshold = 0;
        this.widthThreshold = 0;
        this.colorThreshold = 0;
        this.enableDebug = false;
    }
    
    /** 
     * Reads given xml file and assigns necessary values to variables
     * 
     * @param path Path to the xml file
     * @return Boolean which tells if xml file was read successfully
     */
    public boolean readXml(String path) {
        
        File xmlFile = new File(path);
        
        if(xmlFile.exists()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(xmlFile);
                doc.getDocumentElement().normalize();

                this.sourcePath = getTagStrContent(doc, "source");
                this.destinationPath = getTagStrContent(doc, "destination");
                this.reportName = getTagStrContent(doc, "reportname");
                String strCheckColor = getTagStrContent(doc, "ignore-color");
                this.ignoreColor = !strCheckColor.toLowerCase().equals("false");
                
                this.heightThreshold = getTagIntContent(doc, "height-threshold");
                this.widthThreshold = getTagIntContent(doc, "width-threshold");
                this.colorThreshold = getTagIntContent(doc, "color-threshold");
                
                String strIsDebug = getTagStrContent(doc, "enable-debug");
                this.enableDebug = strIsDebug.toLowerCase().equals("true");
                
                sizeLibrary = createPaperSizeLibrary(doc);
                
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns string from given xml tag
     * 
     * @param doc Xml document
     * @param tag Name of the xml tag
     * @return String inside xml tag
     */
    private String getTagStrContent(Document doc, String tag) {
        String temp = "";
        
        try {
            temp = doc.getElementsByTagName(tag).item(0).getTextContent();
        } catch(Exception ex) {
            ex.printStackTrace();
            temp = "";
        }
        
        return temp;
    }
    
    /**
     * Returns int from given xml tag
     * 
     * @param doc Xml document
     * @param tag Name of the xml tag
     * @return Integer inside xml tag
     */
    private int getTagIntContent(Document doc, String tag) {
        String tempStr = getTagStrContent(doc, tag);
        int tempInt = 0;
        
        if(tempStr.length() > 0) {
            try {
                tempInt = Integer.parseInt(tempStr);
            } catch(Exception ex) {
                ex.printStackTrace();
                tempInt = 0;
            }
        }
        
        return tempInt;
    }
    
    /**
     * Reads all paper categories from the xml file and saves them 
     * to the PaperSizeLibrary class.
     * 
     * @param doc Xml document
     * @return PaperSizeLibrary instance with all the paper categories
     */
    private PaperSizeLibrary createPaperSizeLibrary(Document doc) {
        
        PaperSizeLibrary tempLibrary = PaperSizeLibrary.getInstance();
        
        Node paperSettings = doc.getElementsByTagName("paper-settings").item(0);
        
        if(paperSettings != null) {
            if(paperSettings.hasChildNodes()) {
                NodeList childNodes = paperSettings.getChildNodes();
                for(int i = 0; i < childNodes.getLength(); i++) {
                    if(childNodes.item(i).getNodeName().equals("paper-categories")) {
                        PaperCategory[] paperHeights = readPaperCategoryList(childNodes.item(i));
                        if(paperHeights != null) {
                            tempLibrary.addPaperHeightArray(paperHeights);
                        }
                    }
                    else if(childNodes.item(i).getNodeName().equals("paper-widths")) {
                        PaperCategory[] paperWidths = readPaperWidths(childNodes.item(i));
                        if(paperWidths != null) {
                            tempLibrary.addPaperWidthArray(paperWidths);
                        }
                    }
                    else if(childNodes.item(i).getNodeName().equals("named-papers")) {
                        PaperSizeLibrary.NamedPaper[] namedPapers = readNamedPaperList(childNodes.item(i));
                        if(namedPapers != null) {
                            tempLibrary.addNamedPaperArray(namedPapers);
                        }
                    }
                }
                
            }
        }
        
        return tempLibrary;
    }
        
    /**
     * Iterates through all the paper categories inside the xml file
     * and saves them to a PaperCategory array.
     * 
     * @param categoryNode paper category parent node
     * @return Array of PaperCategory objects
     */
    private PaperCategory[] readPaperCategoryList(Node categoryNode) {
     
        if(categoryNode.hasChildNodes()) {
        
            List<PaperCategory> tempList = new ArrayList<>();
        
            NodeList paperCategories = categoryNode.getChildNodes();
            for(int i = 0; i < paperCategories.getLength(); i++) {
                Node paperCat = paperCategories.item(i);
                if(paperCat.getNodeName().equals("paper-category")) {
                    PaperCategory category = readPaperCategory(paperCat);
                    if(category != null) {
                        tempList.add(category);
                    }
                }
            } 
        
            PaperCategory[] tempArray = new PaperCategory[tempList.size()];
            return tempList.toArray(tempArray);
        }
        
        return null;
        
    }
    
    /**
     * Gets paper category information from paper category node
     * 
     * @param category Paper category node
     * @return PaperCategory object
     */
    private PaperCategory readPaperCategory(Node category) {
        
        if(category.hasChildNodes()) {
            PaperCategory tempGategory = new PaperCategory();
            NodeList gategoryValues = category.getChildNodes();
            for(int i = 0; i < gategoryValues.getLength(); i++) {
                Node value = gategoryValues.item(i);
                if(value.getNodeName().equals("name")) {
                    tempGategory.setName(value.getTextContent());
                }
                else if(value.getNodeName().equals("height")) {
                    String strHeight = value.getTextContent();
                    if(!strHeight.equals("")) {
                        int height = Integer.parseInt(strHeight);
                        tempGategory.setPaperSizeMm(height);
                        tempGategory.setPaperSizePx(PaperSizeLibrary.mmToPixel(height));
                    }
                }
            }
            
            return tempGategory;
        }
        
        return null;
    }
    
    /**
     * Gets all paper width categories from the xml
     * 
     * @param paperWidthsNode paper widths parent node
     * @return Array of PaperCategory objects
     */
    private PaperCategory[] readPaperWidths(Node paperWidthsNode) {
     
        if(paperWidthsNode.hasChildNodes()) {
            List<PaperCategory> tempList = new ArrayList<>();

            NodeList paperWidths = paperWidthsNode.getChildNodes();
            for(int i = 0; i < paperWidths.getLength(); i++) {
                
                if(paperWidths.item(i).getNodeName().equals("paper-width")) {
                    String strWidth = paperWidths.item(i).getTextContent();
                    if(!strWidth.equals("")) {
                        int width = Integer.parseInt(strWidth);
                        tempList.add(new PaperCategory("" + width, width, PaperSizeLibrary.mmToPixel(width), false));
                    }
                }
                
            }

            PaperCategory[] tempArray = new PaperCategory[tempList.size()];
            return tempList.toArray(tempArray);
        }
       
        return null;
    }
    
    /**
     * Gets array of paper size names from the xml
     * 
     * @param namedPaperNodes named papers parent node
     * @return Array of NamedPaper objects
     */
    private PaperSizeLibrary.NamedPaper[] readNamedPaperList(Node namedPaperNodes) {
        
        if(namedPaperNodes.hasChildNodes()) {
            List<PaperSizeLibrary.NamedPaper> tempList = new ArrayList<>();
            
            NodeList namedPapers = namedPaperNodes.getChildNodes();
            for(int i = 0; i < namedPapers.getLength(); i++) {
                if(namedPapers.item(i).getNodeName().equals("paper")) {
                    tempList.add(readNamedPaper(namedPapers.item(i)));
                }
            }
            
            PaperSizeLibrary.NamedPaper[] tempArray = new PaperSizeLibrary.NamedPaper[tempList.size()];
            return tempList.toArray(tempArray);
        }
        
        return null;
    }
    
    /**
     * Gets paper name information from named paper node
     * 
     * @param paper named paper node
     * @return NamedPaper object
     */
    private PaperSizeLibrary.NamedPaper readNamedPaper(Node paper) {
        
        if(paper.hasChildNodes()) {
            PaperSizeLibrary.NamedPaper tempPaper = PaperSizeLibrary.getInstance().new NamedPaper();
            NodeList paperValues = paper.getChildNodes();
            for(int i = 0; i < paperValues.getLength(); i++) {
                
                Node childNode = paperValues.item(i);
                if(childNode.getNodeName().equals("name")) {
                    tempPaper.setName(childNode.getTextContent());
                }
                else if(childNode.getNodeName().equals("height")) {
                    if(!childNode.getTextContent().equals("")) {
                        int height = Integer.parseInt(childNode.getTextContent());
                        tempPaper.setHeightMm(height);
                    }
                }
                else if(childNode.getNodeName().equals("width")) {
                    if(!childNode.getTextContent().equals("")) {
                        int width = Integer.parseInt(childNode.getTextContent());
                        tempPaper.setWidthMm(width);
                    }
                }
                
            }
            
            return tempPaper;
        }
        
        return null;
    }
    
    /**
     * Returns path to the source folder
     * 
     * @return Path to the source folder
     */
    public String getSourcePath() {
        return this.sourcePath;
    }
    
    /**
     * Returns path to the destination folder
     * 
     * @return Path to the destination folder
     */
    public String getDestinationPath() {
        return this.destinationPath;
    }
    
    /**
     * Returns the name of the report
     * 
     * @return Name of the report
     */
    public String getReportName() {
        return this.reportName;
    }
    
    /**
     * Returns paper size library
     * 
     * @return PaperSizeLibrary object
     */
    public PaperSizeLibrary getPaperSizeLibrary() {
        return this.sizeLibrary;
    }
    
    /**
     * Returns ignoreColor boolean which tells if color checking
     * is enabled or disabled
     * 
     * @return Boolean for color checking
     */
    public boolean ignoreColor() {
        return this.ignoreColor;
    }
    
    /**
     * Returns height threshold value
     * 
     * @return Height threshold
     */
    public int getHeightThreshold() {
        return this.heightThreshold;
    }
    
    /**
     * Returns width threshold value
     * 
     * @return Width threshold
     */
    public int getWidthThreshold() {
        return this.widthThreshold;
    }
    
    /**
     * Returns color threshold value
     * 
     * @return Color threshold
     */
    public int getColorThreshold() {
        return this.colorThreshold;
    }
    
    /**
     * Returns enable debug value
     * 
     * @return enable debug boolean
     */
    public boolean getEnableDebug() {
        return this.enableDebug;
    }
    
}
