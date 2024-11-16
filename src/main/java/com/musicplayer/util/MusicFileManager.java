package com.musicplayer.util;

import com.musicplayer.model.Song;
import org.apache.commons.io.FileUtils;
import javazoom.jl.player.advanced.AdvancedPlayer;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Map;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;

public class MusicFileManager {
    private static final String MUSIC_DIR = "music";
    private static final String COVERS_DIR = "covers";
    
    static {
        createDirectories();
    }
    
    private static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(MUSIC_DIR));
            Files.createDirectories(Paths.get(COVERS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String saveMusicFile(File sourceFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        File targetFile = new File(MUSIC_DIR, fileName);
        FileUtils.copyFile(sourceFile, targetFile);
        return targetFile.getPath();
    }
    
    public static Duration getMp3Duration(String filePath) {
        try {
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            int durationInSeconds = audioFile.getAudioHeader().getTrackLength();
            return Duration.ofSeconds(durationInSeconds);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果无法获取准确时长，使用文件大小估算
            try {
                File file = new File(filePath);
                long fileSize = file.length();
                // 假设比特率为128kbps
                long seconds = (fileSize * 8) / (128 * 1024);
                return Duration.ofSeconds(seconds);
            } catch (Exception ex) {
                ex.printStackTrace();
                return Duration.ZERO;
            }
        }
    }
    
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
    
    public static String saveCoverImage(File sourceFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        File targetFile = new File(COVERS_DIR, fileName);
        FileUtils.copyFile(sourceFile, targetFile);
        return targetFile.getPath();
    }
    
    public static void deleteMusicFile(String filePath) {
        FileUtils.deleteQuietly(new File(filePath));
    }
    
    public static void deleteCoverImage(String filePath) {
        FileUtils.deleteQuietly(new File(filePath));
    }
} 