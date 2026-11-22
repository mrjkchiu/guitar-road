package com.chiu.guitarroad.data.repository

import com.chiu.guitarroad.data.model.Chord
import com.chiu.guitarroad.data.model.NodeProgress
import com.chiu.guitarroad.data.model.PathNode
import com.chiu.guitarroad.data.model.PracticeRecord
import com.chiu.guitarroad.data.model.RadarScore
import com.chiu.guitarroad.data.model.ResourceLink
import com.chiu.guitarroad.data.model.SelfRating
import com.chiu.guitarroad.data.model.TheoryCard
import com.chiu.guitarroad.data.model.UserResourceItem
import com.chiu.guitarroad.data.model.WorkItem
import kotlinx.coroutines.flow.Flow

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
