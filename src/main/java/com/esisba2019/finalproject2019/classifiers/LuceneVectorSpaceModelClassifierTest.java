package com.esisba2019.finalproject2019.classifiers;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Controller
public class LuceneVectorSpaceModelClassifierTest {

    private static final Log log = LogFactory.getLog(LuceneVectorSpaceModelClassifierTest.class);

    private static String INPUT_FILE = "data/filiale.txt";
    private static String INDEX_DIR = "data/scc-index";




    public LuceneVectorSpaceModelClassifierTest() throws IOException {
    }

    public static int numberOfFiles(File srcDir) {
        int count = 0;
        File[] listFiles = srcDir.listFiles();
        for(int i = 0; i < listFiles.length; i++){
            if (listFiles[i].isDirectory()) {
                count += numberOfFiles(listFiles[i]);
            } else if (listFiles[i].isFile()) {
                count++;
            }
        }
        return count;
    }

    @RequestMapping(value="/classify1")
//    @BeforeClass
    public static String buildIndex() throws Exception {
        log.debug("Building index...");
        BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE));
        IndexWriter writer = new IndexWriter(FSDirectory.open(new File(INDEX_DIR)),
                new SummaryAnalyzer(), MaxFieldLength.UNLIMITED);
        String line = null;
        int lno = 0;
        StringBuilder bodybuf = new StringBuilder();
        String category = null;
        while ((line = reader.readLine()) != null) {
            if (line.endsWith(".sgm")) {

                if (lno > 0) {
                    writeToIndex(writer, category, bodybuf.toString());
                    bodybuf = new StringBuilder();
                    lno++;
                }
                category = StringUtils.trim(StringUtils.split(line, ":")[1]);
                continue;
            } else {
                bodybuf.append(line).append(" ");
            }
            lno++;
        }
        // last record
        writeToIndex(writer, category, bodybuf.toString());
        reader.close();
        writer.commit();
        writer.optimize();
        writer.close();
        return "uploadstatusview";
    }

    private static void writeToIndex(IndexWriter writer, String category,
                                     String body) throws Exception {
        Document doc = new Document();
        doc.add(new Field("category", category, Store.YES, Index.NOT_ANALYZED));
        doc.add(new Field("body", body, Store.YES, Index.ANALYZED, TermVector.YES));
        writer.addDocument(doc);
    }

    @GetMapping(value = "/classify2")
    @AfterClass
    public static String  deleteIndex() throws Exception {
        log.info("Deleting index directory...");
        FileUtils.deleteDirectory(new File(INDEX_DIR));
        return "uploadstatusview";

    }
    @RequestMapping(value="/classify")

    @Test
    public String testLuceneVectorSpaceClassifier() throws Exception {
        LuceneVectorSpaceModelClassifier classifier = new LuceneVectorSpaceModelClassifier();
        // setup
        classifier.setIndexDir(INDEX_DIR);
        classifier.setAnalyzer(new SummaryAnalyzer());
        classifier.setCategoryFieldName("category");
        classifier.setBodyFieldName("body");
        // this is the default but we set it anyway, to illustrate usage
        classifier.setIndexers(new Transformer[]{
                new TfIndexer(),
                new IdfIndexer()
        });
        // this is the default but we set it anyway, to illustrate usage.
        // Similarity need not be set before training, it can be set before
        // the classification step.

        classifier.setSimilarity(new CosineSimilarity());
        // training
        classifier.train();
        // classification
        List<String> PDF_TO_CLASSIFY = java.nio.file.Files.readAllLines(Paths.get("Links.txt"));
        Map<String, RealMatrix> centroidMap = classifier.getCentroidMap();
        Map<String, Integer> termIdMap = classifier.getTermIdMap();
        String[] categories = centroidMap.keySet().toArray(new String[0]);
        File repertoire = new File("output/");
        File[] files = repertoire.listFiles();


        for (File testDoc : files) {

            FileWriter fileWriter1 = new FileWriter(testDoc, true);
            String textToAppend = "12-12-3000";
            PrintWriter printWriter1 = new PrintWriter(fileWriter1);
            printWriter1.println(textToAppend);  //New line
            printWriter1.close();
            FileWriter fileWriter2 = new FileWriter(testDoc, true);
            String textToAppend1 = "autre";
            PrintWriter printWriter2 = new PrintWriter(fileWriter2);
            printWriter2.println(textToAppend1);  //New line
            printWriter2.close();
            Model model = FileManager.get().loadModel("GSH.owl");

            String query = "PREFIX ns: <http://www.domain.com/your/namespace/>" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                    " select ?y ?m where { " +
                    "  ?y rdfs:subClassOf ?m." +
                    "}";

            Query query1 = QueryFactory.create(query);
            QueryExecution queryExecution = QueryExecutionFactory.create(query1, model);


            ResultSet resultSet = queryExecution.execSelect();
            ArrayList<String> arraydate1 = new ArrayList<String>();


//                        System.out.println(maxEntry.getKey());
            //

            ArrayList<String> arraydate = new ArrayList<String>();

            String category = classifier.classify(centroidMap, termIdMap,

                    FileUtils.readFileToString(testDoc, "UTF-8"));
            int j = 0;
            if ( j==0 ){
                File targetLocation = new File("C:\\Users\\amgsoft\\Desktop\\folder\\" + category);
                targetLocation.mkdir();
                ++j;
                try (Stream linesStream = java.nio.file.Files.lines(testDoc.toPath())) {
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
//                    // Pattern rx3 ex: 02 (Month) 01 (Day), 2000 (Year) MM DD, YYYY
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
//
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
                    while (resultSet.hasNext()) {

                        QuerySolution solution = resultSet.nextSolution();
                        Resource nameM = solution.getResource("y");
//                Resource namez = solution.getResource("z");
                        Resource namem = solution.getResource("m");


                        Stream linesStream1 = java.nio.file.Files.lines(testDoc.toPath());
                        linesStream1.forEach(line -> {
                            if (line.equals(nameM.getLocalName())) {

                                arraydate1.add(namem.getLocalName());
//
                            }
                        });

                    }
                    HashMap<String, Integer> tmap1 = new HashMap<>();
                    for (String t1 : arraydate1) {
                        Integer c1 = tmap1.get(t1);
                        tmap1.put(t1, (c1 == null) ? 1 : c1 + 1);

                    }
                    Map.Entry<String, Integer> maxEntry1 = null;
                    Integer max1 = Collections.max(tmap1.values());

                    TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
                    // yay.countFrequencies(arraydate);
                    for (String t : arraydate) {

                        Integer c = tmap.get(t);
                        tmap.put(t, (c == null) ? 1 : c + 1);
                    }

                    Date d = new Date();

                    String t = String.valueOf(d.getYear() + 1900);

                    int i = 0;
                    for (Map.Entry<String, Integer> m : tmap.entrySet()) {
                        for (Map.Entry<String, Integer> m1 : tmap1.entrySet()) {

                            Integer value1 = m1.getValue();
                            if (null != value1 && max1 == value1) {
                                maxEntry1 = m1;

                                Pattern datePattern = Pattern.compile("(\\d{4})");
                                Matcher dateMatcher = datePattern.matcher(m.getKey());
                                if (dateMatcher.find()  && i==0) {
                                    Integer ss = t.compareTo(dateMatcher.group(1));
                                    int k = Integer.parseInt(dateMatcher.group(1));
                                    if (ss > 0 && k > 1900) {

                                        File targetLocation1 = new File(targetLocation + "/" + dateMatcher.group(1));
                                        targetLocation1.mkdir();

                                        ++i;
                                        File targetLocation2 = new File(targetLocation1 + "/" + maxEntry1.getKey());
                                        targetLocation2.mkdir();

                                        ++i;
//
                                        for (String pdf : PDF_TO_CLASSIFY) {
                                            File pd = new File(pdf);
                                            String nom = (testDoc.getName() != null) ? testDoc.getName().substring(0, testDoc.getName().indexOf('.')) : "";
                                            String nom2 = (pd.getName() != null) ? pd.getName().substring(0, pd.getName().indexOf('.')) : "";
                                            if (nom.compareTo(nom2) == 0) {

                                                InputStream in = new FileInputStream(pd);
                                                OutputStream out = new FileOutputStream(targetLocation2 + "/" + pd.getName());
                                                byte[] buf = new byte[1024];
                                                int len;
                                                while ((len = in.read(buf)) > 0) {
                                                    out.write(buf, 0, len);
                                                }
                                                in.close();
                                                out.close();

                                            } } }
                                    else {
                                        for (String pdf : PDF_TO_CLASSIFY) {
                                            File pd = new File(pdf);
                                            String nom = (testDoc.getName() != null) ? testDoc.getName().substring(0, testDoc.getName().indexOf('.')) : "";
                                            String nom2 = (pd.getName() != null) ? pd.getName().substring(0, pd.getName().indexOf('.')) : "";
                                            if (nom.compareTo(nom2) == 0) {
                                                File targetLocation2 = new File(targetLocation + "/" + maxEntry1.getKey());
                                                targetLocation2.mkdir();
                                                InputStream in = new FileInputStream(pd);
                                                OutputStream out = new FileOutputStream(targetLocation2 + "/" + pd.getName());
                                                byte[] buf = new byte[1024];
                                                int len;
                                                while ((len = in.read(buf)) > 0) {
                                                    out.write(buf, 0, len);
                                                }
                                                in.close();
                                                out.close();

                                            }
                                        }}
                                } } } } }

                Map<String, Double> similarityMap = classifier.getSimilarityMap();
                String[] pairs = new String[categories.length];
                for (int i = 0; i < categories.length; i++) {
                    pairs[i] = categories[i] + ":" + similarityMap.get(categories[i]);
                    System.out.println("(" + StringUtils.join(pairs, ", ") + ")");
                }

                int fileCount=numberOfFiles(targetLocation);
                System.out.println(category +" "+fileCount);
            }

        }
        return "uploadstatusview";

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCrossValidate() throws Exception {
        LuceneVectorSpaceModelClassifier classifier =
                new LuceneVectorSpaceModelClassifier();
        // setup
        classifier.setIndexDir(INDEX_DIR);
        classifier.setAnalyzer(new SummaryAnalyzer());
        classifier.setCategoryFieldName("category");
        classifier.setBodyFieldName("body");
        // this is the default but we set it anyway, to illustrate usage
        classifier.setIndexers(new Transformer[]{
                new TfIndexer(),
                new IdfIndexer()
        });
        // this is the default but we set it anyway, to illustrate usage.
        // Similarity need not be set before training, it can be set before
        // the classification step.
        classifier.setSimilarity(new CosineSimilarity());
        double accuracy = classifier.crossValidate(10, 10);
        System.out.println("accuracy=" + accuracy);
    }

}