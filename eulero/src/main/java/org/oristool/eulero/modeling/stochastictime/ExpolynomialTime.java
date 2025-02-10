package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.math.Continuous;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.List;

@XmlRootElement(name = "Expolynomial")
public class ExpolynomialTime extends StochasticTime {
    private String pdf;

    public ExpolynomialTime(){}

    public ExpolynomialTime(BigDecimal EFT, BigDecimal LFT, String pdf) {
        super(EFT, LFT);
        this.pdf = pdf;
    }

    public String getPdf() {
        return pdf;
    }

    public StochasticTransitionFeature getStochasticTransitionFeature(){
        return StochasticTransitionFeature.newExpolynomial(pdf, new OmegaBigDecimal(getEFT()), new OmegaBigDecimal(getLFT()));
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        return null;
    }

    @Override
    public List<BigDecimal> getWeights() {
        return null;
    }

    @Override
    public Continuous time2QueuingEulero() {
        return null;
    }

    @Override
    public double PDF(double t) {
        return 0;
    }

    @Override
    public double CDF(double t) {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public StochasticTime clone() {
        return new ExpolynomialTime(this.getEFT(), this.getLFT(), this.getPdf());
    }

    @Override
    public void randomizeParameters() {
    }

    /*@Override
    public double[] getNumericalCDF(double step, double limit){
        Activity model = new Simple("Model", this);
        Approximator approximator = new TruncatedExponentialMixtureApproximation();
        AnalysisHeuristicsVisitor analysisHeuristics = new SDFHeuristicsVisitor(BigInteger.TWO, BigInteger.TEN, approximator, false);
        return model.analyze(BigDecimal.valueOf(limit), BigDecimal.valueOf(step), analysisHeuristics);
    }

    @Override
    public double[] getNumericalPDF(double step, double limit){
        double[] cdf = getNumericalCDF(limit, step);
        double[] pdf = new double[cdf.length];
        for(int i = 1; i < cdf.length; i++){
            pdf[i] = (cdf[i] - cdf[i - 1]) / step;
        }

        return pdf;
    }*/

    /*@Override
    public double[] getSpecularNumericalCDF(double step, double limit){
        double[] cdf = getNumericalCDF(step, limit);
        double[] specularCDF = new double[cdf.length];
        for(int i = 0; i < cdf.length; i++){
            specularCDF[i] = 1 - cdf[i];
        }

        return  specularCDF;
    }*/

    @Override
    public double getExpectedValue() {
        return 0;
    }
}
