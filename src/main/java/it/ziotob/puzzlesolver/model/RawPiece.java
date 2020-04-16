package it.ziotob.puzzlesolver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public class RawPiece {

    private final List<Point> points;
    private final List<Point> borderPoints;
    private final List<Point> hullPoints;
    private final List<ConvexityDefect> convexityDefects;
    private final Point center;
    private final List<OuterLock> outerLocks;
    private final List<InnerLock> innerLocks;
    private final Point massCenter;
    private final List<Point> corners;
    private final Integer rotationAngle;

    public boolean isFullyDetected() {

        return Stream.concat(
               outerLocks.stream().map(OuterLock::getConvexityDefects).flatMap(Collection::stream),
               innerLocks.stream().map(InnerLock::getConvexityDefect)
        ).count() == convexityDefects.size();
    }
}
