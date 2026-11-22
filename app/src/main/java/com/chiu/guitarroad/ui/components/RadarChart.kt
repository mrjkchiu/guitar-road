package com.chiu.guitarroad.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiu.guitarroad.data.model.RadarScore
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LineDivider
import com.chiu.guitarroad.ui.theme.Wood
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 六边形雷达图。6 轴标签从顶部顺时针：和弦 / 节奏 / 指法 / 曲目 / 乐理 / 稳定度。
 */
@Composable
fun RadarChart(scores: RadarScore, modifier: Modifier = Modifier) {
    val chord by animateFloatAsState(
        targetValue = scores.chord.toFloat(),
        animationSpec = tween(600),
        label = "radar_chord"
    )
    val rhythm by animateFloatAsState(
        targetValue = scores.rhythm.toFloat(),
        animationSpec = tween(600),
        label = "radar_rhythm"
    )
    val fingering by animateFloatAsState(
        targetValue = scores.fingering.toFloat(),
        animationSpec = tween(600),
        label = "radar_fingering"
    )
    val repertoire by animateFloatAsState(
        targetValue = scores.repertoire.toFloat(),
        animationSpec = tween(600),
        label = "radar_repertoire"
    )
    val theory by animateFloatAsState(
        targetValue = scores.theory.toFloat(),
        animationSpec = tween(600),
        label = "radar_theory"
    )
    val consistency by animateFloatAsState(
        targetValue = scores.consistency.toFloat(),
        animationSpec = tween(600),
        label = "radar_consistency"
    )

    val textMeasurer = rememberTextMeasurer()
    val labels = listOf("和弦", "节奏", "指法", "曲目", "乐理", "稳定度")
    val values = listOf(chord, rhythm, fingering, repertoire, theory, consistency)

    Canvas(modifier = modifier.then(Modifier.size(280.dp))) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = min(size.width, size.height) / 2f - 28.dp.toPx()
        val labelStyle = TextStyle(fontSize = 12.sp, color = InkSecondary)

        fun pointAt(axis: Int, radius: Float): Offset {
            // 顶部起顺时针：-90° + i*60°
            val angleRad = Math.toRadians((-90 + axis * 60).toDouble())
            return Offset(
                center.x + radius * cos(angleRad).toFloat(),
                center.y + radius * sin(angleRad).toFloat()
            )
        }

        fun hexagonPath(radius: Float): Path {
            val path = Path()
            for (i in 0 until 6) {
                val p = pointAt(i, radius)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            return path
        }

        // 网格：3 层六边形 25 / 50 / 100
        for (fraction in listOf(0.25f, 0.5f, 1f)) {
            drawPath(
                path = hexagonPath(maxRadius * fraction),
                color = LineDivider,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
        // 轴线
        for (i in 0 until 6) {
            drawLine(
                color = LineDivider,
                start = center,
                end = pointAt(i, maxRadius),
                strokeWidth = 1f
            )
        }

        // 数据区：木色半透明填充 + 木色描边
        val dataPath = Path()
        val dataPoints = values.mapIndexed { i, v ->
            pointAt(i, maxRadius * (v / 100f).coerceIn(0f, 1f))
        }
        dataPoints.forEachIndexed { i, p ->
            if (i == 0) dataPath.moveTo(p.x, p.y) else dataPath.lineTo(p.x, p.y)
        }
        dataPath.close()
        drawPath(path = dataPath, color = Wood.copy(alpha = 0.25f))
        drawPath(
            path = dataPath,
            color = Wood,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
        for (p in dataPoints) {
            drawCircle(color = Wood, radius = 3.dp.toPx(), center = p)
        }

        // 轴标签
        for (i in 0 until 6) {
            val labelPoint = pointAt(i, maxRadius + 18.dp.toPx())
            val layout = textMeasurer.measure(labels[i], labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = labels[i],
                topLeft = Offset(
                    labelPoint.x - layout.size.width / 2f,
                    labelPoint.y - layout.size.height / 2f
                ),
                style = labelStyle
            )
        }
    }
}
