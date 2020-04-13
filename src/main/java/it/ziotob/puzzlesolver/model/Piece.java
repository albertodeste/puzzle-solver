package it.ziotob.puzzlesolver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class Piece {

    private final List<Point> points;
    private final List<Point> borderPoints;
    private final List<Point> hullPoints;
    private final List<ConvexityDefect> convexityDefects;
    private final Point center;
    private final List<OuterLock> outerLocks;
    private final List<InnerLock> innerLocks;
}
