package com.chiu.guitarroad.di

import android.content.Context
import androidx.room.Room
import com.chiu.guitarroad.data.local.GuitarDatabase
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.data.repository.GuitarRepositoryImpl
import com.chiu.guitarroad.data.settings.SettingsStore

/**
 * 手动依赖容器（无 DI 框架）：单例懒加载。
 * MainActivity 从 application 取到 container 后传给导航图。
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settings: SettingsStore by lazy { SettingsStore(appContext) }

    val db: GuitarDatabase by lazy {
        Room.databaseBuilder(appContext, GuitarDatabase::class.java, "guitar_road.db").build()
    }

    val repository: GuitarRepository by lazy {
        GuitarRepositoryImpl(appContext, db, settings)
    }
}
