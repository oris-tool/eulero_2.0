package org.oristool.eulero.modeling;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.activitytypes.ActivityEnumType;
import org.oristool.eulero.modeling.stochastictime.ExponentialTime;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Simple")
public class Simple extends Activity {
    private static final double TIME_LIMIT_EPSILON = 0.01;
    @XmlElement(name = "pdf", required = true)
    private StochasticTime pdf;

    public Simple(String name, StochasticTime pdf) {
        super(name);
        setEnumType(ActivityEnumType.SIMPLE);
        setMin(pdf.getEFT());
        setMax(pdf.getLFT());
        setC(BigInteger.ONE);
        setQ(BigInteger.ONE);
        setEnumType(ActivityEnumType.SIMPLE);
        setSimplifiedC(BigInteger.ONE);
        setSimplifiedQ(BigInteger.ONE);
        setActivities(new ArrayList<>());
        this.pdf = pdf;
    }

    public Simple(){
        super("");
        setEnumType(ActivityEnumType.SIMPLE);
    }


    @Override
    public Activity copyRecursive(String suffix) {
        return new Simple(this.name() + suffix, getPdf());
    }

    @Override
    public BigInteger computeQ(boolean getSimplified) {
        return null;
    }

    @Override
    public void resetSupportBounds() {
        this.setMin(this.getPdf().getEFT());
        this.setMax(this.getPdf().getLFT());
    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int prio) {

    }

    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
        // TODO: metti la logica della STPN nel singolo stochastic time, perchè almeno disaccoppiamo questo metodo da lo specifico tipo di PDF
        List<StochasticTransitionFeature> features = pdf.getStochasticTransitionFeatures();

        if(features.size() == 1){
            Transition t = pn.addTransition(this.name());
            t.addFeature(new Priority(prio));
            t.addFeature(features.get(0));
            pn.addPrecondition(in, t);
            pn.addPostcondition(t, out);
            return prio + 1;
        }

        for(StochasticTransitionFeature feature: features){
            Transition immediateT = pn.addTransition(this.name() + "_imm_" + features.indexOf(feature));
            immediateT.addFeature(new Priority(prio));
            immediateT.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(pdf.getWeights().get(features.indexOf(feature)).doubleValue())));
            Place p = pn.addPlace("p_" + this.name() + "_" + features.indexOf(feature));
            Transition t = pn.addTransition(this.name() + "_" + features.indexOf(feature));
            t.addFeature(new Priority(prio));
            t.addFeature(feature);

            if(feature.isEXP()){
                Place pDet = pn.addPlace("p_" + this.name() + "_" + features.indexOf(feature) + "DET");
                Transition tDet = pn.addTransition(this.name() + "_DET");
                tDet.addFeature(new Priority(prio));
                tDet.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.valueOf(features.stream().filter(feat -> !feat.isEXP()).mapToDouble(feat -> feat.density().getDomainsLFT().doubleValue()).max().orElse(0)), MarkingExpr.ONE));

                pn.addPostcondition(immediateT, pDet);
                pn.addPrecondition(pDet, tDet);
                pn.addPostcondition(tDet, p);
            } else {
                pn.addPostcondition(immediateT, p);
            }

            pn.addPrecondition(in, immediateT);
            pn.addPrecondition(p, t);
            pn.addPostcondition(t, out);
        }
        return prio + 1;
    }

    @Override
    public double[] analyze(BigDecimal timeLimit, BigDecimal timeStep, AnalysisHeuristicsVisitor visitor) {
        return pdf.getNumericalCDF(timeStep.doubleValue(), timeLimit.doubleValue());
    }

    @Override
    public BigDecimal low() {
        BigDecimal low = this.pdf.getEFT();
        setMin(low);
        return low;
    }

    @Override
    public BigDecimal upp() {
        BigDecimal max = this.pdf.getLFT();
        setMax(max);
        return max;
    }

    @Override
    public boolean isWellNested() {
        return false;
    }

    @Override
    public int countSimpleActivities() {
        return 1;
    }

    @Override
    public Activity clone() {
        return new Simple(this.name(), this.getPdf().clone());
    }

    @Override
    public double getMinimumExpectedValue() {
        return pdf.getExpectedValue();
    }

    @Override
    public double getFairTimeLimit() {
        if(upp().compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) != 0)
            return max().doubleValue();
        else
            return calcTimeLimitHeuristically();
    }
    
    private double calcTimeLimitHeuristically(){
        int times = 3;
        double expectedValue = this.getPdf().getExpectedValue();
        double variance = this.getPdf().getVariance();
        double fairTimeLimit; 
        do{
            fairTimeLimit = expectedValue + times * variance;
            times ++;
        }
        while(this.getPdf().CDF(fairTimeLimit) < 1 - TIME_LIMIT_EPSILON ); 
        return fairTimeLimit;
    }


    public static Simple uniform(String name, BigDecimal a, BigDecimal b) {
        return new Simple(name, new UniformTime(a, b));
    }

    public static Simple exp(String name, BigDecimal lambda) {
        return new Simple(name, new ExponentialTime(lambda));
    }

    public static Simple erlang(String name, int k, BigDecimal lambda) {
        // TODO
        return null;
    }

    public StochasticTime getPdf() {
        return pdf;
    }

    public void setPdf(StochasticTime pdf) {
        this.pdf = pdf;
    }


}
