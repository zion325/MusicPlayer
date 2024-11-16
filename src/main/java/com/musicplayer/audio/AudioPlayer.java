package com.musicplayer.audio;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * 音频播放器类，负责MP3文件的播放控制
 */
public class AudioPlayer {
    /** 播放器实例 */
    private AdvancedPlayer player;
    
    /** 文件输入流 */
    private FileInputStream fileInputStream;
    
    /** 缓冲输入流 */
    private BufferedInputStream bufferedInputStream;
    
    /** 是否正在播放 */
    private boolean isPlaying = false;
    
    /** 播放线程 */
    private Thread playerThread;
    
    /** 当前播放文件路径 */
    private String currentFilePath;
    
    /** 播放事件监听器 */
    private PlaybackListener playbackListener;
    
    /** 进度更新定时器 */
    private Timer progressTimer;
    
    /** 文件总字节数 */
    private long totalBytes;
    
    /** 已读取字节数 */
    private long bytesRead;
    
    /** 保存的播放位置 */
    private long savedPosition;
    
    /**
     * 进度监听器接口
     */
    public interface ProgressListener {
        /**
         * 进度更新回调
         * @param current 当前进度值
         * @param total 总进度值
         */
        void onProgress(int current, int total);
    }
    
    /** 进度监听器实例 */
    private ProgressListener progressListener;
    
    /**
     * 构造函数，初始化播放监听器
     */
    public AudioPlayer() {
        this.playbackListener = new PlaybackListener() {
            @Override
            public void playbackStarted(PlaybackEvent evt) {
                startProgressTimer();
            }
            
            @Override
            public void playbackFinished(PlaybackEvent evt) {
                isPlaying = false;
                stopProgressTimer();
                if (progressListener != null) {
                    progressListener.onProgress(0, 100);
                }
            }
        };
    }
    
    /**
     * 设置进度监听器
     * @param listener 进度监听器
     */
    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }
    
    /**
     * 开始进度更新定时器
     */
    private void startProgressTimer() {
        if (progressTimer != null) {
            progressTimer.stop();
        }
        
        progressTimer = new Timer(100, e -> {
            if (isPlaying && progressListener != null && totalBytes > 0) {
                try {
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
    
    /**
     * 停止进度更新定时器
     */
    private void stopProgressTimer() {
        if (progressTimer != null) {
            progressTimer.stop();
            progressTimer = null;
        }
    }
    
    /**
     * 播放指定的音频文件
     * @param filePath 音频文件路径
     */
    public void play(String filePath) {
        if (!filePath.equals(currentFilePath)) {
            stop();
            currentFilePath = filePath;
            savedPosition = 0;
            startPlayback(0);
        } else {
            savedPosition = 0;
            startPlayback(0);
        }
    }
    
    /**
     * 暂停播放
     */
    public void pause() {
        if (player != null && isPlaying) {
            try {
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
    
    /**
     * 从暂停处继续播放
     */
    public void resume() {
        if (currentFilePath != null && !isPlaying) {
            startPlayback(savedPosition);
        }
    }
    
    /**
     * 开始播放
     * @param startPosition 开始位置（字节数）
     */
    private void startPlayback(long startPosition) {
        try {
            File file = new File(currentFilePath);
            totalBytes = file.length();
            
            playerThread = new Thread(() -> {
                try {
                    fileInputStream = new FileInputStream(currentFilePath);
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
    
    /**
     * 停止播放
     */
    public void stop() {
        if (player != null) {
            try {
                player.close();
                if (fileInputStream != null) fileInputStream.close();
                if (bufferedInputStream != null) bufferedInputStream.close();
                isPlaying = false;
                savedPosition = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (playerThread != null && playerThread.isAlive()) {
            playerThread.interrupt();
        }
    }
    
    /**
     * 获取播放状态
     * @return 是否正在播放
     */
    public boolean isPlaying() {
        return isPlaying;
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