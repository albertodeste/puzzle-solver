package it.ziotob.puzzlesolver.spike;

import it.ziotob.puzzlesolver.model.Piece;
import it.ziotob.puzzlesolver.model.Point;
import it.ziotob.puzzlesolver.services.ImageService;
import it.ziotob.puzzlesolver.services.PieceService;
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
    private final PieceService pieceService = new PieceService();

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

    @Test
    public void shouldInvertBackground() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);

        Assertions.assertThat(piecesPoints).isNotEmpty();

        piecesPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 16737480));
        imageService.writeImage(BASE_PATH_OUT + "single-piece-mask.jpg", image);
    }

    @Test
    public void shouldInvertMultiPiecesBackground() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);

        Assertions.assertThat(piecesPoints).isNotEmpty();

        piecesPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 16737480));
        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-mask.jpg", image);
    }

    @Test
    public void shouldDetectPieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces.size()).isEqualTo(1);

        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        imageService.writeImage(BASE_PATH_OUT + "single-piece-pieces.jpg", image);
    }

    @Test
    public void shouldDetectMultiPieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces.size()).isEqualTo(9);

        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-pieces.jpg", image);
    }
}
