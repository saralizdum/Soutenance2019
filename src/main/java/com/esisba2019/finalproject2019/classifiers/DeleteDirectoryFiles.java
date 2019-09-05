package com.esisba2019.finalproject2019.classifiers;

import java.io.File;

public class DeleteDirectoryFiles {
    public void emptyDirectory() {
        File directory = new File("input");
        File[] files = directory.listFiles();

        File directory2 = new File("input2");
        File[] files2 = directory2.listFiles();

        File directory3 = new File("output");
        File[] files3 = directory3.listFiles();

        File directory4 = new File("output");
        File[] files4 = directory4.listFiles();


        File directory5 = new File("data/scc-index");
        directory5.delete();

        for (File file : files)
        {
            if (!file.delete()) {
                System.out.println("Failed to delete " + file);
            }
        }
        for (File file2 : files2)
        {
            if (!file2.delete()) {
                System.out.println("Failed to delete " + file2);
            }
        }
        for (File file3 : files3)
        {
            if (!file3.delete()) {
                System.out.println("Failed to delete " + file3);
            }
        }

        for (File file4 : files4)
        {
            if (!file4.delete()) {
                System.out.println("Failed to delete " + file4);
            }
        }
    }
}