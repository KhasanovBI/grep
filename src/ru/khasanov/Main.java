package ru.khasanov;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("Illegal arguments count.");
        }
        String pattern = args[0];
        int poolSize = Math.min(Runtime.getRuntime().availableProcessors(), args.length - 1);
        ExecutorService executorService = Executors.newWorkStealingPool(poolSize);
        List<Future<List<String>>> futures = new ArrayList<>();
        byte[] bytePattern = pattern.getBytes();

        for (int i = 1; i < args.length; ++i) {
            String filePath = args[i];
            futures.add(executorService.submit(new GrepResultCallable(filePath, bytePattern)));
        }
        executorService.shutdown();
        for (Future<List<String>> future : futures) {
            try {
                for (String resultLine : future.get()) {
                    System.out.println(resultLine);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
