package com.app;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TileSlicer {

    /** One vertical slice of the source image, with its position in the original. */
    public record Tile(BufferedImage image, int yOffset, int height) {}

    /**
     * Slices {@code source} into vertical tiles no taller than {@code maxDim}, with
     * {@code overlap} pixels shared between consecutive tiles so elements sitting on
     * a tile boundary (a line of text, a button) don't get cut in half with no
     * readable copy on either side.
     */
    public static List<Tile> slice(BufferedImage source, int maxDim, int overlap) {
        if (overlap >= maxDim) {
            throw new IllegalArgumentException("overlap (" + overlap + ") must be smaller than maxDim (" + maxDim + ")");
        }

        int width = source.getWidth();
        int totalHeight = source.getHeight();

        if (width > maxDim) {
            System.err.println("Warning: image width (" + width + "px) exceeds max-dim (" + maxDim
                    + "px). Tiles keep the full width, so their long edge will exceed the requested max.");
        }

        List<Tile> tiles = new ArrayList<>();
        int step = maxDim - overlap;
        int y = 0;

        while (y < totalHeight) {
            int tileHeight = Math.min(maxDim, totalHeight - y);
            // getSubimage shares the source's raster (no pixel copy) - fine here since
            // we write each tile out immediately and don't hold them all in memory at once.
            BufferedImage tileImage = source.getSubimage(0, y, width, tileHeight);
            tiles.add(new Tile(tileImage, y, tileHeight));

            if (y + tileHeight >= totalHeight) {
                break; // just wrote the last (possibly shorter) tile
            }
            y += step;
        }

        return tiles;
    }
}