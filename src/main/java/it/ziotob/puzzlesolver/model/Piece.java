package it.ziotob.puzzlesolver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class Piece {

    private final List<Point> points;
    private final List<Point> borderPoints;
    private final Point center;

    public static Piece factory(List<Point> points) {

        AbstractMap.SimpleEntry<BigDecimal, BigDecimal> sums = points.parallelStream()
                .map(point -> new AbstractMap.SimpleEntry<>(BigDecimal.valueOf(point.getX()), BigDecimal.valueOf(point.getY())))
                .reduce((a, b) -> new AbstractMap.SimpleEntry<>(a.getKey().add(b.getKey()), a.getValue().add(b.getValue())))
                .orElse(new AbstractMap.SimpleEntry<>(BigDecimal.ZERO, BigDecimal.ZERO));
        BigDecimal midX = sums.getKey().divide(BigDecimal.valueOf(points.size()), 0, RoundingMode.HALF_UP);
        BigDecimal midY = sums.getValue().divide(BigDecimal.valueOf(points.size()), 0, RoundingMode.HALF_UP);

        Point center = new Point(midX.intValue(), midY.intValue());

        List<Point> borderPoints = detectBorderPoints(points);

        return new Piece(points, borderPoints, center);
    }

    private static List<Point> detectBorderPoints(List<Point> points) {
        return Collections.emptyList(); //TODO implement
    }

    public int getSize() {
        return points.size();
    }
}
