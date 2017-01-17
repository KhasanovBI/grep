package ru.khasanov;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by bulat on 04.01.17.
 */
public class GrepResultCallable implements Callable<List<String>> {
    private static final int INITIAL_SIZE = 100;
    private static final byte NEW_LINE_BYTE = (byte) '\n';
    private final String filePath;
    private final byte[] pattern;

    public GrepResultCallable(String filePath, byte[] pattern) {
        this.filePath = filePath;
        this.pattern = pattern;
    }

    private boolean isContainPattern(byte[] byteLine) {
        return arrayIndexOf(byteLine, pattern) != -1;
    }

    public int arrayIndexOf(byte[] largeArray, byte[] subArray) {
        if (largeArray.length == 0 || subArray.length == 0) {
            return -1;
        }
        if (subArray.length > largeArray.length) {
            return -1;
        }
        for (int i = 0; i < largeArray.length; i++) {
            if (largeArray[i] == subArray[0]) {
                boolean subArrayFound = true;
                for (int j = 0; j < subArray.length; j++) {
                    if (largeArray.length <= i + j || subArray[j] != largeArray[i + j]) {
                        subArrayFound = false;
                        break;
                    }
                }
                if (subArrayFound) {
                    return i;
                }
            }
        }
        return -1;
    }

    private byte[] getLineBytes(ByteBuffer byteBuffer) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(INITIAL_SIZE);
        while (byteBuffer.position() < byteBuffer.limit()) {
            byte b = byteBuffer.get();
            if (b == NEW_LINE_BYTE) {
                break;
            }
            outputStream.write(b);
        }
        return outputStream.toByteArray();
    }

    @Override
    public List<String> call() throws Exception {
        List<String> result = new ArrayList<>();
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
        FileChannel fileChannel = randomAccessFile.getChannel();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        mappedByteBuffer.load();
        while (mappedByteBuffer.position() < mappedByteBuffer.limit()) {
            byte[] lineBytes = getLineBytes(mappedByteBuffer);
            if (isContainPattern(lineBytes)) {
                result.add(filePath + ": " + new String(lineBytes));
            }
        }
        return result;
    }
}