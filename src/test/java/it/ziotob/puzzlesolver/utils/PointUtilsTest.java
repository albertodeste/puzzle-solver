package it.ziotob.puzzlesolver.utils;

import it.ziotob.puzzlesolver.model.Point;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PointUtilsTest {

    @Test
    public void shouldRevertCounterClockwiseList() {

        Point a = new Point(1, 0);
        Point b = new Point(0, -1);
        Point c = new Point(-1, 0);
        Point d = new Point(0, 1);
        List<Point> list = Arrays.asList(a, b, c, d);

        Assertions.assertThat(PointUtils.sortClockwise(list))
                .containsExactly(d, c, b, a);
    }

    @Test
    public void shouldNotRevertClockwiseList() {

        Point a = new Point(1, 0);
        Point b = new Point(0, -1);
        Point c = new Point(-1, 0);
        Point d = new Point(0, 1);
        List<Point> list = Arrays.asList(d, c, b, a);

        Assertions.assertThat(PointUtils.sortClockwise(list))
                .containsExactly(d, c, b, a);
    }

    @Test
    public void shouldMultipleCallsNotChangeEffectOfClockwise() {

        Point a = new Point(1, 0);
        Point b = new Point(0, -1);
        Point c = new Point(-1, 0);
        Point d = new Point(0, 1);
        List<Point> list = Arrays.asList(a, b, c, d);

        Assertions.assertThat(PointUtils.sortClockwise(PointUtils.sortClockwise(list)))
                .containsExactly(d, c, b, a);
    }
}
