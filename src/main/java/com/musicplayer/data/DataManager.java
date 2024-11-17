package com.musicplayer.data;

import com.musicplayer.model.Playlist;
import com.musicplayer.model.Song;
import com.musicplayer.util.MusicFileManager;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据管理类，负责歌单数据的加载、保存和管理
 * 使用单例模式确保全局只有一个数据管理实例
 */
public class DataManager {
    /** 单例实例 */
    private static DataManager instance;
    
    /** 存储所有歌单的Map，key为歌单名称 */
    private final Map<String, Playlist> playlists;
    
    /** 数据文件路径 */
    private final String DATA_FILE = "playlists.dat";
    
    /** 存储收藏歌曲的Map，key为歌曲ID */
    private final Map<String, Song> favoriteSongs = new HashMap<>();
    
    /** 收藏歌曲数据文件路径 */
    private final String FAVORITE_FILE = "favorites.dat";
    
    /**
     * 私有构造函数，初始化数据
     */
    private DataManager() {
        playlists = new HashMap<>();
        loadData();
        loadFavorites(); // 加载收藏数据
    }
    
    /**
     * 获取DataManager的单例实例
     * @return DataManager实例
     */
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    /**
     * 根据歌单名称获取歌单
     * @param playlistName 歌单名称
     * @return 歌单对象，如果不存在返回null
     */
    public Playlist getPlaylist(String playlistName) {
        return playlists.get(playlistName);
    }
    
    /**
     * 获取所有歌单列表
     * @return 歌单列表
     */
    public List<Playlist> getAllPlaylists() {
        return new ArrayList<>(playlists.values());
    }
    
    /**
     * 添加新歌单
     * @param playlist 要添加的歌单
     */
    public void addPlaylist(Playlist playlist) {
        playlists.put(playlist.getName(), playlist);
        saveData();
    }
    
    /**
     * 删除歌单
     * @param playlistName 要删除的歌单名称
     */
    public void removePlaylist(String playlistName) {
        playlists.remove(playlistName);
        saveData();
    }
    
    /**
     * 更新歌单信息
     * @param playlist 要更新的歌单
     */
    public void updatePlaylist(Playlist playlist) {
        playlists.put(playlist.getName(), playlist);
        saveData();
    }
    
    /**
     * 从文件加载歌单数据
     */
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Map<String, Playlist> loadedPlaylists = (Map<String, Playlist>) ois.readObject();
            playlists.putAll(loadedPlaylists);
        } catch (FileNotFoundException e) {
            // 文件不存在时创建示例数据
            createSampleData();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            createSampleData();
        }
    }
    
    /**
     * 保存歌单数据到文件
     */
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(playlists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 创建示例数据
     */
    private void createSampleData() {
        try {
            // 创建我的歌单
            Playlist myPlaylist = new Playlist("1", "我喜欢的音乐", "学号10001");
            myPlaylist.setCreateDate(LocalDateTime.now());
            playlists.put(myPlaylist.getName(), myPlaylist);

            // 创建我的收藏歌单
            Playlist favoritePlaylist = new Playlist("0", "我的收藏", "学号10001");
            favoritePlaylist.setCreateDate(LocalDateTime.now());
            playlists.put(favoritePlaylist.getName(), favoritePlaylist);

            // 创建网友歌单
            createFriendPlaylist("2", "杂七杂八的歌单", "学号10002", 5, "list1");
            createFriendPlaylist("3", "深情emo的歌单", "学号10003", 3, "list2");
            createFriendPlaylist("4", "66666的歌单", "学号10004", 1, "list3");

            // 保存数据
            saveData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 更新我的收藏歌单
     */
    private void updateFavoritePlaylist() {
        Playlist favoritePlaylist = playlists.get("我的收藏");
        if (favoritePlaylist == null) {
            favoritePlaylist = new Playlist("0", "我的收藏", "学号10001");
            favoritePlaylist.setCreateDate(LocalDateTime.now());
            playlists.put(favoritePlaylist.getName(), favoritePlaylist);
        }
        
        // 清空并重新添加所有收藏的歌曲
        favoritePlaylist.getSongs().clear();
        favoritePlaylist.getSongs().addAll(favoriteSongs.values());
        
        // 保存更新后的歌单数据
        saveData();
    }
    
    /**
     * 创建网友歌单
     * @param id 歌单ID
     * @param name 歌单名称
     * @param ownerId 创建者ID
     * @param daysAgo 创建时间（几天前）
     * @param listDir 歌曲目录名
     */
    private void createFriendPlaylist(String id, String name, String ownerId, int daysAgo, String listDir) {
        try {
            // 创建歌单对象并设置创建时间
            Playlist playlist = new Playlist(id, name, ownerId);
            playlist.setCreateDate(LocalDateTime.now().minusDays(daysAgo));

            // 读取指定目录下的所有MP3文件
            String sampleSongsPath = System.getProperty("user.dir") + File.separator + "sample_songs" + File.separator + listDir;
            File directory = new File(sampleSongsPath);
            
            if (directory.exists() && directory.isDirectory()) {
                System.out.println("正在加载目录: " + sampleSongsPath);
                
                // 获取目录下所有MP3文件
                File[] mp3Files = directory.listFiles((dir, fileName) -> 
                    fileName.toLowerCase().endsWith(".mp3"));
                
                if (mp3Files != null && mp3Files.length > 0) {
                    for (File sourceFile : mp3Files) {
                        try {
                            System.out.println("处理文件: " + sourceFile.getName());
                            
                            // 从文件名解析歌曲信息（格式：歌名 - 歌手.mp3）
                            String fileName = sourceFile.getName();
                            String[] parts = fileName.substring(0, fileName.length() - 4).split(" - ");
                            String title = parts[0];
                            String artist = parts.length > 1 ? parts[1] : "未知歌手";
                            
                            // 复制文件到music目录并获取新路径
                            String targetPath = MusicFileManager.saveMusicFile(sourceFile);
                            System.out.println("文件已复制到: " + targetPath);
                            
                            // 获取音频时长
                            Duration duration = MusicFileManager.getMp3Duration(targetPath);
                            
                            // 创建歌曲对象并添加到歌单
                            Song song = new Song(
                                String.valueOf(System.currentTimeMillis()),
                                title,
                                artist,
                                duration,
                                targetPath
                            );
                            
                            playlist.addSong(song);
                            System.out.println("已添加歌曲: " + title + " - " + artist);
                        } catch (Exception e) {
                            System.err.println("处理文件失败: " + sourceFile.getName());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("目录为空或不包含MP3文件: " + sampleSongsPath);
                }
            } else {
                System.err.println("目录不存在: " + sampleSongsPath);
            }

            // 将歌单添加到集合中
            playlists.put(playlist.getName(), playlist);
            System.out.println("已创建歌单: " + name + "，包含 " + playlist.getSongs().size() + " 首歌");
            
        } catch (Exception e) {
            System.err.println("创建歌单失败: " + name);
            e.printStackTrace();
        }
    }
    
    /**
     * 收藏歌曲
     * @param song 要收藏的歌曲
     * @return 是否收藏成功
     */
    public boolean addFavoriteSong(Song song) {
        if (!favoriteSongs.containsKey(song.getId())) {
            favoriteSongs.put(song.getId(), song);
            saveFavorites();
            updateFavoritePlaylist(); // 更新我的收藏歌单
            return true;
        }
        return false;
    }
    
    /**
     * 取消收藏歌曲
     * @param songId 歌曲ID
     */
    public void removeFavoriteSong(String songId) {
        favoriteSongs.remove(songId);
        saveFavorites();
        updateFavoritePlaylist(); // 更新我的收藏歌单
    }
    
    /**
     * 检查歌曲是否已收藏
     * @param songId 歌曲ID
     * @return 是否已收藏
     */
    public boolean isSongFavorited(String songId) {
        return favoriteSongs.containsKey(songId);
    }
    
    /**
     * 获取所有收藏的歌曲
     * @return 收藏的歌曲列表
     */
    public List<Song> getFavoriteSongs() {
        return new ArrayList<>(favoriteSongs.values());
    }
    
    /**
     * 保存收藏歌曲数据
     */
    private void saveFavorites() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FAVORITE_FILE))) {
            oos.writeObject(favoriteSongs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 加载收藏歌曲数据
     */
    private void loadFavorites() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FAVORITE_FILE))) {
            Map<String, Song> loaded = (Map<String, Song>) ois.readObject();
            favoriteSongs.putAll(loaded);
        } catch (FileNotFoundException e) {
            // 文件不存在时忽略
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
} 