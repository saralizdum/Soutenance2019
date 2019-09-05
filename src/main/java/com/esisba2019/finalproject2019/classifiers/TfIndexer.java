package com.esisba2019.finalproject2019.classifiers;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math.linear.RealMatrix;

public class TfIndexer implements Transformer<RealMatrix, RealMatrix> {

    public RealMatrix transform(RealMatrix matrix) {
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
}
