package com.esisba2019.finalproject2019.tokenizers.lucene;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;

public class NumericTokenFilter extends TokenFilter {

    private TermAttribute termAttribute;

    public NumericTokenFilter(TokenStream input) {
        super(input);
        this.termAttribute = (TermAttribute) addAttribute(TermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String term = termAttribute.term();
            term = term.replaceAll(",", "");
            if (!NumberUtils.isNumber(term)) {
                return true;
            }
        }
        return false;
    }}
