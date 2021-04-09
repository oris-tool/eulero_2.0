/* This program is part of the ORIS Tool.
 * Copyright (C) 2011-2020 The ORIS Authors.
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

/* This program is part of the ORIS Tool.
 * Copyright (C) 2011-2021 The ORIS Authors.
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

package org.oristool.eulero.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.oristool.eulero.MainHelper;
import org.oristool.models.stpn.TransientSolution;

/**
 * Swing plot visualizer.
 */
@SuppressWarnings("serial")
public class ActivityViewer extends JFrame {

    @SafeVarargs
    private static <R, S> ChartPanel solutionChart(
            String title, List<String> stringList, TransientSolution<R, S>... solutions) {

        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int index = 0; index < solutions.length; index++) {
            TransientSolution<R, S> s = solutions[index];
            int r = s.getRegenerations().indexOf(s.getInitialRegeneration());

            for (int m = 0; m < s.getColumnStates().size(); m++) {

                XYSeries series = new XYSeries("("
                        + s.getColumnStates().get(m).toString() + ")." + stringList.get(index));

                double step = s.getStep().doubleValue();
                for (int i = 0, size = s.getSamplesNumber(); i < size; i++)
                    series.add(i * step, s.getSolution()[i][r][m]);

                dataset.addSeries(series);
            }
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Time", "Probability", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        double upper = solutions[0].getStep().doubleValue()
                * solutions[0].getSamplesNumber();
        for (int index = 1; index < solutions.length; index++) {
            if (upper < solutions[index].getStep().doubleValue()
                    * solutions[index].getSamplesNumber())
                upper = solutions[index].getStep().doubleValue()
                        * solutions[index].getSamplesNumber();
        }
        domain.setRange(0.00, upper);
        // domain.setTickUnit(new NumberTickUnit(0.1));
        domain.setVerticalTickLabels(true);

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setAutoRangeMinimumSize(1.01);
        // range.setTickUnit(new NumberTickUnit(0.1));

        ChartPanel chartPanel = new ChartPanel(chart);
        // chartPanel.setMouseZoomable(true, false);

        return chartPanel;
    }

    /**
     * Displays a Swing visualization of the given transient solution, using a set
     * of labels.
     *
     * @param <R> regeneration type
     * @param <S> state type
     * @param stringList labels
     * @param transientSolutions transient solutions
     */
    @SafeVarargs
    public static <R, S> void plot(List<String> stringList, TransientSolution<R, S>... transientSolutions) {

        ActivityViewer v = new ActivityViewer();
        
        @SuppressWarnings("unchecked")
        TransientSolution<R, S>[] derivatives = new TransientSolution[transientSolutions.length];
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < derivatives.length; i++) {
            derivatives[i] = transientSolutions[i].computeDerivativeSolution();
            labels.add(stringList.get(i) + String.format(" (JS %.3f)", 
                    derivatives[i].jsDistance(derivatives[0], 0, 0, 0, 0)));
        }
        
        ChartPanel cdf = solutionChart("CDF", labels, transientSolutions);
        ChartPanel pdf = solutionChart("PDF", labels, derivatives);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("CDF", cdf);
        tabs.add("PDF", pdf);
        
        v.setTitle("Activity Viewer");
        v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        v.add(tabs);
        v.pack();
        v.setLocationRelativeTo(null);
        v.setVisible(true);
    }

    private static ChartPanel solutionChart(
            String title, List<String> stringList, double step, double upper, double[]... graphs) {

        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < graphs.length; i++) {
            double[] graph = graphs[i];
            XYSeries series = new XYSeries(stringList.get(i));

            for (int j = 0; j < graph.length; j++)
                series.add(j * step, graph[j]);

            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Time", "Probability", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(0.00, upper);
        // domain.setTickUnit(new NumberTickUnit(0.1));
        domain.setVerticalTickLabels(true);

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setAutoRangeMinimumSize(1.01);
        // range.setTickUnit(new NumberTickUnit(0.1));

        ChartPanel chartPanel = new ChartPanel(chart);
        // chartPanel.setMouseZoomable(true, false);

        return chartPanel;
    }

    @SafeVarargs
    public static void CompareResults(String title, List<String> stringList, MainHelper.ResultWrapper... results) {
        // TODO check dimension of step and upper

        ActivityViewer v = new ActivityViewer();

        double[][] cdfs = new double[results.length][];
        double[][] pdfs = new double[results.length][];
        double step = results[0].getStep();
        double upper = results[0].getMax() * step;

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            cdfs[i] = results[i].getCdf();
            pdfs[i] = results[i].getPdf();

            // JS divergence
            labels.add(stringList.get(i) + String.format(" (JS %.6f)",
                    results[i].jsDistance(pdfs[0])));
        }

        ChartPanel cdf = solutionChart("CDF - " + title, labels, step, upper, cdfs);
        ChartPanel pdf = solutionChart("PDF - " + title, labels, step, upper, pdfs);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("CDF", cdf);
        tabs.add("PDF", pdf);

        v.setTitle("Activity Viewer");
        v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        v.add(tabs);
        v.pack();
        v.setLocationRelativeTo(null);
        v.setVisible(true);
    }


}

