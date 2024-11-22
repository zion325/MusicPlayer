package com.musicplayer.util;

import com.musicplayer.model.Song;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.time.Duration;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

/**
 * 音乐文件管理工具类
 * 负责音乐文件和封面图片的存储、读取和管理
 */
public class MusicFileManager {
    /** 音乐文件存储目录 */
    private static final String MUSIC_DIR = "music";
    
    /** 封面图片存储目录 */
    private static final String COVERS_DIR = "covers";
    
    // 静态初始化块，创建必要的目录
    static {
        createDirectories();
    }
    
    /**
     * 创建必要的目录结构
     */
    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(MUSIC_DIR));
            Files.createDirectories(Paths.get(COVERS_DIR));
            Files.createDirectories(Paths.get("sample_songs"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存音乐文件到指定目录
     * @param sourceFile 源文件
     * @return 保存后的文件路径
     * @throws IOException 如果文件操作失败
     */
    public static String saveMusicFile(File sourceFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        File targetFile = new File(MUSIC_DIR, fileName);
        FileUtils.copyFile(sourceFile, targetFile);
        return targetFile.getPath();
    }
    
    /**
     * 检查音乐文件是否存在
     * @param filePath 文件路径
     * @return 文件是否存在且可读
     */
    public static boolean isMusicFileExists(String filePath) {
        if (filePath == null) return false;
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }
    
    /**
     * 获取MP3文件的时长
     * @param filePath 文件路径
     * @return 音频时长
     */
    public static Duration getMp3Duration(String filePath) {
        try {
            // 使用JAudioTagger库读取音频文件信息
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            int durationInSeconds = audioFile.getAudioHeader().getTrackLength();
            return Duration.ofSeconds(durationInSeconds);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // 如果无法读取标签，根据文件大小估算时长
                File file = new File(filePath);
                long fileSize = file.length();
                long seconds = (fileSize * 8) / (128 * 1024); // 假设比特率为128kbps
                return Duration.ofSeconds(seconds);
            } catch (Exception ex) {
                ex.printStackTrace();
                return Duration.ZERO;
            }
        }
    }
    
    /**
     * 从文件创建Song对象
     * @param file MP3文件
     * @return Song对象，如果创建失败返回null
     */
    public static Song createSongFromFile(File file) {
        String id = String.valueOf(System.currentTimeMillis());
        String title = file.getName().replaceFirst("[.][^.]+$", "");
        String artist = "未知歌手";
        
        try {
            String filePath = saveMusicFile(file);
            Duration duration = getMp3Duration(filePath);
            return new Song(id, title, artist, duration, filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 保存封面图片
     * @param sourceFile 源图片文件
     * @return 保存后的文件路径
     * @throws IOException 如果文件操作失败
     */
    public static String saveCoverImage(File sourceFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        File targetFile = new File(COVERS_DIR, fileName);
        FileUtils.copyFile(sourceFile, targetFile);
        return targetFile.getPath();
    }
    
    /**
     * 删除音乐文件
     * @param filePath 文件路径
     */
    public static void deleteMusicFile(String filePath) {
        if (filePath != null) {
            FileUtils.deleteQuietly(new File(filePath));
        }
    }
    
    /**
     * 删除封面图片
     * @param filePath 文件路径
     */
    public static void deleteCoverImage(String filePath) {
        if (filePath != null) {
            FileUtils.deleteQuietly(new File(filePath));
        }
    }
    
    /**
     * 检查并创建必要的目录
     */
    public static void checkAndCreateDirectories() {
        createDirectories();
    }
    
    /**
     * 将文件复制到音乐目录
     * @param sourcePath 源文件路径
     * @param targetFileName 目标文件名
     * @return 是否复制成功
     */
    public static boolean copyFileToMusicDir(String sourcePath, String targetFileName) {
        try {
            File sourceFile = new File(sourcePath);
            if (sourceFile.exists()) {
                File targetFile = new File(MUSIC_DIR, targetFileName);
                FileUtils.copyFile(sourceFile, targetFile);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 从输入流保存在线音乐文件
     * @param inputStream 音乐文件输入流
     * @param filename 文件名
     * @return 保存后的文件路径
     * @throws IOException 如果保存失败
     */
    public static String saveOnlineMusicFile(InputStream inputStream, String filename) throws IOException {
        // 确保目录存在
        File musicDir = new File(MUSIC_DIR);
        if (!musicDir.exists()) {
            musicDir.mkdirs();
        }
        
        // 生成唯一文件名
        String uniqueFilename = System.currentTimeMillis() + "_" + filename;
        File targetFile = new File(musicDir, uniqueFilename);
        
        // 复制文件内容
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        return targetFile.getPath();
    }
    
    /**
     * 从输入流获取MP3时长
     */
    public static Duration getMp3DurationFromStream(BufferedInputStream bis) {
        File tempFile = null;
        FileOutputStream fos = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile("temp_music_", ".mp3");
            tempFile.deleteOnExit(); // 确保程序退出时删除临时文件
            
            // 将输入流内容写入临时文件
            fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            // 不使用mark/reset，直接读取整个流
            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            
            // 使用临时文件获取时长
            AudioFile audioFile = AudioFileIO.read(tempFile);
            int durationInSeconds = audioFile.getAudioHeader().getTrackLength();
            
            return Duration.ofSeconds(durationInSeconds);
        } catch (Exception e) {
            e.printStackTrace();
            return Duration.ZERO;
        } finally {
            // 清理资源
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
} 