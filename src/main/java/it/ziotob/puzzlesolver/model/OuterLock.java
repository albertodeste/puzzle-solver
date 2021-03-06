package it.ziotob.puzzlesolver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class OuterLock {

    private final List<Point> borderPoints;
    private final List<Point> points;
    private final List<ConvexityDefect> convexityDefects;
}
