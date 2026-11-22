package com.chiu.guitarroad.data.seed

import android.content.Context
import com.chiu.guitarroad.data.local.AchievementEntity
import com.chiu.guitarroad.data.local.ChordEntity
import com.chiu.guitarroad.data.local.GuitarDao
import com.chiu.guitarroad.data.local.PathNodeEntity
import com.chiu.guitarroad.data.local.PathTemplateEntity
import com.chiu.guitarroad.data.local.ResourceLinkEntity
import com.chiu.guitarroad.data.local.TheoryCardEntity
import com.chiu.guitarroad.data.settings.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * 首次启动时把 assets JSON 写入 Room 静态表。
 *
 * 写入顺序：模板 → 节点 → 和弦 → 乐理卡 → 成就 → 资源外链；全部 insert
 * 均用 OnConflictStrategy.REPLACE，因此中途失败后下次启动可安全重跑；
 * 只有全部写完才把 DataStore 的 seeded_version 置为 1。
 * （DAO 按契约只含固定方法、无 @Transaction 包装方法，故这里按顺序
 * 写入而不是包在一个 Room 事务里；幂等写入 + 最后置版本位保证一致性。）
 */
class AssetSeeder(
    private val context: Context,
    private val dao: GuitarDao,
    private val settings: SettingsStore,
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfNeeded() {
        if (settings.seededVersion.first() == SEEDED_VERSION) return

        val paths = readAsset<PathsFile>("paths.json")
        val chords = readAssetList<ChordJson>("chords.json")
        val cards = readAssetList<TheoryCardJson>("theory_cards.json")
        val achievements = readAssetList<AchievementJson>("achievements.json")
        val resources = readAssetList<ResourceLinkJson>("resources.json")

        dao.insertTemplates(listOf(paths.template.toEntity()))
        dao.insertNodes(paths.nodes.map { it.toEntity(paths.template.id) })
        dao.insertChords(chords.map { it.toEntity() })
        dao.insertTheoryCards(cards.map { it.toEntity() })
        dao.insertAchievements(achievements.map { it.toEntity() })
        dao.insertResourceLinks(resources.map { it.toEntity() })

        settings.setSeededVersion(SEEDED_VERSION)
    }

    private inline fun <reified T> readAsset(fileName: String): T {
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return json.decodeFromString<T>(text)
    }

    private inline fun <reified T> readAssetList(fileName: String): List<T> {
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return json.decodeFromString<List<T>>(text)
    }

    private fun PathTemplateJson.toEntity() = PathTemplateEntity(
        id = id, name = name, description = description, goal = goal, level = level,
        hours = hours, totalNodes = totalNodes, estimatedWeeks = estimatedWeeks, version = version,
    )

    private fun PathNodeJson.toEntity(pathId: String) = PathNodeEntity(
        id = id, pathId = pathId, stage = stage, orderIndex = orderIndex, title = title,
        why = why,
        stepsJson = json.encodeToString(ListSerializer(String.serializer()), steps),
        criteria = criteria, minutes = minutes,
        // tools 按资产原样编码，保留 node_13 的 "progressive" 键
        toolsJson = json.encodeToString(ListSerializer(NodeToolJson.serializer()), tools),
        theoryCardIdsJson = json.encodeToString(ListSerializer(String.serializer()), theoryCardIds),
        resourceTagsJson = json.encodeToString(ListSerializer(String.serializer()), resourceTags),
        unlockCondition = unlockCondition, isCheckpoint = isCheckpoint,
        remedialNodeId = remedialNodeId,
    )

    private fun ChordJson.toEntity() = ChordEntity(
        id = id, label = label, difficulty = difficulty,
        stringsJson = json.encodeToString(ListSerializer(Int.serializer()), strings),
        fingersJson = json.encodeToString(ListSerializer(Int.serializer()), fingers),
        stepsJson = json.encodeToString(ListSerializer(String.serializer()), steps),
        mistakesJson = json.encodeToString(ListSerializer(String.serializer()), mistakes),
        nextChord = next,
        nodeIdsJson = json.encodeToString(ListSerializer(Int.serializer()), nodeIds),
        barre = barre,
    )

    private fun TheoryCardJson.toEntity() = TheoryCardEntity(
        id = id, title = title, category = category, content = content,
        quizQuestion = quizQuestion,
        quizOptionsJson = json.encodeToString(ListSerializer(String.serializer()), quizOptions),
        quizAnswer = quizAnswer,
        tagsJson = json.encodeToString(ListSerializer(String.serializer()), tags),
        nodeIdsJson = json.encodeToString(ListSerializer(Int.serializer()), nodeIds),
    )

    private fun AchievementJson.toEntity() = AchievementEntity(
        id = id, name = name,
        conditionJson = json.encodeToString(AchievementConditionJson.serializer(), condition),
        iconRes = icon,
    )

    private fun ResourceLinkJson.toEntity() = ResourceLinkEntity(
        id = id, nodeId = nodeId, platform = platform, title = title, author = author,
        authorUrl = authorUrl, url = url, durationSec = durationSec, difficulty = difficulty,
        tagsJson = json.encodeToString(ListSerializer(String.serializer()), tags),
        sourceNote = sourceNote, reportedBroken = reportedBroken,
        active = active, usefulCount = usefulCount,
    )

    companion object {
        const val SEEDED_VERSION = 1
    }
}
