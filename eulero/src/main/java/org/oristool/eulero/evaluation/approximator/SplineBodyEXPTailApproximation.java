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

package org.oristool.eulero.evaluation.approximator;

import org.apache.commons.lang3.tuple.Pair;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class SplineBodyEXPTailApproximation extends Approximator {
    private int bodyPieces;
    private int scale = 4;

    public SplineBodyEXPTailApproximation(int bodyPieces){
        this.bodyPieces = bodyPieces;
    }

    @Override
    public Pair<BigDecimal,StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
        if(cdf.length < (upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has enough samples with respect to provided support and time step value");
        }

        ArrayList<GEN> distributionPieces = new ArrayList<>();

        int Q3Index = IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.75)
                .findFirst()
                .orElse(cdf.length-1);

        double Q3 = /*low +*/ Q3Index * step.doubleValue();

        int bodyPieceWidth = (int) ((Q3 - low) / step.doubleValue() / (double) bodyPieces);

        double[] pdf = new double[cdf.length];
        for(int i = 0; i < pdf.length; i++){
            pdf[i] = (i != pdf.length - 1 ? (cdf[i+1] - cdf[i]) : 0) / step.doubleValue() ;
        }

        for(int i = 0; i < bodyPieces; i++){
            // Body
            int bodyPieceStartingIndex = (int) (low / step.doubleValue()) + i * bodyPieceWidth;
            int bodyPieceEndingIndex = (i != bodyPieces - 1) ?  (int) (low / step.doubleValue()) + (i + 1) * bodyPieceWidth : Q3Index;
            OmegaBigDecimal eft = new OmegaBigDecimal(String.valueOf(bodyPieceStartingIndex * step.doubleValue()));
            OmegaBigDecimal lft = new OmegaBigDecimal(String.valueOf(bodyPieceEndingIndex * step.doubleValue())); // sull'ultimo dovrebbe venire proprio Q3
            double bodyPieceLocalWeight = cdf[bodyPieceEndingIndex] - cdf[bodyPieceStartingIndex];

            double bodyPieceLocalMean = 0;

            for (int j = bodyPieceStartingIndex; j < bodyPieceEndingIndex; j++){
                bodyPieceLocalMean += //((pdf[j] + pdf[j+1]) * timeTick / 2 ) * x[j + 1];
                        (pdf[j] * step.doubleValue()) * (j * step.doubleValue());
            }

            bodyPieceLocalMean = bodyPieceLocalMean/bodyPieceLocalWeight;

            double x1 = eft.doubleValue();
            double x2 = lft.doubleValue();
            double f1 = pdf[bodyPieceStartingIndex];
            double h = BigDecimal.valueOf(x2 - x1).setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue();
            double m = (Math.pow(x1, 3)/6 - Math.pow(x2, 3)/6  + (x1 * x2 * h)/2) / (bodyPieceLocalWeight * h);
            double q = (Math.pow(x1, 3)/6 - Math.pow(x2, 3)/6  + (x1 * x2 * h)/2) * f1 / (bodyPieceLocalWeight * h) +
                    (Math.pow(x2, 3)/3 + Math.pow(x1, 3)/6  - (x1 * x2 * x2) / 2) * 2 / (h * h);

            double alpha = (bodyPieceLocalMean - q) / m;

            if(alpha < -f1){
                alpha = -f1;
            }
            if(alpha > 2 * bodyPieceLocalWeight / h - f1){
                alpha = 2 * bodyPieceLocalWeight / h - f1;
            }

            double c1 = (f1 + alpha) / h;
            double c2 = (2 * bodyPieceLocalWeight / h - f1 - alpha) / h;

            String density = c1 * x2 - c2 * x1 + " + " + (c2 - c1) + "*x^1";

            distributionPieces.add(GEN.newExpolynomial(density, eft, lft));
        }

        //tail
        double tailLambda = Double.MAX_VALUE;
        double[] test = new double[cdf.length - Q3Index];
        for(int i = Q3Index ; i < cdf.length; i++){
            double cdfValue = (cdf[i] - cdf[Q3Index]) / (1 - cdf[Q3Index]);

            //Discard bad conditioned values
            if(cdfValue > 0  &&  cdfValue < 1 && /*low + */(i * step.doubleValue()) > Q3) {
                tailLambda = Math.min(
                        tailLambda,
                        -Math.log(1 - cdfValue) / (/*low + */(i * step.doubleValue()) - Q3)
                );
            }
            test[i - Q3Index] = cdfValue;
        }

        String density = (1 - cdf[Q3Index]) * tailLambda * Math.exp(tailLambda * Q3) + " * Exp[-" + tailLambda + " x]";
        distributionPieces.add(GEN.newExpolynomial(density, new OmegaBigDecimal(String.valueOf(Q3)), OmegaBigDecimal.POSITIVE_INFINITY));

        return Pair.of(BigDecimal.ONE, StochasticTransitionFeature.of(new PartitionedGEN(distributionPieces)));
    }

    @Override
    public ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> features = new ArrayList<>();

        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
        if(cdf.length < (upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has enough samples with respect to provided support and time step value");
        }

        ArrayList<GEN> distributionPieces = new ArrayList<>();

        int Q3Index = IntStream.range(0, cdf.length)
                .filter(i -> cdf[i] >= 0.75)
                .findFirst()
                .orElse(cdf.length-1);

        double Q3 = /*low +*/ Q3Index * step.doubleValue();

        int bodyPieceWidth = (int) ((upp - low) / step.doubleValue() / (double) bodyPieces);

        double[] pdf = new double[cdf.length];
        for(int i = 0; i < pdf.length; i++){
            pdf[i] = (i != pdf.length - 1 ? (cdf[i+1] - cdf[i]) : 0) / step.doubleValue() ;
        }

        for(int i = 0; i < bodyPieces; i++){
            // Body
            int bodyPieceStartingIndex = (int) (low / step.doubleValue()) + i * bodyPieceWidth;
            int bodyPieceEndingIndex = (i != bodyPieces - 1) ?  (int) (low / step.doubleValue()) + (i + 1) * bodyPieceWidth : cdf.length - 1;
            OmegaBigDecimal eft = new OmegaBigDecimal(String.valueOf(bodyPieceStartingIndex * step.doubleValue()));
            OmegaBigDecimal lft = new OmegaBigDecimal(String.valueOf(bodyPieceEndingIndex * step.doubleValue())); // sull'ultimo dovrebbe venire proprio Q3
            double bodyPieceLocalWeight = cdf[bodyPieceEndingIndex] - cdf[bodyPieceStartingIndex];

            double bodyPieceLocalMean = 0;

            for (int j = bodyPieceStartingIndex; j < bodyPieceEndingIndex; j++){
                bodyPieceLocalMean += //((pdf[j] + pdf[j+1]) * timeTick / 2 ) * x[j + 1];
                        (pdf[j] * step.doubleValue()) * (j * step.doubleValue());
            }

            bodyPieceLocalMean = bodyPieceLocalMean/bodyPieceLocalWeight;

            double x1 = eft.doubleValue();
            double x2 = lft.doubleValue();
            double f1 = pdf[bodyPieceStartingIndex];
            double h = BigDecimal.valueOf(x2 - x1).setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue();
            double m = (Math.pow(x1, 3)/6 - Math.pow(x2, 3)/6  + (x1 * x2 * h)/2) / (bodyPieceLocalWeight * h);
            double q = (Math.pow(x1, 3)/6 - Math.pow(x2, 3)/6  + (x1 * x2 * h)/2) * f1 / (bodyPieceLocalWeight * h) +
                    (Math.pow(x2, 3)/3 + Math.pow(x1, 3)/6  - (x1 * x2 * x2) / 2) * 2 / (h * h);

            double alpha = (bodyPieceLocalMean - q) / m;

            if(alpha < -f1){
                alpha = -f1;
            }

            if(alpha > 2 * bodyPieceLocalWeight / h - f1){
                alpha = 2 * bodyPieceLocalWeight / h - f1;
            }

            double c1 = (f1 + alpha) / h;
            double c2 = (2 * bodyPieceLocalWeight / h - f1 - alpha) / h;

            // Va capito se qui come è scritto ora è normalizzato, ma credo di sì
            features.add(Pair.of(
                BigDecimal.valueOf(bodyPieceLocalWeight),
                StochasticTransitionFeature.newExpolynomial(
                (c1 * x2 - c2 * x1) / bodyPieceLocalWeight + " + " + (c2 - c1) / bodyPieceLocalWeight+ "*x^1", eft, lft
                )
            ));
        }

        //tail
        /*double tailLambda = Double.MAX_VALUE;
        double[] test = new double[cdf.length - Q3Index];
        for(int i = Q3Index ; i < cdf.length; i++){
            double cdfValue = (cdf[i] - cdf[Q3Index]) / (1 - cdf[Q3Index]);

            //Discard bad conditioned values
            if(cdfValue > 0  &&  cdfValue < 1 && (i * step.doubleValue()) > Q3) {
                tailLambda = Math.min(
                        tailLambda,
                        -Math.log(1 - cdfValue) / ((i * step.doubleValue()) - Q3)
                );
            }
            test[i - Q3Index] = cdfValue;
        }

        features.add(StochasticTransitionFeature.newExponentialInstance(BigDecimal.valueOf(tailLambda)));
        stochasticTransitionFeatureWeights().add(BigDecimal.valueOf(0.25));*/
        return features;
    }

    @Override
    public StochasticTime getApproximatedStochasticTime(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }
}
