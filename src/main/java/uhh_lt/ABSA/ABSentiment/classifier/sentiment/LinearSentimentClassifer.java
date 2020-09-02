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

package uhh_lt.ABSA.ABSentiment.classifier.sentiment;

import uhh_lt.ABSA.ABSentiment.classifier.LinearClassifier;
import uhh_lt.ABSA.ABSentiment.training.LinearTesting;
import uhh_lt.ABSA.ABSentiment.training.util.ProblemBuilder;

/**
 * The LinearSentimentClassifer analyzes the sentiment of a document.
 */
public class LinearSentimentClassifer extends LinearClassifier {

    public LinearSentimentClassifer(String configurationFile) {
        ProblemBuilder.initialise(configurationFile);
        String type = "sentiment";
        LinearTesting linearTesting = new LinearTesting();
        model = linearTesting.loadModel(ProblemBuilder.sentimentModel);
        features = ProblemBuilder.loadFeatureExtractors(type);

        labelMappings = loadLabelMapping(ProblemBuilder.labelMappingsFileSentiment);
    }
}
