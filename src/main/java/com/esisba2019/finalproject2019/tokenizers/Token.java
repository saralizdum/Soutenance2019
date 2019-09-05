package com.esisba2019.finalproject2019.tokenizers;

public class Token {

    public Token() {
        super();
    }

    public Token(String value, TokenType type) {
        this();
        setValue(value);
        setType(type);
    }

    private String value;
    public TokenType type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return value + " (" + type + ")";
    }
//    public String toString() {
//        return value;
//    }


}
