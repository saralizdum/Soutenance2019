package com.esisba2019.finalproject2019.extractContent;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;


@Controller

public class Extractt {

    @RequestMapping(value="/extract")
    public String extract(){
        File repertoire = new File("uploads\\");
        List<File> list = Arrays.asList(repertoire.listFiles());
        if (list != null) {
            list.stream().forEach((file) -> {
                try {
                    main2(file);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

            });

        }
        return "classification3";
    }
    public void main2(File file) throws IOException, SAXException, TikaException {
        String path = file.getAbsolutePath();
            String[] ext = path.split("\\.");
            Parser parser = new AutoDetectParser();
            if (ext[0].equalsIgnoreCase("png") || ext[0].equalsIgnoreCase("jpg") || ext[0].equalsIgnoreCase("pdf")) {
                extractFromFile(parser, path);

            } else {
                extractFromFile(parser, path);
            }
        }

    public void extractFromFile(final Parser parser, final String fileName) throws IOException, SAXException, TikaException {
        long start = System.currentTimeMillis();
        BodyContentHandler handler = new BodyContentHandler(10000000);
        Metadata metaData = new Metadata();
        FileInputStream content = new FileInputStream(fileName);
        parser.parse(content, handler, metaData, new ParseContext());
//        System.out.println("\n\n File content : " + handler.toString());
        File output=new File(fileName+".txt");
        File targetLocation = new File("input\\" + "/"+output.getName());
        PrintStream p=new PrintStream(targetLocation);
        p.println(handler);
        System.out.println((String.format(".............Processing took %s millis\n\n", System.currentTimeMillis() - start)));
    }
}
