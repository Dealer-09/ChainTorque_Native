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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ChainTorque Brand Colors
private val SplashBackground = Color(0xFF0F172A)
private val SplashPrimary = Color(0xFF6366F1)
private val SplashSecondary = Color(0xFF8B5CF6)
private val GearColor1 = Color(0xFF7DD3FC)   // Sky-300 — large gear
private val GearColor2 = Color(0xFFA78BFA)   // Violet-400 — small gear
private val GearShine = Color(0xFFE0E7FF)    // Indigo-100 highlight

@Composable
fun AnimatedSplashScreen(
    onSplashComplete: () -> Unit
) {
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

    // Continuous gear rotation
    val infiniteTransition = rememberInfiniteTransition(label = "gears")
    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gearRotation"
    )

    // Pulse glow
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Floating particles
    val particles = remember {
        List(25) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 5 + 1.5f,
                speed = Random.nextFloat() * 0.003f + 0.001f,
                alpha = Random.nextFloat() * 0.4f + 0.1f
            )
        }
    }
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SplashBackground,
                        Color(0xFF1E1B4B),
                        Color(0xFF172554),
                        SplashBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val animatedY = (particle.y + particleProgress * particle.speed * 200) % 1f
                drawCircle(
                    color = SplashPrimary.copy(alpha = particle.alpha),
                    radius = particle.size.dp.toPx(),
                    center = Offset(
                        x = particle.x * size.width,
                        y = animatedY * size.height
                    )
                )
            }
        }

        // Ambient glow behind gears
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .alpha(pulseAlpha)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SplashPrimary.copy(alpha = 0.5f), Color.Transparent)
                ),
                radius = size.minDimension / 2
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Interlocking gears
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // Large gear (bottom-left) — rotates clockwise
                    val largeRadius = size.minDimension * 0.30f
                    val largeCenterX = centerX - largeRadius * 0.35f
                    val largeCenterY = centerY + largeRadius * 0.15f

                    rotate(degrees = gearRotation, pivot = Offset(largeCenterX, largeCenterY)) {
                        drawGear(
                            center = Offset(largeCenterX, largeCenterY),
                            outerRadius = largeRadius,
                            innerRadius = largeRadius * 0.72f,
                            holeRadius = largeRadius * 0.22f,
                            teeth = 10,
                            gearColor = GearColor1,
                            shineColor = GearShine
                        )
                    }

                    // Small gear (top-right) — rotates counter-clockwise, meshed
                    val smallRadius = largeRadius * 0.62f
                    // Position so teeth mesh: distance between centers = largeOuter + smallOuter - toothDepth
                    val meshDistance = largeRadius + smallRadius - largeRadius * 0.12f
                    val meshAngle = -35f * (PI.toFloat() / 180f) // angle offset
                    val smallCenterX = largeCenterX + meshDistance * cos(meshAngle)
                    val smallCenterY = largeCenterY + meshDistance * sin(meshAngle)

                    // Counter-rotate: gear ratio = large teeth / small teeth
                    val counterRotation = -(gearRotation * 10f / 7f) + 8f // offset for mesh alignment
                    rotate(degrees = counterRotation, pivot = Offset(smallCenterX, smallCenterY)) {
                        drawGear(
                            center = Offset(smallCenterX, smallCenterY),
                            outerRadius = smallRadius,
                            innerRadius = smallRadius * 0.70f,
                            holeRadius = smallRadius * 0.24f,
                            teeth = 7,
                            gearColor = GearColor2,
                            shineColor = GearShine
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // App name
            Text(
                text = "ChainTorque",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "3D CAD Marketplace",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SplashPrimary,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            LoadingDots(modifier = Modifier.alpha(subtitleAlpha))
        }
    }
}

/**
 * Draw a gear with [teeth] evenly-spaced trapezoidal teeth, a body ring, and a center hole.
 */
private fun DrawScope.drawGear(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    holeRadius: Float,
    teeth: Int,
    gearColor: Color,
    shineColor: Color
) {
    val path = Path()
    val toothAngle = (2 * PI / teeth).toFloat()
    val halfTooth = toothAngle * 0.22f      // half-width of tooth tip
    val halfGap = toothAngle * 0.28f        // half-width of gap

    // Build gear outline: alternate between tooth tip (outer) and root (inner)
    for (i in 0 until teeth) {
        val baseAngle = i * toothAngle

        // Root start (inner radius)
        val r1Angle = baseAngle - halfGap
        val r1x = center.x + innerRadius * cos(r1Angle)
        val r1y = center.y + innerRadius * sin(r1Angle)

        // Tooth start (outer radius)
        val t1Angle = baseAngle - halfTooth
        val t1x = center.x + outerRadius * cos(t1Angle)
        val t1y = center.y + outerRadius * sin(t1Angle)

        // Tooth end (outer radius)
        val t2Angle = baseAngle + halfTooth
        val t2x = center.x + outerRadius * cos(t2Angle)
        val t2y = center.y + outerRadius * sin(t2Angle)

        // Root end (inner radius)
        val r2Angle = baseAngle + halfGap
        val r2x = center.x + innerRadius * cos(r2Angle)
        val r2y = center.y + innerRadius * sin(r2Angle)

        if (i == 0) {
            path.moveTo(r1x, r1y)
        } else {
            path.lineTo(r1x, r1y)
        }
        path.lineTo(t1x, t1y)
        path.lineTo(t2x, t2y)
        path.lineTo(r2x, r2y)
    }
    path.close()

    // Gear body gradient fill
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(shineColor.copy(alpha = 0.6f), gearColor),
            center = Offset(center.x - outerRadius * 0.2f, center.y - outerRadius * 0.2f),
            radius = outerRadius * 1.2f
        )
    )

    // Subtle outline
    drawPath(
        path = path,
        color = gearColor.copy(alpha = 0.8f),
        style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner ring
    val ringRadius = (innerRadius + holeRadius) / 2f
    drawCircle(
        color = gearColor.copy(alpha = 0.3f),
        radius = ringRadius,
        center = center,
        style = Stroke(width = (innerRadius - holeRadius) * 0.3f)
    )

    // Center hole
    drawCircle(
        color = Color(0xFF0F172A),
        radius = holeRadius,
        center = center,
        style = Fill
    )

    // Hole ring
    drawCircle(
        color = gearColor.copy(alpha = 0.6f),
        radius = holeRadius,
        center = center,
        style = Stroke(width = 3f)
    )
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )

            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dotScale$index"
            )

            Canvas(
                modifier = Modifier
                    .size(8.dp)
                    .scale(dotScale)
                    .alpha(dotAlpha)
            ) {
                drawCircle(color = SplashPrimary)
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)