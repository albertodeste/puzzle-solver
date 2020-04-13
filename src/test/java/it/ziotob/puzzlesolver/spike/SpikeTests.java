package it.ziotob.puzzlesolver.spike;

import it.ziotob.puzzlesolver.model.Piece;
import it.ziotob.puzzlesolver.model.Point;
import it.ziotob.puzzlesolver.services.ImageService;
import it.ziotob.puzzlesolver.services.PieceService;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.awt.*;
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
        imageService.writeImage(BASE_PATH_OUT + "single-piece-noback.png", image);
    }

    @Test
    public void shouldRemoveImageBackground2() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE_2);
        imageService.setHsvTolerance(5);
        List<Point> backgroundPoints = imageService.detectBackground(image);

        Assertions.assertThat(backgroundPoints.size()).isGreaterThan((image.getWidth() * image.getHeight()) / 2);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0));
        imageService.writeImage(BASE_PATH_OUT + "single-piece-2-noback.png", image);
    }

    @Test
    public void shouldRemoveImageBackgroundOnMultiPieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);

        Assertions.assertThat(backgroundPoints.size()).isGreaterThan((image.getWidth() * image.getHeight()) / 2);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0));
        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-noback.png", image);
    }

    @Test
    public void shouldInvertBackground() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);

        Assertions.assertThat(piecesPoints).isNotEmpty();

        piecesPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 16737480));
        imageService.writeImage(BASE_PATH_OUT + "single-piece-mask.png", image);
    }

    @Test
    public void shouldInvertMultiPiecesBackground() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);

        Assertions.assertThat(piecesPoints).isNotEmpty();

        piecesPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 16737480));
        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-mask.png", image);
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
        imageService.writeImage(BASE_PATH_OUT + "single-piece-pieces.png", image);
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
        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-pieces.png", image);
    }

    @Test
    public void shouldDetectPieceBorders() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> !piece.getBorderPoints().isEmpty());

        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.forEach(piece -> piece.getBorderPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 0x00FF0000)));

        /*
        Point center = pieces.get(0).getCenter();
        List<Point> points = pieces.get(0).getBorderPoints();
        Map<Point, Integer> distances = points.stream()
                .collect(Collectors.toMap(point -> point, point -> point.distance(center)));

        AbstractMap.SimpleEntry<Integer, Integer> minMax = distances.values().stream()
                .map(val -> new AbstractMap.SimpleEntry<>(val, val))
                .reduce(new AbstractMap.SimpleEntry<>(Integer.MAX_VALUE, Integer.MIN_VALUE),
                        (prev, cur) -> new AbstractMap.SimpleEntry<>(Math.min(prev.getKey(), cur.getKey()), Math.max(prev.getValue(), cur.getValue())));

        distances.forEach((point, distance) -> image.setRGB(point.getX(), point.getY(), getColor(distance, minMax.getKey(), minMax.getValue())));
        image.setRGB(center.getX(), center.getY(), 0x00FF0000);
         */

        imageService.writeImage(BASE_PATH_OUT + "single-piece-borders.png", image);
    }

    private int getColor(int distance, int minDistance, int maxDistance) {

        int percentage = (int) (((distance - minDistance) / (float) (maxDistance - minDistance)) * 100);

        int hueMaxVal = 75;
        float hue = (float) (hueMaxVal / 100.0) * percentage;

        return Color.HSBtoRGB((float) (hue / 100.0), (float) 0.75, (float) 0.75);
    }

    @Test
    public void shouldDetectMultiPieceBorders() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> !piece.getBorderPoints().isEmpty());

        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.forEach(piece -> piece.getBorderPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 0x00FF0000)));
        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-borders.png", image);
    }

    @Test
    public void shouldDetectPieceHullPoints() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> !piece.getHullPoints().isEmpty());

        backgroundPoints.forEach(point ->
                image.setRGB(point.getX(), point.getY(), 0X00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.forEach(piece -> piece.getHullPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 0x000000FF)));

        imageService.writeImage(BASE_PATH_OUT + "single-piece-hull.png", image);
    }

    @Test
    public void shouldDetectMultiPiecesHullPoints() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> !piece.getHullPoints().isEmpty());

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.forEach(piece -> piece.getBorderPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 0X0000FF00)));
        pieces.forEach(piece -> piece.getHullPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 0x000000FF)));
        pieces.forEach(piece ->
                image.setRGB(piece.getBorderPoints().get(0).getX(), piece.getBorderPoints().get(0).getY(), 0X00FF0000));

        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-hull.png", image);
    }

    @Test
    public void shouldDetectConvexityDefects() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> piece.getConvexityDefects().size() == 6);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.forEach(piece -> piece.getConvexityDefects().forEach(convexityDefect -> {

            image.setRGB(convexityDefect.getHullPointA().getX(), convexityDefect.getHullPointA().getY(), 0x000000FF);
            image.setRGB(convexityDefect.getHullPointB().getX(), convexityDefect.getHullPointB().getY(), 0x000000FF);
            image.setRGB(convexityDefect.getDeepestPoint().getX(), convexityDefect.getDeepestPoint().getY(), 0x00FF0000);
        }));

        imageService.writeImage(BASE_PATH_OUT + "single-piece-convexity.png", image);
    }

    @Test
    public void shouldDetectConvexityDefectsOnMultiplePieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> !piece.getConvexityDefects().isEmpty());

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.forEach(piece -> piece.getConvexityDefects().forEach(convexityDefect -> {

            image.setRGB(convexityDefect.getHullPointA().getX(), convexityDefect.getHullPointA().getY(), 0x000000FF);
            image.setRGB(convexityDefect.getHullPointB().getX(), convexityDefect.getHullPointB().getY(), 0x000000FF);
            image.setRGB(convexityDefect.getDeepestPoint().getX(), convexityDefect.getDeepestPoint().getY(), 0x00FF0000);
        }));

        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-convexity.png", image);
    }

    @Test
    public void shouldDetectOuterLocksOnSinglePiece() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> piece.getOuterLocks().size() == 2);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.stream().flatMap(piece -> piece.getOuterLocks().stream())
                .flatMap(outerLock -> outerLock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FF0000));

        imageService.writeImage(BASE_PATH_OUT + "single-piece-outer-locks.png", image);
    }

    @Test
    public void shouldDetectOuterLocksOnMultiPieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.stream().flatMap(piece -> piece.getOuterLocks().stream())
                .flatMap(outerLock -> outerLock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FF0000));

        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-outer-locks.png", image);
    }

    @Test
    public void shouldDetectInnerLocksOnSinglePiece() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        Assertions.assertThat(pieces)
                .allMatch(piece -> piece.getInnerLocks().size() == 2);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.stream().flatMap(piece -> piece.getInnerLocks().stream())
                .flatMap(lock -> lock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x0000FF00));

        imageService.writeImage(BASE_PATH_OUT + "single-piece-inner-locks.png", image);
    }

    @Test
    public void shouldDetectInnerLocksOnMultiplePieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.stream().flatMap(piece -> piece.getInnerLocks().stream())
                .flatMap(lock -> lock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x0000FF00));

        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-inner-locks.png", image);
    }

    @Test
    public void shouldDetectLocksOnSinglePiece() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_SINGLE_PIECE);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.stream().flatMap(piece -> piece.getInnerLocks().stream())
                .flatMap(lock -> lock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x0000FF00));
        pieces.stream().flatMap(piece -> piece.getOuterLocks().stream())
                .flatMap(lock -> lock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FF0000));

        imageService.writeImage(BASE_PATH_OUT + "single-piece-locks.png", image);
    }

    @Test
    public void shouldDetectLocksOnMultiplePieces() {

        BufferedImage image = imageService.loadImage(BASE_PATH + IMAGE_MULTI_PIECES);
        List<Point> backgroundPoints = imageService.detectBackground(image);
        List<Point> piecesPoints = imageService.applyMask(image, backgroundPoints);
        List<Piece> pieces = pieceService.detectPieces(piecesPoints);

        backgroundPoints.forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FFFFFF));
        pieces.forEach(piece -> piece.getPoints().forEach(point ->
                image.setRGB(point.getX(), point.getY(), 16737480)));
        pieces.stream().flatMap(piece -> piece.getInnerLocks().stream())
                .flatMap(lock -> lock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x0000FF00));
        pieces.stream().flatMap(piece -> piece.getOuterLocks().stream())
                .flatMap(lock -> lock.getPoints().stream())
                .forEach(point -> image.setRGB(point.getX(), point.getY(), 0x00FF0000));

        imageService.writeImage(BASE_PATH_OUT + "multi-pieces-locks.png", image);
    }
}
