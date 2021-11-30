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
 *
 * @author Joni
 */
public class PdfPageCropper {
    
    private static final int LEFT = 0, RIGHT = 1;
 
    public PdfPageCropper() {
        
    }
    
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
    
    private static boolean isPixelWhite(Color color) {
        return (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255);
    }
    
}
