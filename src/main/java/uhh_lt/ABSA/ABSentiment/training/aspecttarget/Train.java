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

package uhh_lt.ABSA.ABSentiment.training.aspecttarget;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.crfsuite.CrfSuiteStringOutcomeDataWriter;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.util.cr.FilesCollectionReader;
import uhh_lt.ABSA.ABSentiment.classifier.aspecttarget.AspectAnnotator;
import uhh_lt.ABSA.ABSentiment.reader.ConllReader;
import uhh_lt.ABSA.ABSentiment.training.util.ProblemBuilder;
import uhh_lt.ABSA.ABSentiment.util.XMLExtractorTarget;

import java.io.File;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

/**
 * Aspect Target Identification Model Trainer
 */
public class Train extends ProblemBuilder {

    /**
     * Trains the model from an input file
     *
     * @param args optional: input file and directory for model
     */
    public static void main(String[] args) {

        if (args.length == 1) {
            configurationfile = args[0];
        }
        initialise(configurationfile);

        File modelDirectory = new File(modelFolder);
        String inputFile = trainFile;


        if (inputFile.endsWith("tsv")) {
            return;
        }

        if (inputFile.endsWith("xml")) {
            String[] xArgs = new String[3];
            xArgs[0] = inputFile;
            inputFile = inputFile.replace(".xml", "") + ".conll";
            if (inputFile.startsWith("/")) {
                inputFile = "." + inputFile;
            }
            xArgs[1] = inputFile;
            xArgs[2] = semeval16 ? "1": "0";
            XMLExtractorTarget.main(xArgs);
        }

        String modelLocation = modelFolder.concat("opennlp-"+language+"-pos-maxent.bin");

        File trainingFile = new File(inputFile);
        trainingFile.deleteOnExit();
        try {
            runPipeline(
                    FilesCollectionReader.getCollectionReaderWithSuffixes(trainingFile.getAbsolutePath(),
                            ConllReader.CONLL_VIEW, inputFile),
                    createEngine(ConllReader.class),
                    createEngine(OpenNlpPosTagger.class,
                            OpenNlpPosTagger.PARAM_MODEL_LOCATION, modelLocation),

                    createEngine(ClearNlpLemmatizer.class),
                    createEngine(AspectAnnotator.class,
                            CleartkSequenceAnnotator.PARAM_IS_TRAINING, true,
                            DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
                            modelDirectory.getAbsolutePath(),
                            DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
                            CrfSuiteStringOutcomeDataWriter.class));
            org.cleartk.ml.jar.Train.main(modelDirectory.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // remove temporary CRF data
        File model = new File(modelFolder +"crfsuite.model");
        model.deleteOnExit();
        File trainData = new File(modelFolder +"crfsuite.training");
        trainData.deleteOnExit();
        File encoders = new File(modelFolder +"encoders.ser");
        encoders.deleteOnExit();
        File manifest = new File(modelFolder +"MANIFEST.MF");
        manifest.deleteOnExit();

    }
}
