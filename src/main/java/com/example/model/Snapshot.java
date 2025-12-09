package com.example.model;

import java.util.List;

public class Snapshot {
    private String rootPath;
    private List<FileEntry> files;

    public Snapshot() {}

    public Snapshot(String rootPath, List<FileEntry> files) {
        this.rootPath = rootPath;
        this.files = files;
    }

    public String getRootPath() { return rootPath; }
    public List<FileEntry> getFiles() { return files; }
    public void setRootPath(String rootPath) { this.rootPath = rootPath; }
    public void setFiles(List<FileEntry> files) { this.files = files; }

    public static class FileEntry {
        private String path;
        private String hash;

        public FileEntry() {}

        public FileEntry(String path, String hash) {
            this.path = path;
            this.hash = hash;
        }

        public String getPath() { return path; }
        public String getHash() { return hash; }
        public void setPath(String path) { this.path = path; }
        public void setHash(String hash) { this.hash = hash; }
    }
}