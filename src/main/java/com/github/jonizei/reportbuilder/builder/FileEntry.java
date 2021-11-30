/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.builder;

/**
 *
 * @author Joni
 */
public class FileEntry {
    
    private String path;
    private String name;
    private String extension;
    
    public FileEntry(String path, String filename) {
        String[] tokens = parseFilename(filename);
        setPath(path);
        setName(tokens[0]);
        setExtension(tokens[1]);
    }
    
    public FileEntry(String path, String name, String extension) {
        setPath(path);
        setName(name);
        setExtension(extension);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getExtension() {
        return this.extension;
    }
    
    public String getPath() {
        return this.path;
    }
    
    public String getFullname() {
        return String.format("%s.%s", getName(), getExtension());
    }
    
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
