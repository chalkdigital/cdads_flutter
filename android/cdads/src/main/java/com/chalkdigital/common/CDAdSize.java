package com.chalkdigital.common;



/**
 * Created by arungupta on 07/12/16.
 */

public class CDAdSize {

    private int height;
    private int width;

    public CDAdSize(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public enum CDAdSizeConstant {
        /**
         *For Banner Size 300 x 50
         */
        CDAdSize300X50,

        /**
         *For Banner Size 320 x 50
         */
        CDAdSize320X50,

        /**
         * For Banner Size 300 x 250
         */
        CDAdSize300X250,

        /**
         * For Banner Size 320 x 100
         */
        CDAdSize320X100,

       /**
        * For Banner Size 728 x 90
        */
        CDAdSize728X90,
        /**
         * For Banner Size 728 x 250
         */
        CDAdSize728X250,
        /**
        /**
         * For Banner Size 320 x 480
         */
        CDAdSize320X480,
        /**
         * For Banner Size 768 x 1024
         */
        CDAdSize768X1024
    }

    /**
     *Get size from CDAdSizeConstant
     *@param CDAdSizeConstant a constant enum value of type CDAdSizeConstant
     *@return CDAdSize
     */

    public static CDAdSize getSizeFromCDSizeConstant(CDAdSizeConstant CDAdSizeConstant){
        switch (CDAdSizeConstant){
            case CDAdSize300X50:
                return new CDAdSize(300, 50);
            case CDAdSize320X50:
                return new CDAdSize(320, 50);
            case CDAdSize300X250:
                return new CDAdSize(300, 250);
            case CDAdSize320X100:
                return new CDAdSize(320, 100);
            case CDAdSize728X90:
                return new CDAdSize(728, 90);
            case CDAdSize728X250:
                return new CDAdSize(728, 250);
            case CDAdSize320X480:
                return new CDAdSize(320, 480);
            case CDAdSize768X1024:
                return new CDAdSize(768, 1024);
            default :
                return new CDAdSize(300, 50);
        }

    }

}
