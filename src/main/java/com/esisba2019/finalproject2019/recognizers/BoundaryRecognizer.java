package com.esisba2019.finalproject2019.recognizers;

import com.esisba2019.finalproject2019.tokenizers.Token;
import com.esisba2019.finalproject2019.tokenizers.TokenType;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class BoundaryRecognizer implements IRecognizer {
    private Pattern whitespacePattern;
    private Pattern punctuationPattern;
    private Pattern numberPattern;
    public void init() {
        this.whitespacePattern = Pattern.compile("\\s+");
        this.punctuationPattern = Pattern.compile("\\p{Punct}");
        this.numberPattern = Pattern.compile("^[\\+\\-]{0,1}[0-9]+[\\.\\,][0-9]+$"+"|\\d+");
            }
    public List<Token> recognize(List<Token> tokens) {
        List<Token> extractedTokens = new LinkedList<Token>();
        for (Token token : tokens) {
            TokenType type = token.getType();
            if (type != (TokenType.NUMBER) && type != (TokenType.PUNCTUATION) && type != (TokenType.UNKNOWN)) {
                extractedTokens.add(token);
            } else {
                extractedTokens.remove(token);
            }
        }
            return extractedTokens;
        }
}
