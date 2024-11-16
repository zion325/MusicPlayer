package com.musicplayer.controller;

import com.musicplayer.model.Song;
import com.musicplayer.model.Playlist;
import com.musicplayer.audio.AudioPlayer;
import com.musicplayer.util.MusicFileManager;

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
            audioPlayer.resume();
            isPlaying = true;
        }
    }
    
    /**
     * 暂停播放
     */
    public void pause() {
        audioPlayer.pause();
        isPlaying = false;
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
     * 根据当前播放模式决定下一首歌曲
     */
    public void next() {
        if (currentPlaylist != null && currentPlaylist.getSongs().size() > 0) {
            int currentIndex = currentPlaylist.getSongs().indexOf(currentSong);
            if (currentIndex >= 0) {
                audioPlayer.stop();
                
                int nextIndex;
                switch (playMode) {
                    case RANDOM:
                        // 随机模式：随机选择一首（避免重复）
                        do {
                            nextIndex = (int) (Math.random() * currentPlaylist.getSongs().size());
                        } while (nextIndex == currentIndex && currentPlaylist.getSongs().size() > 1);
                        break;
                    case SINGLE_LOOP:
                        // 单曲循环：继续播放当前歌曲
                        nextIndex = currentIndex;
                        break;
                    case SEQUENCE:
                    default:
                        // 顺序播放：播放下一首，到末尾时循环到开头
                        nextIndex = (currentIndex + 1) % currentPlaylist.getSongs().size();
                        break;
                }
                
                setCurrentSong(currentPlaylist.getSongs().get(nextIndex));
                play();
            }
        }
    }
    
    /**
     * 播放上一首歌曲
     * 根据当前播放模式决定上一首歌曲
     */
    public void previous() {
        if (currentPlaylist != null && currentPlaylist.getSongs().size() > 0) {
            int currentIndex = currentPlaylist.getSongs().indexOf(currentSong);
            if (currentIndex >= 0) {
                audioPlayer.stop();
                
                int prevIndex;
                switch (playMode) {
                    case RANDOM:
                        // 随机模式：随机选择一首（避免重复）
                        do {
                            prevIndex = (int) (Math.random() * currentPlaylist.getSongs().size());
                        } while (prevIndex == currentIndex && currentPlaylist.getSongs().size() > 1);
                        break;
                    case SINGLE_LOOP:
                        // 单曲循环：继续播放当前歌曲
                        prevIndex = currentIndex;
                        break;
                    case SEQUENCE:
                    default:
                        // 顺序播放：播放上一首，到开头时循环到末尾
                        prevIndex = (currentIndex - 1 + currentPlaylist.getSongs().size()) % currentPlaylist.getSongs().size();
                        break;
                }
                
                setCurrentSong(currentPlaylist.getSongs().get(prevIndex));
                play();
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
    
    // Getters and Setters
    public Song getCurrentSong() { return currentSong; }
    public void setCurrentSong(Song currentSong) { this.currentSong = currentSong; }
    
    public Playlist getCurrentPlaylist() { return currentPlaylist; }
    public void setCurrentPlaylist(Playlist currentPlaylist) { this.currentPlaylist = currentPlaylist; }
    
    public PlayMode getPlayMode() { return playMode; }
    public boolean isPlaying() { return isPlaying; }
} 