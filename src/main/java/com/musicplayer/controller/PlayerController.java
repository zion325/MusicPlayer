package com.musicplayer.controller;

import com.musicplayer.model.Song;
import com.musicplayer.model.Playlist;
import com.musicplayer.audio.AudioPlayer;

public class PlayerController {
    private Song currentSong;
    private Playlist currentPlaylist;
    private PlayMode playMode = PlayMode.SEQUENCE;
    private boolean isPlaying = false;
    private final AudioPlayer audioPlayer;
    private AudioPlayer.ProgressListener progressListener;
    
    public PlayerController() {
        this.audioPlayer = new AudioPlayer();
    }
    
    public enum PlayMode {
        SEQUENCE,
        RANDOM,
        SINGLE_LOOP
    }
    
    public void play() {
        if (currentSong != null) {
            audioPlayer.play(currentSong.getFilePath());
            isPlaying = true;
        }
    }
    
    public void resume() {
        if (currentSong != null) {
            audioPlayer.resume();
            isPlaying = true;
        }
    }
    
    public void pause() {
        audioPlayer.pause();
        isPlaying = false;
    }
    
    public void stop() {
        audioPlayer.stop();
        isPlaying = false;
    }
    
    public void next() {
        if (currentPlaylist != null) {
            int currentIndex = currentPlaylist.getSongs().indexOf(currentSong);
            if (currentIndex >= 0) {
                int nextIndex;
                if (playMode == PlayMode.RANDOM) {
                    nextIndex = (int) (Math.random() * currentPlaylist.getSongs().size());
                } else {
                    nextIndex = (currentIndex + 1) % currentPlaylist.getSongs().size();
                }
                setCurrentSong(currentPlaylist.getSongs().get(nextIndex));
                play();
            }
        }
    }
    
    public void previous() {
        if (currentPlaylist != null) {
            int currentIndex = currentPlaylist.getSongs().indexOf(currentSong);
            if (currentIndex >= 0) {
                int prevIndex;
                if (playMode == PlayMode.RANDOM) {
                    prevIndex = (int) (Math.random() * currentPlaylist.getSongs().size());
                } else {
                    prevIndex = (currentIndex - 1 + currentPlaylist.getSongs().size()) % currentPlaylist.getSongs().size();
                }
                setCurrentSong(currentPlaylist.getSongs().get(prevIndex));
                play();
            }
        }
    }
    
    public void setPlayMode(PlayMode mode) {
        this.playMode = mode;
    }
    
    public void setProgressListener(AudioPlayer.ProgressListener listener) {
        this.progressListener = listener;
        audioPlayer.setProgressListener(listener);
    }
    
    // Getters and Setters
    public Song getCurrentSong() { return currentSong; }
    public void setCurrentSong(Song currentSong) { 
        this.currentSong = currentSong; 
    }
    
    public Playlist getCurrentPlaylist() { return currentPlaylist; }
    public void setCurrentPlaylist(Playlist currentPlaylist) { 
        this.currentPlaylist = currentPlaylist; 
    }
    
    public PlayMode getPlayMode() { return playMode; }
    public boolean isPlaying() { return isPlaying; }
} 