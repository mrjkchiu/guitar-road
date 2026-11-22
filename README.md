# 吉他提升之路

你的私人吉他成长轨迹 — 告诉你要练什么，记住你练过什么，让你看见自己在进步。

原生 Android App：Kotlin + Jetpack Compose (Material 3) + Navigation Compose + Room + DataStore，纯本地，无后端、无账号、无登录。

## 功能

- **入门测评**：3 题定位目标 / 水平 / 每周时间，生成专属学习路径
- **专属路径**：30 个任务节点，4 个阶段（第 1–2 周上手 → 第 3–4 周节奏 → 第 5–7 周弹歌 → 第 8–10 周弹唱）
- **每日任务**：今天练什么、怎么练、验收标准，一屏一件事
- **练习记录**：计时、自评、备注；六维能力雷达图（和弦 / 节奏 / 指法 / 曲目 / 乐理 / 稳定度）
- **工具**：调音器（麦克风实时音高识别，仅本机处理）、节拍器（BPM / 拍号 / 渐进加速）、和弦谱库（20 个和弦，指板图代码绘制）、乐理卡片（20 张，带小测验）
- **作品档案**：记录你的弹唱成果；数据可导出为 JSON、可一键清除

## 环境要求

| 项 | 版本 |
|---|---|
| JDK | 17 |
| Android SDK | API 34（platforms/android-34、build-tools 34.0.0） |
| Gradle wrapper | 8.7（随仓库提供 `./gradlew`） |
| AGP | 8.5.2 |
| Kotlin | 2.0.20 |
| minSdk / targetSdk | 26 / 34 |

## 本地编译

```bash
# 1. 装好 JDK 17 与 Android SDK（API 34），并设置 ANDROID_HOME 或写 local.properties：
#    sdk.dir=/path/to/Android/Sdk

# 2. 在工程根目录编译 Debug 包
./gradlew assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`（applicationId `com.chiu.guitarroad`，versionName `1.0.0`）。

> 注意：Gradle 守护进程与 Android 构建需要联网下载依赖（Google Maven、Maven Central、Gradle Plugin Portal）。如果在受限网络（如需走代理）下编译，请在 `~/.gradle/gradle.properties` 里配好 `systemProp.http(s).proxyHost/Port` 与信任库。

## 安装到手机

```bash
# 手机开 USB 调试后：
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

也可以把 APK 传到手机上直接点击安装（需允许“安装未知应用”）。

首次启动会做 3 题测评（约 60 秒），然后生成你的专属路径。调音器首次使用时会申请麦克风权限，音频仅在本机实时处理，不会上传、不会存储、不会录音。

## 工程结构

```
app/src/main/java/com/chiu/guitarroad/
├── MainActivity.kt / GuitarRoadApp.kt  # 入口与 Application
├── di/AppContainer.kt                 # 手动依赖容器（db / repository / settings）
├── data/model/                        # 领域模型
├── data/local/                        # Room 实体、DAO、数据库
├── data/repository/                   # Repository 接口与实现
├── data/seed/                         # assets JSON 初始化
├── data/settings/                     # DataStore 设置
├── audio/                             # YIN 音高检测、节拍器引擎
├── ui/theme/                          # 颜色 / 主题 / 字号
├── ui/navigation/                     # 路由、导航图、底部 5 Tab 宿主
├── ui/components/                     # 指板图、雷达图、调音盘、通用组件
└── ui/screens/                        # 测评 / 首页 / 路径 / 任务 / 练习 / 我的 / 工具
app/src/main/assets/                    # paths.json / chords.json / theory_cards.json / resources.json / achievements.json
```

接口标准见 `CONTRACT.md`（包名、类名、函数签名、导航路由、表名、JSON 字段名必须与之一致）。

## 云端构建

打 tag `v*` 推送或在 Actions 里手动触发，会自动跑 `./gradlew assembleDebug` 并上传 `app-debug.apk`（保留 30 天）。只编 Debug 包，不签名 release、不在仓库放 keystore。
