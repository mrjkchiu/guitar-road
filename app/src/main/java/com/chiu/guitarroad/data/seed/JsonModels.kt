package com.chiu.guitarroad.data.seed

import kotlinx.serialization.Serializable

/**
 * assets JSON 的解析模型，字段名与 assets JSON 保持一致。
 * 注意：和弦 JSON 保留 "name" 字段（文档第三节原文），领域模型 Chord
 * 与 ChordEntity 按契约不含 name，种子写入时丢弃该字段。
 * 节点 tools 保留 "progressive" 字段（node_13 渐进加速模式），写入
 * path_node.toolsJson 时原样保留；领域模型 NodeTool 解码时忽略该键。
 */

@Serializable
data class PathsFile(
    val template: PathTemplateJson,
    val stages: List<StageInfoJson>,
    val nodes: List<PathNodeJson>,
)

@Serializable
data class PathTemplateJson(
    val id: String,
    val name: String,
    val description: String,
    val goal: String,
    val level: String,
    val hours: Int,
    val totalNodes: Int,
    val estimatedWeeks: Int,
    val version: Int,
)

@Serializable
data class StageInfoJson(
    val number: Int,
    val title: String,
    val weeks: String,
    val milestone: String,
)

@Serializable
data class NodeToolJson(
    val type: String,
    val target: String? = null,
    val bpm: Int? = null,
    val progressive: Boolean? = null,
    val trigger: String? = null,
)

@Serializable
data class PathNodeJson(
    val id: String,
    val stage: Int,
    val orderIndex: Int,
    val title: String,
    val why: String,
    val steps: List<String>,
    val criteria: String,
    val minutes: Int,
    val tools: List<NodeToolJson> = emptyList(),
    val theoryCardIds: List<String> = emptyList(),
    val resourceTags: List<String> = emptyList(),
    val unlockCondition: String = "self_pass",
    val isCheckpoint: Boolean = false,
    val remedialNodeId: String? = null,
)

@Serializable
data class ChordJson(
    val id: String,
    val name: String,
    val label: String,
    val difficulty: String,
    val strings: List<Int>,
    val fingers: List<Int>,
    val steps: List<String>,
    val mistakes: List<String>,
    val next: String? = null,
    val nodeIds: List<Int> = emptyList(),
    val barre: Boolean = false,
)

@Serializable
data class TheoryCardJson(
    val id: String,
    val title: String,
    val category: String,
    val content: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizAnswer: Int,
    val tags: List<String> = emptyList(),
    val nodeIds: List<Int> = emptyList(),
)

@Serializable
data class AchievementConditionJson(
    val type: String,
    val days: Int? = null,
)

@Serializable
data class AchievementJson(
    val id: String,
    val name: String,
    val condition: AchievementConditionJson,
    val icon: String,
)

@Serializable
data class ResourceLinkJson(
    val id: Long = 0,
    val nodeId: String,
    val platform: String,
    val title: String,
    val author: String,
    val authorUrl: String,
    val url: String,
    val durationSec: Int,
    val difficulty: String,
    val tags: List<String> = emptyList(),
    val sourceNote: String = "",
    val reportedBroken: Int = 0,
    val active: Boolean = true,
    val usefulCount: Int = 0,
)
