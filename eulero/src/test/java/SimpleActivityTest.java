import org.junit.jupiter.api.Test;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.ExponentialTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

public class SimpleActivityTest {
    
    @Test
    public void fairTimeLimitWhenPDFHasFiniteLFT(){
        Simple simple = new Simple("simple", new UniformTime(1, 5));
        assertEquals(simple.getFairTimeLimit(), 5);
    }

    @Test
    public void fairTimeLimitWhenPDFHasInfiniteLFT(){
        double rate = 1;
        ExponentialTime exponentialTime = new ExponentialTime(BigDecimal.valueOf(rate));
        Simple simple = new Simple("simple", exponentialTime);

        double timeLimit = 1/rate + 4*1/Math.pow(rate, 2);

        double value =  1 - Math.exp(-rate *timeLimit);

        assertTrue(value > 1- 0.01);
        assertEquals(simple.getFairTimeLimit(), timeLimit);
    }

}
