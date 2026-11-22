package com.chiu.guitarroad.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.PrimaryButton
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.navigation.MainTab
import com.chiu.guitarroad.ui.theme.Amber
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LineDivider
import com.chiu.guitarroad.ui.theme.Wood

@Composable
fun HomeScreen(
    navController: NavController,
    repo: GuitarRepository,
    onSwitchTab: (MainTab) -> Unit = {},
) {
    val vm: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(repo) } })
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "你好，吉他手", fontSize = 22.sp, color = InkPrimary)
            if (state.streakDays >= 1) {
                Text(
                    text = "连续 ${state.streakDays} 天 🔥",
                    fontSize = 14.sp,
                    color = Amber
                )
            }
        }
        Text(
            text = "今天也要练琴哦",
            fontSize = 14.sp,
            color = InkSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 今日任务
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "今日任务")
            Spacer(modifier = Modifier.height(8.dp))
            when {
                state.allDone -> {
                    Text(text = "30 个节点全部完成 🎉", fontSize = 18.sp, color = InkPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "你已经走完了零基础弹唱之路，去看看你的作品档案吧", fontSize = 14.sp, color = InkSecondary)
                }
                !state.hasStarted -> {
                    Text(text = "还没有练习记录，从今天的第一课开始吧", fontSize = 16.sp, color = InkPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    state.currentNode?.let { node ->
                        PrimaryButton(
                            text = "开始今日练习 →",
                            onClick = { navController.navigate("task/${node.id}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                state.currentNode != null -> {
                    val node = state.currentNode!!
                    Text(text = node.title, fontSize = 18.sp, color = InkPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = node.why, fontSize = 14.sp, color = InkSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "预计 ${node.minutes} 分钟", fontSize = 14.sp, color = InkSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "开始今日练习 →",
                        onClick = { navController.navigate("task/${node.id}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    Text(text = "今天没有待练的任务", fontSize = 16.sp, color = InkSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 本周任务进度
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionTitle(text = "本周任务进度")
                Text(text = "${state.weekDoneCount} / 7", fontSize = 14.sp, color = InkSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (state.weekDoneCount.coerceAtMost(7)) / 7f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Wood,
                trackColor = LineDivider
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 本周练习时长
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            SectionTitle(text = "本周练习时长")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "本周已练 ${state.weekMinutes} 分钟", fontSize = 18.sp, color = InkPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 下一个里程碑
        state.currentStage?.let { stage ->
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(text = "下一个里程碑")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stage.milestone, fontSize = 16.sp, color = InkPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "还剩 ${state.milestoneRemaining} 个任务",
                    fontSize = 14.sp,
                    color = InkSecondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 次级入口
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard(
                modifier = Modifier.weight(1f).clickable {
                    onSwitchTab(MainTab.PRACTICE)
                }
            ) {
                Text(text = "能力雷达图", fontSize = 16.sp, color = InkPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "去练习页查看 →", fontSize = 14.sp, color = InkSecondary)
            }
            SectionCard(
                modifier = Modifier.weight(1f).clickable {
                    onSwitchTab(MainTab.PROFILE)
                }
            ) {
                Text(text = "作品档案", fontSize = 16.sp, color = InkPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "去我的页查看 →", fontSize = 14.sp, color = InkSecondary)
            }
        }
    }
}
