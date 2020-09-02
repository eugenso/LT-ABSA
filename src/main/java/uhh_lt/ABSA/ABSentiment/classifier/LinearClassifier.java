/*
 * ******************************************************************************
 *  Copyright 2017
 *  Copyright (c) 2017 Universität Hamburg
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ****************************************************************************
 */

package uhh_lt.ABSA.ABSentiment.classifier;


import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import org.apache.uima.jcas.JCas;
import uhh_lt.ABSA.ABSentiment.featureExtractor.FeatureExtractor;
import uhh_lt.ABSA.ABSentiment.training.util.ProblemBuilder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

/**
 * The LinearClassifier provides common methods for classification
 */
public class LinearClassifier extends ProblemBuilder implements Classifier {

    protected Model model;

    protected Vector<FeatureExtractor> features;
    protected HashMap<Double, String> labelMappings;

    protected String label;
    private double score;
    private double[] probEstimates;

    @Override
    public String getLabel(JCas cas) {
        Vector<Feature[]> instanceFeatures = applyFeatures(cas, features);
        Feature[] instance = combineInstanceFeatures(instanceFeatures);
        probEstimates = new double[model.getNrClass()];
        Double prediction;
        if (model.getSolverType().isLogisticRegressionSolver()) {
            prediction = Linear.predictProbability(model, instance, probEstimates);
            score = probEstimates[prediction.intValue()];
        } else {
            prediction = Linear.predict(model, instance);
        }
        label = labelMappings.get(prediction);
        return label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public double getScore() {
        return score;
    }

    /**
     * Loads the label--identifier mappings to retrieve the correct String label for the predicted label.
     * @param fileName path to the mapping file
     * @return the mapping between label IDs and the corresponding String identifier
     */
    protected HashMap<Double, String> loadLabelMapping(String fileName) {
        HashMap<Double, String> lMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                String[] catLine = line.split("\\t");
                Double labelId = Double.parseDouble(catLine[0]);
                lMap.put(labelId, catLine[1]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return lMap;
    }
}
