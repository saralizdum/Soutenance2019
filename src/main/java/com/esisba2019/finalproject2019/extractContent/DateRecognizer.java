package com.esisba2019.finalproject2019.extractContent;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Controller
public class DateRecognizer {
//    @RequestMapping(value="/date")
    public static void recognition() throws IOException {
        File repertoire = new File("input\\");
        File[] files = repertoire.listFiles();
        for (File file : files) {

            ArrayList<String> arraydate = new ArrayList<String>();
            // iterating through the file and reading line by line
            try (Stream linesStream = Files.lines(file.toPath())) {
                linesStream.forEach(line -> {
                    // Replacing Month name with it's number (ex: August will be changed to 08)
                    String stlin = line.toString().toLowerCase()
                            .replaceAll("(?:)janvier(?:)", "01")
                            .replaceAll("(?:)jan(?:)", "01")
                            .replaceAll("(?:)janv(?:)", "01")
                            .replaceAll("(?:)fevrier(?:)", "02")
                            .replaceAll("(?:)fev(?:)", "02")
                            .replaceAll("(?:)mars(?:)", "03")
                            .replaceAll("(?:)avril(?:)", "04")
                            .replaceAll("(?:)av(?:)", "04")
                            .replaceAll("(?:)avr(?:)", "04")
                            .replaceAll("(?:)mai(?:)", "05")
                            .replaceAll("(?:)juin(?:)", "06")
                            .replaceAll("(?:)juillet(?:)", "07")
                            .replaceAll("(?:)aout(?:)", "08")
                            .replaceAll("(?:)septembre(?:)", "09")
                            .replaceAll("(?:)sep(?:)", "09")
                            .replaceAll("(?:)sept(?:)", "09")
                            .replaceAll("(?:)septemb(?:)", "09")
                            .replaceAll("(?:)octobre(?:)", "10")
                            .replaceAll("(?:)oct(?:)", "10")
                            .replaceAll("(?:)octob(?:)", "10")
                            .replaceAll("(?:)novembre(?:)", "11")
                            .replaceAll("(?:)nov(?:)", "11")
                            .replaceAll("(?:)novemb(?:)", "11")
                            .replaceAll("(?:)decembre(?:)", "12")
                            .replaceAll("(?:)dec(?:)", "12")
                            .replaceAll("(?:)decemb(?:)", "12");


                    // Creating Date patterns

                    // Pattern rx1 ex: 2000 (Year) - 02 (Month) - 01 (Day) YYYY(-,/,etc..) MM (-,/,etc..) DD
                    Pattern rx1 = Pattern.compile("([0-9]{4}).([0-9]{1,2}).([0-9]{1,2})");
                    // Pattern rx2 ex: 01 (Day) 02 (Month) 2000 (Year) DD MM YYYY
                    Pattern rx2 = Pattern.compile("([0-9]{1,2}).([0-9]{1,2}).([0-9]{4})");
                    // Pattern rx3 ex: 02 (Month) 01 (Day), 2000 (Year) MM DD, YYYY
//                    Pattern rx3 = Pattern.compile("([0-9]{1,2})\\s([0-9]{1,2}),\\s([0-9]{4})");

                    // Finding matches for each pattern

                    Matcher matcher1 = rx1.matcher(stlin);

                    while (matcher1.find()) {
                        String exp1;
                        if (Integer.parseInt(matcher1.group(2)) > 12) {
                            exp1 = matcher1.group(1) + "-" + matcher1.group(3) + "-" + matcher1.group(2);

                        } else if (Integer.parseInt(matcher1.group(3)) > 12) {
                            exp1 = matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3);

                        } else {
                            // I use the standard date format YYYY MM DD when we can't know which one is MM
                            exp1 = matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3);
                        }
                        //Adding Date to the ArrayList
                        arraydate.add(exp1);
                    }

                    Matcher matcher2 = rx2.matcher(stlin);
                    while (matcher2.find()) {
                        String exp2;
                        if (Integer.parseInt(matcher2.group(2)) > 12) {
                            exp2 = matcher2.group(3) + "-" + matcher2.group(1) + "-" + matcher2.group(2);

                        } else if (Integer.parseInt(matcher2.group(1)) > 12) {
                            exp2 = matcher2.group(3) + "-" + matcher2.group(2) + "-" + matcher2.group(1);

                        } else {
                            // I use the standard date format DD MM YYYY when we can't know which one is MM
                            exp2 = matcher2.group(3) + "-" + matcher2.group(1) + "-" + matcher2.group(2);

                        }
                        //Adding Date to the ArrayList
                        arraydate.add(exp2);
                    }

//                    Matcher matcher3 = rx3.matcher(stlin);
//                    while (matcher3.find()) {
//                        String exp3;
//                        if (Integer.parseInt(matcher3.group(1)) > 12) {
//                            exp3 = matcher3.group(3) + "-" + matcher3.group(2) + "," + matcher3.group(1);
//
//                        } else if (Integer.parseInt(matcher3.group(3)) > 12) {
//                            exp3 = matcher3.group(3) + "-" + matcher3.group(1) + "-" + matcher3.group(2);
//
//                        } else {
//                            // I use the standard date format MM DD YYYY when we can't know which one is MM
//                            exp3 = matcher3.group(3) + "-" + matcher3.group(1) + "-" + matcher3.group(2);
//                        }
//                        //Adding Date to the ArrayList
//                        arraydate.add(exp3);
//                    }
                });
                int i = 0;
                for (String t : arraydate) {
                    Date d = new Date();

                    String t1 = String.valueOf(d.getYear() + 1900);
                    Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
                    Matcher dateMatcher = datePattern.matcher(t);
                    Pattern datePattern1 = Pattern.compile("(\\d{4})");
                    Matcher dateMatcher1 = datePattern1.matcher(t);


                    if (dateMatcher.find()) {

                        if (dateMatcher1.find() && i == 0) {
                            Integer ss = t1.compareTo(dateMatcher1.group(1));
                            int k = Integer.parseInt(dateMatcher1.group(1));
                            if (ss > 0 && k > 1900) {
                                FileWriter fileWriter = new FileWriter(file, true); //Set true for append mode
                                PrintWriter printWriter = new PrintWriter(fileWriter);

                                printWriter.println(t);  //New line
                                printWriter.close();
                                ++i;
                            } } }} } }
//    return "recognize";
    }}


