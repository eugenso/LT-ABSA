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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import uhh_lt.ABSA.ABSentiment.type.uima.AspectTarget;
import uhh_lt.ABSA.ABSentiment.type.uima.GoldAspectTarget;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * UIMA Writer for CONNL Output.
 */
public class AspectTermWriter extends JCasConsumer_ImplBase {
    public static final String OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(name = OUTPUT_FILE, mandatory = true)
    private File OutputFile = null;

    public static final String IS_GOLD = "isGold";
    @ConfigurationParameter(name = IS_GOLD, mandatory = false)
    private boolean isGold = false;

    public static final String SENTENCES_ID = "sentencesId";
    @ConfigurationParameter(name = SENTENCES_ID, mandatory = false)
    private List<String> sentencesId = null;

    public static final String LF = System.getProperty("line.separator");
    public static final String TAB = "\t";

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            FileWriter outputWriter = new FileWriter(OutputFile);
            Map<Sentence, Collection<AspectTarget>> sentencesAspect = JCasUtil.indexCovered(jCas,
                    Sentence.class, AspectTarget.class);


            List<Sentence> sentences = new ArrayList<>(sentencesAspect.keySet());
            // sort sentences by sentence
            Collections.sort(sentences, new Comparator<Sentence>() {
                @Override
                public int compare(Sentence arg0, Sentence arg1)
                {
                    return arg0.getBegin() - arg1.getBegin();
                }
            });
            for (Sentence sentence : sentences) {

                for (AspectTarget aspectAnnotation : sentencesAspect.get(sentence)) {

                    String text = aspectAnnotation.getCoveredText().replace(" ", "");

                    StringBuilder sb = new StringBuilder();
                    sb.append(text);
                    sb.append(TAB);
                    if (isGold) {
                        sb.append(JCasUtil.selectCovered(jCas, GoldAspectTarget.class, aspectAnnotation)
                                .get(0).getAspectTargetType());
                    }
                    sb.append(TAB);
                    sb.append(aspectAnnotation.getAspectTargetType());
                    sb.append(LF);
                    outputWriter.write(sb.toString());

                }
                outputWriter.write(LF);
            }
            outputWriter.close();

            getContext().getLogger().log(Level.INFO,
                    "Output written to: " + OutputFile.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}