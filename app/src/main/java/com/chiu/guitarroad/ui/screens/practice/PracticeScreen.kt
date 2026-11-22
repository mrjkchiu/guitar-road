package com.chiu.guitarroad.ui.screens.practice

import androidx.compose.foundation.background
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
import com.chiu.guitarroad.ui.components.RadarChart
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.theme.Amber
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.DisabledGray
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.Wood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PracticeScreen(navController: NavController, repo: GuitarRepository) {
    val vm: PracticeViewModel = viewModel(factory = viewModelFactory { initializer { PracticeViewModel(repo) } })
    val state by vm.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    // 雷达全是 0 时给一句提示，避免只有一个点让人看不懂。
    val radarEmpty = state.radarScore.let {
        it.chord == 0 && it.rhythm == 0 && it.fingering == 0 &&
            it.repertoire == 0 && it.theory == 0 && it.consistency == 0
    }

    // 主按钮固定在底部拇指区（不再跟着滚动走，不会被切一半）。
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "我的成长", fontSize = 22.sp, color = InkPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(modifier = Modifier.fillMaxWidth()) {
                RadarChart(scores = state.radarScore, modifier = Modifier.fillMaxWidth())
                if (radarEmpty) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "练完第一课后这里会长出来",
                        fontSize = 14.sp,
                        color = InkSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(text = "本周练习时长")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "本周已练 ${state.weekMinutes} 分钟", fontSize = 18.sp, color = InkPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(text = "最近 7 天")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.last7Days.forEach { day ->
                        val practiced = state.practicedDates.contains(day)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (practiced) "▦" else "□",
                                fontSize = 22.sp,
                                color = if (practiced) Wood else DisabledGray
                            )
                            Text(
                                text = "${day.monthValue}/${day.dayOfMonth}",
                                fontSize = 12.sp,
                                color = InkSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (state.streakDays >= 1) {
                    Text(
                        text = "连续 ${state.streakDays} 天 🔥",
                        fontSize = 16.sp,
                        color = Amber
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(text = "最近记录")
                Spacer(modifier = Modifier.height(8.dp))
                if (state.recentRecords.isEmpty()) {
                    Text(
                        text = "还没有练习记录，从今天的第一课开始吧",
                        fontSize = 16.sp,
                        color = InkSecondary
                    )
                } else {
                    state.recentRecords.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(Date(record.recordedAt)),
                                fontSize = 14.sp,
                                color = InkSecondary
                            )
                            Text(
                                text = record.nodeId?.let { state.nodeTitles[it] } ?: "自由练习",
                                fontSize = 16.sp,
                                color = InkPrimary,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "${record.durationMin} 分钟",
                                fontSize = 14.sp,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }
        }

        PrimaryButton(
            text = "+ 记一次练习",
            onClick = { vm.showRecordSheet(true) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (state.showRecordSheet) {
        RecordPracticeSheet(
            nodes = state.allNodes,
            onDismiss = { vm.showRecordSheet(false) },
            onSave = { nodeId, durationMin, rating ->
                vm.recordPractice(nodeId, durationMin, rating)
            }
        )
    }
}
