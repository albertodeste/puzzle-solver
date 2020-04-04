package it.ziotob.puzzlesolver.spike;

import it.ziotob.puzzlesolver.model.Point;
import it.ziotob.puzzlesolver.services.ImageService;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.List;

public class SpikeTests {

    private static final String BASE_PATH = "src/test/resources/spike/";
    private static final String BASE_PATH_OUT = "src/test/resources/spike/out/";
    private static final String IMAGE_SINGLE_PIECE = "single-piece.jpg";
    private static final String IMAGE_SINGLE_PIECE_2 = "single-piece-2.jpg";
    private static final String IMAGE_MULTI_PIECES = "multi-pieces.jpg";

    private final ImageService imageService = new ImageService(ImageService.DEFAULT_HSV_TOLERANCE);

    @Test
    public void shouldLoadImage() {

        String imagePath = BASE_PATH + IMAGE_SINGLE_PIECE;
        BufferedImage image = imageService.loadImage(imagePath);

        Assertions.assertThat(image).isNotNull();
    }

    @Test
    public void shouldRemoveImageBackground() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);

        Assertions.assertThat(backgroundPoints.size()).isGreaterThan((image.getWidth() * image.getHeight()) / 2);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0));
        imageService.writeImage(BASE_PATH_OUT + "single-piece-noback.jpg", image);
    }

    @Test
    public void shouldRemoveImageBackground2() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE_2);
        imageService.setHsvTolerance(5);
        List<Point> backgroundPoints = imageService.detectBackground(image);

        Assertions.assertThat(backgroundPoints.size()).isGreaterThan((image.getWidth() * image.getHeight()) / 2);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0));
        imageService.writeImage(BASE_PATH_OUT + "single-piece-2-noback.jpg", image);
    }

    @Test
    public void shouldRemoveImageBackgroundOnMultiPieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);

        Assertions.assertThat(backgroundPoints.size()).isGreaterThan((image.getWidth() * image.getHeight()) / 2);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0));
        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-noback.jpg", image);
    }
}
