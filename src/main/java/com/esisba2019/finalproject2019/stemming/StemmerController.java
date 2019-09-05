package com.esisba2019.finalproject2019.stemming;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tartarus.snowball.SnowballStemmer;
import java.io.*;

@Controller
public class StemmerController {

    private static void usage()
    {
        System.err.println("Usage: StemmerController <algorithm> <input file> [-o <output file>]");
    }
    public static String[] args = new String[20];

	@RequestMapping(value="/stemmer")
    public String main4() throws Throwable {
	if (args.length < 2) {
            usage();
            return "classification4";
        }
        args[0]="french";
        args[1]="french";
        args[2]="-o";
        args[3]="002";

	Class stemClass = Class.forName("com.esisba2019.finalproject2019.stemming.FrenchStemmer");
        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
		File repertoire = new File("input2\\");
		File[] files = repertoire.listFiles();
		for (File testDoc : files) {
	Reader reader;

	reader = new InputStreamReader(new FileInputStream(testDoc.getPath()));
	reader = new BufferedReader(reader);

	StringBuffer input = new StringBuffer();

        OutputStream outstream;

	if (args.length > 2) {
            if (args.length >= 4 && args[2].equals("-o")) {
                outstream = new FileOutputStream("output\\" +testDoc.getName());
            } else {
                usage();
                return "classification4";
            }
	} else {
	    outstream = System.out;
	}
	Writer output = new OutputStreamWriter(outstream);
	output = new BufferedWriter(output);

	int repeat = 1;
	/*if (args.length > 4) {
	    repeat = Integer.parseInt(args[4]);
	}*/

	Object [] emptyArgs = new Object[0];
	int character;
	while ((character = reader.read()) != -1) {
	    char ch = (char) character;
	    if (Character.isWhitespace((char) ch)) {
		if (input.length() > 0) {
		    stemmer.setCurrent(input.toString());
		    for (int i = repeat; i != 0; i--) {
			stemmer.stem();
		    }
		    output.write(stemmer.getCurrent());
		    output.write('\n');
		    input.delete(0, input.length());
		}
	    }

	    else {
		input.append(Character.toLowerCase(ch));
	    }
	}
	output.flush();
    }
return "classification5";
	}}
