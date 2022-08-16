package com.github.jonizei.reportbuilder.utils;

public class PaperSize implements Comparable<PaperSize> {

    private PaperCategory heightCategory;
    private PaperCategory widthCategory;

    public PaperSize() {

    }

    public PaperSize(PaperCategory heightCategory, PaperCategory widthCategory) {
        this.heightCategory = heightCategory;
        this.widthCategory = widthCategory;
    }

    public PaperCategory getHeightCategory() {
        return heightCategory;
    }

    public void setHeightCategory(PaperCategory heightCategory) {
        this.heightCategory = heightCategory;
    }

    public PaperCategory getWidthCategory() {
        return widthCategory;
    }

    public void setWidthCategory(PaperCategory widthCategory) {
        this.widthCategory = widthCategory;
    }

    public int getAreaMm() {
        return this.heightCategory.getPaperSizeMm() * this.widthCategory.getPaperSizeMm();
    }

    public int getAreaPx() {
        return this.heightCategory.getPaperSizePx() * this.widthCategory.getPaperSizePx();
    }

    public PaperSize swapped() {
        return new PaperSize(this.widthCategory.clone(), this.heightCategory.clone());
    }

    @Override
    public int compareTo(PaperSize o) {
        return getAreaMm() - o.getAreaMm();
    }

    @Override
    public String toString() {
        return this.heightCategory.getPaperSizeMm() + "x" + this.widthCategory.getPaperSizeMm();
    }
}
