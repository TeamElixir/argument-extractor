package citation_ditection;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import utils.NLPUtils;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CitationDetector {

    static ArrayList<String> prevCasePetitionerList = new ArrayList<>();
    static String prevSentence = "";
    static ArrayList<String> caseLawSentencesList = new ArrayList<>();
    static boolean isPreviousCitation = false;

    //todo: solve the issue with wrong sentence splitting and relationship with previous sentence (Ibid, id. see)
    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(new File("/home/viraj/FYP/jaeLee.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/viraj/FYP/jaeLeeSent.txt")));

        NLPUtils nlpUtils = new NLPUtils("tokenize,ssplit");

        //to remove the four sentences contatining topic and other meta data
        String topic = sc.nextLine();
        sc.nextLine();
        sc.nextLine();
        sc.nextLine();

        while(sc.hasNextLine()){
            String paragraph = sc.nextLine();
            Annotation ann = nlpUtils.annotate(paragraph);

            if(topic.trim().equals(paragraph.trim())){
                continue;
            }

            List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                //case_number_identify(sentence.toString());
                //v_identify(sentence.toString());
                //ibid_identify(sentence.toString());
                petitioner_based_identify(sentence.toString());
                bw.write(sentence.toString());
                bw.newLine();

                //keep this as the last line within for loop
                prevSentence = sentence.toString();
            }



        }

        bw.close();

    }

    public static boolean v_identify(String text){
        String regex = "^.*[A-Za-z]+\\sv\\.\\s[A-Za-z]+.*$";

        if(text.matches(regex)){
            //System.out.println(text);
            return true;
        }
        return false;
    }

    public static boolean case_number_identify(String text){
        String regex = "^.*[0-9]+[\\s]*U\\.[\\s]*S\\..*$";
        if(text.matches(regex)){
            System.out.println(text);
            return true;
        }
        return false;
    }

    public static boolean ibid_identify(String text){
        String regex_1 = "^.*Id\\..*$";
        String regex_2 = "^.*Ibid.*$";

        if(text.matches(regex_1) || text.matches(regex_2)){
            System.out.println(text);
            return true;
        }
        else{
            return false;
        }
    }

    public static boolean petitioner_based_identify(String text){
        // this part is to identify petitioner names from previous cases
        Pattern pattern = Pattern.compile("[A-Za-z]+\\sv\\.\\s[A-Za-z]+(\\s[A-Za-z])*[\\s|,][\\s][0-9]+\\sU.[\\s]S.");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
        {
            String matchedText = matcher.group();
            prevCasePetitionerList.add(matchedText.split("v.")[0].trim());
        }

        for(String petitionerName : prevCasePetitionerList){
            if(text.contains(petitionerName)){
                System.out.println(text);
                return true;
            }
        }
        return false;
    }

    public static boolean see_identify(String text){
        String regex = "^See\\s.*$";

        if(text.matches(regex)){
            return true;
        }
        return false;
    }


    public static boolean not_ended_sent_identify(String text){
        String regex = "^.*\"$";
        if(text.matches(regex)){
            return true;
        }
        return false;
    }




}
