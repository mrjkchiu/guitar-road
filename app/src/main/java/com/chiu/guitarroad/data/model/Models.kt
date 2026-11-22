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
