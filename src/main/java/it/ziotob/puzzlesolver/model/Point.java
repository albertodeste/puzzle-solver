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
}
