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

package uhh_lt.ABSA.ABSentiment.reader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uhh_lt.ABSA.ABSentiment.featureExtractor.util.Pair;
import uhh_lt.ABSA.ABSentiment.type.Document;
import uhh_lt.ABSA.ABSentiment.type.Opinion;
import uhh_lt.ABSA.ABSentiment.type.Sentence;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * XML input reader to read SemEval sentiment XML format.
 */
public class XMLReaderSemEval implements InputReader {

    private DocumentBuilderFactory dbFactory;
    private DocumentBuilder dBuilder;
    private org.w3c.dom.Document doc;

    private NodeList reviewList;
    private int reviewPosition;

    private static final String docTag = "Review";
    private static final String docAttrId = "rid";
    private static final String sentenceTag = "sentence";
    private static final String sentenceAttrId = "id";
    private static final String opinionTag = "Opinion";
    private static final String opinionAttrCategory = "category";
    private static final String opinionAttrPolarity = "polarity";
    private static final String opinionAttrTarget = "target";
    private static final String opinionAttrTargetBegin = "from";
    private static final String opinionAttrTargetEnd = "to";

    /**
     * Constructor, creates a {@link org.w3c.dom.Document} from an input file.
     * @param filename path and file name of the .xml file
     */
    public XMLReaderSemEval(String filename) {
        dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            doc = dBuilder.parse(new FileInputStream(filename), "UTF-8");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("File could not be opened: " +filename );
            e.printStackTrace();
            System.exit(1);
        }
        doc.getDocumentElement().normalize();

        reviewList = doc.getElementsByTagName(docTag);
        reviewPosition = 0;
    }


    @Override
    public Iterator<Document> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return reviewPosition < reviewList.getLength();
    }

    @Override
    public Document next() {
        Node reviewNode = reviewList.item(reviewPosition);
        if (reviewNode.getNodeType() == Node.ELEMENT_NODE) {
            Element elem = (Element) reviewNode;
            return buildDocument(elem);

        } else {
            return null;
        }
    }

    /**
     * Creats a {@link Document} from an element node in the XML graph
     * @param element the element node containing the docuemnt
     * @return a {@link Document} object with id, label and sentences (text)
     */
    private Document buildDocument(Element element) {
        Document doc = new Document();
        doc.setDocumentId(element.getAttribute(docAttrId));
        NodeList sList = element.getElementsByTagName(sentenceTag);

        Sentence s;

        for (int sI = 0; sI < sList.getLength(); sI++) {
            Node sNode = sList.item(sI);

            s = new Sentence( sNode.getTextContent());
            s.setId(((Element) sNode).getAttribute(sentenceAttrId));
            s.addOpinions(getOpinions(sNode));

            doc.addSentence(s);
        }
        reviewPosition++;
        return doc;
    }

    /**
     * Extracts opinions from a sentence element.
     * @param sNode the sentence Node
     * @return a Vector of opinions for the sentence
     */
    private ArrayList<Opinion> getOpinions(Node sNode) {
        String category,polarity,target;
        int to, from;
        ArrayList<Opinion> opinions = new ArrayList<>();
        NodeList oList = ((Element) sNode).getElementsByTagName(opinionTag);
        for (int oI = 0; oI < oList.getLength(); oI++) {
            Node oNode = oList.item(oI);

            category = ((Element) oNode).getAttribute(opinionAttrCategory);
            polarity = ((Element) oNode).getAttribute(opinionAttrPolarity);
            target = ((Element) oNode).getAttribute(opinionAttrTarget);
            Opinion o = new Opinion(category, polarity, target);

            String fromString = ((Element) oNode).getAttribute(opinionAttrTargetBegin);
            if (fromString != null && !fromString.isEmpty()) {
                from = Integer.parseInt(fromString);
                to = Integer.parseInt(((Element) oNode).getAttribute(opinionAttrTargetEnd));
                o.addTarget(new Pair<>(from,to));
            }
            opinions.add(o);
        }
        return  opinions;
    }

}