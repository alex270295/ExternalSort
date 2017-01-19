package ru.ifmo.ctddev.korolyov;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author korolyov
 *         19.01.17
 */
public class TestUtils {

    static void inmemorySort(String inputFilename, String outputFilename) throws IOException {
        inmemorySort(inputFilename, outputFilename, Comparator.naturalOrder());
    }
    static void inmemorySort(String inputFilename, String outputFilename, Comparator<String> comparator) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(inputFilename));
             PrintWriter out = new PrintWriter(outputFilename)) {
            in.lines().sorted(comparator).forEach(out::println);
        }
    }

    static void createFile(String filename, int totalLines, int stringLengthMin, int stringLengthMax) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(filename);
        Stream.generate(() -> RandomStringUtils.randomAscii(ThreadLocalRandom.current().nextInt(stringLengthMin, stringLengthMax)))
            .limit(totalLines)
            .forEach(out::println);
        out.close();
    }

    static void assertEqualsFiles(String actualFile, String expectedFile) throws IOException {
        try (BufferedReader actualIn = new BufferedReader(new FileReader(actualFile));
             BufferedReader expectedIn = new BufferedReader(new FileReader(expectedFile))) {
            assertEqualsStreams(actualIn.lines(), expectedIn.lines());
        }
    }

    private static void assertEqualsStreams(Stream<?> stream1, Stream<?> stream2) {
        Iterator<?> iter1 = stream1.iterator(), iter2 = stream2.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            assertEquals(iter1.next(), iter2.next());
        }
        assertTrue(!iter1.hasNext() && !iter2.hasNext());
    }

}
