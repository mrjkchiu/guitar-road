package com.chiu.guitarroad.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.chiu.guitarroad.data.local.ChordEntity
import com.chiu.guitarroad.data.local.Converters
import com.chiu.guitarroad.data.local.GuitarDao
import com.chiu.guitarroad.data.local.GuitarDatabase
import com.chiu.guitarroad.data.local.PathNodeEntity
import com.chiu.guitarroad.data.local.PracticeRecordEntity
import com.chiu.guitarroad.data.local.RadarScoreEntity
import com.chiu.guitarroad.data.local.ResourceLinkEntity
import com.chiu.guitarroad.data.local.TheoryCardEntity
import com.chiu.guitarroad.data.local.UserAchievementEntity
import com.chiu.guitarroad.data.local.UserEntity
import com.chiu.guitarroad.data.local.UserNodeProgressEntity
import com.chiu.guitarroad.data.local.UserProfileEntity
import com.chiu.guitarroad.data.local.UserResourceEntity
import com.chiu.guitarroad.data.local.WorkEntity
import com.chiu.guitarroad.data.model.Chord
import com.chiu.guitarroad.data.model.NodeProgress
import com.chiu.guitarroad.data.model.NodeStatus
import com.chiu.guitarroad.data.model.PathNode
import com.chiu.guitarroad.data.model.PracticeRecord
import com.chiu.guitarroad.data.model.RadarScore
import com.chiu.guitarroad.data.model.ResourceLink
import com.chiu.guitarroad.data.model.SelfRating
import com.chiu.guitarroad.data.model.TheoryCard
import com.chiu.guitarroad.data.model.UserResourceItem
import com.chiu.guitarroad.data.model.WorkItem
import com.chiu.guitarroad.data.seed.AssetSeeder
import com.chiu.guitarroad.data.settings.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * GuitarRepository 的 Room 实现。
 *
 * 构造：AppContainer 用 GuitarRepositoryImpl(context, db, settings) 创建，
 * 其中 db 为 GuitarDatabase；写操作里需要事务的地方用 db.withTransaction。
 * completeNode / recordPractice 的多步写在一个事务内完成。
 *
 * userId 属性：ensureUser() 运行后缓存用户 id（MainActivity 启动时按
 * 契约先调 seedIfNeeded() 与 ensureUser()）；此前读取返回 ""。
 */
class GuitarRepositoryImpl(
    private val context: Context,
    private val db: GuitarDatabase,
    private val settings: SettingsStore,
) : GuitarRepository {

    private val dao: GuitarDao get() = db.guitarDao()
    private val converters = Converters()

    @Volatile
    private var cachedUserId: String? = null

    override val userId: String
        get() = cachedUserId ?: ""

    override suspend fun ensureUser(): String {
        cachedUserId?.let { return it }
        val existing = dao.getUser()
        if (existing != null) {
            cachedUserId = existing.id
            if (settings.userId.first() == null) settings.setUserId(existing.id)
            return existing.id
        }
        val id = UUID.randomUUID().toString()
        dao.insertUser(
            UserEntity(
                id = id,
                nickname = "吉他手",
                avatarRes = null,
                createdAt = System.currentTimeMillis(),
                onboardingDone = false,
            ),
        )
        cachedUserId = id
        settings.setUserId(id)
        return id
    }

    override suspend fun isOnboardingDone(): Boolean {
        val user = dao.getUser()
        if (user != null) return user.onboardingDone
        return settings.onboardingDone.first()
    }

    override suspend fun seedIfNeeded() {
        AssetSeeder(context, dao, settings).seedIfNeeded()
    }

    override suspend fun saveOnboarding(goal: String, level: String, weeklyHours: Int): String {
        val uid = ensureUser()
        seedIfNeeded()
        val pathId = resolvePathId(goal, level)
        val now = System.currentTimeMillis()
        dao.insertUserProfile(
            UserProfileEntity(
                userId = uid,
                goal = goal,
                level = level,
                weeklyHours = weeklyHours,
                pathId = pathId,
                measuredAt = now,
            ),
        )
        dao.setOnboardingDone(uid)
        settings.setOnboardingDone(true)
        val nodes = dao.observeNodes(pathId).first()
        dao.upsertProgressList(
            nodes.mapIndexed { index, node ->
                UserNodeProgressEntity(
                    userId = uid,
                    nodeId = node.id,
                    status = if (index == 0) NodeStatus.AVAILABLE.name else NodeStatus.LOCKED.name,
                    selfRating = null,
                    startedAt = null,
                    completedAt = null,
                    attempts = 0,
                    totalMinutes = 0,
                )
            },
        )
        dao.upsertRadarScore(
            RadarScoreEntity(uid, chord = 0, rhythm = 0, fingering = 0, repertoire = 0, theory = 0, consistency = 0, updatedAt = now),
        )
        return pathId
    }

    /**
     * 按 goal/level 找模板；找不到则落到默认模板 path_sing_zero_3h；
     * 若静态表为空（未 seed 成功）则返回默认模板 id，调用方仍可继续。
     */
    private suspend fun resolvePathId(goal: String, level: String): String {
        val templates = dao.getAllTemplates()
        templates.firstOrNull { it.goal == goal && it.level == level }?.let { return it.id }
        templates.firstOrNull { it.id == DEFAULT_PATH_ID }?.let { return it.id }
        templates.firstOrNull()?.let { return it.id }
        return DEFAULT_PATH_ID
    }

    override fun observeNodes(pathId: String): Flow<List<PathNode>> =
        dao.observeNodes(pathId).map { list -> list.map { it.toDomain() } }

    override suspend fun getNode(nodeId: String): PathNode? =
        dao.getNode(nodeId)?.toDomain()

    override fun observeProgress(): Flow<Map<String, NodeProgress>> =
        settings.userId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyMap())
            else dao.observeProgress(uid).map { list ->
                list.associate { it.nodeId to it.toDomain() }
            }
        }

    override suspend fun completeNode(nodeId: String, rating: SelfRating, minutes: Int) {
        val uid = ensureUser()
        val node = dao.getNode(nodeId) ?: return
        val now = System.currentTimeMillis()
        db.withTransaction {
            val existing = dao.getProgress(uid, nodeId)
            dao.upsertProgress(
                UserNodeProgressEntity(
                    userId = uid,
                    nodeId = nodeId,
                    status = NodeStatus.DONE.name,
                    selfRating = rating.name,
                    startedAt = existing?.startedAt ?: now,
                    completedAt = now,
                    attempts = (existing?.attempts ?: 0) + 1,
                    totalMinutes = (existing?.totalMinutes ?: 0) + minutes,
                ),
            )
            dao.insertPracticeRecord(
                PracticeRecordEntity(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    nodeId = nodeId,
                    durationMin = minutes,
                    rating = rating.name,
                    note = null,
                    recordedAt = now,
                    source = "task",
                ),
            )
            // 契约第九节：按节点归属加雷达分
            updateRadar(uid, radarDeltaForNode(nodeNumberOf(nodeId)))
            // 解锁下一 orderIndex 的节点
            val nodes = dao.observeNodes(node.pathId).first()
            val next = nodes.firstOrNull { it.orderIndex == node.orderIndex + 1 }
            if (next != null) {
                val nextProgress = dao.getProgress(uid, next.id)
                if (nextProgress != null && nextProgress.status == NodeStatus.LOCKED.name) {
                    dao.upsertProgress(nextProgress.copy(status = NodeStatus.AVAILABLE.name))
                }
            }
            // 自评太难且有补练节点：插入补练节点进度（AVAILABLE）
            if (rating == SelfRating.TOO_HARD && node.remedialNodeId != null) {
                if (dao.getProgress(uid, node.remedialNodeId) == null) {
                    dao.upsertProgress(
                        UserNodeProgressEntity(
                            userId = uid,
                            nodeId = node.remedialNodeId,
                            status = NodeStatus.AVAILABLE.name,
                            selfRating = null,
                            startedAt = null,
                            completedAt = null,
                            attempts = 0,
                            totalMinutes = 0,
                        ),
                    )
                }
            }
        }
    }

    override suspend fun startNode(nodeId: String) {
        val uid = ensureUser()
        val now = System.currentTimeMillis()
        val existing = dao.getProgress(uid, nodeId)
        if (existing == null) {
            dao.upsertProgress(
                UserNodeProgressEntity(
                    userId = uid, nodeId = nodeId, status = NodeStatus.IN_PROGRESS.name,
                    selfRating = null, startedAt = now, completedAt = null,
                    attempts = 1, totalMinutes = 0,
                ),
            )
        } else if (existing.status != NodeStatus.DONE.name) {
            dao.upsertProgress(
                existing.copy(
                    status = NodeStatus.IN_PROGRESS.name,
                    startedAt = existing.startedAt ?: now,
                    attempts = existing.attempts + 1,
                ),
            )
        }
    }

    override fun observeChords(): Flow<List<Chord>> =
        dao.observeChords().map { list -> list.map { it.toDomain() } }

    override suspend fun getChord(chordId: String): Chord? =
        dao.getChord(chordId)?.toDomain()

    override fun observeTheoryCards(): Flow<List<TheoryCard>> =
        dao.observeTheoryCards().map { list -> list.map { it.toDomain() } }

    override suspend fun getTheoryCard(cardId: String): TheoryCard? =
        dao.getTheoryCard(cardId)?.toDomain()

    override fun observeResourceLinks(nodeId: String): Flow<List<ResourceLink>> =
        dao.observeResourceLinks(nodeId).map { list -> list.map { it.toDomain() } }

    override suspend fun markResourceUseful(id: Long) {
        dao.markResourceUseful(id)
    }

    override suspend fun reportResourceBroken(id: Long) {
        dao.reportResourceBroken(id)
    }

    override suspend fun addUserResource(nodeId: String, title: String, url: String, platform: String) {
        val uid = ensureUser()
        dao.insertUserResource(
            UserResourceEntity(
                id = UUID.randomUUID().toString(),
                userId = uid,
                nodeId = nodeId,
                title = title,
                url = url,
                platform = platform,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    override fun observeUserResources(nodeId: String): Flow<List<UserResourceItem>> =
        dao.observeUserResources(nodeId).map { list -> list.map { it.toDomain() } }

    override suspend fun recordPractice(
        nodeId: String?,
        durationMin: Int,
        rating: SelfRating?,
        note: String?,
        source: String,
    ) {
        val uid = ensureUser()
        val now = System.currentTimeMillis()
        db.withTransaction {
            dao.insertPracticeRecord(
                PracticeRecordEntity(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    nodeId = nodeId,
                    durationMin = durationMin,
                    rating = rating?.name,
                    note = note,
                    recordedAt = now,
                    source = source,
                ),
            )
            // 每次记一次练习：consistency +1
            updateRadar(uid) { copy(consistency = consistency + 1) }
            // 关联节点时累加该节点总分钟数
            if (nodeId != null) {
                val progress = dao.getProgress(uid, nodeId)
                if (progress != null) {
                    dao.upsertProgress(progress.copy(totalMinutes = progress.totalMinutes + durationMin))
                }
            }
            // 连续 7 天打卡：发 streak_7 成就
            if (streakDays(uid) >= STREAK_ACHIEVEMENT_DAYS) {
                dao.insertUserAchievement(
                    UserAchievementEntity(uid, STREAK_ACHIEVEMENT_ID, now),
                )
            }
        }
    }

    override fun observePracticeRecords(): Flow<List<PracticeRecord>> =
        settings.userId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else dao.observePracticeRecords(uid).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun getWeekMinutes(): Int {
        val uid = ensureUser()
        val mondayStart = LocalDate.now()
            .with(DayOfWeek.MONDAY)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return dao.getRecordsSince(uid, mondayStart).sumOf { it.durationMin }
    }

    override suspend fun getStreakDays(): Int = streakDays(ensureUser())

    /**
     * 按 recordedAt 的日期去重，倒数连续天数；今天或昨天无记录则返回 0。
     */
    private suspend fun streakDays(uid: String): Int {
        val days = dao.getRecordsSince(uid, 0L)
            .map { Instant.ofEpochMilli(it.recordedAt).atZone(ZoneId.systemDefault()).toLocalDate() }
            .toSet()
        val today = LocalDate.now()
        var cursor = if (today in days) today else today.minusDays(1)
        if (cursor !in days) return 0
        var streak = 0
        while (cursor in days) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    override fun observeRadarScore(): Flow<RadarScore> =
        settings.userId.flatMapLatest { uid ->
            if (uid == null) flowOf(RadarScore(0, 0, 0, 0, 0, 0))
            else dao.observeRadarScore(uid).map { it?.toDomain() ?: RadarScore(0, 0, 0, 0, 0, 0) }
        }

    override suspend fun addTheoryPoint() {
        val uid = ensureUser()
        updateRadar(uid) { copy(theory = theory + 1) }
    }

    override suspend fun addRhythmPoint(points: Int) {
        val uid = ensureUser()
        updateRadar(uid) { copy(rhythm = rhythm + points) }
    }

    override fun observeWorks(): Flow<List<WorkItem>> =
        settings.userId.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else dao.observeWorks(uid).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun addWork(
        title: String,
        mediaPath: String,
        durationSec: Int,
        linkedNodeId: String?,
        note: String?,
    ) {
        val uid = ensureUser()
        dao.insertWork(
            WorkEntity(
                id = UUID.randomUUID().toString(),
                userId = uid,
                title = title,
                mediaPath = mediaPath,
                durationSec = durationSec,
                linkedNodeId = linkedNodeId,
                createdAt = System.currentTimeMillis(),
                note = note,
            ),
        )
    }

    override suspend fun clearAllLearningData() {
        val uid = ensureUser()
        dao.deleteAllProgress(uid)
        dao.deleteAllPracticeRecords(uid)
        dao.deleteRadarScore(uid)
        dao.deleteAllWorks(uid)
        dao.deleteUserProfile(uid)
        // profile 已删，测评须重做；用户行与静态表保留。
        settings.setOnboardingDone(false)
    }

    override suspend fun exportAllData(): String {
        val uid = ensureUser()
        val profile = dao.getUserProfile(uid)
        val progress = dao.observeProgress(uid).first()
        val records = dao.observePracticeRecords(uid).first()
        val radar = dao.observeRadarScore(uid).first()
        val works = dao.observeWorks(uid).first()
        val root = buildJsonObject {
            put("userId", uid)
            profile?.let {
                putJsonObject("profile") {
                    put("goal", it.goal)
                    put("level", it.level)
                    put("weeklyHours", it.weeklyHours)
                    put("pathId", it.pathId)
                    put("measuredAt", it.measuredAt)
                }
            }
            putJsonArray("progress") {
                progress.forEach { p ->
                    addJsonObject {
                        put("nodeId", p.nodeId)
                        put("status", p.status)
                        p.selfRating?.let { put("selfRating", it) }
                        p.startedAt?.let { put("startedAt", it) }
                        p.completedAt?.let { put("completedAt", it) }
                        put("attempts", p.attempts)
                        put("totalMinutes", p.totalMinutes)
                    }
                }
            }
            putJsonArray("practiceRecords") {
                records.forEach { r ->
                    addJsonObject {
                        put("id", r.id)
                        r.nodeId?.let { put("nodeId", it) }
                        put("durationMin", r.durationMin)
                        r.rating?.let { put("rating", it) }
                        r.note?.let { put("note", it) }
                        put("recordedAt", r.recordedAt)
                        put("source", r.source)
                    }
                }
            }
            radar?.let {
                putJsonObject("radarScore") {
                    put("chord", it.chord)
                    put("rhythm", it.rhythm)
                    put("fingering", it.fingering)
                    put("repertoire", it.repertoire)
                    put("theory", it.theory)
                    put("consistency", it.consistency)
                    put("updatedAt", it.updatedAt)
                }
            }
            putJsonArray("works") {
                works.forEach { w ->
                    addJsonObject {
                        put("id", w.id)
                        put("title", w.title)
                        put("mediaPath", w.mediaPath)
                        put("durationSec", w.durationSec)
                        w.linkedNodeId?.let { put("linkedNodeId", it) }
                        put("createdAt", w.createdAt)
                        w.note?.let { put("note", it) }
                    }
                }
            }
        }
        return Json.encodeToString(JsonObject.serializer(), root)
    }

    // ---- 内部辅助 ----

    private suspend fun updateRadar(userId: String, transform: RadarScoreEntity.() -> RadarScoreEntity) {
        val now = System.currentTimeMillis()
        val current = dao.observeRadarScore(userId).first()
            ?: RadarScoreEntity(userId, 0, 0, 0, 0, 0, 0, now)
        val updated = transform(current).copy(updatedAt = now)
        dao.upsertRadarScore(
            updated.copy(
                chord = updated.chord.coerceIn(0, 100),
                rhythm = updated.rhythm.coerceIn(0, 100),
                fingering = updated.fingering.coerceIn(0, 100),
                repertoire = updated.repertoire.coerceIn(0, 100),
                theory = updated.theory.coerceIn(0, 100),
                consistency = updated.consistency.coerceIn(0, 100),
            ),
        )
    }

    /** 契约第九节：完成节点时按节点归属加分（上限 100，由 updateRadar 钳制）。 */
    private fun radarDeltaForNode(nodeNumber: Int): RadarScoreEntity.() -> RadarScoreEntity = {
        when (nodeNumber) {
            in CHORD_NODES -> copy(chord = chord + 3)
            in RHYTHM_NODES -> copy(rhythm = rhythm + 3)
            in FINGERING_NODES -> copy(fingering = fingering + 3)
            in REPERTOIRE_NODES -> copy(repertoire = repertoire + 3)
            in THEORY_NODES -> copy(theory = theory + 3)
            in CONSISTENCY_NODES -> copy(consistency = consistency + 2)
            else -> copy(consistency = consistency + 1)
        }
    }

    private fun nodeNumberOf(nodeId: String): Int =
        nodeId.removePrefix("node_").toIntOrNull() ?: -1

    // ---- Entity → 领域模型 ----

    private fun PathNodeEntity.toDomain() = PathNode(
        id = id,
        stage = stage,
        orderIndex = orderIndex,
        title = title,
        why = why,
        steps = converters.toStringList(stepsJson),
        criteria = criteria,
        minutes = minutes,
        tools = converters.toNodeToolList(toolsJson),
        theoryCardIds = converters.toStringList(theoryCardIdsJson),
        resourceTags = converters.toStringList(resourceTagsJson),
        unlockCondition = unlockCondition,
        isCheckpoint = isCheckpoint,
        remedialNodeId = remedialNodeId,
    )

    private fun ChordEntity.toDomain() = Chord(
        id = id,
        label = label,
        difficulty = difficulty,
        strings = converters.toIntList(stringsJson),
        fingers = converters.toIntList(fingersJson),
        steps = converters.toStringList(stepsJson),
        mistakes = converters.toStringList(mistakesJson),
        next = nextChord,
        nodeIds = converters.toIntList(nodeIdsJson),
        barre = barre,
    )

    private fun TheoryCardEntity.toDomain() = TheoryCard(
        id = id,
        title = title,
        category = category,
        content = content,
        quizQuestion = quizQuestion,
        quizOptions = converters.toStringList(quizOptionsJson),
        quizAnswer = quizAnswer,
        tags = converters.toStringList(tagsJson),
        nodeIds = converters.toIntList(nodeIdsJson),
    )

    private fun ResourceLinkEntity.toDomain() = ResourceLink(
        id = id,
        nodeId = nodeId,
        platform = platform,
        title = title,
        author = author,
        authorUrl = authorUrl,
        url = url,
        durationSec = durationSec,
        difficulty = difficulty,
        tags = converters.toStringList(tagsJson),
        sourceNote = sourceNote,
        reportedBroken = reportedBroken,
        active = active,
        usefulCount = usefulCount,
    )

    private fun UserNodeProgressEntity.toDomain() = NodeProgress(
        nodeId = nodeId,
        status = runCatching { NodeStatus.valueOf(status) }.getOrDefault(NodeStatus.LOCKED),
        selfRating = selfRating?.let { runCatching { SelfRating.valueOf(it) }.getOrNull() },
        startedAt = startedAt,
        completedAt = completedAt,
        attempts = attempts,
        totalMinutes = totalMinutes,
    )

    private fun PracticeRecordEntity.toDomain() = PracticeRecord(
        id = id,
        nodeId = nodeId,
        durationMin = durationMin,
        rating = rating?.let { runCatching { SelfRating.valueOf(it) }.getOrNull() },
        note = note,
        recordedAt = recordedAt,
        source = source,
    )

    private fun RadarScoreEntity.toDomain() = RadarScore(
        chord = chord,
        rhythm = rhythm,
        fingering = fingering,
        repertoire = repertoire,
        theory = theory,
        consistency = consistency,
    )

    private fun WorkEntity.toDomain() = WorkItem(
        id = id,
        title = title,
        mediaPath = mediaPath,
        durationSec = durationSec,
        linkedNodeId = linkedNodeId,
        createdAt = createdAt,
        note = note,
    )

    private fun UserResourceEntity.toDomain() = UserResourceItem(
        id = id,
        nodeId = nodeId,
        title = title,
        url = url,
        platform = platform,
        createdAt = createdAt,
    )

    companion object {
        const val DEFAULT_PATH_ID = "path_sing_zero_3h"
        const val STREAK_ACHIEVEMENT_ID = "streak_7"
        const val STREAK_ACHIEVEMENT_DAYS = 7

        // 契约第九节：完成节点时的雷达加分归属
        private val CHORD_NODES = setOf(3, 4, 5, 8, 9, 10, 17, 18)
        private val RHYTHM_NODES = setOf(6, 12, 13, 14, 26)
        private val FINGERING_NODES = setOf(15, 16, 25)
        private val REPERTOIRE_NODES = setOf(19, 20, 21, 22, 23, 27, 28)
        private val THEORY_NODES = setOf(2, 11)
        private val CONSISTENCY_NODES = setOf(1, 7, 24, 29, 30)
    }
}
