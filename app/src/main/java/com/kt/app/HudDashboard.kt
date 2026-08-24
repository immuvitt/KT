package com.kt.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF050A0F)
private val Panel = Color(0xFF0A131B)
private val Cyan = Color(0xFF00E5FF)
private val Green = Color(0xFF20E890)
private val Amber = Color(0xFFFFC857)
private val Red = Color(0xFFFF4D67)
private val TextDim = Color(0xFF7F9AAA)

@Composable
fun HudDashboard(
    state: TradingUiState,
    onRefresh: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        HudGrid(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Cyan.copy(alpha = .12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = "KT AI robot",
                            tint = Cyan,
                            modifier = Modifier.size(31.dp)
                        )
                    }
                }

                Spacer(Modifier.size(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "KT CORE",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "AI TRADING COMMAND CENTER",
                        color = Cyan,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp
                    )
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh market data",
                        tint = Cyan
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            RobotStatusCard(
                connected = state.connected,
                running = state.engineRunning,
                loading = state.loading
            )

            Spacer(Modifier.height(12.dp))

            MarketCard(state.quote)

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "API: ${state.error}",
                    color = Amber,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            BinaryTelemetry()

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStart,
                    enabled = !state.engineRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = Color.Black
                    )
                ) {
                    Text("START", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onStop,
                    enabled = state.engineRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("STOP", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RobotStatusCard(
    connected: Boolean,
    running: Boolean,
    loading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Panel.copy(alpha = .94f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HudRing(active = running || connected)

            Spacer(Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "NIDHI AI",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when {
                        loading -> "SYNCING MARKET DATA..."
                        running && connected -> "ENGINE ONLINE • PAPER MODE"
                        connected -> "MARKET LINK ONLINE"
                        else -> "WAITING FOR MARKET API"
                    },
                    color = when {
                        loading -> Amber
                        connected -> Green
                        else -> TextDim
                    },
                    fontSize = 11.sp
                )
            }

            Text(
                if (connected) "● LIVE" else "○ OFFLINE",
                color = if (connected) Green else Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HudRing(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "hud")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(Modifier.size(52.dp)) {
        drawCircle(
            color = Cyan.copy(alpha = .15f),
            radius = size.minDimension / 2f
        )
        drawArc(
            color = if (active) Cyan else TextDim,
            startAngle = rotation,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = if (active) Green else TextDim,
            radius = 5.dp.toPx()
        )
    }
}

@Composable
private fun MarketCard(quote: MarketQuote?) {
    val price = quote?.price?.let { "%.2f".format(it) } ?: "—"
    val change = quote?.change?.let { "%+.2f".format(it) } ?: "—"
    val percent = quote?.changePercent?.let { "%+.2f%%".format(it) } ?: "—"
    val positive = quote?.change?.let { it >= 0 } ?: true

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Panel.copy(alpha = .94f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quote?.symbol ?: "NIFTY 50",
                        color = TextDim,
                        fontSize = 12.sp
                    )
                    Text(
                        price,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        change,
                        color = if (positive) Green else Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        percent,
                        color = if (positive) Green else Red,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "MARKET FEED  •  5 SEC POLL  •  PAPER EXECUTION",
                color = Cyan.copy(alpha = .75f),
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun BinaryTelemetry() {
    val transition = rememberInfiniteTransition(label = "binary")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "binaryOffset"
    )

    val bits = "0 1 0 0 1 0 1 0 0 0 1 1 0 1 0 0 1 0 1 1 0 0 1 0"

    Text(
        text = "SYSTEM TELEMETRY  ${bits.substring((offset * 6).toInt()).take(42)}",
        color = Cyan.copy(alpha = .55f),
        fontSize = 9.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HudGrid(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val step = 48.dp.toPx()

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = Cyan.copy(alpha = .025f),
                start = Offset(x, 0f),
                end = Offset(x, size.height)
            )
            x += step
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Cyan.copy(alpha = .025f),
                start = Offset(0f, y),
                end = Offset(size.width, y)
            )
            y += step
        }

        drawCircle(
            color = Cyan.copy(alpha = .035f),
            radius = size.minDimension * .34f,
            center = Offset(size.width * .78f, size.height * .23f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
