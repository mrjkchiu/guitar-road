package com.chiu.guitarroad.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.chiu.guitarroad.di.AppContainer
import com.chiu.guitarroad.ui.screens.home.HomeScreen
import com.chiu.guitarroad.ui.screens.path.PathScreen
import com.chiu.guitarroad.ui.screens.practice.PracticeScreen
import com.chiu.guitarroad.ui.screens.profile.ProfileScreen
import com.chiu.guitarroad.ui.screens.tools.ToolsScreen
import com.chiu.guitarroad.ui.theme.CardWhite
import com.chiu.guitarroad.ui.theme.DarkOnSurfaceVariant
import com.chiu.guitarroad.ui.theme.DarkPrimary
import com.chiu.guitarroad.ui.theme.DarkSurface
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.Wood
import com.chiu.guitarroad.ui.theme.WoodDark
import com.chiu.guitarroad.ui.theme.WoodLight

/** 底部 5 Tab（顺序固定，契约第四节）。 */
enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("首页", Icons.Filled.Home),
    PATH("路径", Icons.Filled.Timeline),
    PRACTICE("练习", Icons.Filled.BarChart),
    TOOLS("工具", Icons.Filled.Build),
    PROFILE("我的", Icons.Filled.Person),
}

/**
 * 底部 5 Tab 宿主。Tab 切换用内部 state（不走嵌套导航），
 * 首页入口卡通过 onSwitchTab 切到对应 Tab（见 HomeScreen）。
 */
@Composable
fun MainScreen(navController: NavController, container: AppContainer) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val repo = container.repository
    // 底部导航用木色主题，不用 Material 默认紫（一个主色原则）。
    val darkTheme = isSystemInDarkTheme()
    val navContainer = if (darkTheme) DarkSurface else CardWhite
    val navSelected = if (darkTheme) DarkPrimary else Wood
    val navUnselected = if (darkTheme) DarkOnSurfaceVariant else InkSecondary
    val navIndicator = if (darkTheme) WoodDark else WoodLight
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = navContainer) {
                for (tab in MainTab.entries) {
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = navSelected,
                            selectedTextColor = navSelected,
                            unselectedIconColor = navUnselected,
                            unselectedTextColor = navUnselected,
                            indicatorColor = navIndicator
                        )
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // 切 Tab 时 200ms 淡入 + 轻微上滑（微交互预算内）。
            key(selectedTab) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                        animationSpec = tween(200),
                        initialOffsetY = { it / 20 }
                    )
                ) {
                    when (selectedTab) {
                        MainTab.HOME -> HomeScreen(
                            navController = navController,
                            repo = repo,
                            onSwitchTab = { selectedTab = it },
                        )
                        MainTab.PATH -> PathScreen(navController, repo)
                        MainTab.PRACTICE -> PracticeScreen(navController, repo)
                        MainTab.TOOLS -> ToolsScreen(navController, repo)
                        MainTab.PROFILE -> ProfileScreen(navController, repo)
                    }
                }
            }
        }
    }
}
