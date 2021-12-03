/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.builder;

/**
 * This file contains minimal information of a file.
 * This class represents a file in the given source folder.
 * 
 * @author Joni
 * @version 2021-12-03
 */
public class FileEntry {
    
    /**
     * A path to the file
     */
    private String path;
    
    /** 
     * Name of the file
     */
    private String name;
    
    /**
     * File extension
     */
    private String extension;
    
    /**
     * Constructor that takes file path and filename 
     * as parameters and splits filename to a name and a file extension
     * 
     * @param path
     * @param filename 
     */
    public FileEntry(String path, String filename) {
        String[] tokens = parseFilename(filename);
        setPath(path);
        setName(tokens[0]);
        setExtension(tokens[1]);
    }
    
    /** 
     * Constructor that takes file path, filename and file extension
     * as parameters
     * 
     * @param path
     * @param name
     * @param extension 
     */
    public FileEntry(String path, String name, String extension) {
        setPath(path);
        setName(name);
        setExtension(extension);
    }
    
    /**
     * Stores given name to a variable
     * 
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Stores given file extension to a variable
     * 
     * @param extension 
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    /**
     * Stores given file path to a variable
     * 
     * @param path 
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Returns the name variable
     * 
     * @return name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the file extension variable
     * 
     * @return extension
     */
    public String getExtension() {
        return this.extension;
    }
    
    /** 
     * Returns the file path variable
     * 
     * @return path
     */
    public String getPath() {
        return this.path;
    }
    
    /**
     * Returns file name and extension combined
     * to same string
     * 
     * @return 
     */
    public String getFullname() {
        return String.format("%s.%s", getName(), getExtension());
    }
    
    /**
     * Extracts the extension of a file
     * from a given filename
     * 
     * @param filename
     * @return string array that contains file name and file extension
     */
    private String[] parseFilename(String filename) {
        
        String nameToken = filename;
        String extensionToken = "";
        
        if(filename.contains(".")) {
            nameToken = filename.substring(0, filename.lastIndexOf("."));
            extensionToken = filename.substring(filename.lastIndexOf(".") + 1);
        }
        
        return new String[] {nameToken, extensionToken};
    }
   
}
