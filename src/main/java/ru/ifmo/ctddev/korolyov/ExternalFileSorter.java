package ru.ifmo.ctddev.korolyov;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author korolyov
 *         18.01.17
 */
public class ExternalFileSorter {
    private static final int DEFAULT_MAX_OPEN_FILES = 1024;
    private static int MAX_OPEN_FILES = DEFAULT_MAX_OPEN_FILES;

    private static long getAvailableMemory() {
        System.gc();
        return Runtime.getRuntime().freeMemory();
    }

    private static long calculatePreferableSizeOfBlocks(long sizeOfInputFile) {
        long probablyBlockSize = sizeOfInputFile / MAX_OPEN_FILES;
        long freeMemory = getAvailableMemory();
        if (probablyBlockSize < freeMemory / 2) {
            probablyBlockSize = freeMemory / 2;
        } else if (probablyBlockSize >= freeMemory) {
            System.err.println("OutOfMemory incoming");
        }
        return probablyBlockSize;
    }

    public static void sort(String inputFilename, String outputFilename) throws IOException {
        sort(inputFilename, outputFilename, Comparator.naturalOrder());
    }

    public static void sort(String inputFilename, String outputFilename, Comparator<String> comparator) throws IOException {
        mergeSortedFileChunks(divideAndSortChunks(new File(inputFilename), comparator), new File(outputFilename), comparator);
    }

    private static File sortAndDumpChunk(List<String> fileChunk, Comparator<String> comparator) throws IOException {
        fileChunk.sort(comparator);
        File tmpFileForChunk = File.createTempFile("externalSortChunk_", ".tmp");
        tmpFileForChunk.deleteOnExit();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(tmpFileForChunk))) {
            for (String line : fileChunk) {
                out.write(line);
                out.newLine();
            }
        }
        return tmpFileForChunk;
    }

    private static List<File> divideAndSortChunks(File file, Comparator<String> comparator) throws IOException {
        List<File> fileChunks = new ArrayList<>();

        long blockSize = calculatePreferableSizeOfBlocks(file.length());
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            List<String> fileChunk = new ArrayList<>();
            String line = "";
            while (line != null) {
                long currentBlockSize = 0;
                while ((currentBlockSize < blockSize) && ((line = in.readLine()) != null)) {
                    fileChunk.add(line);
                    currentBlockSize += line.length();
                }
                fileChunks.add(sortAndDumpChunk(fileChunk, comparator));
                fileChunk.clear();
            }
        }
        return fileChunks;
    }

    private static void mergeSortedFileChunks(List<File> filesToMerge, File outputFile, final Comparator<String> comparator) throws IOException {
        PriorityQueue<CachedFileReader> filesQueue = new PriorityQueue<>((i, j) -> comparator.compare(i.peek(), j.peek()));
        for (File f : filesToMerge) {
            CachedFileReader fileReader = new CachedFileReader(f);
            if (!fileReader.isEmpty()) {
                filesQueue.add(fileReader);
            }
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputFile))) {
            while (filesQueue.size() > 0) {
                CachedFileReader fileReader = filesQueue.poll();
                String currentLine = fileReader.pop();
                out.write(currentLine);
                out.newLine();
                if (fileReader.isEmpty()) {
                    fileReader.close();
                } else {
                    filesQueue.add(fileReader);
                }
            }
        } finally {
            for (CachedFileReader fileReader : filesQueue) {
                try {
                    fileReader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}

