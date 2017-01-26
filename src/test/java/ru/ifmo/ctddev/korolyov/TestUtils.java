package ru.ifmo.ctddev.korolyov;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author korolyov
 *         19.01.17
 */
class TestUtils {

    static void createFile(String filename, int totalLines, int stringLengthMin, int stringLengthMax) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(filename);
        Stream.generate(() -> RandomStringUtils.randomAscii(ThreadLocalRandom.current().nextInt(stringLengthMin, stringLengthMax)))
            .limit(totalLines)
            .forEach(out::println);
        out.close();
    }

    static void assertFileSorted(String filename, Comparator<String> comparator) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            in.lines().reduce((s, s2) -> {
                assertTrue(comparator.compare(s, s2) <= 0);
                return s2;
            }).isPresent();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
