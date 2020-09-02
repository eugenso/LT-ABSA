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

package uhh_lt.ABSA.ABSentiment.training.util;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Problem;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.jcas.JCas;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import uhh_lt.ABSA.ABSentiment.featureExtractor.*;
import uhh_lt.ABSA.ABSentiment.featureExtractor.util.ConfusionMatrix;
import uhh_lt.ABSA.ABSentiment.reader.InputReader;
import uhh_lt.ABSA.ABSentiment.reader.TsvReader;
import uhh_lt.ABSA.ABSentiment.reader.XMLReader;
import uhh_lt.ABSA.ABSentiment.reader.XMLReaderSemEval;
import uhh_lt.ABSA.ABSentiment.type.Document;
import uhh_lt.ABSA.ABSentiment.type.Sentence;
import uhh_lt.ABSA.ABSentiment.uimahelper.Preprocessor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Generates a common training/testing instance as a Document/Feature matrix for training and testing.
 * Created by abhishek on 19/5/17.
 */
public class ProblemBuilder {

    protected static InputReader fr;
    protected static final Preprocessor preprocessor = new Preprocessor(false);

    protected static String configurationfile;

    private static Integer maxLabelId = -1;
    private static int featureCount = 0;
    protected static boolean useCoarseLabels = false;

    protected static String language;
    private static String format;

    protected static boolean semeval16 = false;
    protected static String trainFile;
    protected static String testFile;
    protected static String predictionFile;
    protected static String labelMappingsFileSentiment;
    protected static String labelMappingsFileRelevance;
    protected static String labelMappingsFileAspect;
    protected static String labelMappingsFileAspectCoarse;
    protected static String featureOutputFile;
    protected static String featureStatisticsFile;
    protected static String idfGazeteerFile;
    protected static String idfFile;
    protected static int minTermFrequency = 100;

    protected static String relevanceModel;
    protected static String aspectModel;
    protected static String aspectCoarseModel;
    protected static String sentimentModel;
    protected static String modelFolder;

    protected static String corpusFile;
    protected static String maxLengthFile;

    protected static String relevanceIdfFile;
    protected static String sentimentIdfFile;
    protected static String aspectIdfFile;
    protected static String aspectCoarseIdfFile;

    protected static String positiveGazeteerFile;
    protected static String negativeGazeteerFile;

    protected static String polarityLexiconFile;
    protected static String aggregateGazeteerFile;

    protected static String DTConfigurationFile;
    protected static String missingWordsFile;
    protected static String DTExpansionFile;
    protected static String DTfile;

    protected static String gloveFile;
    protected static String w2vFile;

    protected static String weightedIdfFile;
    protected static String weightedW2vFile;
    protected static String weightedGloveFile;

    protected static HashMap<String, Integer> labelMappings = new HashMap<>();
    protected static HashMap<Integer, String> labelLookup = new HashMap<>();
    protected static ConfusionMatrix confusionMatrix;
    protected static ArrayList<String> allLabels = new ArrayList<>();


    /**
     * Loads a file and initializes all the variables present in the configuration file.
     * @param configurationFile path to a file containing the variable name and their initialization
     */
    protected static void initialise(String configurationFile){

        language = null;
        format = null;
        idfFile = null;
        positiveGazeteerFile = null;
        negativeGazeteerFile = null;
        gloveFile = null;
        w2vFile = null;
        trainFile = null;
        testFile = null;
        featureOutputFile = null;
        predictionFile = null;
        labelMappingsFileSentiment = null;
        labelMappingsFileRelevance = null;
        labelMappingsFileAspect = null;
        labelMappingsFileAspectCoarse = null;
        relevanceModel = null;
        aspectModel = null;
        aspectCoarseModel = null;
        sentimentModel = null;
        modelFolder = null;
        missingWordsFile = null;
        DTExpansionFile = null;
        weightedW2vFile = null;
        weightedGloveFile = null;
        weightedIdfFile = null;
        polarityLexiconFile = null;
        aggregateGazeteerFile = null;
        DTConfigurationFile = null;
        DTfile = null;
        corpusFile = null;
        maxLengthFile = null;

        relevanceIdfFile = null;
        sentimentIdfFile = null;
        aspectIdfFile = null;
        aspectCoarseIdfFile = null;


        Configuration config = new Configuration();
        HashMap<String, String> fileLocation;
        fileLocation = config.readConfigurationFile(configurationFile);

        for(HashMap.Entry<String, String> entry: fileLocation.entrySet()){
            if (entry.getKey().equals("language")) {
                language = entry.getValue();
            } else if(entry.getKey().equals("format")){
                format = entry.getValue();
                if (format.compareTo("semeval16") == 0) {
                    semeval16 = true;
                }
            }else if(entry.getKey().equals("idfFile")) {
                idfFile = entry.getValue();
            } else if (entry.getKey().equals("minTermFrequency")) {
                minTermFrequency = Integer.parseInt(entry.getValue());
            }else if(entry.getKey().equals("idfGazeteerFile")){
                idfGazeteerFile = entry.getValue();
            }else if(entry.getKey().equals("positiveGazeteerFile")){
                positiveGazeteerFile = entry.getValue();
            }else if(entry.getKey().equals("negativeGazeteerFile")){
                negativeGazeteerFile = entry.getValue();
            }else if(entry.getKey().equals("gloveFile")){
                gloveFile = entry.getValue();
            }else if(entry.getKey().equals("w2vFile")){
                w2vFile = entry.getValue();
            }else if(entry.getKey().equals("trainFile")){
                trainFile = entry.getValue();
            }else if(entry.getKey().equals("testFile")){
                testFile = entry.getValue();
            }else if(entry.getKey().equals("featureOutputFile")){
                featureOutputFile = entry.getValue();
            }else if(entry.getKey().equals("predictionFile")){
                predictionFile = entry.getValue();
            }else if(entry.getKey().equals("relevanceModel")){
                relevanceModel = entry.getValue();
                labelMappingsFileRelevance = entry.getValue()+"_label_mappings.tsv";
            }else if(entry.getKey().equals("aspectModel")){
                aspectModel = entry.getValue();
                labelMappingsFileAspect = entry.getValue()+"_label_mappings.tsv";
            }else if(entry.getKey().equals("aspectCoarseModel")){
                aspectCoarseModel = entry.getValue();
                labelMappingsFileAspectCoarse = entry.getValue()+"_label_mappings.tsv";
            }else if(entry.getKey().equals("sentimentModel")) {
                sentimentModel = entry.getValue();
                labelMappingsFileSentiment = entry.getValue()+"_label_mappings.tsv";
            }else if(entry.getKey().equals("modelFolder") || entry.getKey().equals("crfModelFolder")){
                modelFolder = entry.getValue();
                if (!modelFolder.endsWith("/")){
                    modelFolder = modelFolder.concat("/");
                }
            } else if(entry.getKey().equals("missingWordsFile")) {
                missingWordsFile = entry.getValue();
            }else if(entry.getKey().equals("DTExpansionFile")) {
                DTExpansionFile = entry.getValue();
            }else if(entry.getKey().equals("weightedW2vFile")) {
                weightedW2vFile = entry.getValue();
            }else if(entry.getKey().equals("weightedGloveFile")) {
                weightedGloveFile = entry.getValue();
            }else if(entry.getKey().equals("weightedIdfFile")) {
                weightedIdfFile = entry.getValue();
            }else if(entry.getKey().equals("polarityLexiconFile")) {
                polarityLexiconFile = entry.getValue();
            }else if(entry.getKey().equals("aggregateGazeteerFile")) {
                aggregateGazeteerFile = entry.getValue();
            }else if(entry.getKey().equals("DTConfigurationFile")){
                DTConfigurationFile = entry.getValue();
            }else if(entry.getKey().equals(("DTfile"))){
                DTfile = entry.getValue();
            }else if(entry.getKey().equals(("corpus"))){
                corpusFile = entry.getValue();
            }else if(entry.getKey().equals(("maxLengthFile"))){
                maxLengthFile = entry.getValue();
            }else if(entry.getKey().equals(("relIdfTerms"))){
                relevanceIdfFile = entry.getValue();
            }else if(entry.getKey().equals(("sentIdfTerms"))){
                sentimentIdfFile = entry.getValue();
            }else if(entry.getKey().equals(("aspectIdfTerms"))){
                aspectIdfFile = entry.getValue();
            }else if(entry.getKey().equals(("aspectCoarseIdfTerms"))){
                aspectCoarseIdfFile = entry.getValue();
            }
        }
    }

    /**
     * Computes a feature vector out of all the feature name specified in the configuration file
     * @return a Vector containing all the specified feature
     */
    protected static Vector<FeatureExtractor> loadFeatureExtractors(String type) {
        int offset = 1;
        Vector<FeatureExtractor> features = new Vector<>();

        if(idfFile!=null){
            FeatureExtractor tfidf = new TfIdfFeature(idfFile, offset);
            offset += tfidf.getFeatureCount();
            features.add(tfidf);
        }
        if (type.compareTo("relevance") == 0 && relevanceIdfFile != null) {
                FeatureExtractor gazeteerIdf = new GazetteerFeature(relevanceIdfFile, offset);
                offset += gazeteerIdf.getFeatureCount();
                features.add(gazeteerIdf);
        } else if(type.compareTo("sentiment") == 0 && sentimentIdfFile != null) {
                FeatureExtractor gazeteerIdf = new GazetteerFeature(sentimentIdfFile, offset);
                offset += gazeteerIdf.getFeatureCount();
                features.add(gazeteerIdf);
        } else if(type.compareTo("aspect") == 0 && aspectIdfFile != null) {
            FeatureExtractor gazeteerIdf = new GazetteerFeature(aspectIdfFile, offset);
            offset += gazeteerIdf.getFeatureCount();
            features.add(gazeteerIdf);
        }

        if (positiveGazeteerFile!= null) {
            FeatureExtractor posDict = new AggregatedGazetteerFeature(positiveGazeteerFile, offset);
            offset += posDict.getFeatureCount();
            features.add(posDict);
        }
        if (negativeGazeteerFile!= null) {
            FeatureExtractor negDict = new AggregatedGazetteerFeature(negativeGazeteerFile, offset);
            offset += negDict.getFeatureCount();
            features.add(negDict);
        }
        if(polarityLexiconFile!=null){
            FeatureExtractor polarityLexicon = new PolarityLexiconFeature(polarityLexiconFile, offset);
            offset+=polarityLexicon.getFeatureCount();
            features.add(polarityLexicon);
        }
        if(aggregateGazeteerFile!=null){
            FeatureExtractor aggregatedGazeteerFeature = new AggregatedGazetteerFeature(aggregateGazeteerFile, offset);
            offset+=aggregatedGazeteerFeature.getFeatureCount();
            features.add(aggregatedGazeteerFeature);
        }
        if (gloveFile!=null){
            FeatureExtractor glove = new WordEmbeddingFeature(gloveFile, null, 1, DTExpansionFile, offset);
            offset+=glove.getFeatureCount();
            features.add(glove);
        }
        if(w2vFile!=null){
            FeatureExtractor word2vec = new WordEmbeddingFeature(w2vFile, null, 2, DTExpansionFile,  offset);
            offset+=word2vec.getFeatureCount();
            features.add(word2vec);
        }
        if(weightedGloveFile!=null && weightedIdfFile!=null){
            FeatureExtractor word2vec = new WordEmbeddingFeature(weightedGloveFile, weightedIdfFile, 1, DTExpansionFile,  offset);
            offset+=word2vec.getFeatureCount();
            features.add(word2vec);
        }
        if(weightedW2vFile!=null && weightedIdfFile!=null){
            FeatureExtractor word2vec = new WordEmbeddingFeature(weightedW2vFile, weightedIdfFile, 2, DTExpansionFile,  offset);
            offset+=word2vec.getFeatureCount();
            features.add(word2vec);
        }
        return features;
    }

    /**
     * Builds a problem - the input feature matrix, output labels, total number of feature instances and the feature count
     * @param trainingFile path to the training file
     * @param features feature vector of all the features specified
     * @param type
     * @param ifTraining specifies if this method is used for training or testing
     */
    protected static Problem buildProblem(String trainingFile, Vector<FeatureExtractor> features, String type, Boolean ifTraining) {
        if(ifTraining){
            resetLabelMappings();
        }
        printFeatureStatistics(features);


        if (trainingFile.endsWith("xml")) {
            if (semeval16) {
                fr = new XMLReaderSemEval(trainingFile);
            } else {
                fr = new XMLReader(trainingFile);
            }
        } else {
            fr = new TsvReader(trainingFile);
        }

        int documentCount = 0;
        Vector<Double> labels = new Vector<>();
        Vector<Feature[]> featureVector = new Vector<>();
        Vector<Feature[]> instanceFeatures = null;
        String[] stringLabel = null;

        for (Document doc: fr) {
            for(Sentence sentence:doc.getSentences()){
             preprocessor.processText(sentence.getText());
             instanceFeatures = applyFeatures(preprocessor.getCas(), features);
                if (type == null) {
                    stringLabel = sentence.getAspectCategories();
                }else if(type.compareTo("relevance") == 0){
                    stringLabel = sentence.getRelevance();
                }
                else if (type.compareTo("sentiment") == 0) {
                    try {
                        stringLabel = sentence.getSentiment();
                    } catch (NoSuchFieldException e) {            // COMMENT HERE
                        continue;
                    }
                } else if (type.compareTo("aspect") == 0) {
                    if (useCoarseLabels) {
                        stringLabel = sentence.getAspectCategoriesCoarse();
                    } else {
                        stringLabel = sentence.getAspectCategories();
                    }
                }
                for (String l : stringLabel) {
                    if (l == null || l.isEmpty()) {continue;}
                    Double label = getLabelId(l);
                    labels.add(label);
                    featureVector.add(combineInstanceFeatures(instanceFeatures));
                    documentCount++;
                }
            }
        }

        if (featureOutputFile != null) {
            saveFeatureVectors(featureOutputFile, featureVector, labels);
        }

        Problem problem = new Problem();
        problem.l = documentCount;
        problem.n = featureCount;
        problem.x = new Feature[documentCount][];
        problem.y = new double[documentCount];

        for (int i = 0; i<labels.size(); i++) {
            problem.y[i] = labels.get(i);
            problem.x[i] = featureVector.get(i);

        }
        return problem;
    }

    /**
     * Builds a problem - the input feature matrix, output labels, total number of feature instances and the feature count
     * @param trainingFile path to the training file
     * @param features feature vector of all the features specified
     * @param ifTraining specifies if this method is used for training or testing
     */
    protected static Problem buildProblem(String trainingFile, Vector<FeatureExtractor> features, Boolean ifTraining) {
        resetLabelMappings();
        printFeatureStatistics(features);
        return buildProblem(trainingFile, features, null, ifTraining);
    }

    protected static Vector<Feature[]> applyFeatures(JCas cas, Vector<FeatureExtractor> features) {
        Vector<Feature[]> instanceFeatures = new Vector<>();
        for (FeatureExtractor feature : features) {
            instanceFeatures.add(feature.extractFeature(cas));
            // update the featureCount, the maximal Feature id
            featureCount = feature.getFeatureCount() + feature.getOffset();
        }
        return instanceFeatures;
    }

    protected static Feature[] combineInstanceFeatures(Vector<Feature[]> instanceFeatures) {
        int length = 0;
        for (Feature[] f : instanceFeatures) {
            length += f.length;
        }
        Feature[] instance = new Feature[length];
        int i=0;
        for (Feature[] fa : instanceFeatures) {
            for (Feature value : fa) {
                instance[i++] = value;
            }
        }
        return instance;
    }

    protected static void saveLabelMappings(String mappingFile) {
        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(mappingFile), "UTF-8"));
            for (String label : labelMappings.keySet()) {
                out.write(labelMappings.get(label) + "\t" + label + "\n");
            }
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    protected static Double getLabelId(String label) {
        if (labelMappings.containsKey(label)) {
            return labelMappings.get(label).doubleValue();
        } else {
            labelMappings.put(label, ++maxLabelId);
            labelLookup.put(maxLabelId, label);
            return maxLabelId.doubleValue();
        }
    }

    protected static String getLabelString(Double labelId) {
        return labelLookup.get(labelId.intValue());
    }

    protected static void saveFeatureVectors(String featureVectorFile, Vector<Feature[]> featureVector, Vector<Double> labels) {
        if (featureVectorFile == null) {return;}
        try {
            Writer featureOut = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(featureVectorFile), "UTF-8"));
            for (int i = 0; i< labels.size(); i++) {
                featureOut.write(labels.get(i).toString());
                Feature[] features = featureVector.get(i);
                for (Feature f : features) {
                    featureOut.write(" " + f.getIndex() + ":" + f.getValue());
                }
                featureOut.write("\n");
            }
            featureOut.close();
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void printFeatureStatistics(Vector<FeatureExtractor> features) {
        if (featureStatisticsFile != null) {
            try {
                Writer statisticsOut = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(featureStatisticsFile), "UTF-8"));
                statisticsOut.write("training set: "+ trainFile + "\n");
                if (featureStatisticsFile != null) {
                    int start;
                    int end;
                    for (FeatureExtractor feature : features) {
                        start = feature.getOffset();
                        end = feature.getOffset() + feature.getFeatureCount();
                        statisticsOut.append(feature.getClass().getCanonicalName() + "\t" + start + "\t" + end + "\n");
                    }
                }
                statisticsOut.close();
            } catch (UnsupportedEncodingException | FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void resetLabelMappings() {
        labelMappings = new HashMap<>();
        labelLookup = new HashMap<>();
        maxLabelId = -1;
    }

    protected static void loadLabelMappings(String fileName) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                String[] catLine = line.split("\\t");
                Integer labelId = Integer.parseInt(catLine[0]);
                labelLookup.put(labelId, catLine[1]);

                labelMappings.put(catLine[1], labelId);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected static INDArray classifyTestSet(String inputFile, Model model, Vector<FeatureExtractor> features, String predictionFile, String type, boolean printResult) {
        InputReader fr;
        if (inputFile.endsWith("xml")) {
            fr = new XMLReader(inputFile);
        } else {
            fr = new TsvReader(inputFile);
        }

        Writer out = null;
        Writer featureOut = null;

        try {
            OutputStream predStream = new FileOutputStream(predictionFile);
            out = new OutputStreamWriter(predStream, "UTF-8");
            if (featureOutputFile != null) {
                OutputStream vectorStream = new FileOutputStream(featureOutputFile);
                featureOut = new OutputStreamWriter(vectorStream, "UTF-8");
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

        Feature[] instance;
        Vector<Feature[]> instanceFeatures;

        confusionMatrix = new ConfusionMatrix();
        String item;
        for (int j = 0; j < model.getNrClass(); j++) {
            item = labelLookup.get(Integer.parseInt(model.getLabels()[j]+""));
            confusionMatrix.addLabel(item);
            allLabels.add(item);
        }

        ArrayList<double[]> probability = new ArrayList<>();
        confusionMatrix.createMatrix();

        for (Document doc : fr) {
            for(Sentence sentence:doc.getSentences()){
                int i = 0;
                preprocessor.processText(sentence.getText());
                instanceFeatures = applyFeatures(preprocessor.getCas(), features);
                Double prediction;
                instance = combineInstanceFeatures(instanceFeatures);
                double[] prob_estimates = new double[model.getNrClass()];
                prediction = Linear.predictProbability(model, instance, prob_estimates);
                probability.add(prob_estimates);
                try {
                    out.write(sentence.getId() + "\t" + sentence.getText() + "\t");
                    String goldLabel = null;
                    String predictedLabel = labelLookup.get(prediction.intValue());
                    if (type.compareTo("relevance") == 0) {
                        goldLabel = sentence.getRelevance()[0];
                        System.out.println(goldLabel);
                        System.out.println(predictedLabel);
                        confusionMatrix.updateMatrix(predictedLabel, goldLabel);
                    } else if (type.compareTo("sentiment") == 0) {
                        try {
                        while(i < sentence.getSentiment().length){
                            goldLabel = sentence.getSentiment()[i++];
                            confusionMatrix.updateMatrix(predictedLabel, goldLabel);
                        }
                        } catch (NoSuchFieldException e) {
                        }
                    } else if (useCoarseLabels) {
                        out.append(StringUtils.join(sentence.getAspectCategoriesCoarse(), " "));
                        goldLabel = StringUtils.join(sentence.getAspectCategoriesCoarse(), " ");
                        confusionMatrix.updateMatrix(predictedLabel, goldLabel);
                    } else {
                        out.append(StringUtils.join(sentence.getAspectCategories(), " "));
                        goldLabel = StringUtils.join(sentence.getAspectCategories(), " ");
                        confusionMatrix.updateMatrix(predictedLabel, goldLabel);
                    }
                    out.append("\t").append(labelLookup.get(prediction.intValue())).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (featureOutputFile != null) {
                    String[] labels = sentence.getAspectCategories();
                    if (useCoarseLabels) { labels = sentence.getAspectCategoriesCoarse(); }
                    for (String label : labels) {
                        try {
                            assert featureOut != null;
                            for (Feature f : instance) {
                                featureOut.write(" " + f.getIndex() + ":" + f.getValue());
                            }
                            featureOut.write("\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        INDArray classificationProbability = Nd4j.zeros(probability.size(), model.getNrClass());
        int j=-1;
        for(double prob_estimates[]:probability){
            classificationProbability.putRow(++j, Nd4j.create(prob_estimates));
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String, Float> recall;
        HashMap<String, Float> precision;
        HashMap<String, Float> fMeasure;

        recall = getRecallForAll();
        precision = getPrecisionForAll();
        fMeasure = getFMeasureForAll();

        if(printResult){
            System.out.println("Label"+"\t"+"Recall"+"\t"+"Precision"+"\t"+"F Score");
            for(String itemLabel: allLabels){
                System.out.println(itemLabel+"\t"+recall.get(itemLabel)+"\t"+precision.get(itemLabel)+"\t"+fMeasure.get(itemLabel));
            }
            printFeatureStatistics(features);
            printConfusionMatrix();
            System.out.println("\n");
            System.out.println("True positive     : " + getTruePositive());
            System.out.println("Accuracy          : " + getOverallAccuracy());
            System.out.println("Overall Precision : " + getOverallPrecision());
            System.out.println("Overall Recall    : " + getOverallRecall());
            System.out.println("Overall FMeasure  : " + getOverallFMeasure());
        }

        return classificationProbability;
    }




    protected static void printConfusionMatrix(){
        confusionMatrix.printConfusionMatrix();
    }

    protected static double getRecallForLabel(String label){
        return confusionMatrix.getRecallForLabel(label);
    }

    protected static double getPrecisionForLabel(String label){
        return confusionMatrix.getPrecisionForLabel(label);
    }

    protected static HashMap<String, Float> getRecallForAll(){
        return confusionMatrix.getRecallForAllLabels();
    }

    protected static HashMap<String, Float> getPrecisionForAll(){
        return confusionMatrix.getPrecisionForAllLabels();
    }

    protected static HashMap<String, Float> getFMeasureForAll(){
        return confusionMatrix.getFMeasureForAllLabels();
    }

    protected static int getTruePositive(){
        return confusionMatrix.getTruePositive();
    }

    protected static float getOverallAccuracy(){return confusionMatrix.getOverallAccuracy(); }

    protected static float getOverallRecall(){
        return confusionMatrix.getOverallRecall();
    }

    protected static float getOverallPrecision(){
        return confusionMatrix.getOverallPrecision();
    }

    protected static float getOverallFMeasure(){
        return confusionMatrix.getOverallFMeasure()   ;
    }
}
