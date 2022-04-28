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

package org.oristool.eulero.evaluation.heuristics;

import org.oristool.models.stpn.TransientSolution;

public class EvaluationResult {
    private String title;
    private final double[] cdf;
    private final int min;
    private final int max;
    private final double step;
    private final long computationTime;

    public EvaluationResult(String title, double[] cdf, int min, int max, double step, long computationTime){
        this.title = title;
        this.cdf = cdf;
        this.min = min;
        this.max = max;
        this.step = step;
        this.computationTime = computationTime;
    }

    public EvaluationResult(String title, TransientSolution solution, int min, int max, double step, long computationTime){
        this.title = title;
        this.computationTime = computationTime;
        double[] cdf = new double[solution.getSolution().length];
        for(int count = 0; count < solution.getSolution().length; count++){
            cdf[count] = solution.getSolution()[count][0][0];
        }
        this.cdf = cdf;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public String title() {
        return title;
    }

    public double[] cdf() {
        return cdf;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public double step() {
        return step;
    }

    public double[] pdf() {
        double[] pdf = new double[cdf.length];

        pdf[0] = cdf[0] / step;

        for(int i = 1; i < pdf.length; i++){
            pdf[i] = (cdf[i] - cdf[i - 1]) / step;
        }

        return pdf;
    }

    public double jsDistance(double[] otherPDF) {
        double[] pdf = pdf();

        if (pdf.length != otherPDF.length)
            throw new IllegalArgumentException("Should have the same number of samples");

        double result = 0.0;
        for (int t = 0; t < otherPDF.length; ++t) {
            double x = pdf[t];
            double y = otherPDF[t];
            double m = (x + y)/2.0;
            result += (klDivergence(x, m) + klDivergence(y, m)) / 2.0;
        }

        return result * step;
    }

    public double klDivergence(double px, double py) {
        if (px > 0.0 && py > 0.0) {
            return px * Math.log(px / py);
        } else {
            return 0.0;
        }
    }

    public long computationTime(){
        return computationTime;
    }

    public void setTitle(String title){
        this.title = title;
    }
}
