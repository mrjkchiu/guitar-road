package com.chiu.guitarroad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        UserProfileEntity::class,
        PathTemplateEntity::class,
        PathNodeEntity::class,
        ChordEntity::class,
        TheoryCardEntity::class,
        ResourceLinkEntity::class,
        UserNodeProgressEntity::class,
        PracticeRecordEntity::class,
        RadarScoreEntity::class,
        WorkEntity::class,
        UserResourceEntity::class,
        AchievementEntity::class,
        UserAchievementEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GuitarDatabase : RoomDatabase() {
    abstract fun guitarDao(): GuitarDao
}
