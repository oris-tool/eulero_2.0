/* This program is called EULERO.
 * Copyright (C) 2022 The EULERO Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oristool.eulero.modeling.deprecated;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import org.oristool.eulero.evaluation.heuristics.AnalysisHeuristicsVisitor;
import org.oristool.eulero.modeling.activitytypes.ActivityEnumType;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity with an analytical CDF.
 */
@XmlRootElement(name = "Analytical")
public class Simple extends Activity {

    @XmlTransient
    private StochasticTransitionFeature pdf;

    public ArrayList<StochasticTransitionFeature> pdfFeatures() {
        return pdfFeatures;
    }

    public ArrayList<BigDecimal> pdfWeights() {
        return pdfWeights;
    }

    @XmlTransient
    private ArrayList<StochasticTransitionFeature> pdfFeatures;
    @XmlTransient
    private ArrayList<BigDecimal> pdfWeights;

    /**
     * Creates an activity with analytical PDF. 
     */
    public Simple(String name, StochasticTransitionFeature pdf) {
        super(name);
        setEnumType(ActivityEnumType.SIMPLE);
        setMin(pdf.density().getDomainsEFT().bigDecimalValue());
        setMax((pdf.density().getDomainsLFT().bigDecimalValue() != null) ? pdf.density().getDomainsLFT().bigDecimalValue() : BigDecimal.valueOf(Double.MAX_VALUE));
        setC(BigInteger.ONE);
        setQ(BigInteger.ONE);
        setEnumType(ActivityEnumType.SIMPLE);
        setSimplifiedC(BigInteger.ONE);
        setSimplifiedQ(BigInteger.ONE);
        setEnumType(ActivityEnumType.SIMPLE);
        setActivities(new ArrayList<>());
        this.pdfFeatures = new ArrayList<>();
        this.pdfFeatures.add(pdf);
        this.pdfWeights = new ArrayList<>();
        this.pdfWeights.add(BigDecimal.ONE);
    }

    public Simple(String name, ArrayList<StochasticTransitionFeature> pdfFeatures, ArrayList<BigDecimal> pdfWeights) {
        super(name);
        setEnumType(ActivityEnumType.SIMPLE);
        setMin(BigDecimal.valueOf(pdfFeatures.stream().mapToDouble(t -> t.density().getDomainsEFT().doubleValue()).min().orElse(0)));
        setMax(BigDecimal.valueOf(
                Double.isInfinite(pdfFeatures.stream().mapToDouble(t -> t.density().getDomainsLFT().doubleValue()).max().getAsDouble()) ? Double.MAX_VALUE :
                pdfFeatures.stream().mapToDouble(t -> t.density().getDomainsLFT().doubleValue()).max().getAsDouble())
        );
        setC(BigInteger.ONE);
        setQ(BigInteger.ONE);
        setSimplifiedC(BigInteger.ONE);
        setSimplifiedQ(BigInteger.ONE);
        setEnumType(ActivityEnumType.SIMPLE);
        this.pdfFeatures = pdfFeatures;
        this.pdfWeights = pdfWeights;
    }

    public Simple(){
        super("");
        setEnumType(ActivityEnumType.SIMPLE);
    }

    @Override
    public Activity copyRecursive(String suffix) {
        return new Simple(this.name() + suffix, this.pdfFeatures, this.pdfWeights);
    }

    @Override
    public void resetSupportBounds() {
        setMin(BigDecimal.valueOf(pdfFeatures.stream().mapToDouble(t -> t.density().getDomainsEFT().doubleValue()).min().orElse(0)));
        setMax(BigDecimal.valueOf(
                Double.isInfinite(pdfFeatures.stream().mapToDouble(t -> t.density().getDomainsLFT().doubleValue()).max().getAsDouble()) ? Double.MAX_VALUE :
                        pdfFeatures.stream().mapToDouble(t -> t.density().getDomainsLFT().doubleValue()).max().getAsDouble())
        );
    }

    @Override
    public BigInteger computeQ(boolean getSimplified) {
        return BigInteger.ONE;
    }

    @Override
    public void buildTPN(PetriNet pn, Place in, Place out, int priority) {}


    public StochasticTransitionFeature pdf() {
        return pdf;
    }
    
    @Override
    public String yamlData() {
        StringBuilder b = new StringBuilder();
        List<? extends DBMZone> domains = pdf.density().getDomains();
        List<? extends Expolynomial> densities = pdf.density().getDensities();
        for (int i = 0; i < domains.size(); i++) {
            b.append(String.format("  pdf: [%s, %s] %s\n",
                    domains.get(i).getBound(Variable.TSTAR, Variable.X).negate(),
                    domains.get(i).getBound(Variable.X, Variable.TSTAR),
                    densities.get(i).toString()));
        }
        return b.toString();
    }
    
    public static Simple uniform(String name, BigDecimal a, BigDecimal b) {
        return new Simple(name,
                StochasticTransitionFeature.newUniformInstance(a, b));
    }

    public static Simple exp(String name, BigDecimal lambda) {
        return new Simple(name,
                StochasticTransitionFeature.newExponentialInstance(lambda));
    }

    public static Simple erlang(String name, int k, BigDecimal lambda) {
        return new Simple(name,
                StochasticTransitionFeature.newErlangInstance(k, lambda));
    }

    public double[] getNumericalCDF(BigDecimal timeLimit, BigDecimal step){
        // Se si trova un modo più furbo di fare questa...
        TransientSolution<DeterministicEnablingState, RewardRate> analysisSolution = this.analyze(timeLimit.toString(), step.toString(), "0.001");
        double[] cdf = new double[analysisSolution.getSolution().length];
        for(int i = 0; i < cdf.length; i++){
            cdf[i] = analysisSolution.getSolution()[i][0][0];
        }
        return cdf;
    }

    @Override
    public int buildSTPN(PetriNet pn, Place in, Place out, int prio) {
        if(pdfFeatures.size() == 1){
            Transition t = pn.addTransition(this.name());
            t.addFeature(new Priority(prio));
            t.addFeature(pdfFeatures.get(0));
            pn.addPrecondition(in, t);
            pn.addPostcondition(t, out);
            return prio + 1;
        }

        for(StochasticTransitionFeature feature: pdfFeatures){
            Transition immediateT = pn.addTransition(this.name() + "_imm_" + pdfFeatures.indexOf(feature));
            immediateT.addFeature(new Priority(prio));
            immediateT.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO, MarkingExpr.of(pdfWeights.get(pdfFeatures.indexOf(feature)).doubleValue())));
            Place p = pn.addPlace("p_" + this.name() + "_" + pdfFeatures.indexOf(feature));
            Transition t = pn.addTransition(this.name() + "_" + pdfFeatures.indexOf(feature));
            t.addFeature(new Priority(prio));
            t.addFeature(feature);

            if(feature.isEXP()){
                Place pDet = pn.addPlace("p_" + this.name() + "_" + pdfFeatures.indexOf(feature) + "DET");
                Transition tDet = pn.addTransition(this.name() + "_DET");
                tDet.addFeature(new Priority(prio));
                tDet.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.valueOf(pdfFeatures.stream().filter(feat -> !feat.isEXP()).mapToDouble(feat -> feat.density().getDomainsLFT().doubleValue()).max().orElse(0)), MarkingExpr.ONE));

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
        return new double[0];
    }

    @Override
    public boolean isWellNested() {
        return true;
    }

    @Override
    public BigDecimal low() {
        return this.min();
    }

    @Override
    public BigDecimal upp() {
        return this.max();
    }

    public void setFeatures(ArrayList<StochasticTransitionFeature> pdfFeatures) {
        this.pdfFeatures = pdfFeatures;
    }

    public void setWeights(ArrayList<BigDecimal> pdfWeights) {
        this.pdfWeights = pdfWeights;
    }

}
