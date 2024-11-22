package com.musicplayer.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.musicplayer.controller.PlayerController;
import com.musicplayer.model.Playlist;
import com.musicplayer.model.Song;
import com.musicplayer.data.DataManager;
import com.musicplayer.util.MusicFileManager;
import com.musicplayer.model.OnlineMusicSheet;
import com.musicplayer.util.StreamCache;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.time.Duration;

import java.time.format.DateTimeFormatter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.table.TableCellRenderer;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;



import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;

import javax.imageio.ImageIO;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;

import java.util.ArrayList;

public class MainWindow extends JFrame {
    private final PlayerController playerController;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel playerPanel;
    private JButton playButton;
    private JSlider progressSlider;
    private JList<String> myPlaylistList;
    private JList<String> friendsPlaylistList;
    private JTable songTable;
    private JLabel playlistNameLabel;
    private JLabel playlistOwnerLabel;
    private JLabel playlistDateLabel;
    private JLabel coverImageLabel;
    private DefaultTableModel songTableModel;
    private final DataManager dataManager = DataManager.getInstance();
    private Song currentPlayingSong;
    private JLabel songTitleLabel;
    private JLabel songArtistLabel;
    private JLabel songProgressLabel;
    private JButton modeButton;
    private JPanel infoPanel;
    private JButton playAllButton;
    private JButton downloadAllButton;
    private JButton addToMyPlaylistButton;
    private JButton changeCoverButton;
    private Map<String, OnlineMusicSheet> onlineSheetMap = new HashMap<>();
    private JTextField searchField;
    private DefaultListModel<String> friendsListModel;
    
    public MainWindow() {
        this.playerController = new PlayerController();
        initializeComponents();
        initializeUI();
        
        // 添加窗口关闭事件处理
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // 清理缓存
                StreamCache.clearCache();
            }
        });
    }
    
    private void initializeComponents() {
        playAllButton = new JButton("播放全部");
        downloadAllButton = new JButton("下载全部");
        addToMyPlaylistButton = new JButton("加入我的歌单");
        changeCoverButton = new JButton("更换封面");
        
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        playAllButton.addActionListener(e -> playAllSongs());
        downloadAllButton.addActionListener(e -> downloadAllSongs());
        addToMyPlaylistButton.addActionListener(e -> addToMyPlaylist());
        changeCoverButton.addActionListener(e -> changeCover());
    }
    
    private void initializeUI() {
        setTitle("Music Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // 使用BorderLayout作为主布局
        setLayout(new BorderLayout());
        
        // 创建侧边栏面板
        sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);
        
        // 创建内容面板
        contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // 创建播放器控制面板
        playerPanel = createPlayerPanel();
        add(playerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        
        // 创建垂直分割的面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setDividerSize(1);
        
        // 创建网友歌单面板（上半部分）
        JPanel friendsPanel = new JPanel(new BorderLayout());
        
        // 创建标题和搜索面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        // 创建标题标签
        JLabel friendsLabel = new JLabel("网友歌单", SwingConstants.LEFT);
        friendsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));  // 调整边距
        
        // 创建搜索图标标签
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Dialog", Font.PLAIN, 12));
        searchIcon.setForeground(Color.WHITE);  // 设置为白色
        
        // 创建搜索文本框
        searchField = new JTextField();
        searchField.setToolTipText("搜索歌单");
        searchField.setPreferredSize(new Dimension(0, 20));  // 设置高度
        searchField.setForeground(Color.WHITE);  // 设置文字颜色为白色
        searchField.setCaretColor(Color.WHITE);  // 设置光标颜色为白色
        searchField.setBackground(new Color(60, 63, 65));  // 设置深色背景
        
        // 设置搜索框的边框和内边距
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 83, 85), 1),  // 稍微亮一点的边框
            BorderFactory.createEmptyBorder(1, 5, 1, 5)
        ));
        
        // 添加占位符文本
        searchField.setText("搜索歌单");
        searchField.setForeground(Color.GRAY);
        
        // 添加占位符监听器
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("搜索歌单")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);  // 修改为白色
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("搜索歌单");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        
        // 组装搜索面板
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // 组装标题和搜索面板
        headerPanel.add(friendsLabel, BorderLayout.NORTH);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // 初始化列表模型和列表
        friendsListModel = new DefaultListModel<>();
        friendsPlaylistList = new JList<>(friendsListModel);
        friendsPlaylistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 加载网友歌单
        loadFriendsPlaylists();
        
        // 添加搜索监听器
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterPlaylists(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterPlaylists(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterPlaylists(); }
        });
        
        // 添加网友歌单选择监听器
        friendsPlaylistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedPlaylist = friendsPlaylistList.getSelectedValue();
                if (selectedPlaylist != null) {
                    // 清除我的歌单的选择
                    myPlaylistList.clearSelection();
                    
                    if (selectedPlaylist.equals("未找到匹配的歌单")) {
                        // 如果是提示文本，不进行处理
                        return;
                    }
                    
                    if (selectedPlaylist.startsWith("[在线] ")) {
                        // 处理在线歌单
                        OnlineMusicSheet onlineSheet = onlineSheetMap.get(selectedPlaylist);
                        if (onlineSheet != null) {
                            displayOnlinePlaylist(onlineSheet);
                        }
                    } else {
                        // 处理本地网友歌单
                        Playlist playlist = dataManager.getPlaylist(selectedPlaylist);
                        if (playlist != null) {
                            displayPlaylist(playlist);
                        }
                    }
                }
            }
        });
        
        // 组装网友歌单面板
        friendsPanel.add(headerPanel, BorderLayout.NORTH);
        friendsPanel.add(new JScrollPane(friendsPlaylistList), BorderLayout.CENTER);
        
        // 创建我的歌单面板（下半部分）
        JPanel myPanel = new JPanel(new BorderLayout());
        JLabel myLabel = new JLabel("我的歌单", SwingConstants.LEFT);
        myLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        myLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        myPanel.add(myLabel, BorderLayout.NORTH);
        
        // 创建我的歌单列表和按钮面板
        JPanel myListPanel = createMyPlaylistPanel();
        myPanel.add(myListPanel, BorderLayout.CENTER);
        
        // 将两个面板添加到分割面板中
        splitPane.setTopComponent(friendsPanel);
        splitPane.setBottomComponent(myPanel);
        
        sidebar.add(splitPane, BorderLayout.CENTER);
        return sidebar;
    }
    
    private JPanel createMyPlaylistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建歌单列表
        DefaultListModel<String> listModel = new DefaultListModel<>();
        myPlaylistList = new JList<>(listModel);
        myPlaylistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 加载我的歌单
        for (Playlist playlist : dataManager.getAllPlaylists()) {
            if (playlist.getOwnerId().equals("学号10001")) {
                listModel.addElement(playlist.getName());
            }
        }
        
        // 添加选择监听器
        myPlaylistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedPlaylist = myPlaylistList.getSelectedValue();
                if (selectedPlaylist != null) {
                    updateContentPanel(selectedPlaylist);
                    friendsPlaylistList.clearSelection(); // 清除网友歌单的选择
                }
            }
        });
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("新建歌单");
        JButton deleteButton = new JButton("删除歌单");
        JButton importButton = new JButton("导入音乐");
        
        // 添加新建歌单按钮事件
        createButton.addActionListener(e -> {
            String playlistName = JOptionPane.showInputDialog(
                this,
                "请输入歌单名称：",
                "新建歌单",
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (playlistName != null && !playlistName.trim().isEmpty()) {
                Playlist newPlaylist = new Playlist(
                    String.valueOf(System.currentTimeMillis()),
                    playlistName,
                    "学号10001" // TODO: 替换为实际的用户ID
                );
                dataManager.addPlaylist(newPlaylist);
                listModel.addElement(playlistName);
            }
        });
        
        // 添加删除歌单按钮事件
        deleteButton.addActionListener(e -> {
            int selectedIndex = myPlaylistList.getSelectedIndex();
            if (selectedIndex != -1) {
                String playlistName = myPlaylistList.getSelectedValue();
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除选中的歌单吗",
                    "除歌单",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    dataManager.removePlaylist(playlistName);
                    listModel.remove(selectedIndex);
                }
            }
        });
        
        // 添加导入音乐按钮件
        importButton.addActionListener(e -> importMusic());
        
        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(importButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(myPlaylistList), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建歌单信息面板
        JPanel playlistInfoPanel = createPlaylistInfoPanel();
        
        // 创建歌曲列表面板
        JPanel songListPanel = createSongListPanel();
        
        panel.add(playlistInfoPanel, BorderLayout.NORTH);
        panel.add(songListPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPlaylistInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 200));
        
        // 创建左侧封面面板
        JPanel coverPanel = new JPanel(new BorderLayout());
        coverPanel.setPreferredSize(new Dimension(200, 200));
        coverPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建封面标签并设置默认图片
        coverImageLabel = new JLabel();
        coverImageLabel.setPreferredSize(new Dimension(180, 180));
        coverImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        coverImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        setDefaultCoverImage();
        
        coverPanel.add(coverImageLabel, BorderLayout.CENTER);
        
        // 创建右侧信息面板
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        
        // 创建标签
        playlistNameLabel = new JLabel("歌单名称");
        playlistNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        
        playlistOwnerLabel = new JLabel("创建者：");
        playlistOwnerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        playlistDateLabel = new JLabel("创建时间：");
        playlistDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 初始化按钮
        playAllButton = new JButton("播放全部");
        changeCoverButton = new JButton("更换封面");
        downloadAllButton = new JButton("下载全部");
        addToMyPlaylistButton = new JButton("加入我的歌单");
        
        // 添加按钮事件
        playAllButton.addActionListener(e -> playAllSongs());
        changeCoverButton.addActionListener(e -> changeCover());
        downloadAllButton.addActionListener(e -> downloadAllSongs());
        addToMyPlaylistButton.addActionListener(e -> addToMyPlaylist());
        
        // 默认只添加播放全部按钮
        buttonPanel.add(playAllButton);
        
        // 添加所有组件到信息面板
        infoPanel.add(playlistNameLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(playlistOwnerLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(playlistDateLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(buttonPanel);
        
        panel.add(coverPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSongListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建表格模型
        String[] columnNames = {"序号", "歌名", "歌手", "时长", "操作"};
        songTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // 只有操作列可编辑
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 4 ? JPanel.class : Object.class;
            }
        };
        
        songTable = new JTable(songTableModel);
        songTable.setRowHeight(35);
        
        // 设置表格列宽
        songTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        songTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        songTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        songTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        songTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        
        // 设置操作列的渲染器
        songTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonsRenderer());
        songTable.getColumnModel().getColumn(4).setCellEditor(new ButtonsEditor(songTable));
        
        JScrollPane scrollPane = new JScrollPane(songTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPlayerPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        panel.setLayout(new BorderLayout());
        
        // 创建歌曲信息面板
        JPanel songInfoPanel = new JPanel();
        songInfoPanel.setLayout(new BoxLayout(songInfoPanel, BoxLayout.Y_AXIS));
        songInfoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        songTitleLabel = new JLabel("未播放");
        songTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        songArtistLabel = new JLabel("");
        songArtistLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        songProgressLabel = new JLabel("00:00 / 00:00");
        songProgressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        songInfoPanel.add(songTitleLabel);
        songInfoPanel.add(Box.createVerticalStrut(2));
        songInfoPanel.add(songArtistLabel);
        songInfoPanel.add(Box.createVerticalStrut(2));
        songInfoPanel.add(songProgressLabel);
        
        // 创建进度条和进度信息面板
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setPreferredSize(new Dimension(400, 20));
        
        // 添进度监听器
        playerController.setProgressListener((current, total) -> {
            SwingUtilities.invokeLater(() -> {
                if (!progressSlider.getValueIsAdjusting()) {
                    progressSlider.setValue(current);
                    updateProgressLabel(current, total);
                }
            });
        });
        
        progressPanel.add(progressSlider, BorderLayout.CENTER);
        
        // 创建控制按钮面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevButton = new JButton("上一首");
        playButton = new JButton("继续");
        JButton nextButton = new JButton("下一首");
        modeButton = new JButton("顺序播放");
        
        // 设置按钮图标和提示文本
        modeButton.setToolTipText("点击切换播放模式");
        
        // 添加播放模式按钮事件
        modeButton.addActionListener(e -> {
            PlayerController.PlayMode currentMode = playerController.getPlayMode();
            PlayerController.PlayMode newMode;
            
            switch (currentMode) {
                case SEQUENCE:
                    newMode = PlayerController.PlayMode.RANDOM;
                    modeButton.setText("随机播放");
                    modeButton.setToolTipText("当前模式：随机播放");
                    break;
                case RANDOM:
                    newMode = PlayerController.PlayMode.SINGLE_LOOP;
                    modeButton.setText("单曲循环");
                    modeButton.setToolTipText("当前模式：单曲循环");
                    break;
                default:
                    newMode = PlayerController.PlayMode.SEQUENCE;
                    modeButton.setText("顺序播放");
                    modeButton.setToolTipText("当前模式：顺序播放");
                    break;
            }
            
            playerController.setPlayMode(newMode);
            updatePlayModeButton(newMode);
        });
        
        // 修改播放/暂停按钮事件处理
        playButton.addActionListener(e -> {
            if (currentPlayingSong != null) {
                if (playerController.isPlaying()) {
                    // 暂停播放
                    playerController.pause();
                    playButton.setText("继续");
                } else {
                    // 继续播放
                    if (playerController.isOnlinePlayback()) {
                        // 在线歌曲
                        if (playerController.needNewStream()) {
                            // 需要新流时重新获取
                            playOnlineSong(currentPlayingSong);
                        } else {
                            // 不需要新流时直接继续
                            playerController.resume();
                        }
                    } else {
                        // 本地歌曲
                        if (currentPlayingSong.getFilePath() != null) {
                            // 如果是暂停状态，继续播放
                            playerController.resume();
                        } else {
                            // 如果文件路径丢失，重新播放
                            playerController.play();
                        }
                    }
                    playButton.setText("暂停");
                }
            }
        });
        
        // 添上一首按钮事件
        prevButton.addActionListener(e -> {
            playerController.previous();
            updatePlayerInfo();
        });
        
        // 添加下一首按钮事件
        nextButton.addActionListener(e -> {
            playerController.next();
            updatePlayerInfo();
        });
        
        controlPanel.add(prevButton);
        controlPanel.add(playButton);
        controlPanel.add(nextButton);
        controlPanel.add(modeButton);
        
        // 使BorderLayout布局添加各个面板
        panel.add(songInfoPanel, BorderLayout.WEST);
        panel.add(progressPanel, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 加载网友歌单（包括本地和在线歌单）
     */
    private void loadFriendsPlaylists() {
        friendsListModel.clear();
        onlineSheetMap.clear();
        
        // 加载本地网友歌单
        for (Playlist playlist : dataManager.getAllPlaylists()) {
            if (!playlist.getOwnerId().equals("学号10001")) {
                friendsListModel.addElement(playlist.getName());
            }
        }
        
        // 加载在线歌单
        try {
            List<OnlineMusicSheet> onlineSheets = dataManager.getOnlineMusicSheets();
            for (OnlineMusicSheet sheet : onlineSheets) {
                String displayName = "[在线] " + sheet.getName();
                friendsListModel.addElement(displayName);
                onlineSheetMap.put(displayName, sheet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "加载在线歌单失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 更新歌单内容面板
     */
    private void updateContentPanel(String playlistName) {
        if (playlistName == null || playlistName.equals("未找到匹配的歌单")) {
            return;
        }
        
        if (playlistName.startsWith("[在线] ")) {
            // 处理在线歌单
            OnlineMusicSheet onlineSheet = onlineSheetMap.get(playlistName);
            if (onlineSheet != null) {
                displayOnlinePlaylist(onlineSheet);
            }
        } else {
            // 处理本地歌单
            Playlist playlist = dataManager.getPlaylist(playlistName);
            if (playlist != null) {
                displayPlaylist(playlist);
            }
        }
    }
    
    /**
     * 显示在线歌单内容
     */
    private void displayOnlinePlaylist(OnlineMusicSheet onlineSheet) {
        // 更新歌单信息
        playlistNameLabel.setText(onlineSheet.getName());
        playlistOwnerLabel.setText("创建者：" + onlineSheet.getCreator());
        playlistDateLabel.setText("创建时间：" + onlineSheet.getDateCreated());
        
        // 更新封面图片（如果有）
        if (onlineSheet.getPicture() != null && !onlineSheet.getPicture().isEmpty()) {
            try {
                // 下载并显示封面
                InputStream coverStream = dataManager.getMusicServerAPI().downloadPicture(onlineSheet.getUuid());
                BufferedImage coverImage = ImageIO.read(coverStream);
                if (coverImage != null) {
                    BufferedImage roundedImage = createRoundedImage(coverImage);
                    coverImageLabel.setIcon(new ImageIcon(roundedImage));
                }
            } catch (IOException e) {
                e.printStackTrace();
                setDefaultCoverImage();
            }
        } else {
            setDefaultCoverImage();
        }
        
        // 清空表格
        DefaultTableModel model = (DefaultTableModel) songTable.getModel();
        model.setRowCount(0);
        
        // 保存当前播放列表供按钮使用
        songTable.putClientProperty("currentPlaylist", onlineSheet);
        
        // 添加歌曲到表格
        int index = 0;
        for (Map.Entry<String, String> entry : onlineSheet.getMusicItems().entrySet()) {
            model.addRow(new Object[]{
                index + 1,                    // 序号
                entry.getValue(),             // 文件名（歌曲名）
                "在线音乐",                   // 艺术家
                "加载中...",                  // 时长（异步加载）
                entry.getKey(),               // MD5值（用于下载和播放）
                ""                            // 操作列（由渲染器处理）
            });
            
            // 异步加载时长
            final int rowIndex = index;
            new Thread(() -> {
                try {
                    // 获取文头信息来计算时长
                    InputStream is = dataManager.getMusicServerAPI().streamMusic(entry.getKey());
                    BufferedInputStream bis = new BufferedInputStream(is);
                    Duration duration = MusicFileManager.getMp3DurationFromStream(bis);
                    
                    // 更新表格
                    SwingUtilities.invokeLater(() -> {
                        model.setValueAt(formatDuration(duration), rowIndex, 3);
                    });
                    
                    bis.close();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        model.setValueAt("--:--", rowIndex, 3);
                    });
                }
            }).start();
            
            index++;
        }
        
        // 更新按钮显示
        updateButtonVisibilityForOnlinePlaylist();
    }
    
    /**
     * 新在线歌单的按钮显示
     */
    private void updateButtonVisibilityForOnlinePlaylist() {
        JPanel buttonPanel = (JPanel) infoPanel.getComponent(infoPanel.getComponentCount() - 1);
        buttonPanel.removeAll();
        
        // 显示在线歌单特有的按钮
        buttonPanel.add(playAllButton);
        buttonPanel.add(downloadAllButton);
        buttonPanel.add(addToMyPlaylistButton);
        
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }
    
    /**
     * 更新封面图片
     * @param coverPath 封面图片路径
     */
    private void updateCoverImage(String coverPath) {
        if (coverPath != null && !coverPath.isEmpty()) {
            try {
                File imageFile = new File(coverPath);
                if (imageFile.exists()) {
                    BufferedImage originalImage = ImageIO.read(imageFile);
                    if (originalImage != null) {
                        // 创建一个圆角的图片
                        BufferedImage roundedImage = createRoundedImage(originalImage);
                        coverImageLabel.setIcon(new ImageIcon(roundedImage));
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 如果加载失败或没有封面，显示默认封面
        setDefaultCoverImage();
    }
    
    /**
     * 格式化时长显示
     * @param duration 时长
     * @return 格式化后的字符串（MM:SS）
     */
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
    
    /**
     * 查找歌单
     * @param playlistName 歌单名称
     * @return 歌单对象
     */
    private Playlist findPlaylist(String playlistName) {
        return dataManager.getPlaylist(playlistName);
    }
    
    /**
     * 播放歌曲
     * @param song 要播放的歌曲
     */
    private void playSong(Song song) {
        try {
            if (currentPlayingSong != null && 
                currentPlayingSong.equals(song)) {
                if (playerController.isPlaying()) {
                    playerController.pause();
                    playButton.setText("继续");
                } else {
                    playerController.resume();
                    playButton.setText("暂停");
                }
            } else {
                currentPlayingSong = song;
                playerController.setCurrentSong(song);
                try {
                    playerController.play();
                    playButton.setText("暂停");
                    updateCurrentSongLabel();
                    progressSlider.setValue(0);
                } catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(this,
                        "播放失败：" + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                    currentPlayingSong = null;
                    updateCurrentSongLabel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "播放错：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 下载歌曲
     * @param song 要下的歌曲
     */
    private void downloadSong(Song song) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(song.getTitle() + ".mp3"));
        fileChooser.setDialogTitle("保存歌曲");
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            try {
                File sourceFile = new File(song.getFilePath());
                org.apache.commons.io.FileUtils.copyFile(sourceFile, targetFile);
                JOptionPane.showMessageDialog(this,
                    "下载完成：" + song.getTitle(),
                    "下载成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "下载失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 收藏/取消收藏歌曲
     * @param song 要收藏/取消收藏的歌曲
     */
    private void favoriteSong(Song song) {
        boolean isFavorited = dataManager.isSongFavorited(song.getId());
        
        if (isFavorited) {
            // 如果已收藏，则取消收藏
            dataManager.removeFavoriteSong(song.getId());
            JOptionPane.showMessageDialog(this,
                "已取消收藏：" + song.getTitle(),
                "取消收藏",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 如果未收藏，则添加到收藏
            if (dataManager.addFavoriteSong(song)) {
                JOptionPane.showMessageDialog(this,
                    "已收藏：" + song.getTitle(),
                    "收藏成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "该歌曲已在收藏列表中",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        
        // 更新表格中的收藏按钮状态
        updateSongList(((Playlist)songTable.getClientProperty("currentPlaylist")));
    }
    
    private void updatePlayerInfo() {
        Song currentSong = playerController.getCurrentSong();
        if (currentSong != null) {
            currentPlayingSong = currentSong;
            updateCurrentSongLabel();
            
            // 如果是在线播放且需要新流，重新获取流并播放
            if (playerController.isOnlinePlayback() && playerController.needNewStream()) {
                playOnlineSong(currentSong);
            }
        }
    }
    
    /**
     * 获当前选中的歌单
     * 从两个列表中获取当前选中的歌单名称
     * @return 当前选中的歌单名称，如果没有选中则返回null
     */
    private String getCurrentSelectedPlaylist() {
        // 先检查网友歌单是否有选中项
        String selectedFriendPlaylist = friendsPlaylistList.getSelectedValue();
        if (selectedFriendPlaylist != null) {
            return selectedFriendPlaylist;
        }
        
        // 再检查我的歌单是否有选中项
        return myPlaylistList.getSelectedValue();
    }
    
    /**
     * 播放全部歌曲
     */
    private void playAllSongs() {
        String selectedPlaylist = getCurrentSelectedPlaylist();
        if (selectedPlaylist == null) {
            JOptionPane.showMessageDialog(this,
                "请先选择一个歌单",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 停止当前播放，因为要开始新的播放
        if (playerController.isPlaying()) {
            playerController.stop();
        }
        
        if (selectedPlaylist.startsWith("[在线] ")) {
            // 播放在线歌单
            OnlineMusicSheet onlineSheet = onlineSheetMap.get(selectedPlaylist);
            if (onlineSheet == null || onlineSheet.getMusicItems().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "歌单为空",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 创建临时播放列表
            List<Song> onlineSongs = new ArrayList<>();
            for (Map.Entry<String, String> entry : onlineSheet.getMusicItems().entrySet()) {
                Song song = new Song(
                    entry.getKey(),  // 使用MD5作为ID
                    entry.getValue(), // 文件名作为标题
                    "在线音乐",      // 艺术家
                    Duration.ZERO,   // 临时设置为0
                    entry.getKey()   // 使用MD5作为文件路径标识
                );
                onlineSongs.add(song);
            }
            
            // 设置播放列表
            playerController.setOnlinePlaylist(onlineSongs);
            
            // 开始播放第一首歌
            if (!onlineSongs.isEmpty()) {
                playerController.setCurrentSong(onlineSongs.get(0));
                playOnlineSong(onlineSongs.get(0));
            }
        } else {
            // 播放本地歌单
            playerController.clearOnlinePlaylist(); // 清除在线播放列表
            Playlist playlist = dataManager.getPlaylist(selectedPlaylist);
            if (playlist == null || playlist.getSongs().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "歌单为空",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            playerController.setCurrentPlaylist(playlist);
            
            // 根据当前播放模式选择第一首歌
            if (playerController.getPlayMode() == PlayerController.PlayMode.RANDOM) {
                int randomIndex = (int) (Math.random() * playlist.getSongs().size());
                playerController.setCurrentSong(playlist.getSongs().get(randomIndex));
            } else {
                playerController.setCurrentSong(playlist.getSongs().get(0));
            }
            
            playerController.play();
            playButton.setText("暂停");
            currentPlayingSong = playerController.getCurrentSong();
            updateCurrentSongLabel();
        }
    }
    
    /**
     * 播放在线歌曲
     */
    private void playOnlineSong(Song song) {
        try {
            if (currentPlayingSong != null && 
                currentPlayingSong.equals(song) && 
                playerController.isOnlinePlayback() && 
                !playerController.needNewStream()) {
                // 如果是同一首歌且不需要新流，则继续播放
                playerController.resume();
                playButton.setText("暂停");
                return;
            }
            
            // 检查是否有缓存
            final File cachedFile = StreamCache.getCachedFile(song.getId());
            final File musicFile;
            
            if (cachedFile == null) {
                // 没有缓存，下载并缓存
                InputStream musicStream = dataManager.getMusicServerAPI().streamMusic(song.getId());
                musicFile = StreamCache.cacheStream(song.getId(), musicStream);
            } else {
                musicFile = cachedFile;
            }
            
            // 使用缓存的文件播放
            currentPlayingSong = song;
            // 设置一个默认时长（3分钟）
            song.setDuration(Duration.ofMinutes(3));
            playerController.setCurrentSong(song);
            playerController.playOnlineStream(new FileInputStream(musicFile));
            playButton.setText("暂停");
            updateCurrentSongLabel();
            
            // 异步获取实际时长
            new Thread(() -> {
                try {
                    AudioFile audioFile = AudioFileIO.read(musicFile);
                    int durationInSeconds = audioFile.getAudioHeader().getTrackLength();
                    song.setDuration(Duration.ofSeconds(durationInSeconds));
                    SwingUtilities.invokeLater(this::updateCurrentSongLabel);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 保持默认时长
                    SwingUtilities.invokeLater(this::updateCurrentSongLabel);
                }
            }).start();
            
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "播放失败：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void downloadAllSongs() {
        String selectedPlaylist = getCurrentSelectedPlaylist();
        if (selectedPlaylist == null) {
            JOptionPane.showMessageDialog(this,
                "请先选择一个歌单",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 选择下载目录
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setDialogTitle("选择下载目录");
        
        int result = dirChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File downloadDir = dirChooser.getSelectedFile();
            
            // 创建进度对话框
            JDialog progressDialog = new JDialog(this, "下载进度", true);
            JProgressBar progressBar = new JProgressBar();
            JLabel statusLabel = new JLabel("正在下载...");
            
            progressDialog.setLayout(new BorderLayout(10, 10));
            progressDialog.add(statusLabel, BorderLayout.NORTH);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);
            
            // 在后台线程中执行下载
            Thread downloadThread = new Thread(() -> {
                int successCount = 0;
                int totalCount = 0;  // 初始化为0
                
                if (selectedPlaylist.startsWith("[在线] ")) {
                    // 下载在线歌单
                    OnlineMusicSheet onlineSheet = onlineSheetMap.get(selectedPlaylist);
                    if (onlineSheet != null) {
                        Map<String, String> musicItems = onlineSheet.getMusicItems();
                        totalCount = musicItems.size();
                        progressBar.setMaximum(totalCount);
                        
                        for (Map.Entry<String, String> entry : musicItems.entrySet()) {
                            try {
                                final String filename = entry.getValue();
                                // 更新状态
                                SwingUtilities.invokeLater(() -> {
                                    statusLabel.setText("正在下载: " + filename);
                                    progressBar.setValue(progressBar.getValue() + 1);
                                });
                                
                                // 下载文件
                                InputStream is = dataManager.getMusicServerAPI().downloadMusic(entry.getKey());
                                File targetFile = new File(downloadDir, filename);
                                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                                    byte[] buffer = new byte[8192];
                                    int bytesRead;
                                    while ((bytesRead = is.read(buffer)) != -1) {
                                        fos.write(buffer, 0, bytesRead);
                                    }
                                }
                                successCount++;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                
                // 下载完成后显示结果
                final int finalSuccessCount = successCount;
                final int finalTotalCount = totalCount;
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(this,
                        String.format("下载完成\n成功: %d/%d", finalSuccessCount, finalTotalCount),
                        "下载结果",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            });
            
            downloadThread.start();
            progressDialog.setVisible(true); // 显示进度对话框
        }
    }
    
    /**
     * 将当前歌单添加到我的歌单
     */
    private void addToMyPlaylist() {
        // 获取当前选中的网友歌单
        String selectedPlaylist = friendsPlaylistList.getSelectedValue();
        if (selectedPlaylist == null) {
            JOptionPane.showMessageDialog(this,
                "请先选择一个网友歌单",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 取歌单对象
        Playlist sourcePlaylist = dataManager.getPlaylist(selectedPlaylist);
        if (sourcePlaylist == null || sourcePlaylist.getSongs().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "歌单为空",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 创建新歌单名称（在原歌单名称后添加"的副本"）
        String newPlaylistName = sourcePlaylist.getName() + "的副本";
        
        // 检查歌单名称是否已存在
        int index = 1;
        while (dataManager.getPlaylist(newPlaylistName) != null) {
            newPlaylistName = sourcePlaylist.getName() + "的副本(" + index + ")";
            index++;
        }
        
        // 创建新歌单
        Playlist newPlaylist = new Playlist(
            String.valueOf(System.currentTimeMillis()),
            newPlaylistName,
            "学号10001"
        );
        
        // 复制歌曲
        for (Song song : sourcePlaylist.getSongs()) {
            newPlaylist.addSong(song);
        }
        
        // 复制封面（如果有）
        if (sourcePlaylist.getCoverImagePath() != null) {
            try {
                File sourceFile = new File(sourcePlaylist.getCoverImagePath());
                if (sourceFile.exists()) {
                    String newCoverPath = MusicFileManager.saveCoverImage(sourceFile);
                    newPlaylist.setCoverImagePath(newCoverPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // 封面复制失败不影响整体功能
            }
        }
        
        // 添加到数管理器
        dataManager.addPlaylist(newPlaylist);
        
        // 更新我的歌单列表
        DefaultListModel<String> listModel = (DefaultListModel<String>) myPlaylistList.getModel();
        listModel.addElement(newPlaylistName);
        
        // 显示成提示
        JOptionPane.showMessageDialog(this,
            "已添加到我的歌单：" + newPlaylistName,
            "添加成功",
            JOptionPane.INFORMATION_MESSAGE);
        
        // 切换到我的歌单并选中添加的歌单
        myPlaylistList.setSelectedValue(newPlaylistName, true);
        updateContentPanel(newPlaylistName);
    }
    
    private void changeCover() {
        // 获取当前选中的歌单
        String selectedPlaylist = myPlaylistList.getSelectedValue();
        if (selectedPlaylist == null) {
            JOptionPane.showMessageDialog(this,
                "请先选择一个歌单",
                "提示",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "图片文件", "jpg", "jpeg", "png", "gif"));
            
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // 保存新的封面图片
                String newCoverPath = MusicFileManager.saveCoverImage(selectedFile);
                
                // 更歌单的封面路径
                Playlist playlist = dataManager.getPlaylist(selectedPlaylist);
                if (playlist != null) {
                    // 删除旧的封面图片
                    if (playlist.getCoverImagePath() != null) {
                        MusicFileManager.deleteCoverImage(playlist.getCoverImagePath());
                    }
                    
                    // 设置新的封面路径
                    playlist.setCoverImagePath(newCoverPath);
                    dataManager.updatePlaylist(playlist);
                    
                    // 更新界面显示
                    updateCoverImage(newCoverPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "封面更新失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 在MainWindow类中添加导入音乐的方法
    private void importMusic() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "MP3文件", "mp3"));
        fileChooser.setMultiSelectionEnabled(true);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                Song song = MusicFileManager.createSongFromFile(file);
                if (song != null) {
                    // 将歌曲添加到当前选中的歌单
                    String selectedPlaylist = myPlaylistList.getSelectedValue();
                    if (selectedPlaylist != null) {
                        Playlist playlist = dataManager.getPlaylist(selectedPlaylist);
                        if (playlist != null) {
                            playlist.addSong(song);
                            dataManager.updatePlaylist(playlist);
                            updateContentPanel(selectedPlaylist);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 更新歌曲列表显示
     * @param playlist 要显示的歌单
     */
    private void updateSongList(Playlist playlist) {
        // 清空现有的表格数据
        songTableModel.setRowCount(0);
        
        // 保存当前播放列表供按钮使用
        songTable.putClientProperty("currentPlaylist", playlist);
        
        // 加载歌曲数据
        List<Song> songs = playlist.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            Object[] row = new Object[5];
            row[0] = i + 1; // 序号
            row[1] = song.getTitle(); // 歌名
            row[2] = song.getArtist(); // 歌手
            row[3] = formatDuration(song.getDuration()); // 时长
            row[4] = ""; // 操作按钮由渲染器处理
            songTableModel.addRow(row);
        }
    }
    
    /**
     * 更新当前播放歌曲标签
     */
    private void updateCurrentSongLabel() {
        if (currentPlayingSong != null) {
            songTitleLabel.setText(currentPlayingSong.getTitle());
            songArtistLabel.setText(currentPlayingSong.getArtist());
            
            // 更新时长显示
            Duration duration = currentPlayingSong.getDuration();
            if (duration != null && !duration.isZero()) {
                long totalSeconds = duration.getSeconds();
                songProgressLabel.setText(String.format("00:00 / %02d:%02d",
                    totalSeconds / 60, totalSeconds % 60));
            } else if (playerController.isOnlinePlayback()) {
                songProgressLabel.setText("00:00 / 03:00");
            } else {
                songProgressLabel.setText("00:00 / 00:00");
            }
        } else {
            songTitleLabel.setText("未播放");
            songArtistLabel.setText("");
            songProgressLabel.setText("00:00 / 00:00");
        }
    }
    
    /**
     * 更新进度标签
     */
    private void updateProgressLabel(int current, int total) {
        if (currentPlayingSong != null) {
            Duration duration = currentPlayingSong.getDuration();
            if (duration != null && !duration.isZero()) {
                // 有时长信息时，显示具体时间
                long totalSeconds = duration.getSeconds();
                long currentSeconds = (totalSeconds * current) / total;
                songProgressLabel.setText(String.format("%02d:%02d / %02d:%02d",
                    currentSeconds / 60, currentSeconds % 60,
                    totalSeconds / 60, totalSeconds % 60));
            } else if (playerController.isOnlinePlayback()) {
                // 在线音乐播放时，使用3分钟作为默认时长
                long totalSeconds = 3 * 60; // 3分钟
                long currentSeconds = (totalSeconds * current) / total;
                songProgressLabel.setText(String.format("%02d:%02d / 03:00",
                    currentSeconds / 60, currentSeconds % 60));
            } else {
                // 其他情况显示百分比
                songProgressLabel.setText(String.format("%d%% / 100%%", current));
            }
        } else {
            songProgressLabel.setText("00:00 / 00:00");
        }
    }
    
    // 修改自定义渲染类
    private class ButtonsRenderer implements TableCellRenderer {
        private final JPanel panel;
        private final JButton playButton;
        private final JButton downloadButton;
        private final JButton favoriteButton;
        
        public ButtonsRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            
            playButton = new JButton("播放");
            downloadButton = new JButton("下载");
            favoriteButton = new JButton("收藏");
            
            Dimension buttonSize = new Dimension(60, 25);
            playButton.setPreferredSize(buttonSize);
            downloadButton.setPreferredSize(buttonSize);
            favoriteButton.setPreferredSize(buttonSize);
            
            panel.add(playButton);
            panel.add(downloadButton);
            panel.add(favoriteButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Object playlistObj = table.getClientProperty("currentPlaylist");
            if (playlistObj instanceof Playlist) {
                // 本地歌单
                Playlist playlist = (Playlist) playlistObj;
                if (playlist != null && row >= 0 && row < playlist.getSongs().size()) {
                    Song song = playlist.getSongs().get(row);
                    favoriteButton.setText(dataManager.isSongFavorited(song.getId()) ? "取消收藏" : "收藏");
                }
            } else if (playlistObj instanceof OnlineMusicSheet) {
                // 在线歌单 - 所有歌曲都显示"收藏"按钮
                favoriteButton.setText("收藏");
            }
            return panel;
        }
    }
    
    // 修改自定义编辑器类
    private class ButtonsEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton playButton;
        private final JButton downloadButton;
        private final JButton favoriteButton;
        
        public ButtonsEditor(JTable table) {
            super(new JCheckBox());
            
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            
            playButton = new JButton("播放");
            downloadButton = new JButton("下载");
            favoriteButton = new JButton("收藏");
            
            Dimension buttonSize = new Dimension(60, 25);
            playButton.setPreferredSize(buttonSize);
            downloadButton.setPreferredSize(buttonSize);
            favoriteButton.setPreferredSize(buttonSize);
            
            // 修改播放按钮事件处理
            playButton.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row != -1) {
                    Object playlistObj = table.getClientProperty("currentPlaylist");
                    if (playlistObj instanceof Playlist) {
                        // 本地歌单
                        Playlist playlist = (Playlist) playlistObj;
                        Song song = playlist.getSongs().get(row);
                        playerController.clearOnlinePlaylist();
                        playSong(song);
                    } else if (playlistObj instanceof OnlineMusicSheet) {
                        // 在线歌单
                        String md5 = (String) table.getValueAt(row, 4); // 获取MD5值
                        String filename = (String) table.getValueAt(row, 1); // 获取文件名
                        
                        Song song = new Song(
                            md5,        // 使用MD5作为ID
                            filename,   // 文件名作为标题
                            "在线音乐", // 艺术家
                            Duration.ZERO, // 临时设置为0
                            md5        // 使用MD5作为文件路径标识
                        );
                        playOnlineSong(song);
                    }
                }
                fireEditingStopped();
            });
            
            // 修改下载按钮事件处理
            downloadButton.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row != -1) {
                    Object playlistObj = table.getClientProperty("currentPlaylist");
                    if (playlistObj instanceof Playlist) {
                        // 本地歌单
                        Playlist playlist = (Playlist) playlistObj;
                        Song song = playlist.getSongs().get(row);
                        downloadSong(song);
                    } else if (playlistObj instanceof OnlineMusicSheet) {
                        // 在线歌单
                        String md5 = (String) table.getValueAt(row, 4);
                        String filename = (String) table.getValueAt(row, 1);
                        downloadOnlineSong(md5, filename);
                    }
                }
                fireEditingStopped();
            });
            
            // 修改收藏按钮事件处理
            favoriteButton.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row != -1) {
                    Object playlistObj = table.getClientProperty("currentPlaylist");
                    if (playlistObj instanceof Playlist) {
                        // 本歌单
                        Playlist playlist = (Playlist) playlistObj;
                        Song song = playlist.getSongs().get(row);
                        favoriteSong(song);
                    } else if (playlistObj instanceof OnlineMusicSheet) {
                        // 在线歌单 - 需要先下载再收藏
                        String md5 = (String) table.getValueAt(row, 4);
                        String filename = (String) table.getValueAt(row, 1);
                        downloadAndFavoriteOnlineSong(md5, filename);
                    }
                }
                fireEditingStopped();
            });
            
            panel.add(playButton);
            panel.add(downloadButton);
            panel.add(favoriteButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
    
    // 添加新的辅助方法
    private void downloadOnlineSong(String md5, String filename) {
        // 显示文件保存对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(filename));
        fileChooser.setDialogTitle("保存音乐文件");
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // 获取选择的保存路径
                File targetFile = fileChooser.getSelectedFile();
                
                // 下载文件
                InputStream is = dataManager.getMusicServerAPI().downloadMusic(md5);
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                
                JOptionPane.showMessageDialog(this,
                    "下载完成：" + filename,
                    "下载成功",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "下载失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void downloadAndFavoriteOnlineSong(String md5, String filename) {
        try {
            // 先下载歌曲
            dataManager.downloadOnlineMusic(md5, filename);
            
            // 创建Song对象
            String filePath = MusicFileManager.saveOnlineMusicFile(
                dataManager.getMusicServerAPI().downloadMusic(md5),
                filename
            );
            
            Song song = new Song(
                md5,
                filename,
                "在线音乐",
                Duration.ZERO,
                filePath
            );
            
            // 添加到收藏
            if (dataManager.addFavoriteSong(song)) {
                JOptionPane.showMessageDialog(this,
                    "已收藏：" + filename,
                    "收藏成功",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "收藏失败：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 添加设置默认封面的方法
    private void setDefaultCoverImage() {
        // 加默认封面图片（需要准备一个默认封面图片）
        ImageIcon defaultIcon = new ImageIcon(getClass().getResource("/images/default_cover.png"));
        if (defaultIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            Image scaledImage = defaultIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            coverImageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            // 如果默认图片加载失败，显示文字
            coverImageLabel.setIcon(null);
            coverImageLabel.setText("暂无封面");
        }
    }
    
    // 添加创建圆角图片的方
    private BufferedImage createRoundedImage(BufferedImage image) {
        int width = 180;
        int height = 180;
        int cornerRadius = 20; // 圆角半径
        
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        
        // 设置渲染质量
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // 创建圆角矩形
        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius);
        g2.setClip(roundedRectangle);
        
        // 绘制缩放后的图片
        g2.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        
        // 添加边框
        g2.setClip(null);
        g2.setColor(new Color(200, 200, 200, 100));
        g2.setStroke(new BasicStroke(1));
        g2.draw(roundedRectangle);
        
        g2.dispose();
        return output;
    }
    
    // 添加更新播放模式按钮的方法
    private void updatePlayModeButton(PlayerController.PlayMode mode) {
        switch (mode) {
            case SEQUENCE:
                modeButton.setText("顺序播放");
                modeButton.setToolTipText("当前模式：顺序播放");
                break;
            case RANDOM:
                modeButton.setText("随机播放");
                modeButton.setToolTipText("当前模式：随机播放");
                break;
            case SINGLE_LOOP:
                modeButton.setText("单曲循环");
                modeButton.setToolTipText("当前模式：单曲循环");
                break;
        }
    }
    
    /**
     * 更新按钮显示
     * @param playlist 当前选中的歌单
     */
    private void updateButtonVisibility(Playlist playlist) {
        // 获取按钮面板
        JPanel buttonPanel = (JPanel) infoPanel.getComponent(infoPanel.getComponentCount() - 1);
        buttonPanel.removeAll();
        
        // 始终显示播放全部按钮
        buttonPanel.add(playAllButton);
        
        // 根据歌单所有者判断显示不同的按钮
        if (playlist.getOwnerId().equals("学号10001")) {
            // 我的歌单：显示更换封面和下载全部按钮
            buttonPanel.add(changeCoverButton);
            buttonPanel.add(downloadAllButton);
        } else {
            // 网友歌单：显示加入我的歌单和下载全部按钮
            buttonPanel.add(addToMyPlaylistButton);
            buttonPanel.add(downloadAllButton);
        }
        
        // 刷新面板
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }
    
    /**
     * 应用程序入口点
     * 初始化UI主题并启动主窗口
     */
    public static void main(String[] args) {
        try {
            // 设置现代化暗色主题
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            // 如果主题设置失败，打印错误信息
            System.err.println("Failed to initialize LaF");
        }
        
        // 在EDT线程中创建并显示主窗口
        SwingUtilities.invokeLater(() -> {
            // 创建主窗口实例
            MainWindow window = new MainWindow();
            // 显示窗口
            window.setVisible(true);
        });
    }
    
    /**
     * 显示本地歌单内容
     * @param playlist 要显示的歌单
     */
    private void displayPlaylist(Playlist playlist) {
        // 更新歌单信息
        playlistNameLabel.setText(playlist.getName());
        playlistOwnerLabel.setText("创建者：" + playlist.getOwnerId());
        
        // 格式化创建时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        playlistDateLabel.setText("创建时间：" + playlist.getCreateDate().format(formatter));
        
        // 更新封面图片
        updateCoverImage(playlist.getCoverImagePath());
        
        // 清空表格
        DefaultTableModel model = (DefaultTableModel) songTable.getModel();
        model.setRowCount(0);
        
        // 保存当前播放列表供按钮使用
        songTable.putClientProperty("currentPlaylist", playlist);
        
        // 加载歌曲数据
        List<Song> songs = playlist.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            model.addRow(new Object[]{
                i + 1,                           // 序号
                song.getTitle(),                 // 歌名
                song.getArtist(),                // 歌手
                formatDuration(song.getDuration()), // 时长
                ""                               // 操作列（由渲染器处理）
            });
        }
        
        // 更新按钮显示
        updateButtonVisibility(playlist);
    }
    
    /**
     * 过滤歌单列表
     */
    private void filterPlaylists() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        // 如果是占位符文本或为空，显示所有歌单
        if (searchText.isEmpty() || searchText.equals("搜索歌单")) {
            loadFriendsPlaylists();
            return;
        }
        
        // 清空当前列表
        friendsListModel.clear();
        
        // 加载本地网友歌单
        for (Playlist playlist : dataManager.getAllPlaylists()) {
            if (!playlist.getOwnerId().equals("学号10001")) {
                String name = playlist.getName().toLowerCase();
                String ownerId = playlist.getOwnerId().toLowerCase();
                // 匹配歌单名称或创建者ID
                if (name.contains(searchText) || ownerId.contains(searchText)) {
                    friendsListModel.addElement(playlist.getName());
                }
            }
        }
        
        // 加载在线歌单
        List<OnlineMusicSheet> onlineSheets = dataManager.getOnlineMusicSheets();
        if (onlineSheets != null) {
            for (OnlineMusicSheet sheet : onlineSheets) {
                String name = sheet.getName().toLowerCase();
                String creator = sheet.getCreator().toLowerCase();
                String creatorId = sheet.getCreatorId().toLowerCase();
                // 匹配歌单名、创建者名称或创建者ID
                if (name.contains(searchText) || 
                    creator.contains(searchText) || 
                    creatorId.contains(searchText)) {
                    String displayName = "[在线] " + sheet.getName();
                    friendsListModel.addElement(displayName);
                    onlineSheetMap.put(displayName, sheet);
                }
            }
        }
        
        // 如果没有找到匹配项，显示提示
        if (friendsListModel.isEmpty()) {
            friendsListModel.addElement("未找到匹配的歌单");
        }
    }
} 