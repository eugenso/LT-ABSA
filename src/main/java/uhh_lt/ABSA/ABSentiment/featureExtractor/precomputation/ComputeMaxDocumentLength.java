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

import uhh_lt.ABSA.ABSentiment.reader.InputReader;
import uhh_lt.ABSA.ABSentiment.reader.TsvReader;
import uhh_lt.ABSA.ABSentiment.reader.XMLReader;
import uhh_lt.ABSA.ABSentiment.type.Document;

/**
 * Computes maximum document length for a corpus in TSV format
 */
public class ComputeMaxDocumentLength {

    /**
     * Computes the maximal document length for an input file and stores the result in a file.
     * @param inputFile file containing the input corpus
     * @param outputFile path to the output file which will contain the integer number
     */
    public static void computeMaxDocumentLength(String inputFile, String outputFile) {
        MaxDocumentLength ml = new MaxDocumentLength();
        InputReader fr;
        if (inputFile.endsWith("xml")) {
            fr = new XMLReader(inputFile);
        } else {
            fr = new TsvReader(inputFile);
        }

        System.out.println("Computing corpus length...\n");
        int i = 0;
        for (Document d: fr) {
            ml.addDocument(d);
            i++;
            if (i % 10000 == 0) {
                System.out.print("\n" + i + " ");
            } else if (i % 1000 == 0) {
                System.out.print(".");
            //} else if (i % 100 == 0) {
            //    System.out.print(",");
            }
        }
        ml.saveMaxLength(outputFile);
    }

}
