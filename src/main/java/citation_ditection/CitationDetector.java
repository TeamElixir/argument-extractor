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
    static String petitionerTopic = "";

    //this variable is used to solve Ibid, Id relevance to caselaw sentences
    static boolean isPreviousCitation = false;

    //this variable is used to solve completeness of sentence
    static boolean iscompleted = true;

    //todo: solve the issue with wrong sentence splitting and relationship with previous sentence (Ibid, id. see)
    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(new File("/home/viraj/FYP/jaeLee.txt"));
        BufferedWriter bw_arg = new BufferedWriter(new FileWriter(new File("/home/viraj/FYP/jaeLee_arg.txt")));
        BufferedWriter bw_non_arg = new BufferedWriter(new FileWriter(new File("/home/viraj/FYP/jaeLee_non_arg.txt")));

        NLPUtils nlpUtils = new NLPUtils("tokenize,ssplit");

        //to remove the four sentences contatining topic and other meta data

        String supremecourtStmt = sc.nextLine();
        while(supremecourtStmt.trim().length()==0){
            supremecourtStmt = sc.nextLine();
        }

        String topic = sc.nextLine();
        while(topic.trim().length()==0){
            topic = sc.nextLine();
        }
        petitionerTopic = petitionerTopic(topic).toLowerCase();
        System.out.println(petitionerTopic);
        sc.nextLine();
        sc.nextLine();
        sc.nextLine();

        while(sc.hasNextLine()){
            String paragraph = sc.nextLine();
            Annotation ann = nlpUtils.annotate(paragraph);

            if(sentenceSimilarity(paragraph.toLowerCase().trim(),petitionerTopic)){
                continue;
            }

            List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                String line = completefiltering(sentence.toString());

                if(!pre_filtering(sentence.toString())){
                    continue;
                }
                if(line.length()==0){

                }else{
                    filterDuplicates(line);
                }
                //keep this as the last line within for loop
                prevSentence = sentence.toString();
            }
            //assumed that Ibid, id statements appear in the middle of paragraphs
            isPreviousCitation = false;
            iscompleted = true;
        }

        System.out.println();
        for( String sentence:caseLawSentencesList){
            bw_arg.write(sentence);
            bw_arg.newLine();
            bw_arg.flush();
        }

        for(String p: prevCasePetitionerList){
            System.out.println(p);
        }

        bw_arg.close();
        bw_non_arg.close();


    }

    public static String petitionerTopic(String text){
        Pattern pattern = Pattern.compile("([A-Za-z]+\\s)+v\\.\\s[A-Za-z]+(\\s[A-Za-z])*");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
        {
            String matchedText = matcher.group();
            return matchedText.split("v.")[0].trim();
        }
        return "";
    }

    public static boolean sentenceSimilarity(String text1, String text2){
        for(int i=0;i< Math.min(text1.length(),text2.length());i++){
            if(text1.charAt(i)!=text2.charAt(i)){
                if(i<10){
                    return false;
                }
            }
        }
        return true;
    }

    public static void filterDuplicates(String text){

        if(caseLawSentencesList.size()>0 && text.contains(caseLawSentencesList.get(caseLawSentencesList.size()-1))){
            caseLawSentencesList.remove(caseLawSentencesList.size()-1);
            caseLawSentencesList.add(text);
        }else{
            caseLawSentencesList.add(text);
        }

    }

    //Identifcation based on v. tag : ex: Hill v. Lockhart
    public static boolean v_identify(String text){
        String regex = "^.*[A-Za-z]+\\sv\\.\\s[A-Za-z]+.*$";

        Pattern pattern = Pattern.compile("[A-Za-z]+\\sv\\.\\s[A-Za-z]+(\\s[A-Za-z])*[\\s|,][\\s][0-9]+\\sU.[\\s]S.");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
        {
            String matchedText = matcher.group();
            String petitioner = matchedText.split("v.")[0].trim();
            if(!petitionerTopic.contains(petitioner) && petitioner.trim().length()>0 && !petitioner.toLowerCase().contains("states")){
                System.out.println(text);
                prevCasePetitionerList.add(petitioner);
            }
        }

        if(text.matches(regex)){
            //System.out.println(text);
            return true;
        }
        return false;
    }

    //Identification based on case number : 434 U. S. 579
    public static boolean case_number_identify(String text){
        String regex = "^.*[0-9]+[\\s]*U\\.[\\s]*S\\..*$";
        if(text.matches(regex)){
            //System.out.println(text);
            return true;
        }
        return false;
    }

    //identification based on Ibid and Id.
    public static boolean ibid_identify(String text){
        String regex_1 = "^.*Id\\..*$";
        String regex_2 = "^.*Ibid.*$";

        if(text.matches(regex_1) || text.matches(regex_2)){
            //System.out.println(text);
            return true;
        }
        else{
            return false;
        }
    }

    //Identication based on petitioner name : Pointing to Strickland, Government argues that...
    public static boolean petitioner_based_identify(String text){
        // this part is to identify petitioner names from previous cases
        for(String petitionerName : prevCasePetitionerList){
            if(text.contains(petitionerName)){
                return true;
            }
        }
        return false;
    }

    //identication based on see. See Ins v. Car
    public static boolean see_identify(String text){
        String regex = "^See,*\\s.*$";

        if(text.trim().matches(regex) && (text.contains("§") || v_identify(text) || case_number_identify(text) || petitioner_based_identify(text) || ibid_identify(text))){
            return true;
        }
        return false;
    }

    //identification of incomplete sentences  " ." "
    public static void check_sentence_completeness(String text){
        String regex = "^.*\"$";
        if(text.matches(regex)){
            iscompleted=false;
        }
        iscompleted=true;
    }

    //identification of tiny phrases which have no value
    public static boolean alpha_words_based_filtering(String text){

        String[] words = text.split(" ");
        String regex = "[A-Za-z]+,*\\.*";
        int count = 0;

        for(String word:words){
            if(word.matches(regex) && word.length()>3){
                count += 1;
            }
        }

        if(count > 6){
            return true;
        }
        return false;
    }

    //identification of App. term. this refers to the current case IMO
    public static boolean app_identify(String text){
        String regex = "^.*[Aa]pp\\..*$";
        if(text.matches(regex)){
            return true;
        }
        return false;
    }

    public static boolean pre_filtering(String text){
        String regex1 = "^Opinion of the Court.*$";
        String regex2 = "^No\\.\\s*[0-9]*-[0-9]*.*$";

        if(text.trim().matches(regex1) || text.trim().matches(regex2)){
            return false;
        }
        return true;
    }

    //todo: is completed is added for later use: to avoid unecessary tags from a case
    public static String completefiltering(String text){

        check_sentence_completeness(text);
        if(iscompleted){
            if(see_identify(text)){
                isPreviousCitation = true;
                if(alpha_words_based_filtering(prevSentence)){
                    return prevSentence+" "+text;
                }
            }

            if(!(v_identify(text) || case_number_identify(text) || petitioner_based_identify(text))){
                isPreviousCitation = false;
                return "";
            }

            if((v_identify(text) || case_number_identify(text) || petitioner_based_identify(text))
                    && !alpha_words_based_filtering(text)){
                isPreviousCitation = true;
                if(alpha_words_based_filtering(prevSentence)){
                    return prevSentence+" "+text;
                }

            }

            if((v_identify(text) || case_number_identify(text) || petitioner_based_identify(text))
                    && alpha_words_based_filtering(text)){
                isPreviousCitation = true;
                return text;
            }

            if(isPreviousCitation && ibid_identify(text)){
                if(alpha_words_based_filtering(prevSentence)){
                    return prevSentence;
                }
            }

            if(app_identify(text)){
                isPreviousCitation = false;
                return "";
            }
        }
        else{
            if(see_identify(text)){
                isPreviousCitation = true;
                if(alpha_words_based_filtering(prevSentence)){
                    return prevSentence+" "+text;
                }
            }

            if((v_identify(text) || case_number_identify(text) || petitioner_based_identify(text))
                    && alpha_words_based_filtering(text)){
                isPreviousCitation = true;
                return prevSentence+" "+text;
            }

            if((v_identify(text) || case_number_identify(text) || petitioner_based_identify(text))
                    && !alpha_words_based_filtering(text)){
                isPreviousCitation = true;
                if(alpha_words_based_filtering(prevSentence)){
                    return prevSentence+" "+text;
                }
            }

            if(isPreviousCitation && ibid_identify(text)){
                if(alpha_words_based_filtering(prevSentence)){
                    return prevSentence+" "+text;
                }
            }

            if(app_identify(text)){
                isPreviousCitation = false;
                return "";
            }
        }
        return "";
    }




}
