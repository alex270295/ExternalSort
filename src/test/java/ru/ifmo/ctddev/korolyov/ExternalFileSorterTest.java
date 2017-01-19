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
        String outputFilenameInMemory = "output.txt_inmemory";
        String outputFilenameExternal = "output.txt_external";
        int totalLines = 1;
        for (int i = 0; i < 20; i++) {
            totalLines = totalLines * 2;
            TestUtils.createFile(inputFilename, totalLines, 128, 512);
            System.out.println(String.format("File with %d lines created", totalLines));
            TestUtils.inmemorySort(inputFilename, outputFilenameInMemory, Comparator.naturalOrder());
            ExternalFileSorter.sort(inputFilename, outputFilenameExternal, Comparator.naturalOrder());
            TestUtils.assertEqualsFiles(outputFilenameInMemory, outputFilenameExternal);
            System.out.println(String.format("Sort for file with %d lines correct", totalLines));
        }
        new File(inputFilename).deleteOnExit();
        new File(outputFilenameExternal).deleteOnExit();
        new File(outputFilenameInMemory).deleteOnExit();
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