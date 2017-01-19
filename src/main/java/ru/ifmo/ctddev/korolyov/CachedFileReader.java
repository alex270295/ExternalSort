package ru.ifmo.ctddev.korolyov;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author korolyov
 *         18.01.17
 */
class CachedFileReader implements Closeable {
    private BufferedReader bufferedReader;
    private String cache;
    private boolean empty;

    public CachedFileReader(File file) throws IOException {
        bufferedReader = new BufferedReader(new FileReader(file));
        this.empty = false;
        performPop();
    }

    public boolean isEmpty() {
        return empty;
    }

    private void performPop() throws IOException {
        if (!empty) {
            if ((cache = bufferedReader.readLine()) == null) {
                empty = true;
                cache = null;
            } else {
                empty = false;
            }
        }
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }

    public String peek() {
        if (empty) {
            return null;
        }
        return cache;
    }

    public String pop() throws IOException {
        String answer = peek();
        performPop();
        return answer;
    }
}
