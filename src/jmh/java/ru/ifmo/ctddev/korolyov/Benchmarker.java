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
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(2)
@State(Scope.Benchmark)
public class Benchmarker {
    private static final String INPUT_FILENAME = "input.txt";
    private static final String OUTPUT_FILENAME = "output.txt";

    @Setup
    public void createInputFile() throws FileNotFoundException {
        TestUtils.createFile(INPUT_FILENAME, 2_000_000, 128, 129);
    }

    @TearDown
    public void cleanUp() {
        new File(INPUT_FILENAME).delete();
        new File(OUTPUT_FILENAME).delete();
    }

    @Benchmark
    public void externalSort() throws IOException {
        ExternalFileSorter.sort(INPUT_FILENAME, OUTPUT_FILENAME);
    }

    @Benchmark
    public void inmemorySort() throws IOException {
        TestUtils.inmemorySort(INPUT_FILENAME, OUTPUT_FILENAME);
    }

}
