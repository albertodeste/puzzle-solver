package it.ziotob.puzzlesolver.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"x", "y"})
public class Point {

    private final Integer x;
    private final Integer y;

    public Integer distance(Point point) {
        return (int)Math.sqrt(Math.pow(point.getX() - this.getX(), 2) + Math.pow(point.getY() - this.getY(), 2));
    }
}
