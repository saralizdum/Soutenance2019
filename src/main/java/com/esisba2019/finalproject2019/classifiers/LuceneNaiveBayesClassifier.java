package com.esisba2019.finalproject2019.classifiers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Uses Naive Bayes theorem to compute if a document D belongs to a given
 * class C. Optionally uses feature selection based on information gain,
 * and overfitting prevention heuristics to refine the classification.
 * Depends on a Lucene index of pre-categorized documents for training.
 */
public class LuceneNaiveBayesClassifier {

  private final Log log = LogFactory.getLog(getClass());
  
  private String indexDir;
  private String categoryFieldName;
  private String matchCategoryValue;
  private boolean selectTopFeatures = false;
  private boolean preventOverfitting = false;
  private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
  
  private static final double ALMOST_ZERO_PROBABILITY = 0.00001D;
  
  private Map<String,double[]> trainingSet;
  private double categoryDocRatio;
  
  public void setIndexDir(String indexDir) {
    this.indexDir = indexDir;
  }

  public void setCategoryFieldName(String categoryFieldName) {
    this.categoryFieldName = categoryFieldName;
  }

  public void setMatchCategoryValue(String matchCategoryValue) {
    this.matchCategoryValue = matchCategoryValue;
  }

  public void setSelectTopFeatures(boolean selectTopFeatures) {
    this.selectTopFeatures = selectTopFeatures;
  }

  public void setPreventOverfitting(boolean preventOverfitting) {
    this.preventOverfitting = preventOverfitting;
  }

  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  /**
   * Creates an array of terms and their positive and negative probabilities
   * and the ratio of documents in a certain category. Expects a Lucene
   * index created with the tokenized document bodies, and a category 
   * field that is specified in the setters and populated with the specified
   * category value.
   * @throws Exception if one is thrown.
   */
  public void train() throws Exception {
    this.trainingSet = new HashMap<String,double[]>();
    IndexReader reader = null;
    try {
      reader = IndexReader.open(FSDirectory.open(new File(indexDir)), true);
      Set<Integer> matchedDocIds = computeMatchedDocIds(reader);
      double matchedDocs = (double) matchedDocIds.size();
      double nDocs = (double) reader.numDocs();
      this.categoryDocRatio = matchedDocs / (nDocs - matchedDocs); 
      TermEnum termEnum = reader.terms();
      double nWords = 0.0D;
      double nUniqueWords = 0.0D;
      while (termEnum.next()) {
        double nWordInCategory = 0.0D;
        double nWordNotInCategory = 0.0D;
        Term term = termEnum.term();
        TermDocs termDocs = reader.termDocs(term);
        while (termDocs.next()) {
          int docId = termDocs.doc();
          int frequency = termDocs.freq();
          if (matchedDocIds.contains(docId)) {
            nWordInCategory += frequency;
          } else {
            nWordNotInCategory += frequency;
          }
          nWords += frequency;
          nUniqueWords++;
        }
        double[] pWord = new double[2];
        if (trainingSet.containsKey(term.text())) {
          pWord = trainingSet.get(term.text());
        }
        pWord[0] += (double) nWordInCategory;
        pWord[1] += (double) nWordNotInCategory;
        trainingSet.put(term.text(), pWord);
      }
      // once we have gone through all our terms, we normalize our
      // trainingSet so the values are probabilities, not numbers
      for (String term : trainingSet.keySet()) {
        double[] pWord = trainingSet.get(term);
        for (int i = 0; i < pWord.length; i++) {
          if (preventOverfitting) {
            // apply smoothening formula
            pWord[i] = ((pWord[i] + 1) / (nWords + nUniqueWords));
          } else {
            pWord[i] /= nWords;
          }
        }
      }
      if (selectTopFeatures) {
        InfoGainFeatureSelector featureSelector = new InfoGainFeatureSelector();
        featureSelector.setWordProbabilities(trainingSet);
        featureSelector.setPCategory(matchedDocs / nDocs);
        Map<String,double[]> topFeatures = featureSelector.selectFeatures();
        this.trainingSet = topFeatures;
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  public Map<String,double[]> getTrainingSet() {
    return trainingSet;
  }
  
  public double getCategoryDocRatio() {
    return this.categoryDocRatio;
  }
  
  public boolean classify(Map<String,double[]> wordProbabilities, 
      double categoryDocRatio, String text) throws Exception {
    RAMDirectory ramdir = new RAMDirectory();
    IndexWriter writer = null;
    IndexReader reader = null;
    try {
      writer = new IndexWriter(ramdir, analyzer, MaxFieldLength.UNLIMITED);
      Document doc = new Document();
      doc.add(new Field("text", text, Store.NO, Index.ANALYZED));
      writer.addDocument(doc);
      writer.commit();
      writer.close();
      double likelihoodRatio = categoryDocRatio;
      reader = IndexReader.open(ramdir, true);
      TermEnum termEnum = reader.terms();
      while (termEnum.next()) {
        Term term = termEnum.term();
        TermDocs termDocs = reader.termDocs(term);
        String word = term.text();
        if (trainingSet.containsKey(word)) {
          // we don't care about the frequency since they cancel out
          // when computing p(w|C) and p(w|-C) for the same number of w
          double[] probabilities = trainingSet.get(word);
          if (probabilities[1] == 0.0D) {
            // this means that the word is a very good discriminator word,
            // we put in an artificially low value instead of 0 (preventing
            // a divide by 0) and keeping the term
            likelihoodRatio *= (probabilities[0] / ALMOST_ZERO_PROBABILITY);
          } else {
            likelihoodRatio *= (probabilities[0] / probabilities[1]);
          }
        }
      }
      return (likelihoodRatio > 1.0D); 
    } finally {
      if (writer != null && IndexWriter.isLocked(ramdir)) {
        IndexWriter.unlock(ramdir);
        writer.rollback();
        writer.close();
      }
      if (reader != null) {
        reader.close();
      }
    }
  }
  
  private Set<Integer> computeMatchedDocIds(IndexReader reader) throws IOException {
    Filter categoryFilter = new CachingWrapperFilter(
      new QueryWrapperFilter(new TermQuery(
      new Term(categoryFieldName, matchCategoryValue))));
    DocIdSet docIdSet = categoryFilter.getDocIdSet(reader);
    DocIdSetIterator docIdSetIterator = docIdSet.iterator();
    Set<Integer> matchedDocIds = new HashSet<Integer>();
    int docId;
    while ((docId = docIdSetIterator.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
      matchedDocIds.add(docId);
    }
    return matchedDocIds;
  }
}
