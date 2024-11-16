package com.musicplayer.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.musicplayer.controller.PlayerController;
import com.musicplayer.model.Playlist;
import com.musicplayer.model.Song;
import com.musicplayer.data.DataManager;
import com.musicplayer.util.MusicFileManager;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.IOException;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.BasicStroke;
import javax.imageio.ImageIO;

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
    private JLabel currentSongLabel;
    private Song currentPlayingSong;
    private JLabel songTitleLabel;
    private JLabel songArtistLabel;
    private JLabel songProgressLabel;
    
    public MainWindow() {
        this.playerController = new PlayerController();
        initializeUI();
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
        
        // 创建我的歌单和网友歌单区域
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("我的歌单", createMyPlaylistPanel());
        tabbedPane.addTab("网友歌单", createFriendsPlaylistPanel());
        
        sidebar.add(tabbedPane, BorderLayout.CENTER);
        return sidebar;
    }
    
    private JPanel createMyPlaylistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建歌单列表
        DefaultListModel<String> listModel = new DefaultListModel<>();
        myPlaylistList = new JList<>(listModel);
        myPlaylistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(myPlaylistList);
        
        // 加载所有歌单
        for (Playlist playlist : dataManager.getAllPlaylists()) {
            listModel.addElement(playlist.getName());
        }
        
        // 添加选择监听器
        myPlaylistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedPlaylist = myPlaylistList.getSelectedValue();
                if (selectedPlaylist != null) {
                    updateContentPanel(selectedPlaylist);
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
                    "user123" // TODO: 替换为实际的用户ID
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
                    "确定要删除选中的歌单吗？",
                    "删除歌单",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    dataManager.removePlaylist(playlistName);
                    listModel.remove(selectedIndex);
                }
            }
        });
        
        // 添加导入音乐按钮事件
        importButton.addActionListener(e -> importMusic());
        
        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(importButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFriendsPlaylistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> playlistList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(playlistList);
        
        panel.add(scrollPane, BorderLayout.CENTER);
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
        setDefaultCoverImage(); // 设置默认封面
        
        coverPanel.add(coverImageLabel, BorderLayout.CENTER);
        
        // 创建右侧信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        
        playlistNameLabel = new JLabel("歌单名称");
        playlistNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        
        playlistOwnerLabel = new JLabel("建者：");
        playlistOwnerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        playlistDateLabel = new JLabel("创建时间：");
        playlistDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton playAllButton = new JButton("播放全部");
        JButton downloadAllButton = new JButton("下载全部");
        JButton addToMyPlaylistButton = new JButton("加入我的歌单");
        JButton changeCoverButton = new JButton("更换封面");
        
        // 添加按钮事件
        playAllButton.addActionListener(e -> playAllSongs());
        downloadAllButton.addActionListener(e -> downloadAllSongs());
        addToMyPlaylistButton.addActionListener(e -> addToMyPlaylist());
        changeCoverButton.addActionListener(e -> changeCover());
        
        buttonPanel.add(playAllButton);
        buttonPanel.add(downloadAllButton);
        buttonPanel.add(addToMyPlaylistButton);
        buttonPanel.add(changeCoverButton);
        
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
        
        // 添加进度监听器
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
        playButton = new JButton("播放");
        JButton nextButton = new JButton("下一首");
        JButton modeButton = new JButton("顺序播放");
        
        // 添加播放/暂停按钮事件
        playButton.addActionListener(e -> {
            if (currentPlayingSong != null) {
                if (playerController.isPlaying()) {
                    playerController.pause();
                    playButton.setText("播放");
                } else {
                    playerController.resume();
                    playButton.setText("暂停");
                }
            }
        });
        
        // 添加上一首按钮事件
        prevButton.addActionListener(e -> {
            playerController.previous();
            updatePlayerInfo();
        });
        
        // 添加下一首按钮事件
        nextButton.addActionListener(e -> {
            playerController.next();
            updatePlayerInfo();
        });
        
        // 添加播放模式按钮事件
        modeButton.addActionListener(e -> {
            PlayerController.PlayMode currentMode = playerController.getPlayMode();
            PlayerController.PlayMode newMode;
            
            switch (currentMode) {
                case SEQUENCE:
                    newMode = PlayerController.PlayMode.RANDOM;
                    modeButton.setText("随机播放");
                    break;
                case RANDOM:
                    newMode = PlayerController.PlayMode.SINGLE_LOOP;
                    modeButton.setText("单曲循环");
                    break;
                default:
                    newMode = PlayerController.PlayMode.SEQUENCE;
                    modeButton.setText("顺序播放");
                    break;
            }
            
            playerController.setPlayMode(newMode);
        });
        
        controlPanel.add(prevButton);
        controlPanel.add(playButton);
        controlPanel.add(nextButton);
        controlPanel.add(modeButton);
        
        // 使用BorderLayout布局添加各个面板
        panel.add(songInfoPanel, BorderLayout.WEST);
        panel.add(progressPanel, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void updateContentPanel(String playlistName) {
        // 根据歌单名称查找对应的歌单
        Playlist playlist = findPlaylist(playlistName);
        if (playlist == null) {
            return;
        }
        
        // 更新歌单信息
        playlistNameLabel.setText(playlist.getName());
        playlistOwnerLabel.setText("创建者：" + playlist.getOwnerId());
        
        // 格式化创建时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        playlistDateLabel.setText("创建时间：" + playlist.getCreateDate().format(formatter));
        
        // 更新封面图片
        updateCoverImage(playlist.getCoverImagePath());
        
        // 清空并更新歌曲列表
        songTableModel.setRowCount(0);
        
        // 存储当前播放列表供按钮使用
        songTable.putClientProperty("currentPlaylist", playlist);
        
        // 加载歌曲数据
        List<Song> songs = playlist.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            Object[] row = new Object[5];
            row[0] = i + 1; // 序号
            row[1] = song.getTitle();
            row[2] = song.getArtist();
            row[3] = formatDuration(song.getDuration());
            row[4] = ""; // 操作按钮由渲染器处理
            songTableModel.addRow(row);
        }
    }
    
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
    
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
    
    private Playlist findPlaylist(String playlistName) {
        return dataManager.getPlaylist(playlistName);
    }
    
    // 歌曲操作方法
    private void playSong(Song song) {
        if (currentPlayingSong != null && 
            currentPlayingSong.equals(song)) {
            if (playerController.isPlaying()) {
                playerController.pause();
                playButton.setText("播放");
            } else {
                playerController.resume();
                playButton.setText("暂停");
            }
        } else {
            currentPlayingSong = song;
            playerController.setCurrentSong(song);
            playerController.play();
            playButton.setText("暂停");
            updateCurrentSongLabel();
            progressSlider.setValue(0);
        }
    }
    
    private void downloadSong(Song song) {
        // TODO: 实现下载歌曲的逻辑
        JOptionPane.showMessageDialog(this, 
            "开始下载：" + song.getTitle(),
            "下载提示",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void favoriteSong(Song song) {
        // TODO: 实现收藏歌曲的逻辑
        JOptionPane.showMessageDialog(this,
            "已收藏：" + song.getTitle(),
            "收藏提示",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updatePlayerInfo() {
        Song currentSong = playerController.getCurrentSong();
        if (currentSong != null) {
            currentPlayingSong = currentSong;
            updateCurrentSongLabel();
        }
    }
    
    // 按钮事件处理方法
    private void playAllSongs() {
        String selectedPlaylist = myPlaylistList.getSelectedValue();
        if (selectedPlaylist != null) {
            Playlist playlist = dataManager.getPlaylist(selectedPlaylist);
            if (playlist != null && !playlist.getSongs().isEmpty()) {
                playerController.setCurrentPlaylist(playlist);
                playerController.setCurrentSong(playlist.getSongs().get(0));
                playerController.play();
                playButton.setText("暂停");
                currentPlayingSong = playlist.getSongs().get(0);
                updateCurrentSongLabel();
            }
        }
    }
    
    private void downloadAllSongs() {
        // TODO: 实现下载全部歌曲的逻辑
    }
    
    private void addToMyPlaylist() {
        // TODO: 实现添加到我的歌单的逻辑
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
                
                // 更新歌单的封面路径
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
    
    // 添加更新当前播放歌曲标签的方法
    private void updateCurrentSongLabel() {
        if (currentPlayingSong != null) {
            songTitleLabel.setText(currentPlayingSong.getTitle());
            songArtistLabel.setText(currentPlayingSong.getArtist());
            long totalSeconds = currentPlayingSong.getDuration().getSeconds();
            songProgressLabel.setText(String.format("00:00 / %02d:%02d",
                totalSeconds / 60, totalSeconds % 60));
        } else {
            songTitleLabel.setText("未播放");
            songArtistLabel.setText("");
            songProgressLabel.setText("00:00 / 00:00");
        }
    }
    
    // 修改自定义渲染器类
    private class ButtonsRenderer implements TableCellRenderer {
        private final JPanel panel;
        
        public ButtonsRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            
            JButton playButton = new JButton("播放");
            JButton downloadButton = new JButton("下载");
            JButton favoriteButton = new JButton("收藏");
            
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
            super(new JCheckBox()); // 使用JCheckBox作为默认编辑器
            
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            
            playButton = new JButton("播放");
            downloadButton = new JButton("下载");
            favoriteButton = new JButton("收藏");
            
            Dimension buttonSize = new Dimension(60, 25);
            playButton.setPreferredSize(buttonSize);
            downloadButton.setPreferredSize(buttonSize);
            favoriteButton.setPreferredSize(buttonSize);
            
            playButton.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row != -1) {
                    Song song = ((Playlist)table.getClientProperty("currentPlaylist"))
                        .getSongs().get(row);
                    playSong(song);
                }
                fireEditingStopped();
            });
            
            downloadButton.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row != -1) {
                    Song song = ((Playlist)table.getClientProperty("currentPlaylist"))
                        .getSongs().get(row);
                    downloadSong(song);
                }
                fireEditingStopped();
            });
            
            favoriteButton.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row != -1) {
                    Song song = ((Playlist)table.getClientProperty("currentPlaylist"))
                        .getSongs().get(row);
                    favoriteSong(song);
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
        
        @Override
        public boolean shouldSelectCell(java.util.EventObject anEvent) {
            return true;
        }
    }
    
    // 添加更新进度标签的方法
    private void updateProgressLabel(int current, int total) {
        if (currentPlayingSong != null) {
            long totalSeconds = currentPlayingSong.getDuration().getSeconds();
            long currentSeconds = (totalSeconds * current) / 100;
            songProgressLabel.setText(String.format("%02d:%02d / %02d:%02d",
                currentSeconds / 60, currentSeconds % 60,
                totalSeconds / 60, totalSeconds % 60));
        }
    }
    
    // 添加设置默认封面的方法
    private void setDefaultCoverImage() {
        // 加载默认封面图片（需要准备一个默认封面图片）
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
    
    // 添加创建圆角图片的方法
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
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
} 