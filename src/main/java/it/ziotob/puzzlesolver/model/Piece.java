package it.ziotob.puzzlesolver.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
public class Piece {

    private final List<Point> points;
    private final List<Point> borderPoints;
    private final Point center;
    private final List<Point> corners;
}
