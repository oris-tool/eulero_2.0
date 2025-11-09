package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.eulero.math.Continuous;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "truncatedExpMixt")
public class TruncatedExponentialMixtureTime extends StochasticTime{
    ArrayList<TruncatedExponentialTime> pieces;
    ArrayList<BigDecimal> weights;

    public TruncatedExponentialMixtureTime(ArrayList<TruncatedExponentialTime> pieces, ArrayList<BigDecimal> weights){
        super(
                pieces.stream().reduce((a, b)-> a.getEFT().compareTo(b.getEFT()) != 1 ? a : b).get().getEFT(),
                pieces.stream().reduce((a, b)-> a.getEFT().compareTo(b.getEFT()) != 1 ? a : b).get().getLFT()
        );
        this.pieces = pieces;
        this.weights = weights;

    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public List<StochasticTransitionFeature> getStochasticTransitionFeatures() {
        ArrayList<StochasticTransitionFeature> features = new ArrayList<>();

        for(TruncatedExponentialTime piece: pieces){
            features.add(piece.getStochasticTransitionFeature());
        }

        return features;
    }

    @Override
    public List<BigDecimal> getWeights() {
        return weights;
    }

    @Override
    public Continuous time2QueuingEulero() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizeLinear(double resources) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizeInhomogeneousLinear(double resources, double p) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public StochasticTime computeJobSizePiecewiseLinear(double resources, double Rmax) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double getExpectedValue() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double getVariance() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public double PDF(double t) {
        if(t >= pieces.get(pieces.size() - 1).getLFT().doubleValue()){
            return 0;
        }

        if(t < pieces.get(0).getEFT().doubleValue()){
            return 0;
        }

        TruncatedExponentialTime myTime = pieces.stream().filter(k -> k.getLFT().compareTo(BigDecimal.valueOf(t)) > 0).findFirst().get();

        return myTime.PDF(t) * weights.get(pieces.indexOf(myTime)).doubleValue();
    }

    @Override
    public double CDF(double t) {
        // TODO: soluzione temporanea, ma che può essere atta meglio e resa + efficiente
        /*double step = 0.01;
        double counter = 0.;
        double cdf = 0.;
        while(counter <= t){
            cdf += PDF(counter);
            counter += step;
        }

        return cdf;*/

        if(t >= pieces.get(pieces.size() - 1).getLFT().doubleValue()){
            return 1;
        }

        if(t < pieces.get(0).getEFT().doubleValue()){
            return 0;
        }

        TruncatedExponentialTime myTime = pieces.stream().filter(k -> k.getLFT().compareTo(BigDecimal.valueOf(t)) > 0).findFirst().get();
        return weights.stream().mapToDouble(BigDecimal::doubleValue).limit(pieces.indexOf(myTime)).sum() +
                myTime.CDF(t) * weights.get(pieces.indexOf(myTime)).doubleValue();

    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for(TruncatedExponentialTime piece: pieces){
            b.append("\nPiece ").append(pieces.indexOf(piece)).append(": ").append(piece.toString());
        }

        return b.toString();
    }

    @Override
    public StochasticTime clone() {
        return new TruncatedExponentialMixtureTime(this.pieces, this.weights);
    }

    @Override
    public void randomizeParameters() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

}
