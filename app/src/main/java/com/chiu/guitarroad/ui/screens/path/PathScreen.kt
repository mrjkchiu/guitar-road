package com.chiu.guitarroad.ui.screens.path

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.data.model.NodeStatus
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.SectionCard
import com.chiu.guitarroad.ui.components.SectionTitle
import com.chiu.guitarroad.ui.theme.Amber
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.DisabledGray
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LeafGreen
import com.chiu.guitarroad.ui.theme.LineDivider
import com.chiu.guitarroad.ui.theme.Wood

@Composable
fun PathScreen(navController: NavController, repo: GuitarRepository) {
    val vm: PathViewModel = viewModel(factory = viewModelFactory { initializer { PathViewModel(repo) } })
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = state.pathName, fontSize = 22.sp, color = InkPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "已完成 ${state.totalDone} / ${state.totalNodes}",
            fontSize = 14.sp,
            color = InkSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        state.stages.forEach { stage ->
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "阶段 ${stage.info.number} · ${stage.info.title}",
                        fontSize = 18.sp,
                        color = InkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = when (stage.status) {
                            StageStatus.DONE -> "完成"
                            StageStatus.IN_PROGRESS -> "进行中"
                            StageStatus.LOCKED -> "未解锁"
                        },
                        fontSize = 14.sp,
                        color = when (stage.status) {
                            StageStatus.DONE -> LeafGreen
                            StageStatus.IN_PROGRESS -> Amber
                            StageStatus.LOCKED -> DisabledGray
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stage.info.weeks, fontSize = 14.sp, color = InkSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "里程碑：${stage.info.milestone}", fontSize = 14.sp, color = InkSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = {
                        if (stage.totalCount == 0) 0f
                        else stage.doneCount.toFloat() / stage.totalCount
                    },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = when (stage.status) {
                        StageStatus.DONE -> Wood
                        StageStatus.IN_PROGRESS -> Amber
                        StageStatus.LOCKED -> DisabledGray
                    },
                    trackColor = LineDivider
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stage.doneCount} / ${stage.totalCount}",
                    fontSize = 14.sp,
                    color = InkSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))
                stage.nodes.forEach { node ->
                    val nodeStatus = state.progress[node.id]?.status ?: NodeStatus.LOCKED
                    val clickable = nodeStatus != NodeStatus.LOCKED
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .then(
                                if (clickable) Modifier.clickable {
                                    navController.navigate("task/${node.id}")
                                } else Modifier
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .then(
                                    if (nodeStatus == NodeStatus.IN_PROGRESS) {
                                        Modifier.shadow(6.dp, CircleShape)
                                    } else Modifier
                                )
                                .clip(CircleShape)
                                .background(
                                    when (nodeStatus) {
                                        NodeStatus.DONE -> LeafGreen
                                        NodeStatus.IN_PROGRESS -> Wood
                                        NodeStatus.AVAILABLE -> Wood
                                        NodeStatus.LOCKED -> CreamBg
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (nodeStatus) {
                                    NodeStatus.DONE -> "✓"
                                    NodeStatus.LOCKED -> ""
                                    else -> "●"
                                },
                                fontSize = 14.sp,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = node.title,
                            fontSize = 16.sp,
                            color = if (clickable) InkPrimary else DisabledGray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${node.minutes} 分钟",
                            fontSize = 14.sp,
                            color = if (clickable) InkSecondary else DisabledGray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
