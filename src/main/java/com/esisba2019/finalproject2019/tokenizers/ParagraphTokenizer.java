package com.esisba2019.finalproject2019.tokenizers;

import com.ibm.icu.text.RuleBasedBreakIterator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.BreakIterator;

public class ParagraphTokenizer {

//    public static String paragraphrulesDirectory = System.getProperty("user.dir")+"/data/paragraph_break_rules.txt";
    private String text;
    private int index = 0;
    private RuleBasedBreakIterator breakIterator;

    public ParagraphTokenizer() throws Exception {
        this("data/paragraph_break_rules.txt");
    }

    public ParagraphTokenizer(String rulesfile) throws Exception {
        super();
        this.breakIterator = new RuleBasedBreakIterator(
                FileUtils.readFileToString(new File(rulesfile), "UTF-8"));
    }

    public void setText(String text) {
        this.text = text;
        this.breakIterator.setText(text);
        this.index = 0;
    }

    public String nextParagraph() {
        int end = breakIterator.next();
        if (end == BreakIterator.DONE) {
            return null;
        }
        String sentence = text.substring(index, end);
        index = end;
        return sentence;
    }
}
