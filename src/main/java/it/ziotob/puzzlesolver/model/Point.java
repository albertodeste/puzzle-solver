package it.ziotob.puzzlesolver.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"x", "y"})
@ToString
public class Point {

    private final Integer x;
    private final Integer y;
}
