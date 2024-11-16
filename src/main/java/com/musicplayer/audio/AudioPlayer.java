package com.musicplayer.audio;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.io.*;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.util.Map;
import javax.swing.SwingUtilities;

public class AudioPlayer {
    private AdvancedPlayer player;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private boolean isPlaying = false;
    private Thread playerThread;
    private String currentFilePath;
    private PlaybackListener playbackListener;
    private int totalFrames;
    private int currentFrame;
    private long totalDurationInSeconds;
    private ProgressListener progressListener;
    private Timer progressTimer;
    private long totalBytes;
    private long bytesRead;
    private FileInputStream savedFileInputStream;
    private long savedPosition;
    
    public interface ProgressListener {
        void onProgress(int current, int total);
    }
    
    public AudioPlayer() {
        this.playbackListener = new PlaybackListener() {
            @Override
            public void playbackStarted(PlaybackEvent evt) {
                currentFrame = evt.getFrame();
                startProgressTimer();
            }
            
            @Override
            public void playbackFinished(PlaybackEvent evt) {
                currentFrame = evt.getFrame();
                isPlaying = false;
                stopProgressTimer();
                if (progressListener != null) {
                    progressListener.onProgress(0, 100);
                }
            }
        };
    }
    
    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }
    
    private void startProgressTimer() {
        if (progressTimer != null) {
            progressTimer.stop();
        }
        
        progressTimer = new Timer(100, e -> {
            if (isPlaying && progressListener != null && totalBytes > 0) {
                try {
                    // 更新已读取的字节数
                    bytesRead = totalBytes - fileInputStream.available();
                    int progress = (int) ((bytesRead * 100.0) / totalBytes);
                    progress = Math.min(progress, 100);
                    progressListener.onProgress(progress, 100);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        progressTimer.start();
    }
    
    private void stopProgressTimer() {
        if (progressTimer != null) {
            progressTimer.stop();
            progressTimer = null;
        }
    }
    
    public void play(String filePath) {
        // 如果是新的歌曲，从头开始播放
        if (!filePath.equals(currentFilePath)) {
            stop(); // 停止当前播放
            currentFilePath = filePath;
            savedPosition = 0;
            startPlayback(0);
        } else {
            // 如果是同一首歌，从头开始播放
            savedPosition = 0;
            startPlayback(0);
        }
    }
    
    public void pause() {
        if (player != null && isPlaying) {
            try {
                // 保存当前位置
                savedPosition = bytesRead;
                stopProgressTimer();
                player.close();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                isPlaying = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void resume() {
        if (currentFilePath != null && !isPlaying) {
            startPlayback(savedPosition);
        }
    }
    
    private void startPlayback(long startPosition) {
        try {
            // 获取文件总字节数
            File file = new File(currentFilePath);
            totalBytes = file.length();
            
            playerThread = new Thread(() -> {
                try {
                    fileInputStream = new FileInputStream(currentFilePath);
                    // 跳转到保存的位置
                    if (startPosition > 0) {
                        fileInputStream.skip(startPosition);
                        bytesRead = startPosition;
                    } else {
                        bytesRead = 0;
                    }
                    
                    bufferedInputStream = new BufferedInputStream(fileInputStream);
                    player = new AdvancedPlayer(bufferedInputStream);
                    player.setPlayBackListener(playbackListener);
                    
                    isPlaying = true;
                    startProgressTimer();
                    player.play();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isPlaying = false;
                    stopProgressTimer();
                }
            });
            
            playerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private int getCurrentFramePosition() {
        // 这里我们返回记录的当前帧位置
        return currentFrame;
    }
    
    public void stop() {
        if (player != null) {
            try {
                player.close();
                if (fileInputStream != null) fileInputStream.close();
                if (bufferedInputStream != null) bufferedInputStream.close();
                isPlaying = false;
                currentFrame = 0; // 重置播放位置
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    @Override
    protected void finalize() throws Throwable {
        stopProgressTimer();
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.finalize();
    }
} 