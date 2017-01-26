package ru.ifmo.ctddev.korolyov;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @author korolyov
 *         18.01.17
 */
public class ExternalFileSorter {
    private static final int DEFAULT_MAX_OPEN_FILES = 10;
    private static int MAX_OPEN_FILES = DEFAULT_MAX_OPEN_FILES;

    static void changeMaxOpenFiles(int maxOpenFiles) {
        MAX_OPEN_FILES = maxOpenFiles;
    }

    private static long getAvailableMemory() {
        System.gc();
        return Runtime.getRuntime().freeMemory();
    }

    private static long calculatePreferableSizeOfBlocks() {
        long freeMemory = getAvailableMemory();
        return freeMemory / 3;
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

        long blockSize = calculatePreferableSizeOfBlocks();
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
        if (filesToMerge.size() == 1) {
            File file = filesToMerge.get(0);
            Files.move(file.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("files to merge:" + filesToMerge.size());
        Queue<File> files = new ArrayDeque<>();
        List<File> chunksOfFiles = new ArrayList<>(Math.min(filesToMerge.size(), MAX_OPEN_FILES));
        while (files.size() > 1) {
            int count = 0;
            while (files.size() > 0 && count < MAX_OPEN_FILES) {
                chunksOfFiles.add(files.poll());
                count++;
            }
            File currentOutputFile;
            if (files.size() == 0) {
                currentOutputFile = outputFile;
            } else {
                currentOutputFile = File.createTempFile("externalSortMergeChunk_", ".tmp");
            }

            PriorityQueue<CachedFileReader> filesQueue = new PriorityQueue<>((i, j) -> comparator.compare(i.peek(), j.peek()));
            for (File f : chunksOfFiles) {
                CachedFileReader fileReader = new CachedFileReader(f);
                if (!fileReader.isEmpty()) {
                    filesQueue.add(fileReader);
                }
            }

            try (BufferedWriter out = new BufferedWriter(new FileWriter(currentOutputFile))) {
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
            files.add(currentOutputFile);
            chunksOfFiles.clear();
        }
    }
}

