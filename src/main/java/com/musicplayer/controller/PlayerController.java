package com.musicplayer.controller;

import com.musicplayer.model.Song;
import com.musicplayer.model.Playlist;
import com.musicplayer.audio.AudioPlayer;
import com.musicplayer.util.MusicFileManager;
import java.io.InputStream;
import java.util.List;

/**
 * 播放器控制器类，负责音乐播放的核心控制逻辑
 */
public class PlayerController {
    /** 当前播放的歌曲 */
    private Song currentSong;
    
    /** 当前播放的歌单 */
    private Playlist currentPlaylist;
    
    /** 播放模式 */
    private PlayMode playMode = PlayMode.SEQUENCE;
    
    /** 是否正在播放 */
    private boolean isPlaying = false;
    
    /** 音频播放器实例 */
    private final AudioPlayer audioPlayer;
    
    /** 进度监听器 */
    private AudioPlayer.ProgressListener progressListener;
    
    /** 在线播放列表 */
    private List<Song> onlinePlaylist;
    
    /** 流状态 */
    private boolean streamClosed = false;
    
    /**
     * 构造函数，初始化音频播放器
     */
    public PlayerController() {
        this.audioPlayer = new AudioPlayer();
    }
    
    /**
     * 播放模式枚举
     */
    public enum PlayMode {
        /** 顺序播放 */
        SEQUENCE,
        /** 随机播放 */
        RANDOM,
        /** 单曲循环 */
        SINGLE_LOOP
    }
    
    /**
     * 播放当前歌曲
     * @throws RuntimeException 如果音乐文件不存在
     */
    public void play() {
        if (currentSong != null) {
            String filePath = currentSong.getFilePath();
            if (!MusicFileManager.isMusicFileExists(filePath)) {
                throw new RuntimeException("Music file not found: " + filePath);
            }
            audioPlayer.stop();
            audioPlayer.play(filePath);
            isPlaying = true;
        }
    }
    
    /**
     * 从暂停处继续播放
     */
    public void resume() {
        if (currentSong != null) {
            if (!needNewStream()) {
                audioPlayer.resume();
                isPlaying = true;
            }
        }
    }
    
    /**
     * 暂停播放
     */
    public void pause() {
        audioPlayer.pause();
        isPlaying = false;
        if (onlinePlaylist != null) {
            streamClosed = true;  // 标记在线流已关闭
        }
    }
    
    /**
     * 停止播放
     */
    public void stop() {
        audioPlayer.stop();
        isPlaying = false;
    }
    
    /**
     * 播放下一首歌曲
     */
    public void next() {
        List<Song> currentList = onlinePlaylist != null ? onlinePlaylist : 
            (currentPlaylist != null ? currentPlaylist.getSongs() : null);
            
        if (currentList != null && !currentList.isEmpty()) {
            int currentIndex = currentList.indexOf(currentSong);
            if (currentIndex >= 0) {
                stop();
                
                int nextIndex;
                switch (playMode) {
                    case RANDOM:
                        do {
                            nextIndex = (int) (Math.random() * currentList.size());
                        } while (nextIndex == currentIndex && currentList.size() > 1);
                        break;
                    case SINGLE_LOOP:
                        nextIndex = currentIndex;
                        break;
                    case SEQUENCE:
                    default:
                        nextIndex = (currentIndex + 1) % currentList.size();
                        break;
                }
                
                currentSong = currentList.get(nextIndex);
                if (onlinePlaylist != null) {
                    streamClosed = true;  // 标记需要新的流
                } else {
                    // 本地歌曲直接播放
                    play();
                }
                isPlaying = true;     // 标记为播放状态
            }
        }
    }
    
    /**
     * 播放上一首歌曲
     */
    public void previous() {
        List<Song> currentList = onlinePlaylist != null ? onlinePlaylist : 
            (currentPlaylist != null ? currentPlaylist.getSongs() : null);
            
        if (currentList != null && !currentList.isEmpty()) {
            int currentIndex = currentList.indexOf(currentSong);
            if (currentIndex >= 0) {
                stop();
                
                int prevIndex;
                switch (playMode) {
                    case RANDOM:
                        do {
                            prevIndex = (int) (Math.random() * currentList.size());
                        } while (prevIndex == currentIndex && currentList.size() > 1);
                        break;
                    case SINGLE_LOOP:
                        prevIndex = currentIndex;
                        break;
                    case SEQUENCE:
                    default:
                        prevIndex = (currentIndex - 1 + currentList.size()) % currentList.size();
                        break;
                }
                
                currentSong = currentList.get(prevIndex);
                if (onlinePlaylist != null) {
                    streamClosed = true;  // 标记需要新的流
                } else {
                    // 本地歌曲直接播放
                    play();
                }
                isPlaying = true;     // 标记为播放状态
            }
        }
    }
    
    /**
     * 设置播放模式
     * @param mode 播放模式
     */
    public void setPlayMode(PlayMode mode) {
        this.playMode = mode;
    }
    
    /**
     * 设置进度监听器
     * @param listener 进度监听器
     */
    public void setProgressListener(AudioPlayer.ProgressListener listener) {
        this.progressListener = listener;
        audioPlayer.setProgressListener(listener);
    }
    
    /**
     * 设置在线播放列表
     */
    public void setOnlinePlaylist(List<Song> playlist) {
        this.onlinePlaylist = playlist;
    }
    
    /**
     * 播放在线音乐流
     */
    public void playOnlineStream(InputStream musicStream) {
        try {
            audioPlayer.playStream(musicStream);
            streamClosed = false;  // 重置流状态
            isPlaying = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to play online music: " + e.getMessage());
        }
    }
    
    /**
     * 清除在线播放列表
     */
    public void clearOnlinePlaylist() {
        this.onlinePlaylist = null;
    }
    
    /**
     * 检查是否是在线播放
     */
    public boolean isOnlinePlayback() {
        return onlinePlaylist != null;
    }
    
    /**
     * 检查是否需要新的流
     */
    public boolean needNewStream() {
        return onlinePlaylist != null && streamClosed;
    }
    
    // Getters and Setters
    public Song getCurrentSong() { return currentSong; }
    public void setCurrentSong(Song currentSong) { this.currentSong = currentSong; }
    
    public Playlist getCurrentPlaylist() { return currentPlaylist; }
    public void setCurrentPlaylist(Playlist currentPlaylist) { this.currentPlaylist = currentPlaylist; }
    
    public PlayMode getPlayMode() { return playMode; }
    public boolean isPlaying() { return isPlaying; }
} 