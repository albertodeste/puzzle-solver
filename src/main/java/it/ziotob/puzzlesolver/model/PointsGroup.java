package it.ziotob.puzzlesolver.model;

import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
public class PointsGroup {

    private final Set<Point> points = new HashSet<>();
    private final Map<Integer, Map<Integer, Point>> matrix = new HashMap<>();

    public void addPoint(Point point) {

        points.add(point);
        if (!matrix.containsKey(point.getX())) {
            matrix.put(point.getX(), new HashMap<>());
        }
        matrix.get(point.getX()).put(point.getY(), point);
    }

    public Optional<Point> getRandomPoint() {
        return points.stream().findAny();
    }

    public List<Point> findClosePoints(Point current) {

        return Stream.of(
                pointAt(current.getX() + 1, current.getY()),
                pointAt(current.getX() - 1, current.getY()),
                pointAt(current.getX(), current.getY() + 1),
                pointAt(current.getX(), current.getY() - 1)
        )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Point> pointAt(Integer x, Integer y) {
        return Optional.ofNullable(matrix.get(x)).map(ys -> ys.get(y));
    }

    public void removePoint(Point point) {

        points.remove(point);
        matrix.get(point.getX()).remove(point.getY());
    }
}
