package com.kt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity: ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{KTApp()}}}
@Composable fun KTApp(){var s by remember{mutableStateOf(true)};LaunchedEffect(Unit){delay(1800);s=false};MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF00E5FF),secondary=Color(0xFFFFC107),background=Color(0xFF05070A),surface=Color(0xFF111827))){if(s)Box(Modifier.fillMaxSize().background(Color(0xFF05070A)),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("KT",color=Color(0xFF00E5FF),fontSize=46.sp,fontWeight=FontWeight.Bold);Text("NIDHI ONLINE",color=Color(0xFFFFC107))}} else Column(Modifier.fillMaxSize().background(Color(0xFF05070A)).padding(20.dp)){Text("KT",color=Color(0xFF00E5FF),fontSize=30.sp,fontWeight=FontWeight.Bold);Text("Nidhi • Command Center",color=Color(0xFFFFC107));Spacer(Modifier.height(20.dp));listOf("Bot Status" to "RUNNING","Today's P&L" to "₹0.00","Last Trade" to "--").forEach{Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF111827)),modifier=Modifier.fillMaxWidth().padding(bottom=12.dp)){Column(Modifier.padding(16.dp)){Text(it.first,color=Color(0xFF00E5FF));Text(it.second,color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold)}}}}}}