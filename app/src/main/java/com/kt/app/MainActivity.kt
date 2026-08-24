package com.kt.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF05070A)
private val CardBg = Color(0xFF111827)
private val Cyan = Color(0xFF00E5FF)
private val Gold = Color(0xFFFFC107)
private val Green = Color(0xFF33D17A)
private val Red = Color(0xFFFF5C5C)
private val Muted = Color(0xFFB8C0CC)

enum class Side { BUY, SELL }
private enum class Screen { DASHBOARD, ORDERS, POSITIONS }

data class PaperOrder(
    val id: Long,
    val symbol: String,
    val side: Side,
    val quantity: Int,
    val price: Double,
    val time: Long
)

data class Position(
    val symbol: String,
    val quantity: Int,
    val averagePrice: Double,
    val realizedPnl: Double,
    val markPrice: Double
)

data class PaperState(
    val cash: Double,
    val orders: List<PaperOrder>,
    val positions: List<Position>,
    val running: Boolean
)

private class PaperTradingStore(context: Context) {
    private val prefs = context.getSharedPreferences("kt_paper_trading", Context.MODE_PRIVATE)
    private val initialCapital = 100_000.0

    fun load(): PaperState {
        val cash = prefs.getFloat("cash", initialCapital.toFloat()).toDouble()
        val running = prefs.getBoolean("running", false)
        val orders = mutableListOf<PaperOrder>()
        val orderArray = runCatching { JSONArray(prefs.getString("orders", "[]")) }.getOrDefault(JSONArray())
        for (i in 0 until orderArray.length()) {
            val o = orderArray.getJSONObject(i)
            orders += PaperOrder(
                o.getLong("id"), o.getString("symbol"), Side.valueOf(o.getString("side")),
                o.getInt("quantity"), o.getDouble("price"), o.getLong("time")
            )
        }
        val positions = mutableListOf<Position>()
        val positionArray = runCatching { JSONArray(prefs.getString("positions", "[]")) }.getOrDefault(JSONArray())
        for (i in 0 until positionArray.length()) {
            val p = positionArray.getJSONObject(i)
            positions += Position(
                p.getString("symbol"), p.getInt("quantity"), p.getDouble("averagePrice"),
                p.getDouble("realizedPnl"), p.optDouble("markPrice", 0.0)
            )
        }
        return PaperState(cash, orders, positions, running)
    }

    fun save(state: PaperState) {
        val orders = JSONArray()
        state.orders.forEach { o ->
            orders.put(JSONObject().apply {
                put("id", o.id); put("symbol", o.symbol); put("side", o.side.name)
                put("quantity", o.quantity); put("price", o.price); put("time", o.time)
            })
        }
        val positions = JSONArray()
        state.positions.forEach { p ->
            positions.put(JSONObject().apply {
                put("symbol", p.symbol); put("quantity", p.quantity)
                put("averagePrice", p.averagePrice); put("realizedPnl", p.realizedPnl)
                put("markPrice", p.markPrice)
            })
        }
        prefs.edit()
            .putFloat("cash", state.cash.toFloat())
            .putBoolean("running", state.running)
            .putString("orders", orders.toString())
            .putString("positions", positions.toString())
            .apply()
    }

    fun reset() = prefs.edit().clear().apply()
    fun initialCapital() = initialCapital
}

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
    var state by remember { mutableStateOf(store.load()) }
    var screen by remember { mutableStateOf(Screen.DASHBOARD) }
    var showSplash by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1100)
        showSplash = false
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, secondary = Gold, background = Bg, surface = CardBg)) {
        if (showSplash) {
            SplashScreen()
        } else {
            Scaffold(
                containerColor = Bg,
                bottomBar = {
                    NavigationBar(containerColor = Color(0xFF101116), modifier = Modifier.navigationBarsPadding()) {
                        NavigationBarItem(screen == Screen.DASHBOARD, { screen = Screen.DASHBOARD }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                        NavigationBarItem(screen == Screen.ORDERS, { screen = Screen.ORDERS }, icon = { Icon(Icons.Default.ListAlt, null) }, label = { Text("Orders") })
                        NavigationBarItem(screen == Screen.POSITIONS, { screen = Screen.POSITIONS }, icon = { Icon(Icons.Default.TrendingUp, null) }, label = { Text("Positions") })
                    }
                }
            ) { padding ->
                when (screen) {
                    Screen.DASHBOARD -> Dashboard(
                        state = state,
                        initialCapital = store.initialCapital(),
                        onStart = { state = state.copy(running = true).also(store::save) },
                        onStop = { state = state.copy(running = false).also(store::save) },
                        onOpenOrders = { screen = Screen.ORDERS },
                        onPlaceOrder = { result -> if (result is OrderResult.Success) state = result.state.also(store::save) },
                        onMarkPrice = { symbol, price ->
                            val normalized = symbol.trim().uppercase(Locale.US)
                            if (normalized.isNotBlank() && price > 0) {
                                val updated = state.copy(positions = state.positions.map { p ->
                                    if (p.symbol == normalized) p.copy(markPrice = price) else p
                                })
                                state = updated.also(store::save)
                            }
                        },
                        onReset = { store.reset(); state = store.load() },
                        modifier = Modifier.padding(padding)
                    )
                    Screen.ORDERS -> OrdersScreen(state.orders, modifier = Modifier.padding(padding))
                    Screen.POSITIONS -> PositionsScreen(state.positions, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

private sealed interface OrderResult {
    data class Success(val state: PaperState) : OrderResult
    data class Error(val message: String) : OrderResult
}

private fun placeMarketOrder(state: PaperState, symbolInput: String, side: Side, quantity: Int, price: Double): OrderResult {
    val symbol = symbolInput.trim().uppercase(Locale.US)
    if (symbol.isBlank()) return OrderResult.Error("Enter a symbol")
    if (quantity <= 0) return OrderResult.Error("Quantity must be greater than zero")
    if (price <= 0.0) return OrderResult.Error("Price must be greater than zero")

    val existing = state.positions.firstOrNull { it.symbol == symbol }
    val positions = state.positions.toMutableList()
    var cash = state.cash

    if (side == Side.BUY) {
        val cost = price * quantity
        if (cost > cash) return OrderResult.Error("Insufficient paper cash")
        cash -= cost
        val updated = if (existing == null) {
            Position(symbol, quantity, price, 0.0, price)
        } else {
            val totalQty = existing.quantity + quantity
            Position(symbol, totalQty, ((existing.averagePrice * existing.quantity) + cost) / totalQty, existing.realizedPnl, price)
        }
        positions.removeAll { it.symbol == symbol }
        positions += updated
    } else {
        if (existing == null || quantity > existing.quantity) return OrderResult.Error("Not enough position to sell")
        cash += price * quantity
        val realized = existing.realizedPnl + (price - existing.averagePrice) * quantity
        val remaining = existing.quantity - quantity
        positions.removeAll { it.symbol == symbol }
        if (remaining > 0) positions += existing.copy(quantity = remaining, realizedPnl = realized, markPrice = price)
    }

    val now = System.currentTimeMillis()
    val order = PaperOrder(now, symbol, side, quantity, price, now)
    return OrderResult.Success(state.copy(cash = cash, orders = listOf(order) + state.orders, positions = positions))
}

private fun unrealizedPnl(position: Position): Double =
    if (position.markPrice > 0) (position.markPrice - position.averagePrice) * position.quantity else 0.0

private fun totalPnl(state: PaperState): Double =
    state.positions.sumOf { it.realizedPnl + unrealizedPnl(it) }

@Composable
private fun SplashScreen() {
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Bg, Color(0xFF0B1220)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.kt_logo),
                contentDescription = "Kuber Tijori",
                modifier = Modifier.size(230.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(4.dp))
            Text("KT", color = Cyan, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("NIDHI ONLINE", color = Gold, fontSize = 17.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(18.dp))
            Text("PAPER TRADING", color = Muted, fontSize = 12.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun Dashboard(
    state: PaperState,
    initialCapital: Double,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenOrders: () -> Unit,
    onPlaceOrder: (OrderResult) -> Unit,
    onMarkPrice: (String, Double) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var symbol by remember { mutableStateOf("NIFTY") }
    var price by remember { mutableStateOf("25000") }
    var quantity by remember { mutableStateOf("50") }
    var markPrice by remember { mutableStateOf("25000") }
    var side by remember { mutableStateOf(Side.BUY) }
    var message by remember { mutableStateOf("") }
    val lastTrade = state.orders.firstOrNull()
    val pnl = totalPnl(state)

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Bg).padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("KT", color = Cyan, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text("Nidhi • Command Center", color = Gold, fontSize = 18.sp)
            }
            Image(painter = androidx.compose.ui.res.painterResource(R.drawable.kt_logo), contentDescription = "Kuber Tijori", modifier = Modifier.size(70.dp), contentScale = ContentScale.Fit)
        }
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Bot", if (state.running) "RUNNING" else "STOPPED", if (state.running) Green else Gold, Modifier.weight(1f))
            MetricCard("Cash", money(state.cash), Cyan, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Paper P&L", money(pnl), if (pnl >= 0) Green else Red, Modifier.weight(1f))
            MetricCard("Orders", state.orders.size.toString(), Cyan, Modifier.weight(1f))
        }

        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Place Paper Order", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Simulated execution only • no broker connection", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = side == Side.BUY, onClick = { side = Side.BUY }, label = { Text("BUY") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = side == Side.SELL, onClick = { side = Side.SELL }, label = { Text("SELL") }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(symbol, { symbol = it }, label = { Text("Symbol") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(price, { price = it }, label = { Text("Order Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(quantity, { quantity = it }, label = { Text("Qty") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val result = placeMarketOrder(state, symbol, side, quantity.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0)
                        message = when (result) { is OrderResult.Success -> "${side.name} order executed"; is OrderResult.Error -> result.message }
                        onPlaceOrder(result)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (side == Side.BUY) Cyan else Red),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.AddChart, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("PLACE ${side.name} ORDER", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                if (message.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(message, color = if (message.contains("executed")) Green else Red, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Manual Market Price", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Use this until the live market-data feed is connected.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(markPrice, { markPrice = it }, label = { Text("LTP / Mark") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onMarkPrice(symbol, markPrice.toDoubleOrNull() ?: 0.0); message = "Market price updated for ${symbol.trim().uppercase(Locale.US)}" },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("UPDATE", color = Color.Black, fontWeight = FontWeight.Bold) }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onStart, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Cyan), shape = RoundedCornerShape(22.dp)) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black); Spacer(Modifier.width(5.dp)); Text("Start", color = Color.Black)
            }
            OutlinedButton(onClick = onStop, modifier = Modifier.weight(1f), colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Gold), shape = RoundedCornerShape(22.dp)) {
                Icon(Icons.Default.Stop, null); Spacer(Modifier.width(5.dp)); Text("Stop")
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onOpenOrders, modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp)) { Text("View Orders") }
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp)) { Icon(Icons.Default.DeleteSweep, null); Spacer(Modifier.width(5.dp)); Text("Reset") }
        }
        Spacer(Modifier.height(18.dp))
        Text("Virtual capital: ${money(initialCapital)}", color = Muted, fontSize = 12.sp)
        if (lastTrade != null) {
            Spacer(Modifier.height(4.dp))
            Text("Last: ${lastTrade.side} ${lastTrade.quantity} ${lastTrade.symbol} @ ${money(lastTrade.price)}", color = Cyan, fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text("Module 2 • Paper Ledger + Mark-to-Market", color = Cyan, fontSize = 13.sp)
        Text("Orders and positions persist locally. Live market data and automated strategy execution remain isolated for the next module.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun MetricCard(title: String, value: String, valueColor: Color, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OrdersScreen(orders: List<PaperOrder>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Bg).padding(18.dp)) {
        Text("Orders", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Paper execution ledger • ${orders.size} total", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        if (orders.isEmpty()) EmptyState("No paper orders yet", "Place a BUY or SELL order from Home.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(orders, key = { it.id }) { OrderCard(it) } }
    }
}

@Composable
private fun PositionsScreen(positions: List<Position>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Bg).padding(18.dp)) {
        Text("Positions", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Open holdings • mark-to-market P&L", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        if (positions.isEmpty()) EmptyState("No open positions", "Place a BUY order to create a paper position.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(positions, key = { it.symbol }) { position ->
                val unrealized = unrealizedPnl(position)
                val total = position.realizedPnl + unrealized
                Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(position.symbol, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(if (total >= 0) "+${money(total)}" else money(total), color = if (total >= 0) Green else Red, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text("Qty ${position.quantity} • Avg ${money(position.averagePrice)} • LTP ${if (position.markPrice > 0) money(position.markPrice) else "--"}", color = Cyan, fontSize = 13.sp)
                        Text("Unrealized ${money(unrealized)} • Realized ${money(position.realizedPnl)}", color = if (total >= 0) Green else Red, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: PaperOrder) {
    val date = remember(order.time) { SimpleDateFormat("dd MMM HH:mm:ss", Locale.US).format(Date(order.time)) }
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${order.side} ${order.symbol}", color = if (order.side == Side.BUY) Green else Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Qty ${order.quantity} @ ${money(order.price)}", color = Color.White, fontSize = 13.sp)
                Text(date, color = Muted, fontSize = 11.sp)
            }
            Text("PAPER", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
    }
}

private fun money(value: Double): String = "₹${String.format(Locale.US, "%,.2f", value)}"
