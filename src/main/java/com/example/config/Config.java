package com.example.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Config {
    private final Properties props = new Properties();

    public Config(String propertiesPath) {
        try (FileInputStream fis = new FileInputStream(propertiesPath)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Warning: app.properties not found.");
        }
    }

    public String getHashAlgorithm() {
        return props.getProperty("hash.algorithm", "MD5");
    }

    public List<String> getIgnorePatterns() {
        String ignoreStr = props.getProperty("scan.ignore.patterns", "");
        if (ignoreStr.isEmpty()) return Collections.emptyList();
        return Arrays.asList(ignoreStr.split(","));
    }

    public String getMode(String[] args) {
        if (args.length > 0) {
            return args[0];
        }
        return props.getProperty("app.mode", "scan");
    }

    public String getPath(String envName, String propName) {
        String val = System.getenv(envName);
        if (val != null && !val.isEmpty()) {
            return val;
        }
        val = props.getProperty(propName);
        if (val == null) {
            throw new RuntimeException("Missing config: Set env var " + envName + " or property " + propName);
        }
        return val;
    }
}