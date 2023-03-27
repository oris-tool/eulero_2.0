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
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class SOSplineApproximation extends Approximator{
    private int bodyPieces;

    public SOSplineApproximation(int bodyPieces){
        this.bodyPieces = bodyPieces;
    }

    @Override
    public Pair<BigDecimal,StochasticTransitionFeature> getApproximatedStochasticTransitionFeature(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }

    @Override
    public ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> getApproximatedStochasticTransitionFeatures(double[] cdf, double low, double upp, BigDecimal step) {
        ArrayList<Pair<BigDecimal,StochasticTransitionFeature>> features = new ArrayList<>();

        // Ricorda che la cdf è data da 0 a upp; low si usa se serve sapere il supporto reale.
        if(cdf.length < (upp - low)/step.doubleValue()){
            throw new RuntimeException("cdf has enough samples with respect to provided support and time step value");
        }

        int bodyPieceWidth = (int) ((upp - low) / step.doubleValue() / (double) bodyPieces);

        double[] pdf = new double[cdf.length];
        for(int i = 0; i < pdf.length; i++){
            pdf[i] = (i != pdf.length - 1 ? (cdf[i+1] - cdf[i]) : 0) / step.doubleValue() ;
        }

        for(int i = 0; i < bodyPieces; i++){
            double alpha = Double.MAX_VALUE;

            // Body
            int bodyPieceStartingIndex = (int) (low / step.doubleValue()) + i * bodyPieceWidth;
            int bodyPieceEndingIndex = (i != bodyPieces - 1) ? (int) (low / step.doubleValue()) + (i + 1) * bodyPieceWidth : cdf.length - 1;
            double x1 = BigDecimal.valueOf(bodyPieceStartingIndex * step.doubleValue()).doubleValue();
            double x2 = BigDecimal.valueOf(bodyPieceEndingIndex * step.doubleValue()).doubleValue();
            double h = BigDecimal.valueOf(x2 - x1).setScale(step.scale(), RoundingMode.HALF_DOWN).doubleValue();
            double p = cdf[bodyPieceEndingIndex] - cdf[bodyPieceStartingIndex];

            for (int j = bodyPieceStartingIndex; j < bodyPieceEndingIndex; j++){
                double test = (h * cdf[j] - (p / h * (j * step.doubleValue() - x1) * (j * step.doubleValue() - x1))) /
                        ((x2 + x1) * j * step.doubleValue() + (j * step.doubleValue()) * (j * step.doubleValue()) - (x1 * x2));

                if(test >= 0 && test <= 2 * p / h){
                    alpha = Math.min(test, alpha);
                } else {
                    if(pdf[bodyPieceEndingIndex] < pdf[bodyPieceEndingIndex]){
                        alpha = 0;
                    } else {
                        alpha = 2 * p / h;
                    }
                }
            }

            double c1 = alpha * x1 / h - 2 * p * x1 / (h * h) + alpha * x1 / h;
            double c2 = 2 / h * (p / h - alpha);

            // Va capito se qui come è scritto ora è normalizzato, ma credo di sì
            features.add(Pair.of(
                BigDecimal.valueOf(p),
                StochasticTransitionFeature.newExpolynomial(
                    c1 / p  + " + " + c2 / p + "*x^1", new OmegaBigDecimal(String.valueOf(x1)), new OmegaBigDecimal(String.valueOf(x2))
                )
            ));
        }
        return features;    }

    @Override
    public StochasticTime getApproximatedStochasticTime(double[] cdf, double low, double upp, BigDecimal step) {
        return null;
    }
}
