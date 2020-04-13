package it.ziotob.puzzlesolver.model;

import it.ziotob.puzzlesolver.exception.ApplicationException;
import it.ziotob.puzzlesolver.utils.PointUtils;
import javafx.util.Pair;

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
        convexityDefects = sortClockwise(convexityDefects, borderPoints);
        List<OuterLock> outerLocks = detectOuterLocks(convexityDefects, borderPoints, points);
        List<ConvexityDefect> convexityDefectsNoOuterLocks = excludeOuterLocks(convexityDefects, outerLocks);
        List<InnerLock> innerLocks = detectInnerLocks(convexityDefectsNoOuterLocks, borderPoints, points);

        return new Piece(points, borderPoints, convexHull, convexityDefects, center, outerLocks, innerLocks);
    }

    private static List<InnerLock> detectInnerLocks(List<ConvexityDefect> convexityDefects, List<Point> borderPoints, List<Point> points) {

        List<Point> pointsNegative = PointUtils.negative(points);

        return convexityDefects.stream()
                .map(convexityDefect -> detectInnerLock(convexityDefect, borderPoints, pointsNegative))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<InnerLock> detectInnerLock(ConvexityDefect convexityDefect, List<Point> borderPoints, List<Point> externalPoints) {

        //TODO check, A and B might need to be reversed
        Pair<Point, Point> closestHullPoints = bestMinHullPoints(convexityDefect.getHullPointA(), convexityDefect.getHullPointB(), borderPoints);
        List<Point> perimeter = Stream.concat(
                getClockwisePerimeterBetween(borderPoints, closestHullPoints.getKey(), closestHullPoints.getValue()).stream(),
                PointUtils.segmentBetween(closestHullPoints.getKey(), closestHullPoints.getValue()).stream())
                .distinct().collect(Collectors.toList());

        List<Point> area = extractArea(perimeter, externalPoints);

        BigDecimal circularityRate = BigDecimal.valueOf(area.size()).divide(BigDecimal.valueOf(perimeter.size()).pow(2), 5, RoundingMode.HALF_UP);
        BigDecimal epsilon = new BigDecimal("0.018");
        BigDecimal referenceValue = new BigDecimal("0.08");

        if (circularityRate.subtract(referenceValue).abs().compareTo(epsilon) <= 0) {
            return Optional.of(new InnerLock(perimeter, area, convexityDefect));
        } else {
            return Optional.empty();
        }
    }

    private static Pair<Point, Point> bestMinHullPoints(Point hullPointA, Point hullPointB, List<Point> perimeter) {
        //Assume hullPointA is clockwise to hullPointB

        double distance = PointUtils.getDistance(hullPointA, hullPointB);
        double oldDistance = distance;

        int index = 0;
        while (!perimeter.get(index).equals(hullPointA)) {
            index = (index + 1) % perimeter.size();
        }

        while ((distance = PointUtils.getDistance(perimeter.get(index), hullPointB)) <= oldDistance) {

            oldDistance = distance;
            index = (index + 1) % perimeter.size();
        }
        Point bestA = perimeter.get(index);
        oldDistance = distance;

        while (!perimeter.get(index).equals(hullPointB)) {
            index = (index + 1) % perimeter.size();
        }

        while ((distance = PointUtils.getDistance(perimeter.get(index), bestA)) <= oldDistance) {

            oldDistance = distance;
            index = (index - 1) < 0 ? perimeter.size() - 1 : index - 1;
        }
        Point bestB = perimeter.get(index);

        return new Pair<>(bestA, bestB);
    }

    private static List<ConvexityDefect> excludeOuterLocks(List<ConvexityDefect> convexityDefects, List<OuterLock> outerLocks) {

        return convexityDefects.stream()
                .filter(convexityLock -> outerLocks.stream()
                        .flatMap(outerLock -> outerLock.getConvexityDefects().stream())
                        .noneMatch(cl -> cl.equals(convexityLock)))
                .collect(Collectors.toList());
    }

    private static List<OuterLock> detectOuterLocks(List<ConvexityDefect> convexityDefects, List<Point> borderPoints, List<Point> points) {

        List<OuterLock> outerLocks = new ArrayList<>();

        for (int i = 1; i <= convexityDefects.size(); i++) {

            Optional<OuterLock> outerLockOpt = detectOuterLock(convexityDefects.get(i - 1), convexityDefects.get(i % convexityDefects.size()), borderPoints, points);
            if (outerLockOpt.isPresent()) {

                outerLocks.add(outerLockOpt.get());
                i++;
            }
        }

        return outerLocks;
    }

    private static Optional<OuterLock> detectOuterLock(ConvexityDefect convexityDefectOne, ConvexityDefect convexityDefectTwo, List<Point> borderPoints, List<Point> points) {

        List<Point> perimeter = Stream.concat(
                getClockwisePerimeterBetween(borderPoints, convexityDefectOne.getDeepestPoint(), convexityDefectTwo.getDeepestPoint()).stream(),
                PointUtils.segmentBetween(convexityDefectOne.getDeepestPoint(), convexityDefectTwo.getDeepestPoint()).stream())
                .distinct().collect(Collectors.toList());

        List<Point> area = extractArea(perimeter, points);
        area.addAll(perimeter);

        BigDecimal circularityRate = BigDecimal.valueOf(area.size()).divide(BigDecimal.valueOf(perimeter.size()).pow(2), 5, RoundingMode.HALF_UP);
        BigDecimal epsilon = new BigDecimal("0.018");
        BigDecimal referenceValue = new BigDecimal("0.08");

        if (circularityRate.subtract(referenceValue).abs().compareTo(epsilon) <= 0) {
            return Optional.of(new OuterLock(perimeter, area, Arrays.asList(convexityDefectOne, convexityDefectTwo)));
        } else {
            return Optional.empty();
        }
    }

    private static List<Point> extractArea(List<Point> perimeter, List<Point> points) {

        List<Point> area = new ArrayList<>();
        Map<Integer, List<Integer>> matrix = new HashMap<>();
        for (Point point : points) {

            if (!matrix.containsKey(point.getX())) {
                matrix.put(point.getX(), new ArrayList<>());
            }
            matrix.get(point.getX()).add(point.getY());
        }
        Set<Point> perimeterSet = new HashSet<>(perimeter);
        List<Point> pointsToCheck = new ArrayList<>();

        Point startPoint = PointUtils.detectInternalPoint(perimeter, points)
                .orElseThrow(() -> new ApplicationException("Unable to find point to start area calculation"));
        pointsToCheck.add(startPoint);

        while (!pointsToCheck.isEmpty()) {

            Point point = pointsToCheck.get(0);
            pointsToCheck.remove(0);
            matrix.get(point.getX()).remove(point.getY());

            area.add(point);
            Stream.of(
                    new Point(point.getX() + 1, point.getY()),
                    new Point(point.getX(), point.getY() + 1),
                    new Point(point.getX() - 1, point.getY()),
                    new Point(point.getX(), point.getY() - 1)
            )
                    .filter(p -> matrix.containsKey(p.getX()) && matrix.get(p.getX()).contains(p.getY()))
                    .filter(p -> !perimeterSet.contains(p))
                    .peek(p -> matrix.get(p.getX()).remove(p.getY()))
                    .forEach(pointsToCheck::add);
        }

        return area;
    }

    private static List<Point> getClockwisePerimeterBetween(List<Point> borderPoints, Point pointA, Point pointB) {

        List<Point> perimeter = new ArrayList<>();

        int i = 0;
        while (!borderPoints.get(i).equals(pointA)) {
            i++;
        }
        while (!borderPoints.get(i).equals(pointB)) {

            i = (i + 1) % borderPoints.size();
            perimeter.add(borderPoints.get(i));
        }
        perimeter.add(pointB);

        return perimeter;
    }

    private static List<ConvexityDefect> sortClockwise(List<ConvexityDefect> convexityDefects, List<Point> borderPoints) {

        Map<Point, ConvexityDefect> elementsToPlace = convexityDefects.stream()
                .collect(Collectors.toMap(ConvexityDefect::getHullPointA, e -> e));
        List<ConvexityDefect> sortedList = new ArrayList<>();

        borderPoints.forEach(point -> {

            if (elementsToPlace.containsKey(point)) {

                sortedList.add(elementsToPlace.get(point));
                elementsToPlace.remove(point);
            }
        });

        return sortedList;
    }

    private static List<ConvexityDefect> discardConvexityImperfections(List<ConvexityDefect> convexityDefects) {

        return convexityDefects.stream().filter(d -> d.getDistance() > 10).collect(Collectors.toList());
        /*
        List<ConvexityDefect> sortedDefects = convexityDefects.stream()
                .sorted(Comparator.comparingDouble(ConvexityDefect::getDistance).reversed())
                .collect(Collectors.toList());
        int biggestDifference = (int) IntStream.range(0, sortedDefects.size() - 1)
                .mapToDouble(i -> sortedDefects.get(i).getDistance() - sortedDefects.get(i + 1).getDistance())
                .max().orElse(Double.MAX_VALUE);
        int discardIndex = IntStream.range(1, sortedDefects.size())
                .filter(i -> (int) (sortedDefects.get(i - 1).getDistance() - sortedDefects.get(i).getDistance()) == biggestDifference)
                .findFirst().orElse(sortedDefects.size());

        return IntStream.range(0, sortedDefects.size())
                .filter(i -> i < discardIndex)
                .mapToObj(sortedDefects::get)
                .collect(Collectors.toList());
         */
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
