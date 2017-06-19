package tudarmstadt.lt.ABSentiment.featureExtractor.util;

import org.jobimtext.api.struct.IThesaurusDatastructure;
import org.jobimtext.api.struct.Order2;
import org.jobimtext.api.struct.WebThesaurusDatastructure;
import tudarmstadt.lt.ABSentiment.training.util.ProblemBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abhishek on 24/5/17.
 */
public class DTWebAPI extends ProblemBuilder{

    protected static int numberOfSimilarWords = 6;
    protected static IThesaurusDatastructure<String, String> dt;
    protected static W2vSpace w2vSpace;
    protected static List<Order2> similarWords;

    public static void main(String args[]){
        initialise("configuration.txt");
        dt = new WebThesaurusDatastructure("conf_web_wikipedia_trigram.xml");
        dt.connect();
        w2vSpace =  W2vSpace.load(w2vFile);

        getExpansionUsingDT(missingWordsFile);
    }

    public static void getExpansionUsingDT(String fileName){
        HashMap<String, List<Order2>> hashMap = new HashMap<>();
        String word = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            word = bufferedReader.readLine();
            while(word!=null){
                similarWords = dt.getSimilarTerms(word, numberOfSimilarWords);
                hashMap.put(word, similarWords);
                word = bufferedReader.readLine();
            }
            bufferedReader.close();
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DTExpansionFile), "utf-8"));
            for(HashMap.Entry<String, List<Order2>> entry: hashMap.entrySet()){
                String item = entry.getKey();
                int flag = 0;
                for(Order2 element:entry.getValue()){
                    if(flag == 0){
                        flag = 1;
                    }else if(flag == 1){
                        item = item + "\t"+ element.key;
                        flag = 2;
                    }else{
                        item = item + " "+ element.key;
                    }
                }
                System.out.println(item);
                writer.write(item+"\n");
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
