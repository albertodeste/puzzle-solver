package it.ziotob.puzzlesolver.model;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PointsGroups {

    private final int tolerance;
    private final Map<Integer, List<Point>> groups = new HashMap<>();

    public Stream<Point> getBiggestGroup() {

        int biggestIndex = groups.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(Integer.MAX_VALUE / 2);

        return IntStream.range(Math.max(0, biggestIndex - tolerance), Math.min(100, biggestIndex + tolerance))
                .mapToObj(groups::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream);
    }

    public void addPoint(Point point, int h) {

        if (!groups.containsKey(h)) {
            groups.put(h, new ArrayList<>());
        }

        groups.get(h).add(point);
    }
}
