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

package uhh_lt.ABSA.ABSentiment.type;


import uhh_lt.ABSA.ABSentiment.featureExtractor.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Opinion {

    private char categorySep = '#';
    private String categoryCoarse;
    private String categoryFine;

    private ArrayList<Pair<Integer, Integer>> targets = new ArrayList<>();

    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }

    private String polarity;
    private String target;

    public Opinion(String category) {
        this.categoryFine = category;
        this.categoryCoarse = extractCoarseCategory(category);
    }

    public Opinion(String category, String polarity) {
        this(category);
        this.polarity = polarity;
    }

    public Opinion(String category, String polarity, String target) {
        this(category, polarity);
        this.target = target;
    }

    private String extractCoarseCategory(String categoryFine) {
        if (categoryFine.indexOf(categorySep) == -1) {
            return categoryFine;
        }
        return categoryFine.substring(0, categoryFine.indexOf(categorySep));
    }

    public String getCoarseCategory() throws NoSuchFieldException {
        if (categoryCoarse != null) {
            return categoryCoarse;
        } else {
            throw new NoSuchFieldException("The coarse category is not set");
        }
    }

    public String getFineCategory() throws NoSuchFieldException {
        if (categoryFine != null) {
            return categoryFine;
        } else {
            throw new NoSuchFieldException("The fine category is not set");
        }
    }

    public String getPolarity() throws NoSuchFieldException {
        if (polarity != null) {
            return polarity;
        } else {
            throw new NoSuchFieldException("The polarity is not set");
        }
    }

    public void setTarget(String t) {
        this.target = t;
    }

    public String getTarget() {
        return target;
    }

    public void addTarget(Pair<Integer, Integer> positions) {
        if (positions.getSecond() > 0) {
            targets.add(positions);
        }
    }

    public List<Pair<Integer, Integer>> getTargets() {
        return targets;
    }


    public String toString() {
        return "coarse: " + categoryCoarse + "\tfine: " + categoryFine + "\tpolarity: " + polarity;
    }

    public void setCategorySeparator(char separator) {
        this.categorySep = separator;
    }
}
