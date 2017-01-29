package ru.khasanov;

import com.sun.tools.javac.util.Pair;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by bulat on 04.01.17.
 */


public class GrepResultCallable implements Callable<List<String>> {
    private static final byte NEW_LINE_BYTE = (byte) '\n';
    private final String filePath;
    private final byte[] patternBytes;
    private int previousLinePosition = 0;
    private int currentLinePosition = 0;
    private MappedByteBuffer mappedByteBuffer;
    private List<Pair<Integer, Integer>> resultPositions = new ArrayList<>();

    public GrepResultCallable(String filePath, byte[] patternBytes) {
        this.filePath = filePath;
        this.patternBytes = patternBytes;
    }

    private void skipToNextLine() {
        while ((mappedByteBuffer.get()) != NEW_LINE_BYTE) {
        }
        updateNewLinesInfo();
    }

    private boolean isPatternAtPosition() {
        byte b = mappedByteBuffer.get();
        if (b == NEW_LINE_BYTE) {
            updateNewLinesInfo();
        } else if (b == patternBytes[0]) {
            for (int i = 1; i < patternBytes.length; i++) {
                byte patternByte = patternBytes[i];
                // Не вылетим за пределы так как проверка во внешней функции
                byte b2 = mappedByteBuffer.get();
                if (b2 == NEW_LINE_BYTE) {
                    updateNewLinesInfo();
                    return false;
                } else if (patternByte != b2) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void updateNewLinesInfo() {
        previousLinePosition = currentLinePosition;
        currentLinePosition = mappedByteBuffer.position();
    }

    private List<String> getInclusions() {
        List<String> result = new ArrayList<>();
        if (mappedByteBuffer.limit() == 0 || patternBytes.length == 0 || patternBytes.length > mappedByteBuffer.limit()) {
            return result;
        }
        while (mappedByteBuffer.position() < mappedByteBuffer.limit() - patternBytes.length) {
            if (isPatternAtPosition()) {
                skipToNextLine();
                resultPositions.add(new Pair<>(previousLinePosition, currentLinePosition));
            }
        }
        for (Pair<Integer, Integer> pair : resultPositions) {
            mappedByteBuffer.position(pair.fst);
            result.add(getStringFromSlice(pair.fst, pair.snd));
        }
        return result;
    }

    private String getStringFromSlice(int start, int end) {
        byte[] bytes = new byte[end - start];
        mappedByteBuffer.get(bytes);
        return filePath + ": " + new String(bytes);
    }

    @Override
    public List<String> call() throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
        FileChannel fileChannel = randomAccessFile.getChannel();
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).load();
        return getInclusions();
    }
}