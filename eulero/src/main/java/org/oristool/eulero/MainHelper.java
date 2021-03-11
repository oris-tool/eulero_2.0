package org.oristool.eulero;

import org.jfree.data.json.impl.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.oristool.eulero.math.distribution.discrete.HistogramDistribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class MainHelper {
    private final static String DATASET_PATH = System.getProperty("user.dir")  + "/samples.json";
    private final static int BINS_NUMBER = 64;
    private final static int DISTRIBUTIONS_NUMBER = 38;

    /*
    *
    * Extract histogram from dataset, by applying Tukey's rule.
    *
    * */
    public static Map<String, HistogramDistribution> getHistogramsDistributionMap() {
        // Carico i samples, e in base a come li estrai ne seleziono N a caso, senza ripetizione o volendo anche con.
        // Poi per ogni gruppo, estraggo i campioni con Tukey e costruisco il diagramma vero e proprio.
        // Poi costruisco i vari HistogramDistribution che sono put nella HashMap con il nome delle transizioni che mi servono nel main.
        // Return a questo punto

        Map<String, HistogramDistribution> distributionsMap = new HashMap<>();
        ArrayList<Integer> drawnDistributions = new ArrayList<>();

        // carico roba dalla cartella
        System.out.println("Generating histograms");
        File directory = new File(DATASET_PATH);

        FileReader reader = null;
        try {
            reader = new FileReader(DATASET_PATH);
        } catch (FileNotFoundException e) {
            System.out.println("Samples file is not on the indicated directory...");
        }

        JSONParser jsonParser = new JSONParser();

        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            // Collect 100 histograms from data
            // TODO - insert code to store drawn histogram configuration
            while (drawnDistributions.size() < DISTRIBUTIONS_NUMBER) {
                int drawnIndex = (int) (Math.random() * 99);

                if (!drawnDistributions.contains(Integer.valueOf(drawnIndex))) {
                    drawnDistributions.add(Integer.valueOf(drawnIndex));

                    double[] samples = ((ArrayList<Double>) jsonObject.get("distribution" + String.format("%03d", drawnIndex)))
                            .stream().mapToDouble(Double::doubleValue).toArray();

                    double[] regularizedSamples = TukeysRegularization(samples);
                    Arrays.sort(regularizedSamples);
                    ArrayList<BigDecimal> histogramValues = histogramGeneration(regularizedSamples, BINS_NUMBER);

                    HistogramDistribution histogram = new HistogramDistribution("H" + String.format("%03d", drawnIndex),
                            BigDecimal.valueOf(regularizedSamples[0]), BigDecimal.valueOf(regularizedSamples[regularizedSamples.length - 1]), histogramValues);

                    distributionsMap.put(histogram.getName(), histogram);
                }
            }
        }

        return distributionsMap;
    }

    public static double[] TukeysRegularization(double[] samples){
        double[] v = new double[samples.length];
        System.arraycopy(samples, 0, v, 0, samples.length);
        Arrays.sort(v);

        int n_25 = (int) Math.round(v.length * 25 / 100);
        int n_75 = (int) Math.round(v.length * 75 / 100);

        double firstQuartile = v[n_25];
        double thirdQuartile = v[n_75];
        double IQR = thirdQuartile - firstQuartile;

        ArrayList<Double> selectedSamples = new ArrayList<>();
        for (double sample : samples) {
            if (sample < firstQuartile - 1.5 * IQR || sample > thirdQuartile + 1.5 * IQR) {
                selectedSamples.add(sample);
            }
        }

        return selectedSamples.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static ArrayList<BigDecimal> histogramGeneration(double[] samples, int bins){
        Arrays.sort(samples);
        double a = samples[0];
        double b = samples[samples.length - 1];
        double[] histogram = new double[bins];
        ArrayList<BigDecimal> histogramAsList = new ArrayList<>();

        for (double sample : samples) {
            histogram[(int) Math.floor((sample - a) * bins / (b - a))]++;
        }

        for (int k = 0; k < bins; k++) {
            histogram[k] /= samples.length;
            histogramAsList.add(BigDecimal.valueOf(histogram[k]));
        }

        return histogramAsList;
    }
}
