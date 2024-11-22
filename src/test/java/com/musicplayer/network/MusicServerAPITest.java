package com.musicplayer.network;

import com.musicplayer.model.OnlineMusicSheet;
import java.util.List;

public class MusicServerAPITest {
    public static void main(String[] args) {
        MusicServerAPI api = new MusicServerAPI();
        
        try {
            System.out.println("开始测试获取在线歌单...");
            
            // 测试获取top20歌单
            List<OnlineMusicSheet> sheets = api.queryMusicSheets("top1");
            
            // 打印结果
            System.out.println("成功获取歌单数量: " + sheets.size());
            
            // 打印每个歌单的详细信息
            for (OnlineMusicSheet sheet : sheets) {
                System.out.println("\n歌单信息:");
                System.out.println("ID: " + sheet.getId());
                System.out.println("名称: " + sheet.getName());
                System.out.println("创建者: " + sheet.getCreator());
                System.out.println("创建时间: " + sheet.getDateCreated());
                System.out.println("UUID: " + sheet.getUuid());
                System.out.println("歌曲数量: " + sheet.getMusicItems().size());
                
                // 打印歌单中的歌曲
                System.out.println("歌曲列表:");
                sheet.getMusicItems().forEach((md5, filename) -> 
                    System.out.println("- " + filename + " (MD5: " + md5 + ")"));
            }
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 