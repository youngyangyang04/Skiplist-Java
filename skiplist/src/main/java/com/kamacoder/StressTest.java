package com.kamacoder;

import java.util.Random;

public class StressTest {
    public static final int INSERT_TIMES = 100000;
    public static final int SEARCH_TIMES = 100000;
    public static String generateRandomString() {
        // 定义可能出现在随机字符串中的字符
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        // 指定字符串长度，可以根据需要修改
        int length = 10;
        // 使用StringBuilder来构建最终的字符串
        StringBuilder result = new StringBuilder(length);
        // 创建Random实例用于生成随机数
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            // 生成一个随机索引值，用于从字符集中选择字符
            int index = random.nextInt(characters.length());
            // 将选择的字符添加到结果中
            result.append(characters.charAt(index));
        }
        return result.toString();
    }
    public static void main(String[] args) throws InterruptedException {
        int numberOfThreads = 10;
        // 记录所有任务开始前的时间
        long start = System.currentTimeMillis();

        Thread[] threads = new Thread[numberOfThreads];
        SkipList<String, String> skipList = new SkipList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            // 创建任务线程
            threads[i] = new Thread(new InsertTask<String, String>(skipList));
            threads[i].start();
        }

        // 等待所有线程执行完毕
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i].join();
        }

        // 所有线程都执行完毕，记录结束时间
        long end = System.currentTimeMillis();
        // 计算并打印总执行时间
        System.out.println("在 " + numberOfThreads + " 线程环境下，插入 " + (numberOfThreads * INSERT_TIMES) + " 次数据耗时为：" + (end - start) + "ms");

        // 压测搜索时间
        long start2 = System.currentTimeMillis();

        Thread[] threads2 = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads2[i] = new Thread(new SearchTask<String, String>(skipList));
            threads2[i].start();
        }

        for (int i = 0; i < numberOfThreads; i++) {
            threads2[i].join();
        }

        long end2 = System.currentTimeMillis();
        System.out.println("在 " + numberOfThreads + " 线程环境下，搜索 " + (numberOfThreads * SEARCH_TIMES) + " 次数据耗时为： " + (end2 - start2) + "ms");

    }

    private static class InsertTask<K extends Comparable<K>, V> implements Runnable {
        SkipList<K, V> skipList;
        InsertTask(SkipList<K, V> skipList) {
            this.skipList = skipList;
        }
        @Override
        public void run() {
            for (int i = 0; i < INSERT_TIMES; i++) {
                boolean b = this.skipList.insertNode((K)generateRandomString(), (V)generateRandomString());
            }
        }
    }

    private static class SearchTask<K extends Comparable<K>, V> implements Runnable {
        SkipList<K, V> skipList;
        SearchTask(SkipList<K, V> skipList) {
            this.skipList = skipList;
        }
        @Override
        public void run() {
            for (int i = 0; i < SEARCH_TIMES; i++) {
                this.skipList.searchNode((K)generateRandomString());
            }
        }
    }
}

