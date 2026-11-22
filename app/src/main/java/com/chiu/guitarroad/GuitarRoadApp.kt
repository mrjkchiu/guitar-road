package com.chiu.guitarroad

import android.app.Application
import com.chiu.guitarroad.di.AppContainer

class GuitarRoadApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
