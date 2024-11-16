# Music Player

一个基于Java Swing开发的现代化音乐播放器，支持本地音乐播放、歌单管理等功能。

## 功能特点

### 界面设计
- 使用 FlatLaf 实现现代化的暗色主题界面
- 左侧歌单导航栏
- 中间歌单详情和歌曲列表
- 底部播放控制栏
- 圆角封面图片显示

### 歌单管理
- 创建/删除歌单
- 导入音乐文件
- 更换歌单封面
- 歌单信息显示（名称、创建者、创建时间）

### 音乐播放
- 支持 MP3 格式音乐文件播放
- 播放/暂停控制
- 上一首/下一首切换
- 播放模式切换（顺序播放、随机播放、单曲循环）
- 实时进度条显示
- 播放时间显示

### 数据管理
- 本地数据持久化存储
- 音乐文件自动管理
- 封面图片管理

## 技术栈

- Java 8
- Swing GUI
- Maven
- 第三方库：
  - FlatLaf：现代化界面
  - JLayer：MP3 解码播放
  - JAudioTagger：音频元数据读取
  - Apache Commons IO：文件操作

## 项目结构 