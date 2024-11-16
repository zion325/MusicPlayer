package com.musicplayer.data;

import com.musicplayer.model.Playlist;
import com.musicplayer.model.Song;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static DataManager instance;
    private final Map<String, Playlist> playlists;
    private final String DATA_FILE = "playlists.dat";
    
    private DataManager() {
        playlists = new HashMap<>();
        loadData();
    }
    
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    public Playlist getPlaylist(String playlistName) {
        return playlists.get(playlistName);
    }
    
    public List<Playlist> getAllPlaylists() {
        return new ArrayList<>(playlists.values());
    }
    
    public void addPlaylist(Playlist playlist) {
        playlists.put(playlist.getName(), playlist);
        saveData();
    }
    
    public void removePlaylist(String playlistName) {
        playlists.remove(playlistName);
        saveData();
    }
    
    public void updatePlaylist(Playlist playlist) {
        playlists.put(playlist.getName(), playlist);
        saveData();
    }
    
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
    
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(playlists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createSampleData() {
        // 创建示例歌单
        Playlist playlist1 = new Playlist("1", "我喜欢的音乐", "user123");
        playlist1.setCreateDate(LocalDateTime.now());
        playlist1.addSong(new Song("1", "示例歌曲1", "歌手1", Duration.ofSeconds(210), "songs/song1.mp3"));
        playlist1.addSong(new Song("2", "示例歌曲2", "歌手2", Duration.ofSeconds(255), "songs/song2.mp3"));
        
        Playlist playlist2 = new Playlist("2", "网友推荐", "user456");
        playlist2.setCreateDate(LocalDateTime.now());
        playlist2.addSong(new Song("3", "示例歌曲3", "歌手3", Duration.ofSeconds(180), "songs/song3.mp3"));
        
        playlists.put(playlist1.getName(), playlist1);
        playlists.put(playlist2.getName(), playlist2);
        
        saveData();
    }
} 