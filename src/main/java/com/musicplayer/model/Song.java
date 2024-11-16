package com.musicplayer.model;

import java.io.Serializable;
import java.time.Duration;

public class Song implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String artist;
    private Duration duration;
    private String filePath;
    
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