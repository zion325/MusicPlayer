package com.musicplayer.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StreamCache {
    /** 缓存文件映射 */
    private static final Map<String, File> cacheFiles = new HashMap<>();
    
    /** 缓存目录 */
    private static final String CACHE_DIR = "cache";
    
    /** 缓冲区大小 */
    private static final int BUFFER_SIZE = 8192 * 4;
    
    static {
        // 创建缓存目录
        try {
            Files.createDirectories(Paths.get(CACHE_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取缓存的音乐文件
     */
    public static File getCachedFile(String md5) {
        // 先检查内存中的缓存
        File cachedFile = cacheFiles.get(md5);
        if (cachedFile != null && cachedFile.exists() && cachedFile.length() > 0) {
            return cachedFile;
        }
        
        // 检查磁盘缓存
        File diskCacheFile = new File(CACHE_DIR, "music_" + md5 + ".mp3");
        if (diskCacheFile.exists() && diskCacheFile.length() > 0) {
            cacheFiles.put(md5, diskCacheFile);
            return diskCacheFile;
        }
        
        return null;
    }
    
    /**
     * 缓存音乐流
     */
    public static File cacheStream(String md5, InputStream inputStream) throws IOException {
        // 创建缓存文件
        File cacheFile = new File(CACHE_DIR, "music_" + md5 + ".mp3");
        
        // 使用临时文件
        File tempFile = new File(CACHE_DIR, "temp_" + md5 + ".mp3");
        
        try {
            // 将流写入临时文件
            try (BufferedInputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE);
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile), BUFFER_SIZE)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush();
            }
            
            // 检查文件完整性
            if (tempFile.length() > 0) {
                // 如果文件有效，移动到缓存文件
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
                if (tempFile.renameTo(cacheFile)) {
                    cacheFiles.put(md5, cacheFile);
                    return cacheFile;
                }
            }
            
            throw new IOException("Failed to cache file");
            
        } finally {
            // 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * 获取新的输入流
     */
    public static InputStream getInputStream(String md5) throws IOException {
        File cachedFile = getCachedFile(md5);
        if (cachedFile != null) {
            return new BufferedInputStream(new FileInputStream(cachedFile), BUFFER_SIZE);
        }
        return null;
    }
    
    /**
     * 清理所有缓存
     */
    public static void clearCache() {
        // 清理内存缓存
        cacheFiles.clear();
        
        // 清理磁盘缓存
        try {
            Files.walk(Paths.get(CACHE_DIR))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(file -> {
                    if (file.exists()) {
                        file.delete();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 