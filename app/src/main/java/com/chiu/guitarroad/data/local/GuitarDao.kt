package com.chiu.guitarroad.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GuitarDao {
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
