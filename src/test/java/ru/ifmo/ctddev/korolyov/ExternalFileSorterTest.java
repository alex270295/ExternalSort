package ru.ifmo.ctddev.korolyov;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author korolyov
 *         18.01.17
 */
public class ExternalFileSorterTest {

    @Test
    public void sortCorrectnessTest() throws Exception {
        String inputFilename = "input.txt";
        String outputFilename = "output.txt";
        int totalLines = 1000000;
        for (int i = 0; i < 10; i++) {
            TestUtils.createFile(inputFilename, totalLines, 1024, 2048);
            System.out.println(String.format("File with %d lines created", totalLines));
            ExternalFileSorter.sort(inputFilename, outputFilename, Comparator.naturalOrder());
            TestUtils.assertFileSorted(outputFilename, Comparator.naturalOrder());
            System.out.println(String.format("Sort for file with %d lines correct", totalLines));
        }
        //new File(inputFilename).deleteOnExit();
        new File(outputFilename).deleteOnExit();
    }

    @Test
    public void emptyFileSortTest() throws IOException {
        String input = "empty.txt";
        String output = "empty.txt_sorted";
        File inputFile = new File(input);
        File outputFile = new File(output);
        inputFile.deleteOnExit();
        outputFile.deleteOnExit();
        inputFile.createNewFile();
        ExternalFileSorter.sort(input, output);
        Assert.assertTrue(outputFile.exists());
        Assert.assertTrue(outputFile.length() == 0);
    }

}