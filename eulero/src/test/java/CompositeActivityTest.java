import org.glassfish.jaxb.runtime.v2.model.impl.ModelBuilder;
import org.junit.jupiter.api.Test;
import org.oristool.eulero.evaluation.approximator.TruncatedExponentialMixtureApproximation;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.evaluation.heuristics.SDFHeuristicsVisitor;
import org.oristool.eulero.modeling.Composite;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.GeneralizeErlangTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class CompositeActivityTest {
    
    
    @Test
    public void fairTimeLimitInAndCompositeActivity(){
        Simple simple1 = new Simple("simple1", new UniformTime(1, 5));
        Simple simple2 = new Simple("simple2", new UniformTime(1, 9));
        Composite forkJoin = ModelFactory.forkJoin(simple1, simple2);
        assertEquals(forkJoin.getFairTimeLimit(), 9);
    }

    @Test
    public void fairTimeLimitInSeqCompositeActivity(){
        Simple simple1 = new Simple("simple1", new UniformTime(1, 5));
        Simple simple2 = new Simple("simple2", new UniformTime(1, 9));
        Composite sequence = ModelFactory.sequence(simple1, simple2);
        assertEquals(sequence.getFairTimeLimit(), 5+9);
    }

    @Test
    public void fairTimeLimitInXorCompositeActivity(){
        Simple simple1 = new Simple("simple1", new UniformTime(1, 5));
        Simple simple2 = new Simple("simple2", new UniformTime(1, 9));
        Composite xor = ModelFactory.XOR(List.of(0.1,0.9), simple1, simple2);
        assertEquals(xor.getFairTimeLimit(), 9);
    }

    @Test
    public void testGeneralizedAnalysis() {
        Composite a = ModelFactory.sequence(
            new Simple("s-1", new GeneralizeErlangTime(67, new BigDecimal(14), new BigDecimal(9)))
        );

        AnalysisHeuristicsVisitor analyzer = new SDFHeuristicsVisitor(BigInteger.valueOf(2), BigInteger.valueOf(5), new TruncatedExponentialMixtureApproximation());

        double[] solution = a.analyze(new BigDecimal(100), new BigDecimal(0.1), analyzer);

        System.out.println(solution[solution.length-1]);
    }

}
