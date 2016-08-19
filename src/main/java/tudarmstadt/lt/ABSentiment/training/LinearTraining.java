package tudarmstadt.lt.ABSentiment.training;


import de.bwaldvogel.liblinear.*;
import org.apache.uima.jcas.JCas;
import tudarmstadt.lt.ABSentiment.featureExtractor.FeatureExtractor;
import tudarmstadt.lt.ABSentiment.featureExtractor.TfIdfFeature;
import tudarmstadt.lt.ABSentiment.reader.InputReader;
import tudarmstadt.lt.ABSentiment.reader.TsvReader;
import tudarmstadt.lt.ABSentiment.type.Document;
import tudarmstadt.lt.ABSentiment.uimahelper.Preprocessor;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * Training Class that is extended by all linear learners.
 * Offers methods to train {@link Model}s using different {@link FeatureExtractor}s.
 */
public class LinearTraining {

    protected static InputReader fr;
    protected static Preprocessor preprocessor = new Preprocessor();

    private static Integer maxLabelId = -1;
    private static int featureCount = 0;

    protected static String idfFile = "idfmap.tsv";
    private static HashMap<String, Integer> labelMappings = new HashMap<>();

    /**
     * Loads and initializes {@link FeatureExtractor}s for training and testing. Ensures that there is no feature ID overlap between diefferent {@link FeatureExtractor}s.
     * @return a Vector of {@link FeatureExtractor} entries
     */
    protected static Vector<FeatureExtractor> loadFeatureExtractors() {
        int offset = 0;
        Vector<FeatureExtractor> features = new Vector<>();
        FeatureExtractor tfidf = new TfIdfFeature(idfFile, offset);
        offset += tfidf.getFeatureCount();
        features.add(tfidf);

        // FeatureExtractors are added to the features Vector;
        // the offset should be updated for each new FeatureExtractor to prevent overlapping Feature ids

        //FeatureExtractor tfidf2 = new TfIdfFeature(idfFile, offset);
        //offset += tfidf2.getFeatureCount();
        //features.add(tfidf2);
        return features;
    }

    /**
     * Builds the {@link Problem} from a training file, using provided {@link FeatureExtractor}s.
     * @param trainingFile path to the training file
     * @param features Vectot of {@link FeatureExtractor}s
     * @return @{link Problem}, containing the extracted features per instance
     */
    protected static Problem buildProblem(String trainingFile, Vector<FeatureExtractor> features) {
        fr = new TsvReader(trainingFile);

        int documentCount = 0;
        Vector<Double> labels = new Vector<>();
        Vector<Feature[]> featureVector = new Vector<>();
        Vector<Feature[]> instanceFeatures;
        for (Document d: fr) {
            preprocessor.processText(d.getDocumentText());
            instanceFeatures = applyFeatures(preprocessor.getCas(), features);

            // creates a training instance for each document label (multi-label training)
            for (String l : d.getLabels()) {
                Double label = getLabelId(l);
                labels.add(label);
                // combine feature vectors for one instance
                featureVector.add(combineInstanceFeatures(instanceFeatures));
                documentCount++;
            }
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
     * Applies {@link FeatureExtractor}s to a given CAS. Each {@link FeatureExtractor} creates an individual array of {@link Feature} entries.
     * @param cas the CAS that the {@link FeatureExtractor}s operate on
     * @param features a vector of {@link FeatureExtractor}s
     * @return a vector with an {@link Feature} array from each {@link FeatureExtractor}
     */
    protected static Vector<Feature[]> applyFeatures(JCas cas, Vector<FeatureExtractor> features) {
        Vector<Feature[]> instanceFeatures = new Vector<>();
        for (FeatureExtractor feature : features) {
            instanceFeatures.add(feature.extractFeature(cas));
            // update the featureCount, the maximal Feature id
            featureCount = feature.getFeatureCount() + feature.getOffset();
        }
        return instanceFeatures;
    }

    /**
     * Helper method to combine {@link Feature} arrays that are produced by several {@link FeatureExtractor}s, when applied to a single document.
     * @param instanceFeatures a Vector of {@link Feature} arrays that should be combined
     * @return one {@link Feature} array to be used as a training instance
     */
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

    /**
     * Saves the label--identifier mappings in a TAB separated file using the following format:<br />
     * LABEL_ID  &emsp; LABEL<br />
     * Allows for retrieving the original String labels in classification.
     * @param mappingFile the path to the file
     */
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

    /**
     * Gets the LabelId for a given String label. If the label has not been encountered yet, returns a new Double value.
     * @param label the String label
     * @return Double value to be used as an instance label
     */
    protected static Double getLabelId(String label) {
        if (labelMappings.containsKey(label)) {
            return labelMappings.get(label).doubleValue();
        } else {
            labelMappings.put(label, ++maxLabelId);
            return maxLabelId.doubleValue();
        }
    }

    /**
     * Trains the linear classifier {@link Model} from a given {@link Problem}. Returns the trained {@link Model}.
     * @param problem the Problem containing all the training instances
     * @return trained model
     */
    protected static Model trainModel(Problem problem) {
        SolverType solver = SolverType.L2R_LR;
        double C = 1.0;
        double eps = 0.1;
        Parameter parameter = new Parameter(solver, C, eps);

        return Linear.train(problem, parameter);
    }

    /**
     * Saves the {@link Model} in a model file.
     * @param model the model to be saved
     * @param modelFile path to the output file
     */
    protected static void saveModel(Model model, String modelFile) {
        try {
            model.save(new File(modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}