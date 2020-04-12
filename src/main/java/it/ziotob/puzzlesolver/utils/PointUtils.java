package it.ziotob.puzzlesolver.utils;

import it.ziotob.puzzlesolver.exception.ApplicationException;
import it.ziotob.puzzlesolver.model.Point;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PointUtils {

    private static long cross(Point O, Point A, Point B) {
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

    public static float pDistance(Point p, Point segA, Point segB) {

        float A = p.getX() - segA.getX(); // position of point rel one end of line
        float B = p.getY() - segA.getY();
        float C = segB.getX() - segA.getX(); // vector along line
        float E = -(segB.getY() - segA.getY()); // orthogonal vector

        float dot = A * E + B * C;
        float len_sq = E * E + C * C;

        return (float) (Math.abs(dot) / Math.sqrt(len_sq));
        //return dot * dot / len_sq;
    }

    public static List<Point> sortClockwise(List<Point> points) {

        if (!isClockwise(points)) {
            Collections.reverse(points);
        }

        return points;
    }

    private static boolean isClockwise(List<Point> points) {

        Point minX = points.stream().min(Comparator.comparingInt(Point::getX)).orElseThrow(() -> new ApplicationException("Unable to find minX"));
        Point minY = points.stream().min(Comparator.comparingInt(Point::getY)).orElseThrow(() -> new ApplicationException("Unable to find minY"));
        Point maxX = points.stream().max(Comparator.comparingInt(Point::getX)).orElseThrow(() -> new ApplicationException("Unable to find maxX"));
        Point maxY = points.stream().max(Comparator.comparingInt(Point::getY)).orElseThrow(() -> new ApplicationException("Unable to find maxY"));
        List<Integer> path = new ArrayList<>();

        points.forEach(point -> {

            if (point.equals(minY)) {
                path.add(0);
            } else if (point.equals(maxX)) {
                path.add(1);
            } else if (point.equals(maxY)) {
                path.add(2);
            } else if (point.equals(minX)) {
                path.add(3);
            }
        });

        return IntStream.range(0, path.size())
                .map(i -> path.get(i).compareTo(path.get((i + 1) % path.size())))
                .sum() < 0;
    }

    public static Pair<Point, Float> getMostDistantPoint(List<Point> list, Point pointA, Point pointB) {

        return list.stream()
                .map(point -> new Pair<>(point, pDistance(point, pointA, pointB)))
                .max(Comparator.comparingDouble(pair -> (double) pair.getValue()))
                .orElse(new Pair<>(pointB, (float) 0));
    }

    public static List<Point> segmentBetween(Point a, Point b) {

        List<Point> result = new ArrayList<>();
        result.add(a);
        result.add(b);
        Stack<Pair<Point, Point>> pointsToAnalyze = new Stack<>();
        pointsToAnalyze.push(new Pair<>(a, b));

        while(!pointsToAnalyze.isEmpty()) {

            Pair<Point, Point> pair = pointsToAnalyze.pop();

            if (!areClose(pair.getKey(), pair.getValue())) {

                Point center = midPoint(pair.getKey(), pair.getValue());
                result.add(center);
                pointsToAnalyze.push(new Pair<>(pair.getKey(), center));
                pointsToAnalyze.push(new Pair<>(center, pair.getValue()));
            }
        }

        return result;
    }

    private static boolean areClose(Point pointA, Point pointB) {
        return Math.abs(pointA.getX() - pointB.getX()) <= 1 && Math.abs(pointA.getY() - pointB.getY()) <= 1;
    }

    private static Point midPoint(Point a, Point b) {
        return new Point((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
    }

    public static Optional<Point> detectInternalPoint(List<Point> perimeter, List<Point> points) {
        return points.parallelStream().filter(p -> isInside(p, perimeter)).findAny();
    }

    private static boolean isInside(Point point, List<Point> perimeter) {

        List<Point> collisions = perimeter.stream()
                .filter(p -> p.getX().equals(point.getX()) || p.getY().equals(point.getY()))
                .collect(Collectors.toList());

        boolean xRightIncluded = collisions.stream().filter(p -> p.getY().equals(point.getY())).anyMatch(p -> p.getX() > point.getX());
        boolean xLeftIncluded = collisions.stream().filter(p -> p.getY().equals(point.getY())).anyMatch(p -> p.getX() < point.getX());
        boolean yUpperIncluded = collisions.stream().filter(p -> p.getX().equals(point.getX())).anyMatch(p -> p.getY() > point.getY());
        boolean yLowerIncluded = collisions.stream().filter(p -> p.getX().equals(point.getX())).anyMatch(p -> p.getY() < point.getY());

        return xRightIncluded && xLeftIncluded && yUpperIncluded && yLowerIncluded;
    }
}
