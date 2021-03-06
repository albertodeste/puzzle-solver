package it.ziotob.puzzlesolver.services;

import it.ziotob.puzzlesolver.model.RawPiece;
import it.ziotob.puzzlesolver.model.RawPieceFactory;
import it.ziotob.puzzlesolver.model.Point;
import it.ziotob.puzzlesolver.model.PointsGroup;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RawPieceService {

    public List<RawPiece> detectPieces(List<Point> piecesPoints) {

        PointsGroup pointsGroup = new PointsGroup();
        piecesPoints.forEach(pointsGroup::addPoint);

        List<List<Point>> result = new ArrayList<>();
        Optional<List<Point>> piece = detectPiece(pointsGroup);

        while (piece.isPresent()) {

            result.add(piece.get());
            piece = detectPiece(pointsGroup);
        }

        return discardImperfections(result).parallelStream()
                .map(RawPieceFactory::factory)
                .collect(Collectors.toList());
    }

    private List<List<Point>> discardImperfections(List<List<Point>> pieces) {

        int biggestPiecePoints = pieces.stream().mapToInt(List::size).max().orElse(0);
        List<Integer> distances = pieces.stream()
                .mapToInt(List::size)
                .boxed()
                .map(size -> biggestPiecePoints - size)
                .collect(Collectors.toList());
        Integer biggestDistance = distances.stream().sorted()
                .map(distance -> new AbstractMap.SimpleEntry<>(0, distance))
                .reduce(new AbstractMap.SimpleEntry<>(0, 0), (prev, curr) -> {

                    if (curr.getValue() - prev.getValue() > prev.getKey()) {
                        return new AbstractMap.SimpleEntry<>(curr.getValue() - prev.getValue(), curr.getValue());
                    } else {
                        return new AbstractMap.SimpleEntry<>(prev.getKey(), curr.getValue());
                    }
                }).getKey();

        if (biggestDistance < biggestPiecePoints / 2) {
            return pieces;
        } else {

            List<List<Point>> sortedPieces = pieces.stream()
                    .sorted(Comparator.comparingInt(l -> l.size() * -1))
                    .collect(Collectors.toList());
            int minimumPiecesSize = IntStream.range(1, pieces.size())
                    .filter(i -> sortedPieces.get(i - 1).size() - sortedPieces.get(i).size() == biggestDistance)
                    .map(i -> sortedPieces.get(i - 1).size())
                    .findFirst()
                    .orElse(0);

            return pieces.stream()
                    .filter(piece -> piece.size() >= minimumPiecesSize)
                    .collect(Collectors.toList());
        }
    }

    private Optional<List<Point>> detectPiece(PointsGroup pointsGroup) {

        List<Point> points = new ArrayList<>();
        List<Point> targetPoints = new ArrayList<>();
        pointsGroup.getRandomPoint().ifPresent(targetPoints::add);

        while (!targetPoints.isEmpty()) {

            Point current = targetPoints.get(0);
            targetPoints.remove(0);

            points.add(current);
            pointsGroup.removePoint(current);
            targetPoints.remove(current);

            List<Point> closePoints = pointsGroup.findClosePoints(current);
            targetPoints.addAll(closePoints);
        }

        if (points.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(points);
        }
    }
}
