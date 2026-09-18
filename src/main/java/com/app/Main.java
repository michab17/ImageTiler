package com.app;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "tiler",
        mixinStandardHelpOptions = true,
        version = "0.1",
        description = "Slices a full-page webpage screenshot into AI-readable tiles."
)
public class Main implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the input screenshot (PNG recommended)")
    Path inputImage;

    @Option(names = {"-o", "--output-dir"}, description = "Directory to write tiles into (default: ${DEFAULT-VALUE})")
    Path outputDir = Path.of("output");

    @Option(names = {"-m", "--max-dim"}, description = "Max pixel dimension per tile (default: ${DEFAULT-VALUE})")
    int maxDim = 1568;

    @Option(names = "--overlap", description = "Vertical overlap between tiles, in pixels (default: ${DEFAULT-VALUE})")
    int overlap = 150;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws IOException {
        BufferedImage source = ImageIO.read(inputImage.toFile());
        if (source == null) {
            System.err.println("Could not read image at " + inputImage + " (unsupported format or corrupt file)");
            return 1;
        }

        Files.createDirectories(outputDir);

        List<TileSlicer.Tile> tiles = TileSlicer.slice(source, maxDim, overlap);

        StringBuilder manifest = new StringBuilder("[\n");
        for (int i = 0; i < tiles.size(); i++) {
            TileSlicer.Tile tile = tiles.get(i);
            String filename = String.format("tile-%02d.png", i + 1);
            ImageIO.write(tile.image(), "png", outputDir.resolve(filename).toFile());

            manifest.append("  { \"file\": \"").append(filename)
                    .append("\", \"yOffset\": ").append(tile.yOffset())
                    .append(", \"height\": ").append(tile.height())
                    .append(" }")
                    .append(i < tiles.size() - 1 ? ",\n" : "\n");

            System.out.println("Wrote " + filename + " (y=" + tile.yOffset() + ", h=" + tile.height() + ")");
        }
        manifest.append("]\n");

        Files.writeString(outputDir.resolve("manifest.json"), manifest.toString());
        System.out.println(tiles.size() + " tile(s) written to " + outputDir.toAbsolutePath());

        return 0;
    }
}