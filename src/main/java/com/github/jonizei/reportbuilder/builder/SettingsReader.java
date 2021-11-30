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
 *
 * @author Joni
 */
public class SettingsReader {
    
    private static int PX_FACTOR = 3;
    
    private String sourcePath;
    private String destinationPath;
    private String reportName;
    private boolean ignoreColor;
    private int heightThreshold;
    private int widthThreshold;
    private int colorThreshold;
    
    private PaperSizeLibrary sizeLibrary;
    
    public SettingsReader() {
        this.sourcePath = "";
        this.destinationPath = "";
        this.reportName = "";
        this.ignoreColor = false;
        this.heightThreshold = 0;
        this.widthThreshold = 0;
        this.colorThreshold = 0;
    }
    
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
                
                sizeLibrary = createPaperSizeLibrary(doc);
                
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            
            return true;
        }
        
        return false;
    }
    
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
    
    private PaperCategory readPaperCategory(Node gategory) {
        
        if(gategory.hasChildNodes()) {
            PaperCategory tempGategory = new PaperCategory();
            NodeList gategoryValues = gategory.getChildNodes();
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
    
    public String getSourcePath() {
        return this.sourcePath;
    }
    
    public String getDestinationPath() {
        return this.destinationPath;
    }
    
    public String getReportName() {
        return this.reportName;
    }
    
    public PaperSizeLibrary getPaperSizeLibrary() {
        return this.sizeLibrary;
    }
    
    public boolean ignoreColor() {
        return this.ignoreColor;
    }
    
    public int getHeightThreshold() {
        return this.heightThreshold;
    }
    
    public int getWidthThreshold() {
        return this.widthThreshold;
    }
    
    public int getColorThreshold() {
        return this.colorThreshold;
    }
    
}
