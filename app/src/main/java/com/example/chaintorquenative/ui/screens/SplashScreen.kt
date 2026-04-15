package com.example.chaintorquenative.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaintorquenative.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Gear-specific colors (not in AppColors — unique to this screen)
private val GearColor1 = Color(0xFF7DD3FC)  // Sky-300
private val GearColor2 = Color(0xFFA78BFA)  // Violet-400
private val GearShine  = Color(0xFFE0E7FF)  // Indigo-100

@Composable
fun AnimatedSplashScreen(onSplashComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 900, easing = EaseOutBack),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "logoAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 500),
        label = "textAlpha"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 800),
        label = "subtitleAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gears")
    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "gearRotation"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "particleProgress"
    )

    val particles = remember {
        List(25) {
            Particle(
                x = Random.nextFloat(), y = Random.nextFloat(),
                size = Random.nextFloat() * 5 + 1.5f,
                speed = Random.nextFloat() * 0.003f + 0.001f,
                alpha = Random.nextFloat() * 0.4f + 0.1f
            )
        }
    }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.GradientEnd,
                        Color(0xFF1E1B4B),
                        Color(0xFF172554),
                        AppColors.GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val animY = (p.y + particleProgress * p.speed * 200) % 1f
                drawCircle(
                    color = AppColors.Primary.copy(alpha = p.alpha),
                    radius = p.size.dp.toPx(),
                    center = Offset(p.x * size.width, animY * size.height)
                )
            }
        }

        // Ambient glow
        Canvas(modifier = Modifier.size(280.dp).alpha(pulseAlpha)) {
            drawCircle(
                brush = Brush.radialGradient(listOf(AppColors.Primary.copy(alpha = 0.5f), Color.Transparent)),
                radius = size.minDimension / 2
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            // Interlocking gears
            Box(
                modifier = Modifier.size(180.dp).scale(logoScale).alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val largeR = size.minDimension * 0.30f
                    val largeCx = cx - largeR * 0.35f
                    val largeCy = cy + largeR * 0.15f

                    rotate(gearRotation, pivot = Offset(largeCx, largeCy)) {
                        drawGear(Offset(largeCx, largeCy), largeR, largeR * 0.72f, largeR * 0.22f, 10, GearColor1, GearShine)
                    }
                    val smallR = largeR * 0.62f
                    val meshDist = largeR + smallR - largeR * 0.12f
                    val meshAngle = -35f * (PI.toFloat() / 180f)
                    val smallCx = largeCx + meshDist * cos(meshAngle)
                    val smallCy = largeCy + meshDist * sin(meshAngle)
                    val counterRot = -(gearRotation * 10f / 7f) + 8f

                    rotate(counterRot, pivot = Offset(smallCx, smallCy)) {
                        drawGear(Offset(smallCx, smallCy), smallR, smallR * 0.70f, smallR * 0.24f, 7, GearColor2, GearShine)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text("ChainTorque", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.alpha(textAlpha))
            Spacer(modifier = Modifier.height(8.dp))
            Text("3D CAD Marketplace", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = AppColors.Primary, modifier = Modifier.alpha(subtitleAlpha))
            Spacer(modifier = Modifier.height(48.dp))
            LoadingDots(modifier = Modifier.alpha(subtitleAlpha))
        }
    }
}

private fun DrawScope.drawGear(center: Offset, outerRadius: Float, innerRadius: Float, holeRadius: Float, teeth: Int, gearColor: Color, shineColor: Color) {
    val path = Path()
    val toothAngle = (2 * PI / teeth).toFloat()
    val halfTooth = toothAngle * 0.22f
    val halfGap   = toothAngle * 0.28f

    for (i in 0 until teeth) {
        val base = i * toothAngle
        val r1x = center.x + innerRadius * cos(base - halfGap)
        val r1y = center.y + innerRadius * sin(base - halfGap)
        val t1x = center.x + outerRadius * cos(base - halfTooth)
        val t1y = center.y + outerRadius * sin(base - halfTooth)
        val t2x = center.x + outerRadius * cos(base + halfTooth)
        val t2y = center.y + outerRadius * sin(base + halfTooth)
        val r2x = center.x + innerRadius * cos(base + halfGap)
        val r2y = center.y + innerRadius * sin(base + halfGap)
        if (i == 0) path.moveTo(r1x, r1y) else path.lineTo(r1x, r1y)
        path.lineTo(t1x, t1y); path.lineTo(t2x, t2y); path.lineTo(r2x, r2y)
    }
    path.close()

    drawPath(path, Brush.radialGradient(listOf(shineColor.copy(alpha = 0.6f), gearColor), center = Offset(center.x - outerRadius * 0.2f, center.y - outerRadius * 0.2f), radius = outerRadius * 1.2f))
    drawPath(path, gearColor.copy(alpha = 0.8f), style = Stroke(2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawCircle(gearColor.copy(alpha = 0.3f), (innerRadius + holeRadius) / 2f, center, style = Stroke((innerRadius - holeRadius) * 0.3f))
    drawCircle(AppColors.GradientEnd, holeRadius, center, style = Fill)
    drawCircle(gearColor.copy(alpha = 0.6f), holeRadius, center, style = Stroke(3f))
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse, StartOffset(i * 200)), label = "a$i")
            val scale by transition.animateFloat(0.8f, 1.2f, infiniteRepeatable(tween(600), RepeatMode.Reverse, StartOffset(i * 200)), label = "s$i")
            Canvas(Modifier.size(8.dp).scale(scale).alpha(alpha)) { drawCircle(AppColors.Primary) }
        }
    }
}

private data class Particle(val x: Float, val y: Float, val size: Float, val speed: Float, val alpha: Float)
private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)