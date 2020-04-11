package it.ziotob.puzzlesolver.model;

import it.ziotob.puzzlesolver.exception.ApplicationException;
import it.ziotob.puzzlesolver.utils.PointUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PieceFactory {

    public static Piece factory(List<Point> points) {

        AbstractMap.SimpleEntry<BigDecimal, BigDecimal> sums = points.parallelStream()
                .map(point -> new AbstractMap.SimpleEntry<>(BigDecimal.valueOf(point.getX()), BigDecimal.valueOf(point.getY())))
                .reduce((a, b) -> new AbstractMap.SimpleEntry<>(a.getKey().add(b.getKey()), a.getValue().add(b.getValue())))
                .orElse(new AbstractMap.SimpleEntry<>(BigDecimal.ZERO, BigDecimal.ZERO));
        BigDecimal midX = sums.getKey().divide(BigDecimal.valueOf(points.size()), 0, RoundingMode.HALF_UP);
        BigDecimal midY = sums.getValue().divide(BigDecimal.valueOf(points.size()), 0, RoundingMode.HALF_UP);

        Point center = new Point(midX.intValue(), midY.intValue());

        List<Point> borderPoints = PointUtils.sortClockwise(sortBorders(detectBorderPoints(points)));
        List<Point> convexHull = PointUtils.sortClockwise(PointUtils.convexHull(borderPoints));
        List<ConvexityDefect> convexityDefects = discardConvexityImperfections(detectConvexityDefects(borderPoints, convexHull));

        return new Piece(points, borderPoints, convexHull, convexityDefects, center);
    }

    private static List<ConvexityDefect> discardConvexityImperfections(List<ConvexityDefect> convexityDefects) {

        List<ConvexityDefect> sortedDefects = convexityDefects.stream()
                .sorted(Comparator.comparingDouble(ConvexityDefect::getDistance).reversed())
                .collect(Collectors.toList());
        int biggestDifference = (int)IntStream.range(0, sortedDefects.size() - 1)
                .mapToDouble(i -> sortedDefects.get(i).getDistance() - sortedDefects.get(i + 1).getDistance())
                .max().orElse(Double.MAX_VALUE);
        int discardIndex = IntStream.range(1, sortedDefects.size())
                .filter(i -> (int)(sortedDefects.get(i - 1).getDistance() - sortedDefects.get(i).getDistance()) == biggestDifference)
                .findFirst().orElse(sortedDefects.size());

        return IntStream.range(0, sortedDefects.size())
                .filter(i -> i < discardIndex)
                .mapToObj(sortedDefects::get)
                .collect(Collectors.toList());
    }

    private static List<ConvexityDefect> detectConvexityDefects(List<Point> borderPoints, List<Point> convexHull) {
        //border points and convexHull are both sorted clockwise around the piece

        List<ConvexityDefect> partialDefects = IntStream.range(0, convexHull.size() - 1)
                .mapToObj(i -> ConvexityDefect.factory(convexHull.get(i), convexHull.get(i + 1)))
                .collect(Collectors.toList());
        int index = IntStream.range(0, borderPoints.size())
                .filter(i -> borderPoints.get(i).equals(partialDefects.get(0).getHullPointA()))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("Unable to find starting point of borders"));
        index = (index + 1) % borderPoints.size();

        Map<ConvexityDefect, List<Point>> map = new HashMap<>();

        while (!partialDefects.isEmpty()) {

            index = (index + 1) % borderPoints.size();
            List<Point> containedPoints = new ArrayList<>();

            while (!borderPoints.get(index).equals(partialDefects.get(0).getHullPointB())) {

                containedPoints.add(borderPoints.get(index));
                index = (index + 1) % borderPoints.size();
            }

            map.put(partialDefects.get(0), containedPoints);
            partialDefects.remove(0);
        }

        return map.entrySet().parallelStream()
                .map(entry -> ConvexityDefect.factory(
                        entry.getKey().getHullPointA(),
                        entry.getKey().getHullPointB(),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    private static List<Point> sortBorders(List<Point> borderPoints) {

        Stack<Point> result = new Stack<>();
        Map<Integer, List<Integer>> matrix = new HashMap<>();
        int points = borderPoints.size();
        borderPoints.forEach(point -> {

            if (!matrix.containsKey(point.getX())) {
                matrix.put(point.getX(), new ArrayList<>());
            }
            matrix.get(point.getX()).add(point.getY());
        });

        Optional<Point> point = Optional.of(borderPoints.get(0));

        while (points > 0) {

            if (point.isPresent()) {

                matrix.get(point.get().getX()).remove(point.get().getY());
                result.push(point.get());
                points--;

                point = detectNextPoint(matrix, point.get());
            } else {

                result.pop();
                point = Optional.of(result.pop());
            }
        }

        return result;
    }

    private static Optional<Point> detectNextPoint(Map<Integer, List<Integer>> matrix, Point point) {

        return Stream.of(
                new Point(point.getX(), point.getY() - 1),
                new Point(point.getX() - 1, point.getY() - 1),
                new Point(point.getX() - 1, point.getY()),
                new Point(point.getX() - 1, point.getY() + 1),
                new Point(point.getX(), point.getY() + 1),
                new Point(point.getX() + 1, point.getY() + 1),
                new Point(point.getX() + 1, point.getY()),
                new Point(point.getX() + 1, point.getY() - 1)
        )
                .filter(p -> matrix.containsKey(p.getX()) && matrix.get(p.getX()).contains(p.getY()))
                .findFirst();
    }

    private static List<Point> detectBorderPoints(List<Point> points) {

        PointsGroup group = new PointsGroup();
        points.forEach(group::addPoint);

        return points.stream()
                .filter(point -> group.findClosePoints(point).size() < 4)
                .collect(Collectors.toList());
    }
}
