package com.musicplayer.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 歌单实体类，存储歌单信息和歌曲列表
 * 实现Serializable接口以支持对象序列化
 */
public class Playlist implements Serializable {
    /** 序列化版本ID */
    private static final long serialVersionUID = 1L;
    
    /** 歌单ID */
    private String id;
    
    /** 歌单名称 */
    private String name;
    
    /** 创建者ID（学号） */
    private String ownerId;
    
    /** 创建时间 */
    private LocalDateTime createDate;
    
    /** 封面图片路径 */
    private String coverImagePath;
    
    /** 歌曲列表 */
    private List<Song> songs;
    
    /**
     * 构造函数，创建新的歌单
     * @param id 歌单ID
     * @param name 歌单名称
     * @param ownerId 创建者ID（学号）
     */
    public Playlist(String id, String name, String ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.createDate = LocalDateTime.now();
        this.songs = new ArrayList<>();
    }
    
    /**
     * 获取歌单ID
     * @return 歌单ID
     */
    public String getId() { return id; }
    
    /**
     * 设置歌单ID
     * @param id 歌单ID
     */
    public void setId(String id) { this.id = id; }
    
    /**
     * 获取歌单名称
     * @return 歌单名称
     */
    public String getName() { return name; }
    
    /**
     * 设置歌单名称
     * @param name 歌单名称
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * 获取创建者ID
     * @return 创建者ID（学号）
     */
    public String getOwnerId() { return ownerId; }
    
    /**
     * 设置创建者ID
     * @param ownerId 创建者ID（学号）
     */
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreateDate() { return createDate; }
    
    /**
     * 设置创建时间
     * @param createDate 创建时间
     */
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    
    /**
     * 获取封面图片路径
     * @return 封面图片路径
     */
    public String getCoverImagePath() { return coverImagePath; }
    
    /**
     * 设置封面图片路径
     * @param coverImagePath 封面图片路径
     */
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }
    
    /**
     * 获取歌曲列表
     * @return 歌曲列表
     */
    public List<Song> getSongs() { return songs; }
    
    /**
     * 设置歌曲列表
     * @param songs 歌曲列表
     */
    public void setSongs(List<Song> songs) { this.songs = songs; }
    
    /**
     * 添加歌曲到歌单
     * 如果歌曲已存在，则不重复添加
     * @param song 要添加的歌曲
     */
    public void addSong(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
        }
    }
    
    /**
     * 从歌单中移除歌曲
     * @param song 要移除的歌曲
     */
    public void removeSong(Song song) {
        songs.remove(song);
    }
} 