package com.chiu.guitarroad.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiu.guitarroad.audio.PitchResult
import com.chiu.guitarroad.ui.theme.Amber
import com.chiu.guitarroad.ui.theme.BrickRed
import com.chiu.guitarroad.ui.theme.DisabledGray
import com.chiu.guitarroad.ui.theme.InkPrimary
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.LeafGreen
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.cos

/**
 * 调音圆盘：直径 240dp，外圈 -50~+50 cents 刻度，中心大音名 + cents 数。
 * 指针角度 = cents（钳制 ±50）× 1.2°，spring 动画。
 */
@Composable
fun TunerDial(result: PitchResult, modifier: Modifier = Modifier) {
    val targetAngle = if (result.isSilent) 0f
    else result.cents.coerceIn(-50f, 50f) * 1.2f
    val angle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "tuner_needle"
    )
    val accuracyColor: Color = when {
        result.isSilent -> DisabledGray
        abs(result.cents) <= 5f -> LeafGreen
        abs(result.cents) <= 15f -> Amber
        abs(result.cents) <= 30f -> Amber
        else -> BrickRed
    }

    val textMeasurer = rememberTextMeasurer()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = modifier.then(Modifier.size(240.dp))) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = minOf(size.width, size.height) / 2f

                // 外圈刻度：-50~+50 cents，每 10 一小格，0 位加粗
                for (cents in -50..50 step 10) {
                    val deg = Math.toRadians((cents * 1.2f).toDouble())
                    val dir = Offset(sin(deg).toFloat(), -cos(deg).toFloat())
                    val isZero = cents == 0
                    val outer = center + dir * (radius - 2.dp.toPx())
                    val inner = center + dir * (radius - if (isZero) 20.dp.toPx() else 14.dp.toPx())
                    drawLine(
                        color = if (isZero) InkPrimary else InkSecondary,
                        start = outer,
                        end = inner,
                        strokeWidth = if (isZero) 3f else 1.5f
                    )
                }

                // 指针：细长三角形，绕中心旋转
                rotate(degrees = angle, pivot = center) {
                    val needlePath = Path().apply {
                        val halfBase = 5.dp.toPx()
                        val tipY = center.y - radius * 0.62f
                        moveTo(center.x, tipY)
                        lineTo(center.x + halfBase, center.y + 8.dp.toPx())
                        lineTo(center.x - halfBase, center.y + 8.dp.toPx())
                        close()
                    }
                    drawPath(path = needlePath, color = accuracyColor)
                }
                drawCircle(color = accuracyColor, radius = 6.dp.toPx(), center = center)

                // 中心大音名（音名 32sp Bold + 下标八度）与 cents 数
                val noteText = if (result.isSilent) {
                    AnnotatedString("—", SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = InkSecondary))
                } else {
                    AnnotatedString.Builder().apply {
                        withStyle(SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = InkPrimary)) {
                            append(result.noteName)
                        }
                        withStyle(
                            SpanStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary,
                                baselineShift = androidx.compose.ui.text.style.BaselineShift.Subscript
                            )
                        ) {
                            append(result.octave.toString())
                        }
                    }.toAnnotatedString()
                }
                val noteLayout = textMeasurer.measure(noteText)
                drawText(
                    textMeasurer = textMeasurer,
                    text = noteText,
                    topLeft = Offset(
                        center.x - noteLayout.size.width / 2f,
                        center.y - 52.dp.toPx() - noteLayout.size.height / 2f
                    )
                )

                val centsText = if (result.isSilent) {
                    "—"
                } else {
                    val c = result.cents.roundToInt()
                    "${if (c >= 0) "+" else ""}$c cents"
                }
                val centsStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (result.isSilent) InkSecondary else accuracyColor
                )
                val centsLayout = textMeasurer.measure(centsText, centsStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = centsText,
                    topLeft = Offset(
                        center.x - centsLayout.size.width / 2f,
                        center.y + 22.dp.toPx() - centsLayout.size.height / 2f
                    ),
                    style = centsStyle
                )
            }
        }
        if (result.isSilent) {
            Text(
                text = "请拨响一根弦",
                fontSize = 14.sp,
                color = InkSecondary
            )
        }
    }
}
