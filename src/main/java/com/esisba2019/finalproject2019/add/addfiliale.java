package com.esisba2019.finalproject2019.add;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Arrays;
import java.util.List;


public class addfiliale {

    public  void addd() throws IOException {
        File file = new File("filiale.txt");
//        String et = title.getText();
        String et = "Souad";

        double randomDouble = Math.random();
        randomDouble = randomDouble * 100 +44;
        int randomInt = (int) randomDouble;
        System.out.println(randomInt);

        FileWriter fileWriter = new FileWriter(file, true); //Set true for append mode
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println(randomInt+":"+" "+ et+":"+" "+"-- Document Separator -- reut2-013.sgm");
        printWriter.println(et);//New line
        printWriter.close();


    }

}
