package com.esisba2019.finalproject2019.recognizers;

import com.esisba2019.finalproject2019.extractContent.DateRecognizer;
import com.esisba2019.finalproject2019.tokenizers.SentenceTokenizer;
import com.esisba2019.finalproject2019.tokenizers.Token;
import com.esisba2019.finalproject2019.tokenizers.WordTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Controller
public class RecognizerChainTest {
    private final Log log = LogFactory.getLog(getClass());
    private static RecognizerChain chain;
    public RecognizerChainTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    }

    @RequestMapping(value="/recognize")
    public static String mainmethod() throws Exception {
        DateRecognizer dateRecognizer=new DateRecognizer();
        dateRecognizer.recognition();
        setupBeforeClass();
        testRecognizeAbbreviations();

        return "classification4";
    }

    @BeforeClass
    public static void setupBeforeClass() throws Exception {
        chain = new RecognizerChain(Arrays.asList(new IRecognizer[]{
                new BoundaryRecognizer(),
                new StopwordRecognizer()
        }));
        chain.init();
//        return "uploadstatusview";
    }

//    @RequestMapping(value="/recognize")
    @Test
    public static void testRecognizeAbbreviations() throws Exception {

        File repertoire = new File("input/");
        File[] files = repertoire.listFiles();
        for (File file : files) {
            List<String> paragraph =Files.readAllLines(Paths.get(file.getPath()));
        SentenceTokenizer sentenceTokenizer = new SentenceTokenizer();
        sentenceTokenizer.setText(String.valueOf(paragraph));
        WordTokenizer wordTokenizer = new WordTokenizer();
        List<Token> tokens = new LinkedList<Token>();
        String sentence = null;
        while ((sentence = sentenceTokenizer.nextSentence()) != null) {
            System.out.println(sentence);
            wordTokenizer.setText(sentence);
            Token token = null;
            while ((token = wordTokenizer.nextToken()) != null) {
                BoundaryRecognizer b=new BoundaryRecognizer();
                b.recognize(tokens);
                tokens.add(token);
            }
            File output=new File(String.valueOf(file));
            File targetLocation = new File("input2/" + "/"+output.getName());

            List<Token> recognizedTokens = chain.recognize(tokens);
            for (Token recognizedToken : recognizedTokens) {
                FileWriter fileWriter = new FileWriter(targetLocation,true); //Set true for append mode
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.println(recognizedToken.getValue().toLowerCase().replaceAll("'"," ").
                        replaceAll("!"," "));  //New line
                printWriter.close();

                }
           tokens.clear();
        }
    }
//    return "stemmer";
    }


}
