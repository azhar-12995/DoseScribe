package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.sqrt

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

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.92f),
            shape = RoundedCornerShape(12.dp),
            color = SimSurface,
            shadowElevation = 12.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (openedTool == null) "Calculators" else "Calculator",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), color = SimDeepBlue
                    )
                    if (openedTool != null)
                        TextButton(onClick = { openedTool = null }) { Text("← All tools") }
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "close") }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))

                if (openedTool == null) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ALL_CALC_TOOLS) { tool ->
                            CalcTile(tool) { openedTool = tool.key }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
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
            }
        }
    }
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

@Composable
private fun AddPatientDialog(onClose: () -> Unit, onSave: (StoredPatient) -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Add New Patient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(
                        StoredPatient("p_${System.currentTimeMillis()}", name, age, gender, city, notes)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel") } }
    )
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

