package org.oristool.eulero.modeling.stochastictime;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "truncated-exp-mixt")
public class TruncatedExponentialMixtureTime extends StochasticTime{
    ArrayList<TruncatedExponentialTime> pieces;
    ArrayList<BigDecimal> weights;

    public TruncatedExponentialMixtureTime(ArrayList<TruncatedExponentialTime> pieces, ArrayList<BigDecimal> weights){
        super(
                pieces.stream().reduce((a, b)-> a.getEFT().compareTo(b.getEFT()) != 1 ? a : b).get().getEFT(),
                pieces.stream().reduce((a, b)-> a.getEFT().compareTo(b.getEFT()) != 1 ? a : b).get().getLFT(),
                SIRIOType.EXPO
        );
        this.pieces = pieces;
        this.weights = weights;

    }

    @Override
    public StochasticTransitionFeature getStochasticTransitionFeature() {
        return null;
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
    public double getExpectedValue() {
        return 0;
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
        return new TruncatedExponentialMixtureTime(this.pieces, this.weights);
    }
}
