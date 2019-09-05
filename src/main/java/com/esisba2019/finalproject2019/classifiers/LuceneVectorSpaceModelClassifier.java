package com.esisba2019.finalproject2019.classifiers;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.collections15.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.util.*;

public class LuceneVectorSpaceModelClassifier {

    private final Log log = LogFactory.getLog(getClass());

    private String indexDir;
    private String categoryFieldName;
    private String bodyFieldName;

    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
    private Transformer<RealMatrix, RealMatrix>[] indexers = new Transformer[]{
            new TfIndexer(),
            new IdfIndexer()
    };

    private AbstractSimilarity similarity = new CosineSimilarity();

    private Map<String, RealMatrix> centroidMap;
    private Map<String, Integer> termIdMap;
    private Map<String, Double> similarityMap;

    public void setIndexDir(String indexDir) {
        this.indexDir = indexDir;
    }

    public void setCategoryFieldName(String categoryFieldName) {
        this.categoryFieldName = categoryFieldName;
    }

    public void setBodyFieldName(String bodyFieldName) {
        this.bodyFieldName = bodyFieldName;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void setIndexers(Transformer<RealMatrix, RealMatrix>[] indexers) {
        this.indexers = indexers;
    }

    public void setSimilarity(AbstractSimilarity similarity) {
        this.similarity = similarity;
    }

    public void train() throws Exception {
        train(null);
    }

    public void train(Set<Integer> docIds) throws Exception {
        log.info("Classifier training started");
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexDir)),
                true);
        // Set up a data structure for the term versus the row id in the matrix.
        // This is going to be used for looking up the term's row in the matrix.
        this.termIdMap = computeTermIdMap(reader);
        // Initialize the data structures to hold the td matrices for the various
        // categories.
        Bag<String> docsInCategory = computeDocsInCategory(reader);
        Map<String, Integer> currentDocInCategory = new HashMap<String, Integer>();
        Map<String, RealMatrix> categoryTfMap = new HashMap<String, RealMatrix>();
        for (String category : docsInCategory.uniqueSet()) {
            int numDocsInCategory = docsInCategory.getCount(category);
            categoryTfMap.put(category,
                    new OpenMapRealMatrix(termIdMap.size(), numDocsInCategory));
            currentDocInCategory.put(category, new Integer(0));
        }
        // extract each document body's TermVector into the td matrix for
        // that document's category
        int numDocs = reader.numDocs();
        for (int i = 0; i < numDocs; i++) {
            Document doc = reader.document(i);
            if (docIds != null && docIds.size() > 0) {
                // check to see if the current document is in our training set,
                // and if so, train with it
                if (!docIds.contains(i)) {
                    continue;
                }
            }
            String category = doc.get(categoryFieldName);
            RealMatrix tfMatrix = categoryTfMap.get(category);
            // get the term frequency map
            TermFreqVector vector = reader.getTermFreqVector(i, bodyFieldName);
            String[] terms = vector.getTerms();
            int[] frequencies = vector.getTermFrequencies();
            for (int j = 0; j < terms.length; j++) {
                int row = termIdMap.get(terms[j]);
                int col = currentDocInCategory.get(category);
                tfMatrix.setEntry(row, col, new Double(frequencies[j]));
            }
            incrementCurrentDoc(currentDocInCategory, category);
        }
        reader.close();
        // compute centroid vectors for each category
        this.centroidMap = new HashMap<String, RealMatrix>();
        for (String category : docsInCategory.uniqueSet()) {
            RealMatrix tdmatrix = categoryTfMap.get(category);
            RealMatrix centroid = computeCentroid(tdmatrix);
            centroidMap.put(category, centroid);
        }
        log.info("Classifier training complete");
    }
    public Map<String, RealMatrix> getCentroidMap() {
        return centroidMap;
    }
    public Map<String, Integer> getTermIdMap() {
        return termIdMap;
    }

    public String classify(Map<String, RealMatrix> centroids,
                           Map<String, Integer> termIdMap, String text) throws Exception {
        RAMDirectory ramdir = new RAMDirectory();
        indexDocument(ramdir, "text", text);
        // now find the (normalized) term frequency vector for this
        RealMatrix docMatrix = buildMatrixFromIndex(ramdir, "text");
        // compute similarity using passed in Similarity implementation, we
        // use CosineSimilarity by default.
        this.similarityMap = new HashMap<String, Double>();
        for (String category : centroids.keySet()) {
            RealMatrix centroidMatrix = centroids.get(category);
            double sim = similarity.computeSimilarity(docMatrix, centroidMatrix);
            similarityMap.put(category, sim);
        }
        // sort the categories
        List<String> categories = new ArrayList<String>();
        categories.addAll(centroids.keySet());
        Collections.sort(categories,
                new ReverseComparator<String>(
                        new ByValueComparator<String, Double>(similarityMap)));
        // return the best category, the similarity map is also available
        // to the client for debugging or display.
        return categories.get(0);
    }

    public Map<String, Double> getSimilarityMap() {
        return similarityMap;
    }

    private Map<String, Integer> computeTermIdMap(IndexReader reader) throws Exception {
        Map<String, Integer> termIdMap = new HashMap<String, Integer>();
        int id = 0;
        TermEnum termEnum = reader.terms();
        while (termEnum.next()) {
            String term = termEnum.term().text();
            if (termIdMap.containsKey(term)) {
                continue;
            }
            termIdMap.put(term, id);
            id++;
        }
        return termIdMap;
    }

    private Bag<String> computeDocsInCategory(IndexReader reader) throws Exception {
        int numDocs = reader.numDocs();
        Bag<String> docsInCategory = new HashBag<String>();
        for (int i = 0; i < numDocs; i++) {
            Document doc = reader.document(i);
            String category = doc.get(categoryFieldName);
            docsInCategory.add(category);
        }
        return docsInCategory;
    }

    private void incrementCurrentDoc(Map<String, Integer> currDocs, String category) {
        int currentDoc = currDocs.get(category);
        currDocs.put(category, currentDoc + 1);
    }

    private RealMatrix computeCentroid(RealMatrix tdmatrix) {
        tdmatrix = normalizeWithTfIdf(tdmatrix);
        RealMatrix centroid = new OpenMapRealMatrix(tdmatrix.getRowDimension(), 1);
        int numDocs = tdmatrix.getColumnDimension();
        int numTerms = tdmatrix.getRowDimension();
        for (int row = 0; row < numTerms; row++) {
            double rowSum = 0.0D;
            for (int col = 0; col < numDocs; col++) {
                rowSum += tdmatrix.getEntry(row, col);
            }
            centroid.setEntry(row, 0, rowSum / ((double) numDocs));
        }
        return centroid;
    }

    private void indexDocument(RAMDirectory ramdir, String fieldName, String text)
            throws Exception {
        IndexWriter writer = new IndexWriter(ramdir, analyzer, MaxFieldLength.UNLIMITED);
        Document doc = new Document();
        doc.add(new Field(fieldName, text, Store.YES, Index.ANALYZED, TermVector.YES));
        writer.addDocument(doc);
        writer.commit();
        writer.close();
    }

    private RealMatrix buildMatrixFromIndex(RAMDirectory ramdir, String fieldName)
            throws Exception {
        IndexReader reader = IndexReader.open(ramdir, true);
        TermFreqVector vector = reader.getTermFreqVector(0, fieldName);
        String[] terms = vector.getTerms();
        int[] frequencies = vector.getTermFrequencies();
        RealMatrix docMatrix = new OpenMapRealMatrix(termIdMap.size(), 1);
        for (int i = 0; i < terms.length; i++) {
            String term = terms[i];
            if (termIdMap.containsKey(term)) {
                int row = termIdMap.get(term);
                docMatrix.setEntry(row, 0, frequencies[i]);
            }
        }
        reader.close();
        // normalize the docMatrix using TF*IDF
        docMatrix = normalizeWithTfIdf(docMatrix);
        return docMatrix;
    }

    private RealMatrix normalizeWithTfIdf(RealMatrix docMatrix) {
        for (Transformer<RealMatrix, RealMatrix> indexer : indexers) {
            docMatrix = indexer.transform(docMatrix);
        }
        return docMatrix;
    }

    public double crossValidate(int folds, int times) throws Exception {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexDir)),
                true);
        int numDocs = reader.maxDoc();
        int numDocsPerFold = numDocs / folds;
        Set<String> categories = computeDocsInCategory(reader).uniqueSet();
        Map<String, Integer> categoryPosMap = new HashMap<String, Integer>();
        int pos = 0;
        for (String categoryName : categories) {
            categoryPosMap.put(categoryName, pos);
            pos++;
        }
        int numCats = categories.size();
        RealMatrix confusionMatrix =
                new Array2DRowRealMatrix(numCats, numCats);
        for (int i = 0; i < times; i++) {
            for (int j = 0; j < folds; j++) {
                reset();
                Map<String, Set<Integer>> partition = new HashMap<String, Set<Integer>>();
                partition.put("test", new HashSet<Integer>());
                partition.put("train", new HashSet<Integer>());
                Set<Integer> testDocs = generateRandomTestDocs(numDocs, numDocsPerFold);
                for (int k = 0; k < numDocs; k++) {
                    if (testDocs.contains(k)) {
                        partition.get("test").add(k);
                    } else {
                        partition.get("train").add(k);
                    }
                }
                train(partition.get("train"));
                for (int docId : partition.get("test")) {
                    Document testDoc = reader.document(docId);
                    String actualCategory = testDoc.get(categoryFieldName);
                    String body = testDoc.get(bodyFieldName);
                    Map<String, RealMatrix> centroidMap = getCentroidMap();
                    Map<String, Integer> termIdMap = getTermIdMap();
                    String predictedCategory = classify(centroidMap, termIdMap, body);
                    // increment the counter for the confusion matrix
                    int row = categoryPosMap.get(actualCategory);
                    int col = categoryPosMap.get(predictedCategory);
                    confusionMatrix.setEntry(row, col,
                            confusionMatrix.getEntry(row, col) + 1);
                }
            }
        }
        // print confusion matrix
        prettyPrint(confusionMatrix, categoryPosMap);
        // compute accuracy
        double trace = confusionMatrix.getTrace(); // sum of diagnonal elements
        double sum = 0.0D;
        for (int i = 0; i < confusionMatrix.getRowDimension(); i++) {
            for (int j = 0; j < confusionMatrix.getColumnDimension(); j++) {
                sum += confusionMatrix.getEntry(i, j);
            }
        }
        // if sum is 0 then trace is also 0, so return 0
        return (sum > 0.0D ? trace / sum : 0.0D);
    }

    private Set<Integer> generateRandomTestDocs(int numDocs, int numDocsPerFold) {
        Set<Integer> docs = new HashSet<Integer>();
        while (docs.size() < numDocsPerFold) {
            docs.add((int) (numDocs * Math.random() - 1));
        }
        return docs;
    }

    private void prettyPrint(RealMatrix confusionMatrix,
                             Map<String, Integer> categoryPosMap) {
        System.out.println("==== Confusion Matrix ====");
        // invert the map and write the header
        System.out.printf("%10s", " ");
        Map<Integer, String> posCategoryMap = new HashMap<Integer, String>();
        for (String category : categoryPosMap.keySet()) {
            posCategoryMap.put(categoryPosMap.get(category), category);
            System.out.printf("%8s", category);
        }
        System.out.printf("%n");
        for (int i = 0; i < confusionMatrix.getRowDimension(); i++) {
            System.out.printf("%10s", posCategoryMap.get(i));
            for (int j = 0; j < confusionMatrix.getColumnDimension(); j++) {
                System.out.printf("%8d", (int) confusionMatrix.getEntry(i, j));
            }
            System.out.printf("%n");
        }
    }

    /**
     * Reset internal data structures. Used by the cross validation process
     * to reset the internal state of the classifier between runs.
     */
    private void reset() {
        if (centroidMap != null) {
            centroidMap.clear();
        }
        if (termIdMap != null) {
            termIdMap.clear();
        }
        if (similarityMap != null) {
            similarityMap.clear();
        }
    }
}
