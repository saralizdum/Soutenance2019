package com.esisba2019.finalproject2019.classifiers;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math.linear.RealMatrix;


public class IdfIndexer implements Transformer<RealMatrix, RealMatrix> {

    public RealMatrix transform(RealMatrix matrix) {
        // Phase 1: apply IDF weight to the raw word frequencies
        int n = matrix.getColumnDimension();
        for (int j = 0; j < matrix.getColumnDimension(); j++) {
            for (int i = 0; i < matrix.getRowDimension(); i++) {
                double matrixElement = matrix.getEntry(i, j);
                if (matrixElement > 0.0D) {
                    double dm = countDocsWithWord(
                            matrix.getSubMatrix(i, i, 0, matrix.getColumnDimension() - 1));
                    matrix.setEntry(i, j, matrix.getEntry(i, j) * (1 + Math.log(n) - Math.log(dm)));
                }
            }
        }
        // Phase 2: normalize the word scores for a single document
        for (int j = 0; j < matrix.getColumnDimension(); j++) {
            double sum = sum(matrix.getSubMatrix(0, matrix.getRowDimension() - 1, j, j));
            for (int i = 0; i < matrix.getRowDimension(); i++) {
                if (sum > 0.0D) {
                    matrix.setEntry(i, j, (matrix.getEntry(i, j) / sum));
                } else {
                    matrix.setEntry(i, j, 0.0D);
                }
            }
        }
        return matrix;
    }

    private double sum(RealMatrix colMatrix) {
        double sum = 0.0D;
        for (int i = 0; i < colMatrix.getRowDimension(); i++) {
            sum += colMatrix.getEntry(i, 0);
        }
        return sum;
    }

    private double countDocsWithWord(RealMatrix rowMatrix) {
        double numDocs = 0.0D;
        for (int j = 0; j < rowMatrix.getColumnDimension(); j++) {
            if (rowMatrix.getEntry(0, j) > 0.0D) {
                numDocs++;
            }
        }
        return numDocs;
    }
}
