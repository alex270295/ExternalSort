package ru.ifmo.ctddev.korolyov;

import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author korolyov
 *         19.01.17
 */

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Benchmark)
public class Benchmarker {
    private static final String INPUT_FILENAME = "input.txt";
    private static final String OUTPUT_FILENAME = "output.txt";

    @Setup
    public void createInputFile() throws FileNotFoundException {
        TestUtils.createFile(INPUT_FILENAME, 1_000_000, 1024, 2048);
    }

    @TearDown
    public void cleanUp() {
        new File(OUTPUT_FILENAME).delete();
    }

    @Benchmark
    public void externalSortTwoMaxOpenFiles() throws IOException {
        ExternalFileSorter.changeMaxOpenFiles(2);
        ExternalFileSorter.sort(INPUT_FILENAME, OUTPUT_FILENAME);
    }

    @Benchmark
    public void externalSortThreeMaxOpenFiles() throws IOException {
        ExternalFileSorter.changeMaxOpenFiles(3);
        ExternalFileSorter.sort(INPUT_FILENAME, OUTPUT_FILENAME);
    }

    @Benchmark
    public void externalSortTenMaxOpenFiles() throws IOException {
        ExternalFileSorter.changeMaxOpenFiles(10);
        ExternalFileSorter.sort(INPUT_FILENAME, OUTPUT_FILENAME);
    }

    @Benchmark
    public void externalSortInfMaxOpenFiles() throws IOException {
        ExternalFileSorter.changeMaxOpenFiles(1024);
        ExternalFileSorter.sort(INPUT_FILENAME, OUTPUT_FILENAME);
    }

}
