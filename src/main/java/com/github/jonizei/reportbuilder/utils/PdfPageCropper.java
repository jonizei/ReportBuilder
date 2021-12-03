/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jonizei.reportbuilder.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods to crop images
 * 
 * @author Joni
 * @version 2021-12-03
 */
public class PdfPageCropper {
    
    /**
     * Integer values to represent left or right side of an image
     */
    private static final int LEFT = 0, RIGHT = 1;
 
    /**
     * Constructor of PdfPageCropper
     */
    public PdfPageCropper() {
        
    }
    
    /**
     * Crops image as minimum size as possible following the given
     * margin value.
     * 
     * @param pageImg Image to be cropped
     * @param marginMm Crop margin
     * @return Cropped image
     */
    public BufferedImage cropPage(BufferedImage pageImg, int marginMm) {
        
        int marginPx = PaperSizeLibrary.mmToPixel(marginMm);
        int[][] rowCropPoints = new int[pageImg.getHeight()][2];
        
        for(int i = 0; i < pageImg.getHeight(); i++) {
            int[] pixelArray = pageImg.getRGB(0, i, pageImg.getWidth(), 1, null, 0, pageImg.getWidth());
            rowCropPoints[i][LEFT] = findHorizontalCropPoint(pixelArray, false);
            rowCropPoints[i][RIGHT] = findHorizontalCropPoint(pixelArray, true);
        }
        
        int topCropPoint = findVerticalCropPoint(rowCropPoints, false);
        int bottomCropPoint = findVerticalCropPoint(rowCropPoints, true);
        int leftCropPoint = Utilities.arrayMin(getCropPoints(rowCropPoints, topCropPoint, false));
        int rightCropPoint = Utilities.arrayMax(getCropPoints(rowCropPoints, topCropPoint, true));
        
        bottomCropPoint = bottomCropPoint == 0 ? pageImg.getHeight() : bottomCropPoint;
        rightCropPoint = rightCropPoint == 0 ? pageImg.getWidth() : rightCropPoint;
        
        int imgHeight = bottomCropPoint - topCropPoint + 1;
        int imgWidth = rightCropPoint - leftCropPoint + 1;
        
        topCropPoint = topCropPoint - marginPx > 0 ? topCropPoint - marginPx : 0;
        leftCropPoint = leftCropPoint - marginPx > 0 ? leftCropPoint - marginPx : 0;
        imgWidth = imgWidth + marginPx < pageImg.getWidth() ? imgWidth + marginPx : pageImg.getWidth();
        imgHeight = imgHeight + marginPx < pageImg.getHeight() ? imgHeight + marginPx : pageImg.getHeight();
        
        return pageImg.getSubimage(leftCropPoint, topCropPoint, imgWidth, imgHeight);
    }
    
    /**
     * Extracts left or right column from the 2d array based on
     * the boolean "fromRight" and returns single integer array
     * 
     * @param rowCropPoints 2D array holding crop points from left and right side
     * @param startHeight Height where the iteration starts
     * @param fromRight Boolean which tell which column will be extracted from
     * the 2d array
     * @return Column from the 2d array
     */
    private int[] getCropPoints(int[][] rowCropPoints, int startHeight, boolean fromRight) {
        
        List<Integer> cropPoints = new ArrayList<>();
        
        for(int i = startHeight; i < rowCropPoints.length; i++) {
            int point = fromRight ? rowCropPoints[i][RIGHT] : rowCropPoints[i][LEFT];
            if(point > 0) {
                cropPoints.add(point);
            }
        }   
        
        return cropPoints.stream().mapToInt(i->i).toArray();
    }
    
    /**
     * Finds the first non-white pixel vertically
     * 
     * @param rowCropPoints 2D array holding crop points from left and right side
     * @param startEnd Boolean which tells if it starts iteration from the
     * top or from the bottom
     * @return Index of the first non-white pixel vertically
     */
    private int findVerticalCropPoint(int[][] rowCropPoints, boolean startEnd) {
        
        int cropPoint = 0;
        boolean pointFound = false;
        
        if(startEnd) {
            for(int i = rowCropPoints.length - 1; i > -1 && !pointFound; i--) {
                if(rowCropPoints[i][LEFT] > 0 || rowCropPoints[i][RIGHT] > 0) {
                    cropPoint = i;
                    pointFound = true;
                }
            }
        }
        else {
            for(int i = 0; i < rowCropPoints.length && !pointFound; i++) {
                if(rowCropPoints[i][LEFT] > 0 || rowCropPoints[i][RIGHT] > 0) {
                    cropPoint = i;
                    pointFound = true;
                }
            }
        }
        
        return cropPoint;
    }
    
    /**
     * Finds the first non-white pixel horizontally
     * 
     * @param pixelArray Single pixel row
     * @param startEnd Boolean which tells if iteration start from left
     * or from right
     * @return Index of first non-white pixel horizontally
     */
    private int findHorizontalCropPoint(int[] pixelArray, boolean startEnd) {
        
        boolean isWhite = true;
        int cropPoint = 0; 
        
        if(startEnd) {
            for(int i = pixelArray.length - 1; i > -1 && isWhite; i--) {
                isWhite = isPixelWhite(new Color(pixelArray[i]));
                
                if(!isWhite) {
                    cropPoint = i;
                }
            }
        }
        else {
            for(int i = 0; i < pixelArray.length && isWhite; i++) {
                isWhite = isPixelWhite(new Color(pixelArray[i]));
                
                if(!isWhite) {
                    cropPoint = i;
                }
            }
        }
        
        return cropPoint;
    }
    
    /**
     * Checks if pixel is white
     * 
     * @param color Color of the pixel
     * @return Boolean which tells if color is white or not
     */
    private static boolean isPixelWhite(Color color) {
        return (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255);
    }
    
}
