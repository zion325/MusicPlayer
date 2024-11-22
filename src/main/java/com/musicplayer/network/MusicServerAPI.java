package com.musicplayer.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musicplayer.model.OnlineMusicSheet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MusicServerAPI {
    private static final String BASE_URL = "http://119.167.221.14:38080/music.server";
    private final Gson gson = new Gson();

    /**
     * 获取在线歌单列表
     * @param type 查询类型(all/top20/top1)
     * @return 歌单列表
     */
    public List<OnlineMusicSheet> queryMusicSheets(String type) throws IOException {
        String urlStr = BASE_URL + "/queryMusicSheets?type=" + type;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            if (conn.getResponseCode() != 200) {
                throw new IOException("Failed to query music sheets: " + conn.getResponseMessage());
            }
            
            // 使用 ByteArrayOutputStream 来读取响应数据
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            InputStream inputStream = conn.getInputStream();
            
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            
            // 将响应数据转换为字符串
            String response = result.toString("UTF-8");
            Map<String, Object> resultMap = gson.fromJson(response, 
                new TypeToken<Map<String, Object>>(){}.getType());
            
            return gson.fromJson(
                gson.toJson(resultMap.get("musicSheetList")),
                new TypeToken<List<OnlineMusicSheet>>(){}.getType()
            );
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 下载音乐文件
     * @param md5 音乐文件MD5值
     * @return 音乐文件输入流
     */
    public InputStream downloadMusic(String md5) throws IOException {
        URL url = new URL(BASE_URL + "/downloadMusic?md5=" + md5);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new IOException("Failed to download music: " + conn.getResponseMessage());
        }
        
        return conn.getInputStream();
    }

    /**
     * 下载歌单封面
     * @param uuid 歌单UUID
     * @return 封面图片输入流
     */
    public InputStream downloadPicture(String uuid) throws IOException {
        URL url = new URL(BASE_URL + "/downloadPicture?uuid=" + uuid);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new IOException("Failed to download picture: " + conn.getResponseMessage());
        }
        
        return conn.getInputStream();
    }

    /**
     * 在线播放音乐
     * @param md5 音乐文件MD5值
     * @return 音乐文件输入流
     */
    public InputStream streamMusic(String md5) throws IOException {
        URL url = new URL(BASE_URL + "/music?md5=" + md5);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new IOException("Failed to stream music: " + conn.getResponseMessage());
        }
        
        return conn.getInputStream();
    }
} 