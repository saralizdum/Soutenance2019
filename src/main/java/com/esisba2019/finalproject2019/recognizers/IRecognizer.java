package com.esisba2019.finalproject2019.recognizers;

import com.esisba2019.finalproject2019.tokenizers.Token;

import java.util.List;

public interface IRecognizer {

    /**
     * Rule initialization code goes here.
     *
     * @throws Exception
     */
    public void init() throws Exception;


    public List<Token> recognize(List<Token> tokens);
}
