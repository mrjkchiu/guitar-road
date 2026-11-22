# 吉他提升之路 — 工程契约 CONTRACT（所有开发子代理必读，先读再写代码）

> 项目：原生 Android App「吉他提升之路」，包名 `com.chiu.guitarroad`，工程根目录 `~/workspace/guitar-improvement-road/`。
> 技术：Kotlin + Jetpack Compose (Material 3) + Navigation Compose + Room + DataStore，纯本地，无后端、无账号、无登录。
> 规则：**本文件是唯一接口标准**。包名、类名、函数签名、导航路由、表名、JSON 字段名必须与本文件一致，不许自创。写完自查：Kotlin 语法、import 齐全、Compose API 正确、Room 注解正确。不留 TODO、占位、假数据、半成品。

## 一、版本（写死，不许换）

| 项 | 值 |
|---|---|
| compileSdk / targetSdk | 34 |
| minSdk | 26 |
| AGP | 8.5.2 |
| Kotlin | 2.0.20 |
| Gradle wrapper | 8.7 |
| Compose BOM | 2024.09.00 |
| Room | 2.6.1（KSP 2.0.20-1.0.24） |
| Navigation Compose | 2.7.7 |
| lifecycle-viewmodel-compose / lifecycle-runtime-compose | 2.8.5 |
| datastore-preferences | 1.1.1 |
| kotlinx-serialization-json | 1.7.1 |
| core-ktx | 1.13.1 |
| activity-compose | 1.9.2 |
| JVM target / Kotlin jvmTarget | 17 |
| applicationId | com.chiu.guitarroad |
| versionCode / versionName | 1 / "1.0.0" |

依赖管理用 `gradle/libs.versions.toml`（脚手架代理写），所有模块引用 catalog，不许在 build.gradle.kts 里散写版本号。

## 二、目录与分工（谁写什么，不许越界改别人的文件）

```
guitar-improvement-road/
├── settings.gradle.kts / build.gradle.kts / gradle.properties / gradle/wrapper/*   [脚手架]
├── .github/workflows/build-apk.yml                                                [脚手架]
├── README.md（构建说明）                                                          [脚手架]
├── app/build.gradle.kts / app/proguard-rules.pro                                  [脚手架]
├── app/src/main/AndroidManifest.xml                                               [脚手架]
├── app/src/main/res/ (values/strings,colors,themes; mipmap icon; xml/backup规则)    [脚手架]
├── app/src/main/assets/ (paths.json chords.json theory_cards.json resources.json achievements.json) [数据层]
└── app/src/main/java/com/chiu/guitarroad/
    ├── MainActivity.kt / GuitarRoadApp.kt                                         [脚手架]
    ├── di/AppContainer.kt                                                         [脚手架]
    ├── data/model/Models.kt                                                       [数据层]
    ├── data/local/Entities.kt, Converters.kt, GuitarDao.kt, GuitarDatabase.kt      [数据层]
    ├── data/repository/GuitarRepository.kt, GuitarRepositoryImpl.kt                [数据层]
    ├── data/seed/AssetSeeder.kt, JsonModels.kt                                     [数据层]
    ├── data/settings/SettingsStore.kt                                             [数据层]
    ├── audio/Pitch.kt, AudioPitchRecorder.kt, Tuning.kt,
    │   ClickGenerator.kt, MetronomeEngine.kt, ProgressiveTempo.kt                 [音频]
    ├── ui/theme/Color.kt, Theme.kt, Type.kt                                       [脚手架]
    ├── ui/navigation/Routes.kt, NavGraph.kt, MainScreen.kt                        [脚手架]
    ├── ui/components/ChordDiagram.kt, RadarChart.kt, TunerDial.kt, Common.kt      [UI-工具]
    ├── ui/screens/onboarding/OnboardingScreen.kt, OnboardingViewModel.kt         [UI-核心]
    ├── ui/screens/home/HomeScreen.kt, HomeViewModel.kt                            [UI-核心]
    ├── ui/screens/path/PathScreen.kt, PathViewModel.kt                            [UI-核心]
    ├── ui/screens/task/TaskDetailScreen.kt, TaskDetailViewModel.kt                [UI-核心]
    ├── ui/screens/practice/PracticeScreen.kt, RecordPracticeSheet.kt, PracticeViewModel.kt [UI-核心]
    ├── ui/screens/profile/ProfileScreen.kt, ProfileViewModel.kt                   [UI-核心]
    └── ui/screens/tools/ToolsScreen.kt, TunerScreen.kt, MetronomeScreen.kt,
        ChordLibraryScreen.kt, ChordDetailScreen.kt, TheoryScreen.kt,
        TheoryCardScreen.kt, ToolsViewModel.kt                                     [UI-工具]
```

## 三、主题色（UI 规范原文，写死）

```
Primary 木色 #8B6F47 / PrimaryVariant #6B5435 / Secondary 绿 #4CAF7D
SecondaryLight #E8F5EE / Error 砖红 #D96C5B / ErrorLight #FCEEEB
Warning 琥珀 #E5A93C / Background 米白 #FAF7F2 / Surface #FFFFFF
SurfaceVariant #F1EBE2 / TextPrimary #2E2A26 / TextSecondary #7A726A
Divider #E3DCD2 / Disabled #C9C2B8
深色：Background #1E1B18 / Surface #2B2723 / Primary #C9A87C
```
`ui/theme/Color.kt` 定义以上颜色常量（Wood, WoodDark, LeafGreen, LeafGreenLight, BrickRed, BrickRedLight, Amber, CreamBg, CardWhite, CardVariant, InkPrimary, InkSecondary, LineDivider, DisabledGray）；`Theme.kt` 提供 `GuitarRoadTheme`（亮色 + 深色两套 Material 3 colorScheme）；`Type.kt` 用默认字体，字号按规范：Display 32 Bold、Headline 22 Bold、Title 18 SemiBold、Body 16、Caption 14、Button 16 Medium。

## 四、导航路由（字符串写死）

```
"onboarding"                                  入门测评（未完成测评时为起始页）
"main"                                        底部 5 Tab 宿主
"task/{nodeId}"                               任务详情，nodeId 如 node_03
"tuner?nodeId={nodeId}"                       调音器，nodeId 默认 "none"
"metronome?nodeId={nodeId}&bpm={bpm}"         节拍器，nodeId 默认 "none"，bpm 默认 60
"chords"                                      和弦谱库
"chord/{chordId}"                             和弦详情，chordId 如 Em
"theory"                                      乐理卡片列表
"theory/{cardId}"                             乐理卡详情，cardId 如 card_01
```
底部 5 Tab（顺序固定）：home 首页 / path 路径 / practice 练习 / tools 工具 / profile 我的。
导航结构：MainActivity 内一个 NavHost；"main" 目的地显示带底部栏的 MainScreen，Tab 切换用内部 state（不走嵌套导航）；其余路由为同级目的地，底部栏隐藏。判断是否显示底部栏：当前路由 == "main"。

## 五、数据模型（data/model/Models.kt，领域模型；Room 实体见第六节）

```kotlin
package com.chiu.guitarroad.data.model

enum class NodeStatus { LOCKED, AVAILABLE, IN_PROGRESS, DONE }
enum class SelfRating { TOO_EASY, JUST_RIGHT, TOO_HARD }
enum class MetronomeRating { STEADY, RUSHING, DRAGGING } // 稳 / 抢拍 / 拖拍

data class PathTemplate(val id: String, val name: String, val description: String,
    val goal: String, val level: String, val hours: Int,
    val totalNodes: Int, val estimatedWeeks: Int, val version: Int)

data class StageInfo(val number: Int, val title: String, val weeks: String, val milestone: String)

data class NodeTool(val type: String, val target: String?, val bpm: Int?, val trigger: String)
// type 取值： "tuner" | "metronome" | "chord" | "theory"
//   tuner: target=null；metronome: bpm=60；chord: target=和弦 id；theory: target=卡片 id

data class PathNode(val id: String, val stage: Int, val orderIndex: Int, val title: String,
    val why: String, val steps: List<String>, val criteria: String, val minutes: Int,
    val tools: List<NodeTool>, val theoryCardIds: List<String>, val resourceTags: List<String>,
    val unlockCondition: String, val isCheckpoint: Boolean, val remedialNodeId: String?)

data class Chord(val id: String, val label: String, val difficulty: String,
    val strings: List<Int>, val fingers: List<Int>, val steps: List<String>,
    val mistakes: List<String>, val next: String?, val nodeIds: List<Int>, val barre: Boolean)
// strings/fingers：6→1 弦顺序；-1=不弹，0=空弦；difficulty: beginner/intermediate/advanced

data class TheoryCard(val id: String, val title: String, val category: String,
    val content: String, val quizQuestion: String, val quizOptions: List<String>,
    val quizAnswer: Int, val tags: List<String>, val nodeIds: List<Int>)
// category: reading/pitch/harmony/rhythm；quizAnswer 为正确选项下标 0..2

data class ResourceLink(val id: Long, val nodeId: String, val platform: String,
    val title: String, val author: String, val authorUrl: String, val url: String,
    val durationSec: Int, val difficulty: String, val tags: List<String>,
    val sourceNote: String, val reportedBroken: Int, val active: Boolean, val usefulCount: Int)
// platform: bilibili/douyin；sourceNote 固定格式："由 @UP主 发布于 B站，点击前往观看"

data class NodeProgress(val nodeId: String, val status: NodeStatus, val selfRating: SelfRating?,
    val startedAt: Long?, val completedAt: Long?, val attempts: Int, val totalMinutes: Int)

data class PracticeRecord(val id: String, val nodeId: String?, val durationMin: Int,
    val rating: SelfRating?, val note: String?, val recordedAt: Long, val source: String)
// source: task/manual/timer

data class RadarScore(val chord: Int, val rhythm: Int, val fingering: Int,
    val repertoire: Int, val theory: Int, val consistency: Int)
// 全部 0..100

data class WorkItem(val id: String, val title: String, val mediaPath: String,
    val durationSec: Int, val linkedNodeId: String?, val createdAt: Long, val note: String?)

data class UserResourceItem(val id: String, val nodeId: String, val title: String,
    val url: String, val platform: String, val createdAt: Long)
```

## 六、Room（data/local/，表名与字段写死）

数据库名 `guitar_road.db`，`GuitarDatabase : RoomDatabase`，version = 1，exportSchema = false。
TypeConverters（Converters.kt）：List<String> / List<Int> 用 kotlinx-serialization 编码为 JSON 字符串存 TEXT。

实体（字段名与类型写死；JSON 字段以 `*Json: String` 存 TEXT）：

```kotlin
@Entity(tableName = "user")
data class UserEntity(@PrimaryKey val id: String, val nickname: String, val avatarRes: Int?,
    val createdAt: Long, val onboardingDone: Boolean)

@Entity(tableName = "user_profile", primaryKeys = ["userId"])
data class UserProfileEntity(val userId: String, val goal: String, val level: String,
    val weeklyHours: Int, val pathId: String, val measuredAt: Long)

@Entity(tableName = "path_template")
data class PathTemplateEntity(@PrimaryKey val id: String, val name: String, val description: String,
    val goal: String, val level: String, val hours: Int, val totalNodes: Int,
    val estimatedWeeks: Int, val version: Int)

@Entity(tableName = "path_node")
data class PathNodeEntity(@PrimaryKey val id: String, val pathId: String, val stage: Int,
    val orderIndex: Int, val title: String, val why: String, val stepsJson: String,
    val criteria: String, val minutes: Int, val toolsJson: String, val theoryCardIdsJson: String,
    val resourceTagsJson: String, val unlockCondition: String, val isCheckpoint: Boolean,
    val remedialNodeId: String?)

@Entity(tableName = "chord")
data class ChordEntity(@PrimaryKey val id: String, val label: String, val difficulty: String,
    val stringsJson: String, val fingersJson: String, val stepsJson: String,
    val mistakesJson: String, val nextChord: String?, val nodeIdsJson: String, val barre: Boolean)

@Entity(tableName = "theory_card")
data class TheoryCardEntity(@PrimaryKey val id: String, val title: String, val category: String,
    val content: String, val quizQuestion: String, val quizOptionsJson: String,
    val quizAnswer: Int, val tagsJson: String, val nodeIdsJson: String)

@Entity(tableName = "resource_link")
data class ResourceLinkEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: String, val platform: String, val title: String, val author: String,
    val authorUrl: String, val url: String, val durationSec: Int, val difficulty: String,
    val tagsJson: String, val sourceNote: String, val reportedBroken: Int,
    val active: Boolean, val usefulCount: Int)

@Entity(tableName = "user_node_progress", primaryKeys = ["userId", "nodeId"])
data class UserNodeProgressEntity(val userId: String, val nodeId: String, val status: String,
    val selfRating: String?, val startedAt: Long?, val completedAt: Long?,
    val attempts: Int, val totalMinutes: Int)

@Entity(tableName = "practice_record")
data class PracticeRecordEntity(@PrimaryKey val id: String, val userId: String, val nodeId: String?,
    val durationMin: Int, val rating: String?, val note: String?, val recordedAt: Long,
    val source: String)

@Entity(tableName = "radar_score", primaryKeys = ["userId"])
data class RadarScoreEntity(val userId: String, val chord: Int, val rhythm: Int,
    val fingering: Int, val repertoire: Int, val theory: Int, val consistency: Int,
    val updatedAt: Long)

@Entity(tableName = "work")
data class WorkEntity(@PrimaryKey val id: String, val userId: String, val title: String,
    val mediaPath: String, val durationSec: Int, val linkedNodeId: String?,
    val createdAt: Long, val note: String?)

@Entity(tableName = "user_resource")
data class UserResourceEntity(@PrimaryKey val id: String, val userId: String, val nodeId: String,
    val title: String, val url: String, val platform: String, val createdAt: Long)

@Entity(tableName = "achievement")
data class AchievementEntity(@PrimaryKey val id: String, val name: String,
    val conditionJson: String, val iconRes: String)

@Entity(tableName = "user_achievement", primaryKeys = ["userId", "achievementId"])
data class UserAchievementEntity(val userId: String, val achievementId: String, val earnedAt: Long)
```

`GuitarDao`（单个 DAO 接口，写死以下方法；返回 Flow 的用于界面订阅，suspend 的用于写操作）：

```kotlin
@Dao interface GuitarDao {
    // 静态内容
    @Query("SELECT * FROM path_template WHERE id = :id") suspend fun getTemplate(id: String): PathTemplateEntity?
    @Query("SELECT * FROM path_template") suspend fun getAllTemplates(): List<PathTemplateEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTemplates(v: List<PathTemplateEntity>)
    @Query("SELECT * FROM path_node WHERE pathId = :pathId ORDER BY orderIndex ASC") fun observeNodes(pathId: String): Flow<List<PathNodeEntity>>
    @Query("SELECT * FROM path_node WHERE id = :id") suspend fun getNode(id: String): PathNodeEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNodes(v: List<PathNodeEntity>)
    @Query("SELECT * FROM chord ORDER BY id ASC") fun observeChords(): Flow<List<ChordEntity>>
    @Query("SELECT * FROM chord WHERE id = :id") suspend fun getChord(id: String): ChordEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertChords(v: List<ChordEntity>)
    @Query("SELECT * FROM theory_card ORDER BY id ASC") fun observeTheoryCards(): Flow<List<TheoryCardEntity>>
    @Query("SELECT * FROM theory_card WHERE id = :id") suspend fun getTheoryCard(id: String): TheoryCardEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTheoryCards(v: List<TheoryCardEntity>)
    @Query("SELECT * FROM resource_link WHERE nodeId = :nodeId AND active = 1 ORDER BY usefulCount DESC") fun observeResourceLinks(nodeId: String): Flow<List<ResourceLinkEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertResourceLinks(v: List<ResourceLinkEntity>)
    @Query("UPDATE resource_link SET usefulCount = usefulCount + 1 WHERE id = :id") suspend fun markResourceUseful(id: Long)
    @Query("UPDATE resource_link SET reportedBroken = reportedBroken + 1 WHERE id = :id") suspend fun reportResourceBroken(id: Long)
    // 用户
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertUser(v: UserEntity)
    @Query("SELECT * FROM user LIMIT 1") suspend fun getUser(): UserEntity?
    @Query("UPDATE user SET onboardingDone = 1 WHERE id = :userId") suspend fun setOnboardingDone(userId: String)
    @Query("UPDATE user SET nickname = :name WHERE id = :userId") suspend fun updateNickname(userId: String, name: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertUserProfile(v: UserProfileEntity)
    @Query("SELECT * FROM user_profile WHERE userId = :userId") suspend fun getUserProfile(userId: String): UserProfileEntity?
    // 进度
    @Query("SELECT * FROM user_node_progress WHERE userId = :userId") fun observeProgress(userId: String): Flow<List<UserNodeProgressEntity>>
    @Query("SELECT * FROM user_node_progress WHERE userId = :userId AND nodeId = :nodeId") suspend fun getProgress(userId: String, nodeId: String): UserNodeProgressEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProgress(v: UserNodeProgressEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProgressList(v: List<UserNodeProgressEntity>)
    // 练习记录
    @Insert suspend fun insertPracticeRecord(v: PracticeRecordEntity)
    @Query("SELECT * FROM practice_record WHERE userId = :userId ORDER BY recordedAt DESC") fun observePracticeRecords(userId: String): Flow<List<PracticeRecordEntity>>
    @Query("SELECT * FROM practice_record WHERE userId = :userId AND recordedAt >= :since ORDER BY recordedAt DESC") suspend fun getRecordsSince(userId: String, since: Long): List<PracticeRecordEntity>
    @Query("DELETE FROM practice_record WHERE userId = :userId") suspend fun deleteAllPracticeRecords(userId: String)
    // 雷达
    @Query("SELECT * FROM radar_score WHERE userId = :userId") fun observeRadarScore(userId: String): Flow<RadarScoreEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertRadarScore(v: RadarScoreEntity)
    // 作品 / 自建资源 / 成就
    @Insert suspend fun insertWork(v: WorkEntity)
    @Query("SELECT * FROM work WHERE userId = :userId ORDER BY createdAt DESC") fun observeWorks(userId: String): Flow<List<WorkEntity>>
    @Insert suspend fun insertUserResource(v: UserResourceEntity)
    @Query("SELECT * FROM user_resource WHERE nodeId = :nodeId ORDER BY createdAt DESC") fun observeUserResources(nodeId: String): Flow<List<UserResourceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAchievements(v: List<AchievementEntity>)
    @Query("SELECT * FROM achievement") suspend fun getAchievements(): List<AchievementEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertUserAchievement(v: UserAchievementEntity)
    @Query("SELECT * FROM user_achievement WHERE userId = :userId") suspend fun getUserAchievements(userId: String): List<UserAchievementEntity>
    // 清除
    @Query("DELETE FROM user_node_progress WHERE userId = :userId") suspend fun deleteAllProgress(userId: String)
    @Query("DELETE FROM radar_score WHERE userId = :userId") suspend fun deleteRadarScore(userId: String)
    @Query("DELETE FROM user_profile WHERE userId = :userId") suspend fun deleteUserProfile(userId: String)
    @Query("DELETE FROM work WHERE userId = :userId") suspend fun deleteAllWorks(userId: String)
}
```

## 七、Repository（data/repository/GuitarRepository.kt，接口写死；实现类 GuitarRepositoryImpl）

```kotlin
interface GuitarRepository {
    val userId: String
    suspend fun ensureUser(): String                       // 没有用户则创建（UUID，昵称"吉他手"），返回 id
    suspend fun isOnboardingDone(): Boolean
    suspend fun seedIfNeeded()                            // 首次启动：读 assets 初始化静态表（见第八节）
    suspend fun saveOnboarding(goal: String, level: String, weeklyHours: Int): String // 写 profile+初始化进度+雷达0分，返回 pathId
    fun observeNodes(pathId: String): Flow<List<PathNode>>
    suspend fun getNode(nodeId: String): PathNode?
    fun observeProgress(): Flow<Map<String, NodeProgress>>
    suspend fun completeNode(nodeId: String, rating: SelfRating, minutes: Int)
    suspend fun startNode(nodeId: String)
    fun observeChords(): Flow<List<Chord>>
    suspend fun getChord(chordId: String): Chord?
    fun observeTheoryCards(): Flow<List<TheoryCard>>
    suspend fun getTheoryCard(cardId: String): TheoryCard?
    fun observeResourceLinks(nodeId: String): Flow<List<ResourceLink>>
    suspend fun markResourceUseful(id: Long)
    suspend fun reportResourceBroken(id: Long)
    suspend fun addUserResource(nodeId: String, title: String, url: String, platform: String)
    fun observeUserResources(nodeId: String): Flow<List<UserResourceItem>>
    suspend fun recordPractice(nodeId: String?, durationMin: Int, rating: SelfRating?, note: String?, source: String)
    fun observePracticeRecords(): Flow<List<PracticeRecord>>
    suspend fun getWeekMinutes(): Int                     // 本周练习总分钟
    suspend fun getStreakDays(): Int                      // 连续练习天数（今天或昨天有记录才计）
    fun observeRadarScore(): Flow<RadarScore>
    suspend fun addTheoryPoint()                          // 答对乐理小题：theory +1
    suspend fun addRhythmPoint(points: Int)               // 节拍器自评"稳"：rhythm +2
    fun observeWorks(): Flow<List<WorkItem>>
    suspend fun addWork(title: String, mediaPath: String, durationSec: Int, linkedNodeId: String?, note: String?)
    suspend fun clearAllLearningData()                    // 清除进度/记录/雷达/作品/profile（保留静态表）
    suspend fun exportAllData(): String                   // 导出全部用户数据为 JSON 字符串
}
```
实现要点：`completeNode` 必须在一个事务里做：进度 status=DONE + completedAt + selfRating；插入 practice_record（source="task"，durationMin=minutes）；按第九节加雷达分；解锁下一节点（下一 orderIndex 的节点 status LOCKED→AVAILABLE）；若 rating==TOO_HARD 且节点有 remedialNodeId，插入补练节点进度（AVAILABLE）。`recordPractice` 同样加分（见第九节）并检查成就 streak_7。

## 八、Assets JSON（数据层代理写，照文档原文录入，不许改写文案）

```
app/src/main/assets/paths.json         { template + stages + 30 nodes }
app/src/main/assets/chords.json        [20 个和弦]——照和弦文档第三节 JSON 原样
app/src/main/assets/theory_cards.json  [20 张卡]
app/src/main/assets/resources.json     []（空数组；文档里没有真实视频链接，运营录入工作不在本文档内——见第十一节）
app/src/main/assets/achievements.json  [{ "id":"streak_7","name":"坚持一周","condition":{"type":"streak","days":7},"icon":"flame" }]
```
- paths.json 模板：id `path_sing_zero_3h`，name `零基础弹唱之路`，description `从零开始，10 周弹唱完整一首歌`，goal `sing`，level `zero`，hours 3，totalNodes 30，estimatedWeeks 10，version 1。stages：1 `让手和琴成为朋友` `第 1–2 周` 里程碑 `Em / Am / G 切换 1 分钟 30 次`；2 `让节奏活起来` `第 3–4 周` 里程碑 `60 BPM 下连续扫 8 小节不抢拍`；3 `弹出第一首歌` `第 5–7 周` 里程碑 `完整录一遍主歌`；4 `从“能弹”到“能弹唱”` `第 8–10 周` 里程碑 `完整弹唱一首歌，存入作品档案`。
- 30 节点：id `node_01`…`node_30`，stage/orderIndex 按文档：阶段一 1–7、阶段二 8–14、阶段三 15–23、阶段四 24–30；标题/why/steps/criteria/minutes 照节点文档原文；isCheckpoint：node_07、node_14、node_23、node_30 为 true，其余 false；unlockCondition 一律 `self_pass`；remedialNodeId 一律 null。tools/theoryCards 按节点文档与乐理卡关联节点取并集：
  - node_02: tools [{tuner}]，theoryCards [card_05, card_09]
  - node_03: tools [{chord Em}]，theoryCards []
  - node_04: tools [{chord Am}]，theoryCards [card_10]
  - node_05: tools [{metronome 60}]，theoryCards []
  - node_06: tools [{metronome 60}]，theoryCards [card_03, card_17]
  - node_08: tools [{chord G}]，theoryCards []
  - node_09: tools [{chord G}]，theoryCards []
  - node_10: tools [{metronome 60}]，theoryCards []
  - node_11: tools [{theory card_01}]，theoryCards [card_01, card_03]
  - node_12: tools [{metronome 60}]，theoryCards [card_19]
  - node_13: tools [{metronome progressive}]，theoryCards [card_16, card_20]
  - node_15: tools [{metronome 60}]，theoryCards [card_05, card_02]
  - node_16: tools [{metronome 60}]，theoryCards [card_19]
  - node_17: tools [{chord C}]，theoryCards [card_11]
  - node_18: tools [{metronome 60},{chord C}]，theoryCards [card_12]
  - node_20: tools [{metronome 60}]，theoryCards []
  - node_21: tools [{metronome 60}]，theoryCards []
  - node_22: tools [{metronome 60}]，theoryCards []
  - node_24: tools [{metronome 60}]，theoryCards []
  - node_25: tools [{chord Fmaj7}]，theoryCards [card_08]
  - node_26: tools [{metronome 60}]，theoryCards []
  - 其余节点 tools []，theoryCards []。metronome progressive 指渐进加速模式（从 60 起，每 4 小节 +5，上限 240）。
- theory_cards.json：id `card_01`…`card_20`，照乐理文档逐张转录：title、category（1–4 reading，5–9 pitch，10–15 harmony，16–20 rhythm）、content（正文段落用 \n 连接，保留原文措辞；列表项保留）、quizQuestion、quizOptions（3 个，顺序照文档，去掉 ①②③ 序号）、quizAnswer（文档答案 ①→0、②→1、③→2）、tags（文档资源标签）、nodeIds（文档关联节点；"阶段五""进阶""P1 曲谱库""节奏进阶"等非数字关联记为 []）。
- chords.json 的 barre：F、Bm 为 true，其余 false。
- AssetSeeder：读 assets 4 个 JSON（paths/chords/theory_cards/achievements；resources 为空数组也照常读），用事务一次性写入 Room；DataStore 记录 seededVersion=1，已 seed 则跳过。

## 九、雷达计分（Repository 实现，写死）

完成节点时按节点归属加分（上限 100）：
- chord 维：node_03, 04, 05, 08, 09, 10, 17, 18 → +3
- rhythm 维：node_06, 12, 13, 14, 26 → +3
- fingering 维：node_15, 16, 25 → +3
- repertoire 维：node_19, 20, 21, 22, 23, 27, 28 → +3
- theory 维：node_02, 11 → +3
- consistency 维：node_01, 07, 24, 29, 30 → +2
- 其余节点完成：consistency +1。
- 乐理小题答对：theory +1（addTheoryPoint）。节拍器自评"稳"：rhythm +2（addRhythmPoint）。每次记一次练习：consistency +1。连续 7 天打卡：发 streak_7 成就。

## 十、音频引擎（audio/，照文档骨架写完整实现）

```kotlin
// Pitch.kt
data class PitchResult(val frequency: Float, val noteName: String, val octave: Int,
    val cents: Float, val isSilent: Boolean) {
    companion object { val SILENT: PitchResult
        fun fromFrequency(freq: Float): PitchResult } // A4=440Hz=69，cents=(midi-rounded)*100
}
class YinPitchDetector(private val sampleRate: Int) {
    fun detect(samples: FloatArray): PitchResult } // 阈值 0.12，minLag=sr/1500，maxLag=sr/70，RMS 门限 0.01，抛物线插值
class MedianSmoother(private val windowSize: Int = 5) { fun push(value: PitchResult): PitchResult }

// AudioPitchRecorder.kt
class AudioPitchRecorder(private val sampleRate: Int = 44100, private val bufferSize: Int = 4096) {
    @Volatile var isRunning: Boolean; var onPitch: (PitchResult) -> Unit
    fun start(); fun stop() } // AudioRecord MIC，PCM 16bit 单声道；后台线程读 + YIN + 中值滤波回调

// Tuning.kt
enum class TuningMode(val displayName: String, val semitoneOffset: Int) {
    STANDARD("标准调弦", 0), HALF_STEP_DOWN("降半音", -1), WHOLE_STEP_DOWN("降全音", -2), OPEN_G("Open G", 0) }
data class GuitarString(val stringNumber: Int, val noteName: String, val octave: Int, val frequency: Float)
object Tuning {
    fun stringsFor(mode: TuningMode): List<GuitarString> // 6→1 弦顺序
    fun targetFor(pitch: PitchResult, strings: List<GuitarString>): GuitarString? // 最近的目标弦
    fun centsToTarget(pitch: PitchResult, target: GuitarString): Float }
```
标准调弦频率（Hz）：6弦 E2 82.41 / 5弦 A2 110.00 / 4弦 D3 146.83 / 3弦 G3 196.00 / 2弦 B3 246.94 / 1弦 E4 329.63。降半音/降全音 = 标准频率 × 2^(offset/12)。Open G：D2 73.42 / G2 98.00 / D3 146.83 / G3 196.00 / B3 246.94 / D4 293.66。

```kotlin
// ClickGenerator.kt
object ClickGenerator { fun generate(sampleRate: Int, freq: Int, durationMs: Int, volume: Float): ShortArray }
// 强拍 1320Hz 0.9，弱拍 880Hz 0.6，30ms，exp(-8t) 衰减

// ProgressiveTempo.kt
class ProgressiveTempo(private val barsPerStep: Int = 4, private val deltaBpm: Int = 5, private val maxBpm: Int = 240) {
    fun onBarComplete(currentBpm: Int): Int; fun reset() }

// MetronomeEngine.kt
class MetronomeEngine(private val sampleRate: Int = 44100) {
    var bpm: Int = 60; var beatsPerBar: Int = 4
    var onBeat: (beatIndex: Int) -> Unit = {}
    var onBarComplete: (() -> Unit)? = null
    val isRunning: Boolean
    fun start(); fun stop() }
```
调度要求（照文档）：AudioTrack 预写 PCM，提前 200ms 排程，nanoTime 计时；**不许用 Thread.sleep / Handler.postDelayed 做节拍计时**（调度循环内的 50ms 休眠除外）；BPM 变化即时生效；stop 后释放 AudioTrack。

## 十一、组件与页面契约

共享组件（ui/components/，UI-工具代理写，UI-核心代理按签名调用）：
```kotlin
@Composable fun ChordDiagram(strings: List<Int>, fingers: List<Int>, modifier: Modifier = Modifier)
@Composable fun RadarChart(scores: RadarScore, modifier: Modifier = Modifier)
@Composable fun TunerDial(result: PitchResult, modifier: Modifier = Modifier)
```
- ChordDiagram：Canvas 绘制，6 弦 × 5 品；数组 6→1 弦，UI 从左到右画 6→1 弦（与六线谱一致：左为 6 弦）；-1 画 ×（砖红），0 画 ○（木色描边），n>0 在对应品格画实心木色圆 + 白色手指号；最低品 >1 时左侧标注 "Nfr"；同一品 ≥3 个按弦点时画横按圆角条；用 TextMeasurer 画文字（不许用已废弃的 drawText 旧写法）。
- RadarChart：六边形，6 轴标签顺序：和弦 / 节奏 / 指法 / 曲目 / 乐理 / 稳定度（从顶部顺时针）；网格线灰色，数据区木色半透明填充（alpha 0.25）+ 木色描边；数值变化 600ms 插值动画（animateFloatAsState）。
- TunerDial：直径 240dp，外圈 -50~+50 cents 刻度，中心大音名 32sp + cents 数；指针 spring 动画（阻尼 0.6）；颜色：|cents|≤5 绿 #4CAF7D，≤15 琥珀 #E5A93C，≤30 黄（用琥珀），>30 砖红 #D96C5B；静音时显示 "—" 与"请拨响一根弦"。
- Common.kt：PrimaryButton（木色实心、白字、高 48dp、圆角 12dp）、SecondaryButton（木色描边）、SectionCard（白卡、圆角 16dp、内边距 16dp、1px 边框 #E3DCD2）、SectionTitle、EmptyState（文案 + 行动按钮）。

页面要点（照原型文档；每屏只有一个主按钮）：
- Onboarding：3 题各一屏，进度 1/3；Q1 目标 5 选 1（弹唱流行歌/指弹独奏/古典入门/给唱歌伴奏/纯粹兴趣随便玩），Q2 现状 4 选 1（零基础/会 2–3 个和弦/能弹简单歌/弹了一阵卡住了），Q3 每周时间 3 选 1（每周 <2 小时/2–5 小时/>5 小时）；选中后"下一步"才亮；第 3 题提交后显示"正在生成你的路径…"1.5 秒再进首页。
- Home（首页 Tab）：今日任务卡（当前节点 + 开始今日练习主按钮）、连续天数火焰徽章（streak≥1 才显示）、本周练习时长、本周任务进度、下个里程碑、雷达图小卡入口、作品档案入口。
- Path（路径 Tab）：纵向时间轴；4 个阶段卡片（阶段名、周次、里程碑、进度条、状态：完成/进行中/未解锁）；阶段内节点列表（完成=绿对勾、当前=木色实心带光晕、锁定=灰空心）；点可用节点进任务详情。
- TaskDetail：标题 + 节点序号（节点 N / 共 30）；为什么练；怎么练（编号步骤）；关联工具入口（按 tools 渲染：调音器/节拍器 BPM/查指板/乐理卡）；乐理卡：首次进入该节点自动弹出卡片对话框（看完关闭）；资源卡：B站/抖音角标 + 标题 + UP 主 + 时长 + 难度 + 来源声明 + "去 B站看 →"（Intent.ACTION_VIEW 外跳，**站内无播放器**）；空状态文案："这个任务还没有收录视频" + 资源标签 chips + [添加我的链接]（标题+链接+平台，存 user_resource）；自评三选（太简单/刚好/太难）；主按钮"我练好了 ✓"（完成→记 practice_record→加雷达分→解锁下一节点→自动跳下一节点）；"跳过"文字按钮（不计入完成，仅当 attempts≥3 时显示——按原型"连续卡住 3 次以上出现"）。
- Tuner：调弦模式下拉（标准/降半音/降全音/Open G）；TunerDial；6 弦状态条（每弦目标音名 + ●/○，|cents|≤5 判准）；"🎤 正在聆听…" + 常驻文案"音频仅在本机处理，不会上传"；首次进页面先弹权限说明对话框，文案原文：**"调音需要访问麦克风。音频仅在本机实时处理，不会上传、不会存储、不会录音。"**；拒绝后显示"调音需要麦克风权限，请在设置中开启" + [去设置]（跳应用设置页）；从任务进入时底部显示 [返回节点 N ←]；页面离开时停止 AudioRecord。
- Metronome：拍号下拉（4/4、3/4）；节拍方块（按拍号个数，当前拍木色高亮放大）；BPM 大字（等宽字体 48sp）+ [−] [+]（步长 1，范围 30–240；另加 Slider）；渐进加速开关（每 4 小节 +5 BPM，上限 240）；"点按打拍"按钮（连续点按测 BPM）；开始/停止主按钮；停止后弹自评对话框"刚才跟得怎么样？"（稳/抢拍/拖拍），选"稳"则 rhythm +2；从任务进入时按节点 bpm 预设并显示 [返回节点 N ←]；离开页面停止引擎。
- ChordLibrary：搜索框（按 id/label 模糊搜）；筛选 chips：全部/入门/初级/进阶 + 横按；卡片网格（指板图缩略 + 和弦名 + 难度）；点进详情。
- ChordDetail：大指板图；按法步骤（编号）；常见错误（✗ 列表）；"下一个推荐：X"按钮（跳到该和弦详情；若 next 不在库中如 Dm7 则按钮置灰显示"暂未收录"——不许编造不存在的和弦）；相关节点 chips（nodeIds → 跳任务详情）。
- Theory：分类筛选（全部/读谱/音高/和声/节奏）+ 卡片列表（标题 + 分类 + 一句话内容预览）；点进详情。
- TheoryCard：一次一张卡：标题 + 正文（content 按 \n 分段显示）+ 小测验（3 选 1）；答对显示"答对了 ✓ 乐理 +1"并调 addTheoryPoint（同一张卡每天只加一次——用 DataStore 记 cardId+日期）；答错显示正确答案 + "再读一遍卡片内容"，不惩罚；[上一张][下一张]切换。
- Practice（练习 Tab）：RadarChart 大图；本周练习时长；7 天热条（▦/□）；连续天数火焰；最近记录列表（日期 + 节点名 + 时长）；[+ 记一次练习]主按钮 → 底部弹窗：选节点（可选）→ 时长（手填分钟，默认 15）→ 自评 → 保存（2 次点击完成：选节点后点保存即完成，时长/自评有默认值）。
- Profile（我的 Tab）：昵称（可改）、作品档案列表（标题 + 日期；空状态"录下你的第一段弹唱，3 个月后回听会惊讶" + [去录音]——录音功能 MVP 只做"添加作品记录"：标题 + 时长手填，mediaPath 可为空字符串；**不许做假录音**）、数据导出（导出全部数据为 JSON → 分享/保存到下载目录）、清除所有学习记录（二次确认对话框）、关于（版本 1.0.0、隐私说明：调音/录音仅本机处理）、设置入口（与清除/导出同页即可）。
- 空状态文案照 UI 规范第八节原文。

## 十二、脚手架要点

- AndroidManifest：application name `.GuitarRoadApp`，label `吉他提升之路`，MainActivity exported + MAIN/LAUNCHER；权限 `RECORD_AUDIO`；`android:usesCleartextTraffic="false"`；allowBackup true + backup_rules；`android:requestLegacyExternalStorage` 不许写。
- MainActivity：setContent { GuitarRoadTheme { 容器提供 → GuitarNavGraph } }；启动时 `repo.seedIfNeeded()` + `repo.ensureUser()`（IO 协程），完成后按 onboardingDone 决定起始路由。
- AppContainer（di/AppContainer.kt）：`class AppContainer(context: Context) { val db: GuitarDatabase; val repository: GuitarRepository; val settings: SettingsStore }`，单例懒加载；RoomDatabase.Builder(context, GuitarDatabase::class.java, "guitar_road.db").build()。
- SettingsStore（DataStore Preferences，文件名 "guitar_road_settings"）：keys：onboarding_done Boolean、user_id String、seeded_version Int、quiz_bonus_date String（"cardId:yyyy-MM-dd" 集合存 StringSet，key 为 quiz_bonus_days）。
- ViewModel 写法（统一）：`val vm: XxxViewModel = viewModel(factory = viewModelFactory { initializer { XxxViewModel(repo) } })`，XxxViewModel 继承 androidx.lifecycle.ViewModel，内部用 StateFlow + viewModelScope.launch。
- 图标：mipmap-anydpi-v26 用自适应图标（吉他拨片造型矢量，木色底 #8B6F47 + 米白拨片）；同时提供 legacy mipmap 各密度占位——用 Android Studio 自带 Image Asset 向量生成方式写 XML，不许放二进制 PNG（没有图片资源）。
- GitHub Actions（.github/workflows/build-apk.yml）：on push/tags + workflow_dispatch；ubuntu-latest；JDK 17；Android SDK（android-actions/setup-android）；./gradlew assembleDebug；上传 app/build/outputs/apk/debug/app-debug.apk 为 artifact（保留 30 天）；不签名 release，不许把 keystore 写入仓库。

## 十三、自查清单（每个代理交工前逐项过）

- [ ] 所有 import 齐全，无未解析引用；包名全部是 com.chiu.guitarroad 开头
- [ ] Room 实体字段与第六节一致；DAO 方法签名与第六节一致
- [ ] Repository 方法签名与第七节一致；JSON 字段名与第八节一致
- [ ] 导航路由字符串与第四节一致；ViewModel 工厂写法与第十二节一致
- [ ] Compose API：remember/StateFlow/collectAsState 用法正确；Canvas 用 TextMeasurer（androidx.compose.ui.text.rememberTextMeasurer + drawText）；不许用已删除的 drawText 旧 API
- [ ] 字符串：中文界面；权限文案用第十一节原文，一字不差
- [ ] 不许留 TODO/FIXME/占位文本/假数据/编造的视频链接/编造的和弦
- [ ] 报工时列出：写了哪些文件、与契约不一致的地方（有则必须写）、自查发现的问题
