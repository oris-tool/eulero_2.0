package modelling.stachastictime;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.oristool.eulero.modeling.stochastictime.HistogramTime;


public class HistogramTimeTest {

    @Test
    public void testCloneDeepCopy() {
        ArrayList<Double> originalValues = new ArrayList<>(Arrays.asList(10.0, 20.0, 30.0));
        HistogramTime original = new HistogramTime(0.0, 10.0, 3, originalValues);

        HistogramTime cloned = original.clone();

        assertNotNull(cloned);
        assertEquals(original.bins(), cloned.bins());
        assertEquals(original.values(), cloned.values());

        cloned.values().add(40.0);

        assertEquals(4, cloned.values().size(), "The cloned list should contain 4 elements after adding a new value.");
        assertEquals(3, original.values().size(), "The original list should still contain 3 elements.");
    }
}
