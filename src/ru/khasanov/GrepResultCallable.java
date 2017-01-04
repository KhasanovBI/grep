package ru.khasanov;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by bulat on 04.01.17.
 */
public class GrepResultCallable implements Callable<List<String>> {
    private final String filePath;
    private final String pattern;

    public GrepResultCallable(String filePath, String pattern) {
        this.filePath = filePath;
        this.pattern = pattern;
    }

    private boolean isContains(String line, String pattern) {
        return line.contains(pattern);
    }

    @Override
    public List<String> call() throws Exception {
        List<String> result = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (isContains(line, pattern)) {
                    result.add(filePath + ": " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
