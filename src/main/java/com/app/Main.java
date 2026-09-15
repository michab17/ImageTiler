package com.app;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        BufferedImage image;

        try {
            image = ImageIO.read(new File("C:\\Users\\micha\\Documents\\Java\\site-screenshot.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int imageHeight = 0;
        int imageWidth = 0;

        if (image != null) {
            imageHeight = image.getHeight();
            imageWidth = image.getWidth();
        }

        System.out.println("This is the image height " + imageHeight);
        System.out.println("This is the image width " + imageWidth);
    }
}