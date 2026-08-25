package com.kt.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF05070A)
private val CardBg = Color(0xFF0B1630)
private val CardBg2 = Color(0xFF0B1426)
private val Cyan = Color(0xFF00E5FF)
private val Gold = Color(0xFFFFC107)
private val Green = Color(0xFF22D878)
private val Red = Color(0xFFFF4D5A)
private val Muted = Color(0xFFB6BECC)
private val Border = Color(0xFF17374A)

// Configure the real market-data provider when selected. Never store API keys here.
private const val MARKET_BASE_URL = "https://YOUR-MARKET-DATA-API/"
private const val MARKET_QUOTE_PATH = "v1/quote/NIFTY"

private enum class Screen { WATCHLIST, ORDERS, CORE, AI, POSITIONS }
private enum class Side { BUY, SELL }

private data class PaperOrder(val id: Long, val symbol: String, val side: Side, val quantity: Int, val price: Double, val time: Long)
private data class Position(val symbol: String, val quantity: Int, val averagePrice: Double, val realizedPnl: Double, val markPrice: Double)
private data class PaperState(val cash: Double, val orders: List<PaperOrder>, val positions: List<Position>, val running: Boolean)
private data class Quote(val symbol: String, val exchange: String, val price: Double, val change: Double, val changePct: Double, val up: Boolean)

private val quotes = listOf(
    Quote("NIFTY 50", "NSE", 24311.80, 12.45, 0.05, true),
    Quote("BANKNIFTY", "NSE", 52840.75, 28.60, 0.05, true),
    Quote("FINNIFTY", "NSE", 23485.10, -15.30, -0.07, false),
    Quote("RELIANCE", "NSE", 2952.40, 8.90, 0.30, true),
    Quote("TCS", "NSE", 3685.75, -4.20, -0.11, false),
    Quote("HDFCBANK", "NSE", 1678.20, 2.15, 0.13, true)
)

private class PaperTradingStore(context: Context) {
    private val prefs = context.getSharedPreferences("kt_paper_trading", Context.MODE_PRIVATE)
    private val initialCapital = 100_000.0

    fun load(): PaperState {
        val cash = prefs.getFloat("cash", initialCapital.toFloat()).toDouble()
        val running = prefs.getBoolean("running", false)
        val orders = mutableListOf<PaperOrder>()
        runCatching { JSONArray(prefs.getString("orders", "[]")) }.getOrDefault(JSONArray()).let { a ->
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                orders += PaperOrder(o.getLong("id"), o.getString("symbol"), Side.valueOf(o.getString("side")), o.getInt("quantity"), o.getDouble("price"), o.getLong("time"))
            }
        }
        val positions = mutableListOf<Position>()
        runCatching { JSONArray(prefs.getString("positions", "[]")) }.getOrDefault(JSONArray()).let { a ->
            for (i in 0 until a.length()) {
                val p = a.getJSONObject(i)
                positions += Position(p.getString("symbol"), p.getInt("quantity"), p.getDouble("averagePrice"), p.getDouble("realizedPnl"), p.optDouble("markPrice", 0.0))
            }
        }
        return PaperState(cash, orders, positions, running)
    }

    fun save(state: PaperState) {
        val orders = JSONArray().apply { state.orders.forEach { o -> put(JSONObject().apply { put("id", o.id); put("symbol", o.symbol); put("side", o.side.name); put("quantity", o.quantity); put("price", o.price); put("time", o.time) }) } }
        val positions = JSONArray().apply { state.positions.forEach { p -> put(JSONObject().apply { put("symbol", p.symbol); put("quantity", p.quantity); put("averagePrice", p.averagePrice); put("realizedPnl", p.realizedPnl); put("markPrice", p.markPrice) }) } }
        prefs.edit().putFloat("cash", state.cash.toFloat()).putBoolean("running", state.running).putString("orders", orders.toString()).putString("positions", positions.toString()).apply()
    }

    fun reset() = prefs.edit().clear().apply()
    fun initialCapital() = initialCapital
}

private sealed interface OrderResult {
    data class Success(val state: PaperState) : OrderResult
    data class Error(val message: String) : OrderResult
}

private fun placeOrder(state: PaperState, symbolInput: String, side: Side, quantity: Int, price: Double): OrderResult {
    val symbol = symbolInput.trim().uppercase(Locale.US)
    if (symbol.isBlank()) return OrderResult.Error("Enter a symbol")
    if (quantity <= 0 || price <= 0) return OrderResult.Error("Quantity and price must be greater than zero")
    val existing = state.positions.firstOrNull { it.symbol == symbol }
    val positions = state.positions.toMutableList()
    var cash = state.cash
    if (side == Side.BUY) {
        val cost = quantity * price
        if (cost > cash) return OrderResult.Error("Insufficient paper cash")
        cash -= cost
        val next = if (existing == null) Position(symbol, quantity, price, 0.0, price) else {
            val totalQty = existing.quantity + quantity
            Position(symbol, totalQty, ((existing.averagePrice * existing.quantity) + cost) / totalQty, existing.realizedPnl, price)
        }
        positions.removeAll { it.symbol == symbol }
        positions += next
    } else {
        if (existing == null || quantity > existing.quantity) return OrderResult.Error("Not enough position to sell")
        cash += quantity * price
        val realized = existing.realizedPnl + (price - existing.averagePrice) * quantity
        positions.removeAll { it.symbol == symbol }
        if (existing.quantity - quantity > 0) positions += existing.copy(quantity = existing.quantity - quantity, realizedPnl = realized, markPrice = price)
    }
    val now = System.currentTimeMillis()
    return OrderResult.Success(state.copy(cash = cash, orders = listOf(PaperOrder(now, symbol, side, quantity, price, now)) + state.orders, positions = positions))
}

private fun unrealized(position: Position) = if (position.markPrice > 0) (position.markPrice - position.averagePrice) * position.quantity else 0.0
private fun totalPnl(state: PaperState) = state.positions.sumOf { it.realizedPnl + unrealized(it) }
private fun money(v: Double) = "₹${String.format(Locale.US, "%,.2f", v)}"
private fun signedMoney(v: Double) = if (v >= 0) "+${money(v)}" else money(v)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KTApp() }
    }
}

@Composable
private fun KTApp() {
    val context = LocalContext.current
    val store = remember { PaperTradingStore(context) }
    val repository = remember {
        MarketRepository(
            MarketApi(
                MarketApiConfig(
                    baseUrl = MARKET_BASE_URL,
                    quotePath = MARKET_QUOTE_PATH
                )
            )
        )
    }

    var state by remember { mutableStateOf(store.load()) }
    var screen by remember { mutableStateOf(Screen.CORE) }
    var showSplash by remember { mutableStateOf(true) }
    var liveQuote by remember { mutableStateOf<MarketQuote?>(null) }
    var apiError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200)
        showSplash = false
    }

    LaunchedEffect(state.running) {
        if (MARKET_BASE_URL.contains("YOUR-MARKET-DATA-API")) {
            apiError = "Market API not configured"
            return@LaunchedEffect
        }

        while (state.running) {
            repository.getNiftyQuote()
                .onSuccess {
                    liveQuote = it
                    apiError = null
                }
                .onFailure {
                    apiError = it.message ?: "Market API unavailable"
                }
            kotlinx.coroutines.delay(5_000)
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, secondary = Gold, background = Bg, surface = CardBg)) {
        if (showSplash) {
            SplashScreen()
        } else {
            Scaffold(
                containerColor = Bg,
                bottomBar = { BottomNav(screen) { screen = it } }
            ) { padding ->
                when (screen) {
                    Screen.WATCHLIST -> WatchlistScreen(Modifier.padding(padding))
                    Screen.ORDERS -> OrdersScreen(
                        state,
                        { result -> if (result is OrderResult.Success) state = result.state.also(store::save) },
                        Modifier.padding(padding)
                    )
                    Screen.CORE -> CoreScreen(
                        state = state,
                        initialCapital = store.initialCapital(),
                        liveQuote = liveQuote,
                        apiError = apiError,
                        onStart = { state = state.copy(running = true).also(store::save) },
                        onStop = { state = state.copy(running = false).also(store::save) },
                        onRefresh = {
                            state = store.load()
                            if (!MARKET_BASE_URL.contains("YOUR-MARKET-DATA-API")) {
                                scope.launch {
                                    repository.getNiftyQuote()
                                        .onSuccess { liveQuote = it; apiError = null }
                                        .onFailure { apiError = it.message ?: "Market API unavailable" }
                                }
                            } else {
                                apiError = "Market API not configured"
                            }
                        },
                        onReset = {
                            store.reset()
                            state = store.load()
                            liveQuote = null
                        },
                        onOrders = { screen = Screen.ORDERS },
                        modifier = Modifier.padding(padding)
                    )
                    Screen.AI -> AiScreen(state, Modifier.padding(padding))
                    Screen.POSITIONS -> PositionsScreen(state, Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    var binary by remember { mutableStateOf("0100101010011010010110010100101101") }
    val hudTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "nidhiHud")
    val sweep by hudTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(2800, easing = androidx.compose.animation.core.LinearEasing),
            androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "hudSweep"
    )

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(90)
            val next = (0..1).random()
            binary = (binary.drop(1) + next).takeLast(36)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Bg, Color(0xFF081224)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NIDHI BOOT SEQUENCE",
                color = Cyan,
                fontSize = 12.sp,
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(22.dp))

            // KT logo centered inside the Nidhi HUD. The uploaded logo is transparent,
            // so the HUD rings remain visible around it without a white square.
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension * 0.40f

                    // Outer technical rings.
                    drawCircle(
                        color = Cyan.copy(alpha = 0.14f),
                        radius = radius * 1.25f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Cyan.copy(alpha = 0.45f),
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                    val arcRadius = radius * 1.10f
                    val arcSize = androidx.compose.ui.geometry.Size(arcRadius * 2f, arcRadius * 2f)
                    val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)
                    drawArc(
                        color = Cyan.copy(alpha = 0.95f),
                        startAngle = sweep,
                        sweepAngle = 72f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                    drawArc(
                        color = Gold.copy(alpha = 0.80f),
                        startAngle = sweep + 150f,
                        sweepAngle = 34f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

                    // Small HUD tick marks.
                    for (i in 0 until 24) {
                        val angle = Math.toRadians((i * 15.0))
                        val inner = radius * 1.04f
                        val outer = if (i % 3 == 0) radius * 1.15f else radius * 1.10f
                        val x1 = center.x + kotlin.math.cos(angle).toFloat() * inner
                        val y1 = center.y + kotlin.math.sin(angle).toFloat() * inner
                        val x2 = center.x + kotlin.math.cos(angle).toFloat() * outer
                        val y2 = center.y + kotlin.math.sin(angle).toFloat() * outer
                        drawLine(
                            color = Cyan.copy(alpha = if (i % 3 == 0) 0.55f else 0.22f),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }
                }

                Image(
                    painter = painterResourceCompat(R.drawable.kt_hud_center),
                    contentDescription = "KT logo",
                    modifier = Modifier.size(148.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "NIDHI ONLINE",
                color = Gold,
                fontSize = 20.sp,
                letterSpacing = 2.0.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "AI TRADING ASSISTANT",
                color = Muted,
                fontSize = 12.sp,
                letterSpacing = 1.8.sp
            )
            Spacer(Modifier.height(34.dp))
            Text(
                "INITIALIZING SYSTEM...",
                color = Cyan,
                fontSize = 11.sp,
                letterSpacing = 2.4.sp
            )
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Cyan.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                    .background(Color(0xFF061421))
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "  ${binary.chunked(1).joinToString("  ")}  ...",
                    color = Cyan,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(Modifier.height(26.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.76f)
                    .height(2.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Cyan, Color.Transparent)))
            )
        }
    }
}

@Composable
private fun BottomNav(screen: Screen, onSelect: (Screen) -> Unit) {
    Surface(color = Color(0xFF0A0C12), modifier = Modifier.navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavItem(screen == Screen.WATCHLIST, { onSelect(Screen.WATCHLIST) }, Icons.Default.WatchLater, "Watchlist")
            NavItem(screen == Screen.ORDERS, { onSelect(Screen.ORDERS) }, Icons.Default.ListAlt, "Orders")
            NavItem(screen == Screen.CORE, { onSelect(Screen.CORE) }, Icons.Default.AutoGraph, "KT Core")
            NavItem(screen == Screen.AI, { onSelect(Screen.AI) }, Icons.Default.Bolt, "AI")
            NavItem(screen == Screen.POSITIONS, { onSelect(Screen.POSITIONS) }, Icons.Default.Work, "Positions")
        }
    }
}

@Composable
private fun NavItem(selected: Boolean, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(
        modifier = Modifier.width(72.dp).fillMaxSize().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) Cyan else Muted, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 9.sp, color = if (selected) Cyan else Muted)
    }
}

@Composable
private fun CoreScreen(
    state: PaperState,
    initialCapital: Double,
    liveQuote: MarketQuote?,
    apiError: String?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
    onReset: () -> Unit,
    onOrders: () -> Unit,
    modifier: Modifier
) {
    val pnl = totalPnl(state)
    val last = state.orders.firstOrNull()
    val displayQuote = liveQuote?.let { Quote(it.symbol, "NSE", it.price, it.change, it.changePercent, it.change >= 0) } ?: quotes.first()
    val isLive = liveQuote != null

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Bg).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("KT", color = Cyan, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nidhi Online", color = Color.White, fontSize = 16.sp)
                    Spacer(Modifier.width(5.dp))
                    Box(Modifier.size(7.dp).clip(RoundedCornerShape(8.dp)).background(Green))
                }
                Text("AI TRADING ASSISTANT • PAPER MODE", color = Muted, fontSize = 9.sp, letterSpacing = 1.1.sp)
            }
            StatusChip(state.running)
            Spacer(Modifier.width(7.dp))
            Image(
                painter = painterResourceCompat(R.drawable.kt_logo_transparent),
                contentDescription = "Kuber Tijori",
                modifier = Modifier.size(54.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                SimpleDateFormat("dd MMM yyyy  •  HH:mm:ss", Locale.US).format(Date()),
                color = Muted,
                fontSize = 8.sp
            )
        }

        Spacer(Modifier.height(8.dp))
        HeroBotCard(state.running, isLive, apiError)

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("TODAY'S P&L", signedMoney(pnl), if (pnl >= 0) Green else Red, Modifier.weight(1f))
            MetricCard(
                "LAST TRADE",
                last?.let { "${it.side} ${it.symbol}" } ?: "--",
                Color.White,
                Modifier.weight(1f),
                last?.let { "${it.quantity} @ ${money(it.price)}" } ?: "No trades yet"
            )
        }

        Spacer(Modifier.height(10.dp))
        MarketCard(displayQuote, isLive)

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                shape = RoundedCornerShape(22.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                Spacer(Modifier.width(5.dp))
                Text("Start Bot", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                shape = RoundedCornerShape(22.dp)
            ) {
                Icon(Icons.Default.Stop, null)
                Spacer(Modifier.width(5.dp))
                Text("Stop Bot")
            }
        }

        Spacer(Modifier.height(9.dp))
        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Refresh, null, tint = Color.Black)
            Spacer(Modifier.width(6.dp))
            Text("Refresh Data", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onOrders, Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) { Text("New / View Orders") }
            OutlinedButton(onClick = onReset, Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) { Text("Reset") }
        }

        Spacer(Modifier.height(16.dp))
        Text("Virtual capital: ${money(initialCapital)}", color = Muted, fontSize = 12.sp)
        Text("Paper engine • local persistence • no broker connection", color = Cyan, fontSize = 11.sp)
    }
}

@Composable
private fun HeroBotCard(running: Boolean, apiConnected: Boolean, apiError: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071326)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(20.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val r = minOf(size.width, size.height) * 0.38f
                    drawCircle(
                        color = Cyan.copy(alpha = 0.10f),
                        radius = r * 1.30f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                    drawCircle(
                        color = Cyan.copy(alpha = 0.35f),
                        radius = r * 1.05f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                    )
                    drawArc(
                        color = Cyan.copy(alpha = 0.95f),
                        startAngle = -145f,
                        sweepAngle = 95f,
                        useCenter = false,
                        topLeft = Offset(center.x - r * 1.18f, center.y - r * 1.18f),
                        size = androidx.compose.ui.geometry.Size(r * 2.36f, r * 2.36f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(4.dp.toPx())
                    )
                    drawArc(
                        color = Gold.copy(alpha = 0.9f),
                        startAngle = 25f,
                        sweepAngle = 42f,
                        useCenter = false,
                        topLeft = Offset(center.x - r * 1.18f, center.y - r * 1.18f),
                        size = androidx.compose.ui.geometry.Size(r * 2.36f, r * 2.36f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                    )
                    for (i in 0 until 18) {
                        val a = Math.toRadians(i * 20.0)
                        val inner = r * 1.08f
                        val outer = r * if (i % 3 == 0) 1.18f else 1.13f
                        drawLine(
                            color = Cyan.copy(alpha = if (i % 3 == 0) 0.55f else 0.22f),
                            start = Offset(
                                center.x + kotlin.math.cos(a).toFloat() * inner,
                                center.y + kotlin.math.sin(a).toFloat() * inner
                            ),
                            end = Offset(
                                center.x + kotlin.math.cos(a).toFloat() * outer,
                                center.y + kotlin.math.sin(a).toFloat() * outer
                            ),
                            strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }
                }
                Image(
                    painter = painterResourceCompat(R.drawable.kt_robot),
                    contentDescription = "Nidhi trading robot",
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(176.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071A22)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .border(1.dp, Cyan.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("BOT STATUS", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                        Text(
                            if (running) "RUNNING" else "STOPPED",
                            color = if (running) Green else Gold,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    MiniSparkline(running, Modifier.width(92.dp).height(34.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            apiConnected -> "API ONLINE"
                            apiError != null -> "API STANDBY"
                            else -> "PAPER READY"
                        },
                        color = if (apiConnected) Green else Muted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(running: Boolean) {
    Box(Modifier.border(1.dp, if (running) Green else Gold, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(if (running) "CONNECTED" else "READY", color = if (running) Green else Gold, fontSize = 10.sp)
    }
}

@Composable
private fun MetricCard(title: String, value: String, color: Color, modifier: Modifier, subtitle: String? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(it, color = Muted, fontSize = 9.sp) }
        }
    }
}

@Composable
private fun MarketCard(q: Quote, isLive: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(q.symbol, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(7.dp)); Text(if (isLive) "LIVE" else "DEMO", color = if (isLive) Gold else Muted, fontSize = 8.sp) }
                Text(q.exchange, color = Muted, fontSize = 10.sp)
                Text("${if (q.change >= 0) "+" else ""}${q.change} (${q.changePct}%)", color = if (q.up) Green else Red, fontSize = 11.sp)
            }
            MiniSparkline(q.up, Modifier.width(115.dp).height(42.dp))
            Spacer(Modifier.width(12.dp))
            Text(String.format(Locale.US, "%,.2f", q.price), color = if (q.symbol == "NIFTY 50") Gold else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WatchlistScreen(modifier: Modifier) {
    var tab by remember { mutableStateOf(0) }
    Column(modifier.fillMaxSize().background(Bg).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Watchlist", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = Cyan) }
            IconButton(onClick = {}) { Icon(Icons.Default.Settings, null, tint = Muted) }
        }
        Spacer(Modifier.height(8.dp))
        TabRowLike(listOf("NIFTY", "BANKNIFTY", "FINNIFTY", "STOCKS"), tab) { tab = it }
        Spacer(Modifier.height(10.dp))
        val list = when (tab) {
            0 -> quotes
            1 -> quotes.filter { it.symbol.contains("BANK") || it.symbol == "HDFCBANK" }
            2 -> quotes.filter { it.symbol.contains("FIN") }
            else -> quotes.drop(3)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) { items(list) { QuoteRow(it) } }
    }
}

@Composable
private fun TabRowLike(labels: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        labels.forEachIndexed { i, label ->
            val active = selected == i
            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(if (active) Color(0xFF071C27) else Color.Transparent).border(1.dp, if (active) Cyan else Color.Transparent, RoundedCornerShape(12.dp)).clickable { onSelected(i) }.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(label, color = if (active) Cyan else Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun QuoteRow(q: Quote) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg2), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(q.symbol, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(q.exchange, color = Muted, fontSize = 9.sp) }
            MiniSparkline(q.up, Modifier.width(82.dp).height(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) { Text(String.format(Locale.US, "%,.2f", q.price), color = Color.White, fontSize = 15.sp); Text("${if (q.change >= 0) "+" else ""}${q.change} (${q.changePct}%)", color = if (q.up) Green else Red, fontSize = 9.sp) }
        }
    }
}

@Composable
private fun OrdersScreen(state: PaperState, onOrder: (OrderResult) -> Unit, modifier: Modifier) {
    var side by remember { mutableStateOf(Side.BUY) }
    var symbol by remember { mutableStateOf("NIFTY") }
    var price by remember { mutableStateOf("25000") }
    var qty by remember { mutableStateOf("50") }
    var tab by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().background(Bg).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Orders", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Icon(Icons.Default.ListAlt, null, tint = Cyan) }
        Text("Paper execution ledger", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))
        TabRowLike(listOf("Open Orders", "Trade History", "Positions"), tab) { tab = it }
        Spacer(Modifier.height(10.dp))
        if (tab == 0) {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(15.dp)) {
                    Text("New Paper Order", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("Simulated execution only • no broker connection", color = Muted, fontSize = 10.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { ToggleButton("BUY", side == Side.BUY, { side = Side.BUY }, Modifier.weight(1f)); ToggleButton("SELL", side == Side.SELL, { side = Side.SELL }, Modifier.weight(1f)) }
                    Spacer(Modifier.height(9.dp))
                    OutlinedTextField(symbol, { symbol = it }, label = { Text("Symbol") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(price, { price = it }, label = { Text("Order Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(qty, { qty = it }, label = { Text("Qty") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { val r = placeOrder(state, symbol, side, qty.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0); message = if (r is OrderResult.Success) "${side.name} order executed" else (r as OrderResult.Error).message; onOrder(r) }, Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = if (side == Side.BUY) Cyan else Red), shape = RoundedCornerShape(17.dp)) { Icon(Icons.Default.AddChart, null, tint = Color.Black); Spacer(Modifier.width(6.dp)); Text("PLACE ${side.name} ORDER", color = Color.Black, fontWeight = FontWeight.Bold) }
                    if (message.isNotBlank()) { Spacer(Modifier.height(6.dp)); Text(message, color = if (message.contains("executed")) Green else Red, fontSize = 10.sp) }
                }
            }
            Spacer(Modifier.height(10.dp))
            if (state.orders.isEmpty()) EmptyState("No Open Orders", "Your paper orders will appear here", Icons.Default.ListAlt)
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(state.orders.take(8), key = { it.id }) { OrderCard(it) } }
        } else if (tab == 1) {
            if (state.orders.isEmpty()) EmptyState("No Trade History", "Executed paper trades will appear here", Icons.Default.BarChart)
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(state.orders, key = { it.id }) { OrderCard(it) } }
        } else {
            if (state.positions.isEmpty()) EmptyState("No Positions", "Open paper positions will appear here", Icons.Default.Work)
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(state.positions, key = { it.symbol }) { PositionCard(it) } }
        }
    }
}

@Composable
private fun ToggleButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) Color(0xFF514A5D) else Color.Transparent).border(1.dp, if (selected) Color(0xFF514A5D) else Border, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) { Text(text, color = if (selected) Color.White else Muted, fontWeight = FontWeight.Medium) }
}

@Composable
private fun OrderCard(order: PaperOrder) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg2), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("${order.side} ${order.symbol}", color = if (order.side == Side.BUY) Green else Red, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("Qty ${order.quantity} @ ${money(order.price)}", color = Color.White, fontSize = 11.sp); Text(SimpleDateFormat("dd MMM HH:mm:ss", Locale.US).format(Date(order.time)), color = Muted, fontSize = 9.sp) }
            Text("PAPER", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PositionsScreen(state: PaperState, modifier: Modifier) {
    var tab by remember { mutableStateOf(0) }
    val pnl = totalPnl(state)
    Column(modifier.fillMaxSize().background(Bg).padding(14.dp)) {
        Text("Positions", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Open positions and performance", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))
        TabRowLike(listOf("Open Positions", "Performance"), tab) { tab = it }
        Spacer(Modifier.height(10.dp))
        if (tab == 0) {
            if (state.positions.isEmpty()) EmptyState("No Open Positions", "Your open positions will appear here", Icons.Default.Work)
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(state.positions, key = { it.symbol }) { PositionCard(it) } }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("Performance", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    MetricCard("TOTAL PAPER P&L", signedMoney(pnl), if (pnl >= 0) Green else Red, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { MetricCard("TRADES", state.orders.size.toString(), Cyan, Modifier.weight(1f)); MetricCard("OPEN", state.positions.size.toString(), Gold, Modifier.weight(1f)) }
                    Spacer(Modifier.height(16.dp)); MiniSparkline(pnl >= 0, Modifier.fillMaxWidth().height(90.dp)); Spacer(Modifier.height(10.dp)); Text("Performance analytics will expand as strategy execution is connected.", color = Muted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun PositionCard(p: Position) {
    val u = unrealized(p); val total = p.realizedPnl + u
    Card(colors = CardDefaults.cardColors(containerColor = CardBg2), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(p.symbol, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text("Qty ${p.quantity} • Avg ${money(p.averagePrice)}", color = Muted, fontSize = 10.sp) }; Text(signedMoney(total), color = if (total >= 0) Green else Red, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(6.dp)); Text("LTP ${if (p.markPrice > 0) money(p.markPrice) else "--"} • Unrealized ${signedMoney(u)} • Realized ${signedMoney(p.realizedPnl)}", color = Cyan, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AiScreen(state: PaperState, modifier: Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Bg).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("AI Assistant", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Icon(Icons.Default.Settings, null, tint = Muted) }
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF081C2A)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Bolt, null, tint = Cyan, modifier = Modifier.size(34.dp)) }
                Spacer(Modifier.width(12.dp)); Column { Row(verticalAlignment = Alignment.CenterVertically) { Text("Nidhi is online", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(5.dp)); Box(Modifier.size(7.dp).clip(RoundedCornerShape(8.dp)).background(Green)) }; Text("How can I help you today?", color = Muted, fontSize = 11.sp) }
            }
        }
        Spacer(Modifier.height(14.dp)); Text("Smart Actions", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { ActionCard("Market Analysis", Icons.Default.Analytics, Modifier.weight(1f)); ActionCard("Trade Ideas", Icons.Default.AutoGraph, Modifier.weight(1f)) }
        Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { ActionCard("Risk Check", Icons.Default.Security, Modifier.weight(1f)); ActionCard("Performance", Icons.Default.BarChart, Modifier.weight(1f)) }
        Spacer(Modifier.height(14.dp)); Text("Ask Nidhi", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(7.dp))
        listOf("How is the market today?", "Give me a trade idea for NIFTY", "What is my today's performance?", "Show my win rate this week").forEach { PromptRow(it) }
        Spacer(Modifier.height(12.dp)); Text("Current paper P&L: ${signedMoney(totalPnl(state))}", color = Cyan, fontSize = 11.sp)
    }
}

@Composable
private fun ActionCard(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg2), shape = RoundedCornerShape(12.dp), modifier = modifier) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Cyan, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(text, color = Color.White, fontSize = 10.sp) } }
}

@Composable
private fun PromptRow(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF070D18)), shape = RoundedCornerShape(11.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) { Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Text(text, color = Color.White, fontSize = 10.sp, modifier = Modifier.weight(1f)); Text("›", color = Muted, fontSize = 18.sp) } }
}

@Composable
private fun EmptyState(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg2), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(34.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = Cyan, modifier = Modifier.size(50.dp)); Spacer(Modifier.height(12.dp)); Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 10.sp) }
    }
}

@Composable
private fun MiniSparkline(up: Boolean, modifier: Modifier = Modifier.fillMaxWidth().height(45.dp)) {
    val points = if (up) listOf(.20f,.35f,.27f,.48f,.42f,.66f,.57f,.82f,.74f,1f) else listOf(.72f,.45f,.60f,.30f,.40f,.20f,.35f,.12f,.25f,.05f)
    Canvas(modifier) {
        val path = Path(); points.forEachIndexed { i, v -> val x = size.width * i / (points.lastIndex.coerceAtLeast(1)); val y = size.height * (1f - v); if (i == 0) path.moveTo(x,y) else path.lineTo(x,y) }; drawPath(path, color = if (up) Green else Red, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f))
        drawLine(if (up) Green else Red, Offset(0f, size.height - 1), Offset(size.width, size.height - 1), 0.7f)
    }
}

@Composable
private fun painterResourceCompat(id: Int) = androidx.compose.ui.res.painterResource(id)
