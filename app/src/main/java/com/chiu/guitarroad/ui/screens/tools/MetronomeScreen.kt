package com.chiu.guitarroad.ui.screens.tools

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.chiu.guitarroad.audio.MetronomeEngine
import com.chiu.guitarroad.audio.ProgressiveTempo
import com.chiu.guitarroad.data.repository.GuitarRepository
import com.chiu.guitarroad.ui.components.PrimaryButton
import com.chiu.guitarroad.ui.components.SecondaryButton
import com.chiu.guitarroad.ui.theme.CardVariant
import com.chiu.guitarroad.ui.theme.CreamBg
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.Wood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetronomeViewModel(
    private val repo: GuitarRepository,
    initialBpm: Int
) : ViewModel() {

    private val engine = MetronomeEngine()
    private val progressiveTempo = ProgressiveTempo()

    private val _bpm = MutableStateFlow(initialBpm.coerceIn(30, 240))
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _beatsPerBar = MutableStateFlow(4)
    val beatsPerBar: StateFlow<Int> = _beatsPerBar.asStateFlow()

    private val _currentBeat = MutableStateFlow(0)
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _progressiveEnabled = MutableStateFlow(false)
    val progressiveEnabled: StateFlow<Boolean> = _progressiveEnabled.asStateFlow()

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    private val tapTimes = mutableListOf<Long>()

    init {
        engine.bpm = _bpm.value
        engine.beatsPerBar = _beatsPerBar.value
        engine.onBeat = { beatIndex ->
            _currentBeat.value = beatIndex
        }
        engine.onBarComplete = {
            if (_progressiveEnabled.value) {
                val next = progressiveTempo.onBarComplete(_bpm.value)
                setBpm(next)
            }
        }
    }

    fun setBpm(value: Int) {
        val clamped = value.coerceIn(30, 240)
        _bpm.value = clamped
        engine.bpm = clamped
    }

    fun setBeatsPerBar(value: Int) {
        _beatsPerBar.value = value
        engine.beatsPerBar = value
        _currentBeat.value = 0
    }

    fun setProgressiveEnabled(enabled: Boolean) {
        _progressiveEnabled.value = enabled
        if (enabled) {
            progressiveTempo.reset()
        }
    }

    /** 点按打拍：连续 4 次以上取平均间隔算 BPM，钳制 30–240。 */
    fun tapTempo() {
        val now = SystemClock.elapsedRealtime()
        tapTimes.add(now)
        if (tapTimes.size > 8) {
            tapTimes.removeAt(0)
        }
        if (tapTimes.size >= 4) {
            val intervals = tapTimes.zipWithNext { a, b -> b - a }
            val avgMs = intervals.average()
            if (avgMs > 0) {
                val bpm = (60_000.0 / avgMs).toInt()
                setBpm(bpm)
            }
        }
    }

    fun toggleStartStop() {
        if (_isRunning.value) {
            engine.stop()
            _isRunning.value = false
            tapTimes.clear()
            _showRatingDialog.value = true
        } else {
            _currentBeat.value = 0
            engine.bpm = _bpm.value
            engine.beatsPerBar = _beatsPerBar.value
            engine.start()
            _isRunning.value = true
        }
    }

    fun stopEngine() {
        if (_isRunning.value) {
            engine.stop()
            _isRunning.value = false
        }
    }

    /** 自评“稳”：节奏 +2。 */
    fun rateSteady() {
        _showRatingDialog.value = false
        viewModelScope.launch {
            repo.addRhythmPoint(2)
        }
    }

    fun dismissRatingDialog() {
        _showRatingDialog.value = false
    }

    override fun onCleared() {
        engine.stop()
        super.onCleared()
    }
}

@Composable
fun MetronomeScreen(nodeId: String, bpm: Int, navController: NavController, repo: GuitarRepository) {
    val vm: MetronomeViewModel = viewModel(
        factory = viewModelFactory { initializer { MetronomeViewModel(repo, bpm) } }
    )
    val currentBpm by vm.bpm.collectAsState()
    val beatsPerBar by vm.beatsPerBar.collectAsState()
    val currentBeat by vm.currentBeat.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val progressiveEnabled by vm.progressiveEnabled.collectAsState()
    val showRatingDialog by vm.showRatingDialog.collectAsState()

    DisposableEffect(Unit) {
        onDispose { vm.stopEngine() }
    }

    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissRatingDialog() },
            title = { Text(text = "刚才跟得怎么样？", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { vm.rateSteady() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "稳", fontSize = 16.sp) }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.dismissRatingDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "抢拍", fontSize = 16.sp) }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.dismissRatingDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "拖拍", fontSize = 16.sp) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { vm.dismissRatingDialog() }) {
                    Text(text = "跳过", fontSize = 16.sp)
                }
            }
        )
    }

    val nodeNumber = nodeId.removePrefix("node_").toIntOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "节拍器",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            BeatsPerBarDropdown(beatsPerBar = beatsPerBar, onSelected = { vm.setBeatsPerBar(it) })
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 节拍方块：按拍号个数，当前拍木色填充放大 1.15 倍
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (i in 0 until beatsPerBar) {
                val isCurrent = isRunning && i == currentBeat
                val scale by animateFloatAsState(
                    targetValue = if (isCurrent) 1.15f else 1f,
                    animationSpec = tween(120),
                    label = "beat_$i"
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(scale)
                        .background(
                            color = if (isCurrent) Wood else CardVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${i + 1}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) androidx.compose.ui.graphics.Color.White else InkSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // BPM 大字 + [−] [+]
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { vm.setBpm(currentBpm - 1) },
                modifier = Modifier.size(48.dp)
            ) { Text(text = "−", fontSize = 20.sp) }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "$currentBpm BPM",
                fontSize = 48.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(
                onClick = { vm.setBpm(currentBpm + 1) },
                modifier = Modifier.size(48.dp)
            ) { Text(text = "+", fontSize = 20.sp) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentBpm.toFloat(),
            onValueChange = { vm.setBpm(it.toInt()) },
            valueRange = 30f..240f,
            steps = 209,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 渐进加速开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "渐进加速：每 4 小节 +5 BPM",
                fontSize = 16.sp,
                color = InkPrimary
            )
            Switch(
                checked = progressiveEnabled,
                onCheckedChange = { vm.setProgressiveEnabled(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryButton(
            text = "点按打拍",
            onClick = { vm.tapTempo() },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (isRunning) "停止" else "开始",
            onClick = { vm.toggleStartStop() },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (nodeId != "none" && nodeNumber != null) {
            SecondaryButton(
                text = "返回节点 $nodeNumber ←",
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BeatsPerBarDropdown(beatsPerBar: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = "$beatsPerBar/4 ▾", fontSize = 16.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (beats in listOf(4, 3)) {
                DropdownMenuItem(
                    text = { Text(text = "$beats/4", fontSize = 16.sp) },
                    onClick = {
                        expanded = false
                        onSelected(beats)
                    }
                )
            }
        }
    }
}
