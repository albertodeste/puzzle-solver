package it.ziotob.puzzlesolver.services;

import it.ziotob.puzzlesolver.exception.ApplicationException;
import it.ziotob.puzzlesolver.model.Point;
import it.ziotob.puzzlesolver.model.PointsColorGroup;
import lombok.AllArgsConstructor;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor
public class ImageService {

    public static final int DEFAULT_HSV_TOLERANCE = 20;
    @Setter
    private int hsvTolerance;

    public BufferedImage loadImage(String imagePath) {

        try {
            return ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new ApplicationException("Error while loading image", e);
        }
    }

    public List<Point> detectBackground(BufferedImage image) {

        PointsColorGroup group = new PointsColorGroup(hsvTolerance);
        pointsStream(image).forEach(point -> {

            int rgb = image.getRGB(point.getX(), point.getY());
            float hue = Color.RGBtoHSB(red(rgb), green(rgb), blue(rgb), null)[0];
            group.addPoint(point, (int) (hue * 100));
        });

        return group.getBiggestGroup().collect(Collectors.toList());
    }

    private Stream<Point> pointsStream(BufferedImage image) {

        return IntStream.range(0, image.getWidth())
                .mapToObj(x -> IntStream.range(0, image.getHeight())
                        .mapToObj(y -> new Point(x, y)))
                .flatMap(a -> a);
    }

    private int blue(int rgb) {
        return rgb & 0xFF;
    }

    private int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public void writeImage(String imagePath, BufferedImage image) {

        try {
            ImageIO.write(image, "PNG", new File(imagePath));
        } catch (IOException e) {
            throw new ApplicationException("Error while persisting image", e);
        }
    }

    public List<Point> applyMask(BufferedImage image, List<Point> backgroundPoints) {

        boolean[][] points = new boolean[image.getHeight()][image.getWidth()];

        for (Point point : backgroundPoints) {
            points[point.getY()][point.getX()] = true;
        }

        return IntStream.range(0, image.getWidth()).parallel()
                .mapToObj(x -> IntStream.range(0, image.getHeight())
                        .filter(y -> !points[y][x])
                        .mapToObj(y -> new Point(x, y)))
                .flatMap(s -> s)
                .collect(Collectors.toList());
    }
}
