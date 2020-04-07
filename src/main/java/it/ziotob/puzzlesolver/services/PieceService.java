package it.ziotob.puzzlesolver.services;

import it.ziotob.puzzlesolver.model.Piece;
import it.ziotob.puzzlesolver.model.Point;
import it.ziotob.puzzlesolver.model.PointsGroup;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PieceService {

    public List<Piece> detectPieces(List<Point> piecesPoints) {

        PointsGroup pointsGroup = new PointsGroup();
        piecesPoints.forEach(pointsGroup::addPoint);

        List<Piece> result = new ArrayList<>();
        Optional<Piece> piece = detectPiece(pointsGroup);

        while (piece.isPresent()) {

            result.add(piece.get());
            piece = detectPiece(pointsGroup);
        }

        return discardImperfections(result);
    }

    private List<Piece> discardImperfections(List<Piece> pieces) {

        int biggestPiecePoints = pieces.stream().mapToInt(Piece::getSize).max().orElse(0);
        List<Integer> distances = pieces.stream()
                .mapToInt(Piece::getSize)
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

            List<Piece> sortedPieces = pieces.stream()
                    .sorted(Comparator.comparingInt(p -> p.getSize() * -1))
                    .collect(Collectors.toList());
            int minimumPiecesSize = IntStream.range(1, pieces.size())
                    .filter(i -> sortedPieces.get(i - 1).getSize() - sortedPieces.get(i).getSize() == biggestDistance)
                    .map(i -> sortedPieces.get(i -1).getSize())
                    .findFirst()
                    .orElse(0);

            return pieces.stream()
                    .filter(piece -> piece.getSize() >= minimumPiecesSize)
                    .collect(Collectors.toList());
        }
    }

    private Optional<Piece> detectPiece(PointsGroup pointsGroup) {

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
            return Optional.of(Piece.factory(points));
        }
    }
}
