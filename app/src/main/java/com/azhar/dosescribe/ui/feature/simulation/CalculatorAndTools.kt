package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────
// Calculator menu — tile grid; tapping a tile opens that tool
// ─────────────────────────────────────────────────────────────────
private data class CalcTool(val key: String, val title: String, val description: String)

private val ALL_CALC_TOOLS = listOf(
    CalcTool("powder_grams", "Powder / Grams Needed", "g of powder for solution"),
    CalcTool("ped_dose", "Pediatric Dose", "mg/kg/day calculation"),
    CalcTool("ped_weight", "Pediatric Weight-Based", "weight × mg/kg → dose"),
    CalcTool("chemo_bsa", "Chemo BSA-Based Dose", "BSA × mg/m²"),
    CalcTool("bsa_calc", "BSA Calculator", "Du Bois formula"),
    CalcTool("hepatic_adj", "Hepatic Dose Adjustment", "Child-Pugh class"),
    CalcTool("renal_dose", "Renal Dosing", "CrCl-based adjustment"),
    CalcTool("dose_adj", "Dose Adjustment", "% reduction"),
    CalcTool("iv_fluid_rate", "IV Fluid Rate", "mL/hr → drops/min"),
    CalcTool("iv_kcl", "IV Potassium Prep", "KCl mEq → mL"),
    CalcTool("electrolyte", "Electrolyte Replacement", "Na/K/Mg/Ca"),
    CalcTool("sodium_correction", "Safe Sodium Correction", "≤ 8–10 mEq/L per 24h"),
    CalcTool("vanco_initial", "Vancomycin Initial Dose", "mg/kg loading"),
    CalcTool("vanco_trough", "Vancomycin Trough Review", "interpret levels"),
    CalcTool("vol_from_stock", "Volume from Stock", "C1V1 = C2V2"),
    CalcTool("total_volume", "Total Volume Needed", "mg ÷ concentration"),
    CalcTool("tablet_count", "Tablet Count", "total dose ÷ strength")
)

@Composable
fun CalculatorMenuDialog(vm: SimulationViewModel, onClose: () -> Unit) {
    var openedTool by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.98f).fillMaxHeight(0.98f),
            shape = RoundedCornerShape(16.dp),
            color = SimSurface,
            shadowElevation = 14.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                // Compact title row + tabs
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when {
                            openedTool != null -> "Calculator"
                            tab == 0 -> "Calculator"
                            else -> "Clinical Calculators"
                        },
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), color = SimDeepBlue
                    )
                    if (openedTool != null)
                        TextButton(onClick = { openedTool = null }) { Text("← All tools") }
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "close") }
                }
                if (openedTool == null) {
                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = Color.White,
                        contentColor = SimDeepBlue,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Tab(selected = tab == 0, onClick = { tab = 0 },
                            text = { Text("Calculator", fontSize = 12.sp) })
                        Tab(selected = tab == 1, onClick = { tab = 1 },
                            text = { Text("Clinical Tools", fontSize = 12.sp) })
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))

                when {
                    openedTool != null -> {
                        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            CalculatorTool(
                                key = openedTool!!,
                                onResult = { inputs, result ->
                                    vm.logCalculator(
                                        tool = ALL_CALC_TOOLS.first { it.key == openedTool }.title,
                                        inputs = inputs,
                                        result = result
                                    )
                                }
                            )
                        }
                    }
                    tab == 0 -> {
                        // Tight padding so the calculator gets MAX usable space
                        Box(Modifier.fillMaxSize().padding(8.dp)) {
                            BasicCalculator(onResult = { expression, result ->
                                vm.logCalculator("Basic Calculator", mapOf("expression" to expression), result)
                            })
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 180.dp),
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ALL_CALC_TOOLS) { tool ->
                                CalcTile(tool) { openedTool = tool.key }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Basic general-purpose calculator — rewritten for clarity:
//   • Large 5×4 button grid that fills the available space
//   • High-contrast colors (dark display, big white digits)
//   • Each operator clearly labelled
//   • Real-time live result + final = commit
// ─────────────────────────────────────────────────────────────────
@Composable
fun BasicCalculator(onResult: (String, String) -> Unit = { _, _ -> }) {
    var expression by remember { mutableStateOf("") }
    var liveResult by remember { mutableStateOf("0") }

    fun recompute() {
        liveResult = if (expression.isBlank()) "0" else (evalExpression(expression) ?: "…")
    }

    fun append(s: String) {
        val ops = setOf('+', '−', '×', '÷')
        val last = expression.lastOrNull()
        val incoming = s.firstOrNull()
        // If user taps two operators in a row, replace the previous one
        // (typical calculator behavior).
        if (incoming != null && incoming in ops && last != null && last in ops) {
            expression = expression.dropLast(1) + s
        } else if (s == "." ) {
            // Prevent two dots in the same number.
            val lastNumStart = expression.indexOfLast { it in ops } + 1
            val curNum = expression.substring(lastNumStart)
            if (!curNum.contains('.')) expression += s
        } else {
            expression += s
        }
        recompute()
    }

    fun clear() { expression = ""; liveResult = "0" }
    fun backspace() {
        if (expression.isNotEmpty()) expression = expression.dropLast(1)
        recompute()
    }
    fun equals() {
        val r = evalExpression(expression)
        if (r != null) {
            onResult(expression, r)
            expression = r
            liveResult = r
        } else {
            liveResult = "Error"
        }
    }

    // Layout: buttons on the LEFT (60% width), display on the RIGHT (40%).
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── LEFT: Buttons grid (5 rows × 4 cols) ──
        data class Key(val label: String, val tone: CalcKeyTone, val action: () -> Unit)
        val rows: List<List<Key>> = listOf(
            listOf(
                Key("C",  CalcKeyTone.DANGER)  { clear() },
                Key("⌫",  CalcKeyTone.DANGER)  { backspace() },
                Key("(",  CalcKeyTone.NEUTRAL) { append("(") },
                Key(")",  CalcKeyTone.NEUTRAL) { append(")") }
            ),
            listOf(
                Key("7", CalcKeyTone.DIGIT)  { append("7") },
                Key("8", CalcKeyTone.DIGIT)  { append("8") },
                Key("9", CalcKeyTone.DIGIT)  { append("9") },
                Key("÷", CalcKeyTone.PRIMARY){ append("÷") }
            ),
            listOf(
                Key("4", CalcKeyTone.DIGIT)  { append("4") },
                Key("5", CalcKeyTone.DIGIT)  { append("5") },
                Key("6", CalcKeyTone.DIGIT)  { append("6") },
                Key("×", CalcKeyTone.PRIMARY){ append("×") }
            ),
            listOf(
                Key("1", CalcKeyTone.DIGIT)  { append("1") },
                Key("2", CalcKeyTone.DIGIT)  { append("2") },
                Key("3", CalcKeyTone.DIGIT)  { append("3") },
                Key("−", CalcKeyTone.PRIMARY){ append("−") }
            ),
            listOf(
                Key("0", CalcKeyTone.DIGIT)  { append("0") },
                Key(".", CalcKeyTone.DIGIT)  { append(".") },
                Key("=", CalcKeyTone.EQUALS) { equals() },
                Key("+", CalcKeyTone.PRIMARY){ append("+") }
            )
        )
        Column(
            modifier = Modifier.weight(0.6f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { k ->
                        CalcKey(
                            label = k.label,
                            tone = k.tone,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = k.action
                        )
                    }
                }
            }
        }

        // ── RIGHT: Display ──
        Surface(
            modifier = Modifier.weight(0.4f).fillMaxHeight(),
            color = Color(0xFF0F1B2A),
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 4.dp
        ) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (expression.isBlank()) " " else expression,
                    color = Color(0xFFBFD0E2),
                    fontSize = 16.sp,
                    maxLines = 4,
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = liveResult,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private enum class CalcKeyTone { DIGIT, PRIMARY, DANGER, NEUTRAL, EQUALS }

@Composable
private fun CalcKey(
    label: String,
    modifier: Modifier = Modifier,
    tone: CalcKeyTone,
    onClick: () -> Unit
) {
    val bg = when (tone) {
        CalcKeyTone.PRIMARY -> SimDeepBlue
        CalcKeyTone.EQUALS  -> Color(0xFF1E88E5)
        CalcKeyTone.DANGER  -> Color(0xFFFDECEC)
        CalcKeyTone.NEUTRAL -> Color(0xFFE9EEF5)
        CalcKeyTone.DIGIT   -> Color.White
    }
    val fg = when (tone) {
        CalcKeyTone.PRIMARY, CalcKeyTone.EQUALS -> Color.White
        CalcKeyTone.DANGER  -> SimDanger
        CalcKeyTone.NEUTRAL -> Color(0xFF2A2A2A)
        CalcKeyTone.DIGIT   -> Color(0xFF1A2230)
    }
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 3.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = fg,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tiny expression evaluator: supports + − × ÷, decimals and parentheses.
 * Returns formatted result string, or null on parse / math error.
 */
private fun evalExpression(raw: String): String? {
    if (raw.isBlank()) return null
    val src = raw.replace('×', '*').replace('÷', '/').replace('−', '-')
    // Recursive descent parser
    return try {
        val parser = ExprParser(src)
        val v = parser.parseExpr()
        if (!parser.atEnd()) return null
        formatNumber(v)
    } catch (_: Throwable) { null }
}

private class ExprParser(private val s: String) {
    private var i = 0
    fun atEnd(): Boolean { skip(); return i >= s.length }
    private fun skip() { while (i < s.length && s[i].isWhitespace()) i++ }
    private fun peek(): Char? { skip(); return if (i < s.length) s[i] else null }
    private fun eat(): Char { skip(); return s[i++] }

    fun parseExpr(): Double {
        var v = parseTerm()
        while (true) {
            val c = peek() ?: return v
            if (c == '+' || c == '-') { eat(); val r = parseTerm(); v = if (c == '+') v + r else v - r }
            else return v
        }
    }
    private fun parseTerm(): Double {
        var v = parseFactor()
        while (true) {
            val c = peek() ?: return v
            if (c == '*' || c == '/') {
                eat()
                val r = parseFactor()
                v = if (c == '*') v * r else {
                    if (r == 0.0) throw ArithmeticException("÷0"); v / r
                }
            } else return v
        }
    }
    private fun parseFactor(): Double {
        val c = peek() ?: throw IllegalStateException("expected number")
        if (c == '+') { eat(); return parseFactor() }
        if (c == '-') { eat(); return -parseFactor() }
        if (c == '(') {
            eat()
            val v = parseExpr()
            if (peek() != ')') throw IllegalStateException("missing )")
            eat()
            return v
        }
        // number
        skip()
        var j = i
        while (j < s.length && (s[j].isDigit() || s[j] == '.')) j++
        if (j == i) throw IllegalStateException("expected number")
        val num = s.substring(i, j).toDouble()
        i = j
        return num
    }
}

private fun formatNumber(d: Double): String {
    if (d.isNaN() || d.isInfinite()) return "Error"
    if (d == d.toLong().toDouble() && kotlin.math.abs(d) < 1e15) return d.toLong().toString()
    val s = "%.6f".format(d).trimEnd('0').trimEnd('.')
    return if (s.isEmpty()) "0" else s
}

@Composable
private fun CalcTile(tool: CalcTool, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(tool.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SimDeepBlue)
            Spacer(Modifier.height(4.dp))
            Text(tool.description, fontSize = 11.sp, color = SimMuted)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Compact unified calculator surface — formula-driven, real-time
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CalculatorTool(key: String, onResult: (Map<String, String>, String) -> Unit) {
    when (key) {
        "powder_grams" -> SimpleTwoFieldCalc(
            "Final concentration (mg/mL)", "Final volume (mL)", "Powder needed (g)",
            onResult
        ) { c, v -> (c * v / 1000.0) }

        "ped_dose", "ped_weight" -> SimpleTwoFieldCalc(
            "Weight (kg)", "Dose (mg/kg/day)", "Daily dose (mg)", onResult
        ) { w, d -> w * d }

        "chemo_bsa" -> SimpleTwoFieldCalc(
            "BSA (m²)", "Dose (mg/m²)", "Dose (mg)", onResult
        ) { b, d -> b * d }

        "bsa_calc" -> SimpleTwoFieldCalc(
            "Height (cm)", "Weight (kg)", "BSA (m²) [Du Bois]", onResult
        ) { h, w -> 0.007184 * Math.pow(h, 0.725) * Math.pow(w, 0.425) }

        "hepatic_adj" -> SimpleTwoFieldCalc(
            "Standard dose (mg)", "Reduction % (Child-Pugh)", "Adjusted dose (mg)", onResult
        ) { dose, pct -> dose * (1.0 - pct / 100.0) }

        "renal_dose" -> SimpleTwoFieldCalc(
            "Standard dose (mg)", "CrCl factor (e.g. 0.5)", "Adjusted dose (mg)", onResult
        ) { d, f -> d * f }

        "dose_adj" -> SimpleTwoFieldCalc(
            "Current dose (mg)", "Adjustment %", "New dose (mg)", onResult
        ) { d, p -> d * (1.0 + p / 100.0) }

        "iv_fluid_rate" -> SimpleTwoFieldCalc(
            "Volume (mL)", "Time (hours)", "Rate (mL/hr)", onResult
        ) { v, t -> v / t }

        "iv_kcl" -> SimpleTwoFieldCalc(
            "KCl needed (mEq)", "Stock concentration (mEq/mL)", "Volume to draw (mL)", onResult
        ) { meq, conc -> meq / conc }

        "electrolyte" -> SimpleTwoFieldCalc(
            "Deficit (mEq)", "Replacement rate (mEq/hr)", "Hours to infuse", onResult
        ) { d, r -> d / r }

        "sodium_correction" -> SimpleTwoFieldCalc(
            "Current Na (mEq/L)", "Target Na (mEq/L)", "Daily change cap (mEq/L)", onResult
        ) { _, _ -> 8.0 }

        "vanco_initial" -> SimpleTwoFieldCalc(
            "Weight (kg)", "Loading dose (mg/kg)", "Loading dose (mg)", onResult
        ) { w, d -> w * d }

        "vanco_trough" -> VancoTroughTool(onResult)

        "vol_from_stock" -> ThreeFieldCalc(
            "Stock conc. (mg/mL)", "Desired dose (mg)", "Final volume (mL)",
            "Volume of stock (mL)", onResult
        ) { c1, dose, _ -> dose / c1 }

        "total_volume" -> SimpleTwoFieldCalc(
            "Dose needed (mg)", "Concentration (mg/mL)", "Volume (mL)", onResult
        ) { d, c -> d / c }

        "tablet_count" -> SimpleTwoFieldCalc(
            "Total daily dose (mg)", "Tablet strength (mg)", "Tablets per day", onResult
        ) { d, s -> d / s }

        else -> Text("Tool not implemented yet.")
    }
}

@Composable
private fun SimpleTwoFieldCalc(
    label1: String,
    label2: String,
    resultLabel: String,
    onResult: (Map<String, String>, String) -> Unit,
    formula: (Double, Double) -> Double
) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    val result = remember(a, b) {
        val da = a.toDoubleOrNull(); val db = b.toDoubleOrNull()
        if (da != null && db != null) {
            val r = formula(da, db)
            onResult(mapOf(label1 to a, label2 to b), "%.3f".format(r))
            "%.3f".format(r)
        } else "—"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text(label1) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text(label2) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SimDeepBlue,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(resultLabel, color = Color.White.copy(0.85f), fontSize = 12.sp)
                Text(result, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ThreeFieldCalc(
    label1: String, label2: String, label3: String, resultLabel: String,
    onResult: (Map<String, String>, String) -> Unit,
    formula: (Double, Double, Double) -> Double
) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    val result = remember(a, b, c) {
        val da = a.toDoubleOrNull(); val db = b.toDoubleOrNull(); val dc = c.toDoubleOrNull()
        if (da != null && db != null && dc != null) {
            val r = formula(da, db, dc)
            onResult(mapOf(label1 to a, label2 to b, label3 to c), "%.3f".format(r))
            "%.3f".format(r)
        } else "—"
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text(label1) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text(label2) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text(label3) }, modifier = Modifier.fillMaxWidth())
        Surface(modifier = Modifier.fillMaxWidth(), color = SimDeepBlue, shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text(resultLabel, color = Color.White.copy(0.85f), fontSize = 12.sp)
                Text(result, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VancoTroughTool(onResult: (Map<String, String>, String) -> Unit) {
    var trough by remember { mutableStateOf("") }
    val interp = remember(trough) {
        val v = trough.toDoubleOrNull()
        val out = when {
            v == null -> "—"
            v < 10 -> "Sub-therapeutic — increase dose / shorten interval."
            v in 10.0..15.0 -> "Standard target for most infections."
            v in 15.0..20.0 -> "Target for severe MRSA infections."
            else -> "Supra-therapeutic — reduce dose / extend interval; check renal function."
        }
        if (v != null) onResult(mapOf("Trough (mg/L)" to trough), out)
        out
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = trough, onValueChange = { trough = it },
            label = { Text("Measured trough (mg/L)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth())
        Surface(modifier = Modifier.fillMaxWidth(), color = SimDeepBlue, shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("Interpretation", color = Color.White.copy(0.85f), fontSize = 12.sp)
                Text(interp, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Patient Files — search existing + add new patient
// ─────────────────────────────────────────────────────────────────
@Composable
fun PatientFilesDialog(vm: SimulationViewModel, onClose: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var addOpen by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<StoredPatient?>(null) }

    val list = vm.storedPatients.filter { query.isBlank() || it.name.contains(query, true) }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.78f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Patient Files", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), color = SimDeepBlue)
                    Button(
                        onClick = { addOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add New Patient", fontSize = 12.sp)
                    }
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "close") }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))

                Row(Modifier.fillMaxSize()) {
                    // Left: search list
                    Column(Modifier.weight(1f).padding(12.dp)) {
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            placeholder = { Text("Search patient…", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Filled.Search, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        if (list.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No patients yet. Add one to begin.", color = SimMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(list) { p ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { selected = p },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selected?.id == p.id) SimSurface else Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(1.dp)
                                    ) {
                                        Column(Modifier.padding(10.dp)) {
                                            Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("${p.age} · ${p.gender} · ${p.city}",
                                                fontSize = 11.sp, color = SimMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    VerticalDivider()

                    // Right: profile detail
                    Column(Modifier.weight(1f).padding(12.dp)) {
                        if (selected == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Select a patient to view profile.", color = SimMuted, fontSize = 12.sp)
                            }
                        } else {
                            val p = selected!!
                            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${p.age} · ${p.gender}", fontSize = 12.sp, color = SimMuted)
                            Spacer(Modifier.height(8.dp))
                            Text("City: ${p.city}", fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Notes:", fontSize = 12.sp, color = SimMuted)
                            Text(p.notes.ifBlank { "—" }, fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = { vm.selectPatient(p); onClose() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Select This Patient")
                            }
                        }
                    }
                }

                if (addOpen) AddPatientDialog(
                    onClose = { addOpen = false },
                    onSave = { vm.addPatient(it); selected = it; addOpen = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPatientDialog(onClose: () -> Unit, onSave: (StoredPatient) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var genderOpen by remember { mutableStateOf(false) }
    var allergies by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    var meds by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val nameError = name.isBlank()

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.92f)
                .imePadding(),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add New Patient", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), color = SimDeepBlue)
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "close") }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Scrollable form body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Patient Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        isError = name.isNotEmpty() && nameError
                    )
                    OutlinedTextField(
                        value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    ExposedDropdownMenuBox(expanded = genderOpen, onExpandedChange = { genderOpen = it }) {
                        OutlinedTextField(
                            value = gender, onValueChange = {}, readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { Icon(if (genderOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(expanded = genderOpen, onDismissRequest = { genderOpen = false }) {
                            listOf("Male", "Female").forEach { g ->
                                DropdownMenuItem(text = { Text(g) },
                                    onClick = { gender = g; genderOpen = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = city, onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = allergies, onValueChange = { allergies = it },
                        label = { Text("Allergies") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = history, onValueChange = { history = it },
                        label = { Text("Medical History") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = meds, onValueChange = { meds = it },
                        label = { Text("Current Medications") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = notes, onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // Fixed action row
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClose) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = !nameError,
                        onClick = {
                            val combinedNotes = buildString {
                                if (allergies.isNotBlank()) append("Allergies: $allergies\n")
                                if (history.isNotBlank()) append("History: $history\n")
                                if (meds.isNotBlank()) append("Current Meds: $meds\n")
                                if (notes.isNotBlank()) append(notes)
                            }.trim()
                            onSave(
                                StoredPatient(
                                    id = "p_${System.currentTimeMillis()}",
                                    name = name, age = age, gender = gender,
                                    city = city, notes = combinedNotes
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Save Patient") }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Clinical Reference (Books)
// ─────────────────────────────────────────────────────────────────
private val CLINICAL_REFS = listOf(
    "Pharmacology Handbook" to "Quick reference for drug class, mechanism, indications and adverse effects.",
    "Drug Interactions Guide" to "Major / moderate interactions with mechanism and management.",
    "Pediatric Dosing" to "Weight-based dosing for common pediatric medications.",
    "Renal Dose Adjustments" to "Dose modifications by CrCl ranges.",
    "Hepatic Dose Adjustments" to "Modifications by Child-Pugh class.",
    "Auxiliary Label Guide" to "When to apply each auxiliary warning label.",
    "Counseling Pearls" to "Patient education points for common medications."
)

@Composable
fun ClinicalReferenceDialog(onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clinical Reference", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), color = SimDeepBlue)
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "close") }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CLINICAL_REFS.size) { idx ->
                        val (title, desc) = CLINICAL_REFS[idx]
                        Card(colors = CardDefaults.cardColors(containerColor = SimSurface)) {
                            Column(Modifier.padding(10.dp)) {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(desc, fontSize = 11.sp, color = SimMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

