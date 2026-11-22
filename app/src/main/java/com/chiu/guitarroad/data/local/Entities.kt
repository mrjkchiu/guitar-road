package com.chiu.guitarroad.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

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
