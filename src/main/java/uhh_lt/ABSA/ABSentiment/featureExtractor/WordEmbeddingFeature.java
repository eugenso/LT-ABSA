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

package uhh_lt.ABSA.ABSentiment.featureExtractor;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import org.apache.uima.jcas.JCas;
import org.jblas.FloatMatrix;
import uhh_lt.ABSA.ABSentiment.featureExtractor.util.GenericWordSpace;
import uhh_lt.ABSA.ABSentiment.featureExtractor.util.GloVeSpace;
import uhh_lt.ABSA.ABSentiment.featureExtractor.util.VectorMath;
import uhh_lt.ABSA.ABSentiment.featureExtractor.util.W2vSpace;
import uhh_lt.ABSA.ABSentiment.uimahelper.Preprocessor;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Word Embedding {@link FeatureExtractor}, extracts the averaged word representation for an instance using a word embedding file.
 */
public class WordEmbeddingFeature implements FeatureExtractor {

    private int offset = 0;
    private int featureCount = 0;
    private String weightedIdfFile;
    private static GenericWordSpace<FloatMatrix> model;
    private static TfIdfFeature tfIdfFeature;
    private static HashMap<String, ArrayList<String>> DTExpansion = null;

    /**
     * Constructor; specifies the word embedding file. The type of word embedding. Feature offset is set to '0' by default.
     * @param embeddingFile path to the file containing word embeddings
     * @param wordRepresentation specifies the type of word embedding to be used
     */
    public WordEmbeddingFeature(String embeddingFile, String weightedIdfFile, int wordRepresentation, String DTExpansionFile){
        this.weightedIdfFile = weightedIdfFile;

        if(wordRepresentation == 1){
            model = GloVeSpace.load(embeddingFile, true, true);
        }else{
            model = W2vSpace.load(embeddingFile, true);
        }
        featureCount = model.getVectorLength();

        if(this.weightedIdfFile != null){
            tfIdfFeature = new TfIdfFeature(this.weightedIdfFile);
        }

        if(DTExpansionFile != null){
            DTExpansion = new HashMap<>();
            BufferedReader bufferedReader;
            try {
                bufferedReader = new BufferedReader(new FileReader(DTExpansionFile));
                String word = bufferedReader.readLine();
                while(word!=null){
                    String words[] = word.split("\t");
                    String wordList[] = words[1].split(" ");
                    ArrayList<String> expansionWord = new ArrayList<>();
                    for(int i=0;i<wordList.length;i++){
                        expansionWord.add(wordList[i]);
                    }
                    DTExpansion.put(words[0], expansionWord);
                    word = bufferedReader.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructor; specifies the word embedding file. The type of word embedding. Feature offset is specified.
     * @param embeddingFile path to the file containing word embeddings
     * @param wordRepresentation specifies the type of word embedding to be used
     * @param offset the feature offset, all features start from this offset
     */
    public WordEmbeddingFeature(String embeddingFile, String weightedIdfFile, int wordRepresentation, String DTExpansionFile, int offset){
        this(embeddingFile, weightedIdfFile, wordRepresentation, DTExpansionFile);
        this.offset = offset;
    }

    private Preprocessor preprocessor = new Preprocessor(true);

    @Override
    public Feature[] extractFeature(JCas cas) {
        Collection<String> documentText = preprocessor.getTokenStrings(cas);
        FloatMatrix wordVector = new FloatMatrix(featureCount);
        int num = 0;
        float termTfIdfWeight;
        if(weightedIdfFile != null){
            for (String token : documentText) {
                termTfIdfWeight = 1;
                if(tfIdfFeature.containsToken(token)){
                    termTfIdfWeight = (float) tfIdfFeature.getIdfScore(token);
                }
                if(model.contains(token)){
                    wordVector = wordVector.add(model.vector(token).mul(termTfIdfWeight));
                    num++;
                }else{
                    if((DTExpansion != null) && (DTExpansion.containsKey(token))) {
                        for (String word : DTExpansion.get(token)) {
                            if (model.contains(word)) {
                                wordVector = wordVector.add(model.vector(word).mul(termTfIdfWeight));
                                num++;
                                break;
                            }
                        }
                    }
                }
            }
        }else{
            for (String token : documentText) {
                if(model.contains(token)){
                    wordVector = wordVector.add(model.vector(token));
                    num++;
                }else{
                    if((DTExpansion != null) && (DTExpansion.containsKey(token))) {
                        for (String word : DTExpansion.get(token)) {
                            if (model.contains(word)) {
                                wordVector = wordVector.add(model.vector(word));
                                num++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        Feature[] instance = new Feature[featureCount];
        if(VectorMath.sum(wordVector) != 0.0){
            wordVector = VectorMath.normalize(wordVector);
        }
        for(int i=0;i<featureCount;i++){
            instance[i] = new FeatureNode(i+offset+1, wordVector.get(i));
        }
        return instance;
    }

    @Override
    public int getFeatureCount() {
        return featureCount;
    }

    @Override
    public int getOffset() {
        return offset;
    }
}
