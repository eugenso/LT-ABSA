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

package uhh_lt.ABSA.ABSentiment.featureExtractor.precomputation;

import uhh_lt.ABSA.ABSentiment.featureExtractor.DocumentLengthFeature;
import uhh_lt.ABSA.ABSentiment.type.Document;
import uhh_lt.ABSA.ABSentiment.uimahelper.Preprocessor;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

/**
 * Computes the maximal document length from a collection of {@link Document}s.
 * This length can be used by the {@link DocumentLengthFeature}
 */
public class MaxDocumentLength {

    private int maxLength = 1;
    private int limit = 500;

    private Preprocessor preprocessor;

    /**
     * Constructor
     */
    public MaxDocumentLength() {
        preprocessor = new Preprocessor(true);
    }



    /**
     * Processes a {@link Document}, extracts tokens and increases their document frequency
     * @param d the Document that is added to the collection
     */
    public void addDocument(Document d) {
        preprocessor.processText(d.getDocumentText());

        int documentLength = preprocessor.getTokenStrings().size();
        if (documentLength > maxLength) {
            maxLength = documentLength;
        }

    }


    /**
     * Saves the IDF scores in a tab-separated format:<br>
     * TOKEN  &emsp; TOKEN_ID &emsp; IDF-SCORE &emsp; FREQUENCY
     * @param outFile path to the output file
     */
    protected void saveMaxLength(String outFile) {
        try {
            Writer out;
            if (outFile.endsWith(".gz")) {
                out = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outFile)), "UTF-8");
            } else {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outFile), "UTF-8"));
            }
            out.write(maxLength > limit ? limit : maxLength);
            out.write("\n");
            out.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }
}
