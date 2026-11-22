package com.chiu.guitarroad.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.chiu.guitarroad.ui.theme.CardWhite
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LineDivider
import com.chiu.guitarroad.ui.theme.Wood
import com.chiu.guitarroad.ui.theme.WoodLight

@Composable
fun OnboardingScreen(navController: NavController, repo: GuitarRepository) {
    val vm: OnboardingViewModel = viewModel(factory = viewModelFactory { initializer { OnboardingViewModel(repo) } })
    val step by vm.step.collectAsState()
    val selectedGoal by vm.selectedGoal.collectAsState()
    val selectedLevel by vm.selectedLevel.collectAsState()
    val selectedHours by vm.selectedHours.collectAsState()
    val isGenerating by vm.isGenerating.collectAsState()
    val navigateToMain by vm.navigateToMain.collectAsState()

    LaunchedEffect(navigateToMain) {
        if (navigateToMain) {
            navController.navigate("main") {
                popUpTo("onboarding") { inclusive = true }
            }
        }
    }

    if (isGenerating) {
        Box(
            modifier = Modifier.fillMaxSize().background(CreamBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Wood)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "正在生成你的路径…", fontSize = 18.sp, color = InkPrimary)
            }
        }
        return
    }

    val question = when (step) {
        1 -> "你想先用吉他做什么？"
        2 -> "你现在是什么水平？"
        else -> "每周能练多久？"
    }
    val options: List<OnboardingOption> = when (step) {
        1 -> vm.goalOptions
        2 -> vm.levelOptions
        else -> vm.hoursOptions
    }
    val selected: String? = when (step) {
        1 -> selectedGoal
        2 -> selectedLevel
        else -> selectedHours?.toString()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(CreamBg).padding(24.dp)
    ) {
        Text(text = "3 个问题，找到你的起点", fontSize = 22.sp, color = InkPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "第 $step / 3 题", fontSize = 14.sp, color = InkSecondary)
            Text(text = "$step / 3", fontSize = 14.sp, color = InkSecondary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { step / 3f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Wood,
            trackColor = LineDivider
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = question, fontSize = 18.sp, color = InkPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { option ->
            val isSelected = selected == option.value
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        when (step) {
                            1 -> vm.selectGoal(option.value)
                            2 -> vm.selectLevel(option.value)
                            else -> vm.selectHours(option.value.toInt())
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                // 选中态用木色浅底（一个主色原则），不用绿色——绿色只留给成功状态。
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) WoodLight else CardWhite
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Wood else LineDivider
                )
            ) {
                Text(
                    text = option.label,
                    fontSize = 16.sp,
                    color = InkPrimary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (step > 1) {
            TextButton(onClick = { vm.goBack() }) {
                Text(text = "← 上一题", fontSize = 16.sp, color = Wood)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        PrimaryButton(
            text = if (step == 3) "生成我的路径 →" else "下一步 →",
            onClick = { vm.next() },
            enabled = vm.canGoNext(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
