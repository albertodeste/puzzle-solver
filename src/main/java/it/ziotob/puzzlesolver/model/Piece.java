package it.ziotob.puzzlesolver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class Piece {

    private final List<Point> points;
    private final List<Point> borderPoints;
    private final List<Point> hullPoints;
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

        return new Piece(points, borderPoints, convexHull(borderPoints), center);
    }

    private static List<Point> detectBorderPoints(List<Point> points) {

        PointsGroup group = new PointsGroup();
        points.forEach(group::addPoint);

        return points.stream()
                .filter(point -> group.findClosePoints(point).size() < 4)
                .collect(Collectors.toList());
    }

    public static long cross(Point O, Point A, Point B) {
        return (A.getX() - O.getX()) * (long) (B.getY() - O.getY()) - (A.getY() - O.getY()) * (long) (B.getX() - O.getX());
    }

    public static List<Point> convexHull(List<Point> P) {

        if (P.size() > 1) {
            int n = P.size(), k = 0;
            Point[] H = new Point[2 * n];

            P = P.stream()
                    .sorted((a, b) -> a.getX().equals(b.getX()) ?
                            (a.getY().compareTo(b.getY())) :
                            (a.getX().compareTo(b.getX())))
                    .collect(Collectors.toList());

            // Build lower hull
            for (Point point : P) {
                while (k >= 2 && cross(H[k - 2], H[k - 1], point) <= 0)
                    k--;
                H[k++] = point;
            }

            // Build upper hull
            for (int i = n - 2, t = k + 1; i >= 0; i--) {
                while (k >= t && cross(H[k - 2], H[k - 1], P.get(i)) <= 0)
                    k--;
                H[k++] = P.get(i);
            }
            if (k > 1) {
                H = Arrays.copyOfRange(H, 0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
            }
            return Arrays.asList(H);
        } else {
            return P;
        }
    }

    public int getSize() {
        return points.size();
    }
}
