package com.example.service;

import com.example.config.Config;
import com.example.model.Snapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис, отвечающий за бизнес-логику приложения:
 * сканирование директорий и сравнение снимков файловой системы.
 */
public class ComparatorService {
    private final Config config;
    private final ObjectMapper mapper;

    /**
     * Конструктор сервиса.
     * @param config конфигурация приложения
     */
    public ComparatorService(Config config) {
        this.config = config;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Выполняет сканирование директории.
     * Считывает путь из SCAN_PATH, обходит файлы, считает хэши
     * и сохраняет результат в JSON по пути SNAPSHOT_OUTPUT.
     *
     * @throws Exception в случае ошибок ввода-вывода или хэширования
     */
    public void runScan() throws Exception {
        String scanPathStr = config.getPath("SCAN_PATH", "scan.path");
        String outputPathStr = config.getPath("SNAPSHOT_OUTPUT", "snapshot.output");

        System.out.println("Scanning: " + scanPathStr);
        Path scanPath = Paths.get(scanPathStr);
        List<Snapshot.FileEntry> entries = new ArrayList<>();

        if (!Files.exists(scanPath)) {
            throw new IllegalArgumentException("Directory not found: " + scanPathStr);
        }

        Files.walkFileTree(scanPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (shouldIgnore(file.getFileName().toString())) return FileVisitResult.CONTINUE;
                try {
                    String relativePath = scanPath.relativize(file).toString().replace("\\", "/");
                    String hash = calculateHash(file);
                    entries.add(new Snapshot.FileEntry(relativePath, hash));
                } catch (Exception e) {
                    System.err.println("Error processing file: " + file + " (" + e.getMessage() + ")");
                }
                return FileVisitResult.CONTINUE;
            }
        });

        Snapshot snapshot = new Snapshot(scanPath.toAbsolutePath().toString(), entries);
        File outFile = new File(outputPathStr);
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }
        mapper.writeValue(outFile, snapshot);
        System.out.println("Saved to: " + outputPathStr);
    }

    /**
     * Выполняет сравнение двух снимков (snapshots).
     * Загружает старый и новый JSON файлы, сравнивает хэши файлов
     * и выводит список добавленных, удаленных и измененных файлов.
     *
     * @throws Exception в случае ошибок чтения JSON
     */
    public void runDiff() throws Exception {
        String oldPath = config.getPath("SNAPSHOT_OLD", "snapshot.old");
        String newPath = config.getPath("SNAPSHOT_NEW", "snapshot.new");

        System.out.println("Comparing: " + oldPath + " vs " + newPath);
        Snapshot snapOld = mapper.readValue(new File(oldPath), Snapshot.class);
        Snapshot snapNew = mapper.readValue(new File(newPath), Snapshot.class);

        Map<String, String> oldMap = snapOld.getFiles().stream()
                .filter(f -> !shouldIgnore(f.getPath()))
                .collect(Collectors.toMap(Snapshot.FileEntry::getPath, Snapshot.FileEntry::getHash));
        Map<String, String> newMap = snapNew.getFiles().stream()
                .filter(f -> !shouldIgnore(f.getPath()))
                .collect(Collectors.toMap(Snapshot.FileEntry::getPath, Snapshot.FileEntry::getHash));

        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(oldMap.keySet());
        allFiles.addAll(newMap.keySet());

        List<String> output = new ArrayList<>();
        for (String path : allFiles) {
            boolean inOld = oldMap.containsKey(path);
            boolean inNew = newMap.containsKey(path);

            if (inOld && !inNew) {
                output.add("REMOVED: " + path);
            } else if (!inOld && inNew) {
                output.add("ADDED: " + path);
            } else if (inOld && inNew && !oldMap.get(path).equals(newMap.get(path))) {
                output.add("CHANGED: " + path);
            }
        }
        Collections.sort(output);
        if (output.isEmpty()) {
            System.out.println("No changes found.");
        } else {
            output.forEach(System.out::println);
        }
    }

    /**
     * Проверяет, нужно ли игнорировать файл на основе паттернов конфигурации.
     * @param fileName имя файла или путь
     * @return true, если файл нужно пропустить
     */
    public boolean shouldIgnore(String fileName) {
        for (String pattern : config.getIgnorePatterns()) {
            pattern = pattern.trim();
            if (pattern.startsWith("*") && fileName.endsWith(pattern.substring(1))) return true;
            if (fileName.equals(pattern)) return true;
        }
        return false;
    }

    /**
     * Вычисляет хэш-сумму файла.
     * @param path путь к файлу
     * @return строковое представление хэша (hex)
     * @throws Exception если алгоритм не найден или ошибка чтения
     */
    private String calculateHash(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getHashAlgorithm());
        try (InputStream fis = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = fis.read(buffer)) != -1) digest.update(buffer, 0, n);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}