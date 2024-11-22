package com.musicplayer.audio;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import javax.swing.Timer;
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
    
    /** 播放开始时间 */
    private long startTime;
    
    /** 播放暂停时的时间点 */
    private long pausedTime;
    
    /** 缓存的音频数据 */
    private byte[] audioData;
    
    /** 当前播放位置 */
    private long currentPosition;
    
    /** 是否是在线播放 */
    private boolean isOnlinePlayback;
    
    /** 播放进度（百分比）*/
    private int currentProgress = 0;
    
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
        this.startTime = 0;
        this.pausedTime = 0;
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
            if (isPlaying && progressListener != null) {
                try {
                    if (fileInputStream != null && totalBytes > 0) {
                        // 本地文件播放进度
                        bytesRead = totalBytes - fileInputStream.available();
                        currentProgress = (int) ((bytesRead * 100.0) / totalBytes);
                        currentProgress = Math.min(currentProgress, 100);
                        progressListener.onProgress(currentProgress, 100);
                    } else if (isOnlinePlayback && audioData != null) {
                        // 在线播放进度 - 使用实际数据大小计算
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - startTime;
                        
                        // 使用音频数据大小和播放时间计算进度
                        long totalDuration = 180000; // 3分钟 = 180000毫秒
                        currentProgress = (int) ((elapsedTime * 100.0) / totalDuration);
                        currentProgress = Math.min(currentProgress, 100);
                        
                        // 如果播放完成，重置进度
                        if (currentProgress >= 100) {
                            isPlaying = false;
                            stopProgressTimer();
                        }
                        
                        progressListener.onProgress(currentProgress, 100);
                    }
                } catch (IOException ex) {
                    // 忽略Stream Closed异常
                    if (!(ex.getMessage() != null && ex.getMessage().contains("Stream Closed"))) {
                        ex.printStackTrace();
                        stopProgressTimer();
                    }
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
     * 播放音乐流（在线音乐）
     */
    public void playStream(InputStream inputStream) throws Exception {
        stop();
        isOnlinePlayback = true;
        
        // 重置所有计数器
        totalBytes = 0;
        bytesRead = 0;
        currentPosition = 0;
        startTime = System.currentTimeMillis();
        pausedTime = 0;
        currentProgress = 0;
        
        // 读取整个流到内存
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        
        // 保存音频数据
        audioData = baos.toByteArray();
        totalBytes = audioData.length;
        
        // 创建新的播放流
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        bufferedInputStream = new BufferedInputStream(bais);
        player = new AdvancedPlayer(bufferedInputStream);
        
        if (playbackListener != null) {
            player.setPlayBackListener(playbackListener);
        }
        
        isPlaying = true;
        startProgressTimer();
        
        playerThread = new Thread(() -> {
            try {
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isPlaying = false;
                stopProgressTimer();
            }
        });
        playerThread.start();
    }
    
    /**
     * 播放本地音乐文件
     */
    public void play(String filePath) {
        if (!filePath.equals(currentFilePath)) {
            stop();
            currentFilePath = filePath;
            isOnlinePlayback = false;
            startPlayback(0);
        } else {
            startPlayback(0);
        }
    }
    
    /**
     * 开始播放
     */
    private void startPlayback(long startPosition) {
        try {
            if (isOnlinePlayback) {
                // 在线音乐播放
                if (audioData != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                    if (startPosition > 0) {
                        bais.skip(startPosition);
                    }
                    bufferedInputStream = new BufferedInputStream(bais);
                }
            } else {
                // 本地音乐播放
                File file = new File(currentFilePath);
                totalBytes = file.length();
                fileInputStream = new FileInputStream(currentFilePath);
                if (startPosition > 0) {
                    fileInputStream.skip(startPosition);
                }
                bufferedInputStream = new BufferedInputStream(fileInputStream);
            }
            
            if (bufferedInputStream != null) {
                player = new AdvancedPlayer(bufferedInputStream);
                player.setPlayBackListener(playbackListener);
                isPlaying = true;
                startProgressTimer();
                
                playerThread = new Thread(() -> {
                    try {
                        player.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        isPlaying = false;
                        stopProgressTimer();
                    }
                });
                playerThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 暂停播放
     */
    public void pause() {
        if (player != null && isPlaying) {
            try {
                // 保存当前位置和进度
                if (isOnlinePlayback) {
                    pausedTime = System.currentTimeMillis() - startTime;
                    currentProgress = Math.min((int)((pausedTime * 100.0) / (3 * 60 * 1000)), 100);
                } else if (fileInputStream != null) {
                    currentPosition = totalBytes - fileInputStream.available();
                }
                
                // 停止播放
                player.close();
                isPlaying = false;
                stopProgressTimer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 继续播放
     */
    public void resume() {
        if (!isPlaying) {
            if (isOnlinePlayback && audioData != null) {
                // 从暂停位置继续播放
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                try {
                    // 跳过已播放的部分
                    long skipBytes = (pausedTime * totalBytes) / (3 * 60 * 1000);
                    bais.skip(skipBytes);
                    
                    bufferedInputStream = new BufferedInputStream(bais);
                    player = new AdvancedPlayer(bufferedInputStream);
                    
                    if (playbackListener != null) {
                        player.setPlayBackListener(playbackListener);
                    }
                    
                    startTime = System.currentTimeMillis() - pausedTime;
                    isPlaying = true;
                    startProgressTimer();
                    
                    playerThread = new Thread(() -> {
                        try {
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
            } else {
                // 本地文件播放
                startPlayback(currentPosition);
            }
        }
    }
    
    /**
     * 停止播放
     */
    public void stop() {
        stopProgressTimer();
        
        if (player != null) {
            try {
                player.close();
                player = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 清理资源
        try {
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
                bufferedInputStream = null;
            }
            if (fileInputStream != null) {
                fileInputStream.close();
                fileInputStream = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        isPlaying = false;
        currentPosition = 0;
        totalBytes = 0;
        bytesRead = 0;
        audioData = null;
        currentFilePath = null;
        currentProgress = 0;  // 重置进度
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