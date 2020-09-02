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
import uhh_lt.ABSA.ABSentiment.uimahelper.Preprocessor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Gazeteer {@link FeatureExtractor}, extracts binary features if words from a word list are found in a document.
 */
public class GazetteerFeature implements FeatureExtractor {

    private int offset = 0;

    private ArrayList<String> terms = new ArrayList<>();

    private Preprocessor preprocessor = new Preprocessor(true);

    /**
     * Constructor; specifies the gazetteer file. Feature offset is set to '0' by default.
     * @param gazetteer path to a wordlist
     */
    public GazetteerFeature(String gazetteer) {
        System.out.println("Gazetteer: loading " + gazetteer + "...");
        loadWordList(gazetteer);
    }

    /**
     * Constructor; specifies the gazetteer file. Feature offset is specified.
     * @param gazetteer path to a wordlist
     * @param offset the feature offset, all features start from this offset
     */
    public GazetteerFeature(String gazetteer, int offset) {
        this(gazetteer);
        this.offset = offset;
    }

    @Override
    public Feature[] extractFeature(JCas cas) {
        Collection<String> documentText = preprocessor.getTokenStrings(cas);
        int matchCount = 0;

        // find matches
        Vector<Integer> matches = new Vector<>();
        String term;
        for (int i = 0; i<terms.size(); i++) {
            term = terms.get(i);
            if (documentText.contains(term)) {
                matches.add(i+1);
                matchCount++;
            }
        }
        // construct feature array
        Feature[] features = new Feature[matches.size()];
        int i = 0;
        for (Integer match : matches) {
            features[i++] = new FeatureNode(match+offset, 1);
        }
        return features;
    }

    @Override
    public int getFeatureCount() {
        return terms.size();
    }

    @Override
    public int getOffset() {
        return offset;
    }


    /**
     * Loads a word list with words, other columns optional.
     * @param fileName the path to the wordlist
     */
    private void loadWordList(String fileName) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokenLine = line.split("\\t");
                terms.add(tokenLine[0]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
