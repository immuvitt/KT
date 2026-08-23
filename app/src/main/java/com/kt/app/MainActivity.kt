package com.kt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KTTheme {
                KTApp()
            }
        }
    }
}

private val Bg = Color(0xFF05070A)
private val Cyan = Color(0xFF00E5FF)
private val Gold = Color(0xFFFFC107)
private val Card = Color(0xFF111827)

@Composable
fun KTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Cyan,
            secondary = Gold,
            surface = Card,
            background = Bg
        ),
        content = content
    )
}

@Composable
fun KTApp() {
    var splash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1800)
        splash = false
    }

    if (splash) SplashScreen() else Dashboard()
}

@Composable
fun SplashScreen() {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Bg, Color(0xFF0B1220))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .background(Color(0xFF0E1A2B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("KT", color = Cyan, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "KUBER TIJORI",
                color = Gold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "NIDHI • COMMAND CENTER",
                color = Cyan,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun Dashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(20.dp)
    ) {

        Spacer(Modifier.height(18.dp))

        Text("KT", color = Cyan, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Nidhi • Command Center", color = Gold)

        Spacer(Modifier.height(24.dp))

        DashboardCard("Bot Status", "RUNNING")
        Spacer(Modifier.height(14.dp))
        DashboardCard("Today's P&L", "₹0.00")
        Spacer(Modifier.height(14.dp))
        DashboardCard("Last Trade", "--")

        Spacer(Modifier.height(26.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Button(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                Spacer(Modifier.width(6.dp))
                Text("Start", color = Color.Black)
            }

            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
            ) {
                Icon(Icons.Default.Stop, null)
                Spacer(Modifier.width(6.dp))
                Text("Stop")
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Gold)
        ) {
            Icon(Icons.Default.Refresh, null, tint = Color.Black)
            Spacer(Modifier.width(6.dp))
            Text("Refresh", color = Color.Black)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Module 1 • Paper Trading • EMA 9/21",
            color = Cyan,
            fontSize = 13.sp
        )
    }
}

@Composable
fun DashboardCard(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, color = Color.LightGray)
            Spacer(Modifier.height(6.dp))
            Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}