package com.musicplayer.model;

import java.util.Map;

public class OnlineMusicSheet {
    private String creator;
    private String creatorId;
    private String dateCreated;
    private int id;
    private Map<String, String> musicItems; // MD5 -> filename
    private String name;
    private String picture;
    private String uuid;

    // Getters and Setters
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Map<String, String> getMusicItems() { return musicItems; }
    public void setMusicItems(Map<String, String> musicItems) { this.musicItems = musicItems; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
} 