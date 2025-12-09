package com.example.service;

import com.example.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ComparatorServiceTest {

    @TempDir
    Path tempDir;

    private Config config;
    private ComparatorService service;
    private File propsFile;

    @BeforeEach
    void setUp() throws Exception {
        propsFile = tempDir.resolve("app.properties").toFile();
        try (FileWriter writer = new FileWriter(propsFile)) {
            writer.write("hash.algorithm=MD5\n");
            writer.write("scan.ignore.patterns=*.log,ignore.me\n");
            writer.write("app.mode=scan\n");
            writer.write("scan.path=.\n");
            writer.write("snapshot.output=out.json\n");
        }
        config = new Config(propsFile.getAbsolutePath());
        service = new ComparatorService(config);
    }

    @Test
    void testShouldIgnore() {
        assertTrue(service.shouldIgnore("error.log"));
        assertTrue(service.shouldIgnore("test.log"));
        assertTrue(service.shouldIgnore("ignore.me"));

        assertFalse(service.shouldIgnore("data.txt"));
        assertFalse(service.shouldIgnore("image.png"));
    }

    @Test
    void testFullCycleLogic() throws Exception {
        File scanDir = tempDir.resolve("src_files").toFile();
        scanDir.mkdirs();
        new FileWriter(new File(scanDir, "file1.txt")).append("content1").close();
    }
}