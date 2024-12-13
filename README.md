# Music Player

一个基于Java Swing开发的现代化音乐播放器，支持本地音乐播放、在线音乐播放、歌单管理等功能

## 软件模块设计与实现

### 模块说明
1. 歌单管理模块
   - 我的歌单：创建、删除、导入音乐、更换封面
   - 网友歌单：浏览、收藏、加入我的歌单
   - 在线歌单：浏览、播放、下载、收藏
   - 收藏歌曲：收藏、取消收藏、查看收藏列表

2. 播放控制模块
   - 播放控制：播放、暂停、继续、上一首、下一首
   - 播放模式：顺序播放、随机播放、单曲循环
   - 进度控制：显示播放进度、时间显示
   - 在线播放：流式播放、缓存管理

3. 数据管理模块
   - 本地存储：歌单数据、收藏数据的持久化
   - 文件管理：音乐文件、封面图片的管理
   - 缓存管理：在线音乐的缓存处理
   - 数据同步：保持数据的一致性

### 1 核心模块说明
- **数据模型（model包）**
  - Song：歌曲实体类，存储歌曲信息
  - Playlist：歌单实体类，管理歌曲集合
  - OnlineMusicSheet：在线歌单实体类，管理在线音乐

- **控制器（controller包）**
  - PlayerController：播放控制器，处理播放逻辑
  - DataManager：数据管理器，处理数据持久化

- **界面（ui包）**
  - MainWindow：主窗口类，实现GUI界面
  - 自定义渲染器：实现歌曲列表中的操作按钮

- **工具类（util包）**
  - MusicFileManager：音乐文件管理工具
  - StreamCache：在线音乐缓存管理
  - AudioPlayer：音频播放器实现

- **网络（network包）**
  - MusicServerAPI：在线音乐服务器接口

### 2 关键技术实现
- **界面实现**
  - 使用Java Swing构建GUI界面
  - 采用FlatLaf实现现代化外观
  - 自定义表格渲染器实现操作按钮
  - 实现圆角图片显示效果
  - 实现实时搜索功能

- **音频处理**
  - 使用JLayer实现MP3解码播放
  - 使用JAudioTagger读取音频元数据
  - 实现播放进度实时更新
  - 支持多种播放模式切换
  - 实现在线音乐流式播放

- **数据管理**
  - 使用序列化实现数据持久化
  - 管理音乐文件和封面图片
  - 实现歌单的增删改查
  - 处理收藏歌曲功能
  - 实现在线音乐缓存

- **多线程处理**
  - 使用独立线程处理音频播放
  - 使用SwingWorker处理文件操作
  - 实现进度条的平滑更新
  - 确保UI响应的流畅性
  - 异步加载在线资源

### 3 使用说明
1. **歌单管理**
   - 点击"新建歌单"创建个人歌单
   - 选择歌单后点击"导入音乐"添加歌曲
   - 点击"更换封面"可以自定义歌单封面
   - 在网友歌单中可以将喜欢的歌单添加到我的歌单
   - 支持搜索歌单名称和创建者

2. **音乐播放**
   - 点击单首歌曲的播放按钮播放
   - 点击"播放全部"播放整个歌单
   - 使用底部控制栏控制播放进度和切换歌曲
   - 点击播放模式按钮切换不同播放模式
   - 支持在线音乐播放和本地音乐播放

3. **歌曲收藏**
   - 点击歌曲右侧的收藏按钮收藏歌曲
   - 收藏的歌曲会自动添加到"我的收藏"歌单
   - 再次点击可以取消收藏
   - 支持收藏在线音乐

4. **歌曲下载**
   - 点击单首歌曲的下载按钮下载
   - 点击"下载全部"下载整个歌单
   - 选择保存位置后自动开始下载
   - 下载过程中显示进度提示
   - 支持在线音乐下载

### 4 注意事项
- 首次运行会自动创建必要的目录
- 支持的音频格式仅限MP3
- 建议使用相对较新的Java版本以获得更好的界面效果
- 导入音乐文件时请确保文件名格式正确
- 在线音乐需要网络连接
- 程序退出时会自动清理缓存
