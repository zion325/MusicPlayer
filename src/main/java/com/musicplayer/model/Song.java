package com.musicplayer.model;

import java.io.Serializable;
import java.time.Duration;

/**
 * 歌曲实体类，存储单首歌曲的信息
 */
public class Song implements Serializable {
    /** 序列化版本ID */
    private static final long serialVersionUID = 1L;
    
    /** 歌曲ID */
    private String id;
    
    /** 歌曲标题 */
    private String title;
    
    /** 歌手名称 */
    private String artist;
    
    /** 歌曲时长 */
    private Duration duration;
    
    /** 音频文件路径 */
    private String filePath;
    
    /**
     * 构造函数
     * @param id 歌曲ID
     * @param title 歌曲标题
     * @param artist 歌手名称
     * @param duration 歌曲时长
     * @param filePath 音频文件路径
     */
    public Song(String id, String title, String artist, Duration duration, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.filePath = filePath;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
} 