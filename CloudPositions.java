/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudtagger;

import java.awt.Point;
import java.util.HashMap;

/**
 *
 * @author Sascha Rasler
 */
public class CloudPositions {

    int cloudHeight;
    int cloudWidth;
    int rows = 4;
    int columns = 5;
    HashMap<Integer, Point> positions;
    int averageTagWidth = 50;
    int averageTagHeight = 20;
    double columnWidth;
    double rowHeight;

    public CloudPositions(int cloudWidth, int cloudHeight, int numTags) {
        this.cloudWidth = cloudWidth;
        this.cloudHeight = cloudHeight;
        double aspectRatio = (double) cloudWidth / (double) cloudHeight;
        //columns = (int) (numTags / Math.floor(aspectRatio));
        //rows = (int) Math.ceil((double) numTags / (double) columns);
        columnWidth = (double) cloudWidth / (double) columns;
        rowHeight = (double) cloudHeight / (double) rows;
        //System.out.println("columns: " + columns);
        //System.out.println("rows: " + rows);
        positions = new HashMap();
        setupPositions(numTags);
    }

    //TODO:
    private void setupPositions(int numPos) {
        int lastRow = rows - 1;
        int lastCol = columns - 1;

        // Position corners (furthest)
        positions.put(0, new Point(0, 0));
        positions.put(1, new Point(lastCol, lastRow));
        positions.put(2, new Point(lastCol, 0));
        positions.put(3, new Point(0, lastRow));
        
        // Tags close to corners:
        positions.put(4, new Point(0, 1));
        positions.put(5, new Point(lastCol, lastRow - 1));
        positions.put(6, new Point(lastCol, 1));
        positions.put(7, new Point(0, lastRow - 1));

        positions.put(8, new Point(1, 0));
        positions.put(9, new Point(lastCol - 1, lastRow));
        positions.put(10, new Point(lastCol - 1, 0));
        positions.put(11, new Point(1, lastRow));

        //position closest tags
        positions.put(12, new Point(2, 0));
        positions.put(13, new Point(1, lastRow - 1));
        positions.put(14, new Point(2, lastRow));
        positions.put(15, new Point(lastCol - 1, lastRow - 1));
    }

    public Point getTagPostion(int order) {
        Point position = positions.get((Integer) order);
        //System.out.println("set position: " + position + " order: " + order);
        Point newPos = new Point();
        newPos.x = (int) (position.x * columnWidth);
        newPos.y = (int) (position.y * rowHeight);
        return newPos;
    }

    public Point getCenterTagPosition() {
        Point newPos = new Point();
        newPos.x = (int) (columns/2 * columnWidth);
        newPos.y = (int) (rows/2 * rowHeight);
        return newPos;
    }

    public void setAverageTagSize(int tagWidth, int tagHeight) {
        averageTagWidth = tagWidth;
        averageTagHeight = tagHeight;
    }
}
