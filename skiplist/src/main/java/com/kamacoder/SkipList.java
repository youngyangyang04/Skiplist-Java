package com.kamacoder;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class SkipList<K extends Comparable<K>, V> {
    /**
     * Node 类，用于实际存储数据
     * @param <K>
     * @param <V>
     */
    private static class Node<K extends Comparable<K>, V> {
        K key;    // 存储的 key
        V value;  // 存储的 value
        int level;  // 节点所在的层级
        ArrayList<Node<K, V>> forwards;

        Node(K key, V value, int level) {
            this.key = key;
            this.value = value;
            this.level = level;
            this.forwards = new ArrayList<>(Collections.nCopies(level + 1, null));
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
    /**
     * 跳表的最大高度
     */
    private static final int MAX_LEVEL = 32;  // 跳表的最大高度
    /**
     * 跳表的头节点
     */
    private Node<K, V> header;  // 头节点
    /**
     * 跳表中的节点数量
     */
    private int nodeCount;
    /**
     * 跳表当前的层级
     */
    private int skipListLevel;
    /**
     * 跳表数据持久化路径
     */
    private static final String STORE_FILE = "./store";

    /**
     * 跳表构造方法
     */
    SkipList() {
        this.header = new Node<>(null, null, MAX_LEVEL);
        this.nodeCount = 0;
        this.skipListLevel = 0;
    }

    /**
     * 创建 Node 方法
     *
     * @param key 存入的键
     * @param value 存入的值
     * @param level 该节点所在的层级
     * @return 返回创建后的该节点
     */
    private Node<K, V> createNode(K key, V value, int level) {
        return new Node<>(key, value, level);
    }

    /**
     * 生成 Node 所在层级方法
     * @return 返回节点层级
     */
    private static int generateRandomLevel() {  // 生成随机层级方法
        int level = 1;
        Random random = new Random();
        while (random.nextInt(2) == 1) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }

    /**
     * @return 返回跳表中节点的数量
     */
    public int size() {
        return this.nodeCount;
    }

    /**
     * 向跳表中插入一个键值对，如果跳表中已经存在相同 key 的节点，则更新这个节点的 value
     * @param key 插入的 Node 的键
     * @param value 插入的 Node 的值
     * @return 返回插入结果，插入成功返回 true，插入失败返回 false
     */
    public synchronized boolean insertNode(K key, V value) {
        Node<K, V> current = this.header;
        ArrayList<Node<K, V>> update = new ArrayList<>(Collections.nCopies(MAX_LEVEL + 1, null));

        for (int i = this.skipListLevel; i >= 0; i--) {
            while (current.forwards.get(i) != null && current.forwards.get(i).getKey().compareTo(key) < 0) {
                current = current.forwards.get(i);
            }
            update.set(i, current);
        }

        current = current.forwards.get(0);

        if (current != null && current.getKey().compareTo(key) == 0) { // 如果 key 已经存在
            // 更新 key 对应的 value
            current.setValue(value);
            return true;
        }

        // 生成节点随机层数
        int randomLevel = generateRandomLevel();

        if (current == null || current.getKey().compareTo(key) != 0) {

            if (randomLevel > skipListLevel) {
                for (int i = skipListLevel + 1; i < randomLevel + 1; i++) {
                    update.set(i, header);
                }
                skipListLevel = randomLevel;  // 更新跳表的当前高度
            }

            Node<K, V> insertNode = createNode(key, value, randomLevel);

            // 修改跳表中的指针指向
            for (int i = 0; i <= randomLevel; i++) {
                insertNode.forwards.set(i, update.get(i).forwards.get(i));
                update.get(i).forwards.set(i, insertNode);
            }
            nodeCount++;
            return true;
        }
        return false;
    }

    /**
     * 搜索跳表中是否存在键为 key 的键值对
     * @param key 键
     * @return 跳表中存在键为 key 的键值对返回 true，不存在返回 false
     */
    public boolean searchNode(K key) {
        Node<K, V> current = this.header;

        for (int i = this.skipListLevel; i >= 0; i--) {
            while (current.forwards.get(i) != null && current.forwards.get(i).getKey().compareTo(key) < 0) {
                current = current.forwards.get(i);
            }
        }

        current = current.forwards.get(0);
        return current != null && current.getKey().compareTo(key) == 0;
    }

    /**
     * 获取键为 key 的 Node 的值
     * @param key 键
     * @return 返回键为 key 的节点，如果不存在则返回 null
     */
    public V getNode(K key) {
        Node<K, V> current = this.header;

        for (int i = this.skipListLevel; i >= 0; i--) {
            while (current.forwards.get(i) != null && current.forwards.get(i).getKey().compareTo(key) < 0) {
                current = current.forwards.get(i);
            }
        }

        current = current.forwards.get(0);

        if (current != null && current.getKey().compareTo(key) == 0) {
            return current.getValue();
        }
        // 这里有一个限制，存入的 key 和 value 必须是 Java 对象
        return null;
    }

    /**
     * 根据 key 删除 SkipList 中的 Node
     *
     * @param key 需要删除的 Node 的 key
     * @return 删除成功返回 true，失败返回 false
     */
    public synchronized boolean deleteNode(K key) {
        Node<K, V> current = this.header;
        ArrayList<Node<K, V>> update = new ArrayList<>(Collections.nCopies(MAX_LEVEL + 1, null));

        for (int i = this.skipListLevel; i >= 0; i--) {
            while (current.forwards.get(i) != null && current.forwards.get(i).getKey().compareTo(key) < 0) {
                current = current.forwards.get(i);
            }
            update.set(i, current);
        }

        current = current.forwards.get(0);

        // 搜索到 key
        if (current != null && current.getKey().compareTo(key) == 0) {
            for (int i = 0; i < this.skipListLevel; i++) {

                if (update.get(i).forwards.get(i) != current) break;

                update.get(i).forwards.set(i, current.forwards.get(i));
            }
        }

        while (this.skipListLevel > 0 && this.header.forwards.get(this.skipListLevel) == null) {
            this.skipListLevel--;
        }

        this.nodeCount--;
        return true;
    }

    /**
     * 持久化跳表内的数据
     */
    public void dumpFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(STORE_FILE))) {
            Node<K, V> node = this.header.forwards.get(0);
            while (node != null) {
                String data = node.getKey() + ":" + node.getValue() + ";";
                bufferedWriter.write(data);
                bufferedWriter.newLine();
                node = node.forwards.get(0);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to dump file", e);
        }
    }

    /**
     * 从文本文件中读取数据
     */
    public void loadFile() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(STORE_FILE))) {
            String data;
            while ((data = bufferedReader.readLine()) != null) {
                System.out.println(data);
                Node<K, V> node = getKeyValueFromString(data);
                if (node != null) {
                    insertNode(node.getKey(), node.getValue());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断读取的字符串是否合法
     *
     * @param data 字符串
     * @return 合法返回 true，非法返回 false
     */
    private boolean isValidString(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        if (!data.contains(":")) {
            return false;
        }
        return true;
    }

    /**
     * 根据文件中的持久化字符串，获取 key 和 value，并将 key 和 value 封装到 Node 对象中
     * @param data 字符串
     * @return 返回该字符串对应的key和value 组成的 Node 实例，如果字符串非法，则返回 null
     */
    private Node<K, V> getKeyValueFromString(String data) {
        if (!isValidString(data)) return null;
        String substring = data.substring(0, data.indexOf(":"));
        K key = (K) substring;
        // 去掉分号，不要结尾冒号
        String substring1 = data.substring(data.indexOf(":") + 1, data.length() - 1);
        V value = (V) substring1;
        return new Node<K, V>(key, value, 1);
    }

    /**
     * 打印跳表的结构
     */
    public void displaySkipList() {
        // 从最上层开始向下遍历所有层
        for (int i = this.skipListLevel; i >= 0; i--) {
            Node<K, V> node = this.header.forwards.get(i);
            System.out.print("Level " + i + ": ");
            // 遍历当前层的所有节点
            while (node != null) {
                // 打印当前节点的键和值，键值对之间用":"分隔
                System.out.print(node.getKey() + ":" + node.getValue() + ";");
                // 移动到当前层的下一个节点
                node = node.forwards.get(i);
            }
            // 当前层遍历结束，换行
            System.out.println();
        }
    }

    public static void main(String[] args) {
        SkipList<String, String> skipList = new SkipList<>();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String command = scanner.nextLine();
            String[] commandList = command.split(" ");
            if (commandList[0].equals("insert")) {
                boolean b = skipList.insertNode(commandList[1], commandList[2]);
                if (b) {
                    System.out.println("Key: " + commandList[1] + " Value: " + commandList[2] + " insert success!");
                } else {
                    System.out.println("Key: " + commandList[1] + " Value: " + commandList[2] + " insert failed");
                }
            } else if (commandList[0].equals("delete")) {
                boolean b = skipList.deleteNode(commandList[1]);
                if (b) {
                    System.out.println("Key: " + commandList[1] + " deleted!");
                } else {
                    System.out.println("skiplist not exists the key: " + commandList[1]);
                }
            } else if (commandList[0].equals("search")) {
                boolean b = skipList.searchNode(commandList[1]);
                if (b) {
                    System.out.println("Key: " + commandList[1] + " searched!");
                } else {
                    System.out.println("Key: " + commandList[1] + " not exists!");
                }
            } else if (commandList[0].equals("get")) {
                if (!skipList.searchNode(commandList[1])) {
                    System.out.println("Key: " + commandList[1] + " not exists!");
                }
                String node = skipList.getNode(commandList[1]);
                if (node != null) {
                    System.out.println("Key: " + commandList[1] + "'s value is " + node);
                }
            } else if (commandList[0].equals("dump")) {
                skipList.dumpFile();
                System.out.println("Already saved skiplist.");
            } else if (commandList[0].equals("load")) {
                skipList.loadFile();
            } else {
                System.out.println("********skiplist*********");
                skipList.displaySkipList();
                System.out.println("*************************");
            }
        }
    }
}