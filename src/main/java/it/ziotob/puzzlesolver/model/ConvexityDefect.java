package it.ziotob.puzzlesolver.model;

import it.ziotob.puzzlesolver.utils.PointUtils;
import javafx.util.Pair;
import lombok.*;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString(exclude = "convexityPoints")
@EqualsAndHashCode(of = {"hullPointA", "hullPointB", "deepestPoint"})
public class ConvexityDefect {

    private final Point hullPointA;
    private final Point hullPointB;
    private final List<Point> convexityPoints;
    private final Point deepestPoint;
    private final Float distance;

    public static ConvexityDefect factory(Point hullPointA, Point hullPointB) {
        return new ConvexityDefect(hullPointA, hullPointB, Collections.emptyList(), null, null);
    }

    public static ConvexityDefect factory(Point hullPointA, Point hullPointB, List<Point> convexityPoints) {

        Pair<Point, Float> mostDistantPair = PointUtils.getMostDistantPoint(convexityPoints, hullPointA, hullPointB);

        return new ConvexityDefect(hullPointA, hullPointB, convexityPoints, mostDistantPair.getKey(), mostDistantPair.getValue());
    }
}
