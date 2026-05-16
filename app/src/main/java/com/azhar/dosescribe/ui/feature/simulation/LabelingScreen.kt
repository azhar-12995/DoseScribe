package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────
// Labeling screen — form on left, LIVE label preview on right
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LabelingScreen(vm: SimulationViewModel, onClose: () -> Unit) {
    val rx = vm.case?.prescription ?: return
    val available = vm.selectedDrugs.toList()

    val editing = vm.editingLabelId?.let { id -> vm.completedLabels.firstOrNull { it.id == id } }
    val initialDrugId = editing?.drugId
        ?: vm.labelingPrefillDrugId
        ?: available.firstOrNull()?.id
        ?: ""

    var selectedDrugId by remember { mutableStateOf(initialDrugId) }
    var qty by remember { mutableStateOf(editing?.quantity ?: "") }
    var dose by remember { mutableStateOf(editing?.dose ?: "") }
    var direction by remember { mutableStateOf(editing?.direction ?: "") }
    var duration by remember { mutableStateOf(editing?.duration ?: "") }
    var aux by remember { mutableStateOf(editing?.auxLabels?.toSet() ?: emptySet()) }
    var auxOpen by remember { mutableStateOf(false) }

    // Top-of-form action mode: Dispense (build label) vs Hold/Review
    var mode by remember { mutableStateOf(LabelMode.DISPENSE) }

    // Inline error for "duplicate label per drug"
    var duplicateError by remember { mutableStateOf<String?>(null) }
    // "Label saved" success indicator (shown briefly after save).
    var savedAt by remember { mutableStateOf(0L) }
    LaunchedEffect(savedAt) {
        if (savedAt > 0L) {
            kotlinx.coroutines.delay(2200)
            // Only clear if the user hasn't started another save in the meantime.
            if (System.currentTimeMillis() - savedAt >= 2100) savedAt = 0L
        }
    }

    val selectedDrug = available.firstOrNull { it.id == selectedDrugId }
    val isEditing = editing != null

    // If user switches drug, clear duplicate error
    LaunchedEffect(selectedDrugId) { duplicateError = null }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.94f)
                .imePadding(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isEditing) "Edit Label" else "Build a Label",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.weight(1f), color = SimDeepBlue
                    )
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "close") }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))

                Row(modifier = Modifier.fillMaxSize()) {
                    // ─── LEFT: scrollable form ───
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ── Top mode toggle: Dispense / Hold Review ──
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFF1F4F8)
                        ) {
                            Row(Modifier.padding(4.dp)) {
                                ModeChip(
                                    text = "Dispense",
                                    selected = mode == LabelMode.DISPENSE,
                                    modifier = Modifier.weight(1f)
                                ) { mode = LabelMode.DISPENSE }
                                ModeChip(
                                    text = "Hold / Review",
                                    selected = mode == LabelMode.HOLD,
                                    modifier = Modifier.weight(1f)
                                ) { mode = LabelMode.HOLD }
                            }
                        }

                        // ── Drug picker (used in both modes) ──
                        Text("Drug", fontSize = 11.sp, color = SimMuted)
                        var drugMenuOpen by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = drugMenuOpen, onExpandedChange = { drugMenuOpen = it }) {
                            OutlinedTextField(
                                value = selectedDrug?.let { "${it.name} ${it.strength}" } ?: "Select drug",
                                onValueChange = {}, readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = { Icon(if (drugMenuOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null) }
                            )
                            ExposedDropdownMenu(expanded = drugMenuOpen, onDismissRequest = { drugMenuOpen = false }) {
                                available.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text("${d.name} ${d.strength}") },
                                        onClick = { selectedDrugId = d.id; drugMenuOpen = false }
                                    )
                                }
                            }
                        }

                        if (mode == LabelMode.DISPENSE) {
                            // ── Dispense form ──
                            OutlinedTextField(value = qty, onValueChange = { qty = it },
                                label = { Text("Quantity (e.g. 20 tablets)") },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = dose, onValueChange = { dose = it },
                                label = { Text("Dose (e.g. 500 mg)") },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = direction, onValueChange = { direction = it },
                                label = { Text("Direction (e.g. 1 tab PO q6h PRN)") },
                                modifier = Modifier.fillMaxWidth(), minLines = 2,
                                shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = duration, onValueChange = { duration = it },
                                label = { Text("Duration (e.g. 7 days)") },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                shape = RoundedCornerShape(10.dp))

                            // ── Auxiliary labels (button trigger + chips) ──
                            Text("Auxiliary labels", fontSize = 11.sp, color = SimMuted)
                            OutlinedButton(
                                onClick = { auxOpen = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SimDeepBlue.copy(alpha = 0.4f))
                            ) {
                                Icon(Icons.Filled.LocalOffer, null,
                                    tint = SimDeepBlue, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (aux.isEmpty()) "Tap to add auxiliary labels…"
                                    else "${aux.size} auxiliary label(s) selected",
                                    color = SimDeepBlue
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Filled.ExpandMore, null, tint = SimDeepBlue)
                            }
                            if (aux.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    aux.forEach { label ->
                                        AuxChip(label = label, onRemove = { aux = aux - label })
                                    }
                                }
                            }
                            if (auxOpen) {
                                AuxLabelPickerDialog(
                                    selected = aux,
                                    onToggle = { lbl ->
                                        aux = if (aux.contains(lbl)) aux - lbl else aux + lbl
                                    },
                                    onClose = { auxOpen = false }
                                )
                            }

                            if (duplicateError != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Warning, null,
                                            tint = SimDanger, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(duplicateError!!, fontSize = 12.sp, color = SimDanger)
                                    }
                                }
                            }

                            // ── "Saved ✓" success banner — visible after each save ──
                            if (savedAt > 0L) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = SimSuccess.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, SimSuccess.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            androidx.compose.material.icons.Icons.Filled.CheckCircle, null,
                                            tint = SimSuccess, modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Label saved. Review it on the right ➜",
                                            fontSize = 12.sp, color = SimSuccess,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            // Primary Save action — KEEPS the form populated so
                            // the live preview stays visible after saving.
                            Button(
                                onClick = {
                                    val d = selectedDrug ?: return@Button
                                    val existing = vm.completedLabels.firstOrNull { it.drugId == d.id }
                                    if (existing != null && existing.id != vm.editingLabelId) {
                                        duplicateError =
                                            "Only one label allowed per drug. Tap the existing label below to edit it."
                                        return@Button
                                    }
                                    vm.addLabel(
                                        CompletedLabel(
                                            drugId = d.id, drugName = d.name,
                                            quantity = qty, dose = dose, direction = direction,
                                            duration = duration, auxLabels = aux.toList()
                                        )
                                    )
                                    duplicateError = null
                                    savedAt = System.currentTimeMillis()
                                    // ── DO NOT clear the form ── so the preview
                                    // continues to show the just-saved label.
                                    // The user can hit "New Label" below to start fresh.
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (isEditing) "Save Changes" else "Save Label",
                                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            // Secondary "Start a new label" — clears the form
                            OutlinedButton(
                                onClick = {
                                    qty = ""; dose = ""; direction = ""
                                    duration = ""; aux = emptySet()
                                    duplicateError = null
                                    savedAt = 0L
                                    // Reset editing context (so the next save creates
                                    // a fresh label instead of overwriting one).
                                    vm.openLabeling()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, SimDeepBlue.copy(alpha = 0.40f))
                            ) {
                                Text("Start a New Label", color = SimDeepBlue,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            // ── HOLD/REVIEW mode ──
                            var holdReason by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = holdReason, onValueChange = { holdReason = it },
                                label = { Text("Reason for hold (clinical)") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                minLines = 3,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    val d = selectedDrug ?: return@Button
                                    vm.addHold(HoldEntry(drugId = d.id, drugName = d.name, reason = holdReason))
                                    holdReason = ""
                                    onClose()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SimDanger),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Filled.Warning, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Hold / Do Not Dispense", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // ─── RIGHT: live label preview + saved-labels dropdown ───
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(12.dp)
                    ) {
                        // The preview now fills the FULL width and height of
                        // the right pane (no inner shrinking) so every keystroke
                        // is large and immediately visible.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            LabelPreview(
                                patientName = vm.selectedPatient?.name ?: rx.patientName,
                                drugName = selectedDrug?.let { "${it.name} ${it.strength}" } ?: "—",
                                qty = qty,
                                dose = dose,
                                direction = direction,
                                duration = duration,
                                aux = aux.toList(),
                                doctor = rx.doctorName,
                                date = rx.date
                            )
                        }

                        // ── Saved labels dropdown UNDER the preview ──
                        Spacer(Modifier.height(10.dp))
                        SavedLabelsDropdown(vm = vm)
                    }
                }
            }
        }
    }
}

private enum class LabelMode { DISPENSE, HOLD }

@Composable
private fun ModeChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        color = if (selected) SimDeepBlue else Color.Transparent,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (selected) Color.White else Color(0xFF333333),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedLabelsDropdown(vm: SimulationViewModel) {
    var open by remember { mutableStateOf(true) }
    val labels = vm.completedLabels
    val holds = vm.holds
    // Build a per-drug status row for ALL selected drugs (so the user
    // sees which drugs are still missing a label).
    data class StatusRow(
        val drugId: String,
        val drugName: String,
        val status: String,         // "Completed" | "Held" | "Not made yet"
        val color: Color,
        val labelId: String?
    )
    val rows: List<StatusRow> = vm.selectedDrugs.map { d ->
        val l = labels.firstOrNull { it.drugId == d.id }
        val h = holds.firstOrNull { it.drugId == d.id }
        when {
            l != null -> StatusRow(d.id, d.name, "Completed", SimSuccess, l.id)
            h != null -> StatusRow(d.id, d.name, "Held",      SimDanger,  null)
            else      -> StatusRow(d.id, d.name, "Not made yet", SimMuted, null)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { open = !open },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, SimDeepBlue.copy(alpha = 0.4f))
        ) {
            Text(
                "Labels (${labels.size}/${vm.selectedDrugs.size} done)",
                color = SimDeepBlue, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            Icon(
                if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                null, tint = SimDeepBlue
            )
        }
        if (open) {
            Spacer(Modifier.height(6.dp))
            if (rows.isEmpty()) {
                Text("Pick drugs from the shelf first.",
                    fontSize = 11.sp, color = SimMuted,
                    modifier = Modifier.padding(8.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(rows) { row ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SimSurface,
                            border = BorderStroke(1.dp, row.color.copy(alpha = 0.40f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (row.labelId != null) vm.editLabel(row.labelId)
                                    else vm.openLabelingFor(row.drugId)
                                }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(row.drugName, fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold, color = SimDeepBlue)
                                    
                                    if (row.labelId != null) {
                                        val l = labels.first { it.id == row.labelId }
                                        Text(
                                            "Qty: ${l.quantity.ifBlank { "—" }} · Dose: ${l.dose.ifBlank { "—" }}",
                                            fontSize = 10.sp, color = Color(0xFF555555)
                                        )
                                        if (l.auxLabels.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                l.auxLabels.forEach { text ->
                                                    val auxInfo = AuxiliaryLabels.WITH_ICONS.find { it.text == text }
                                                    if (auxInfo?.iconRes != null) {
                                                        Image(
                                                            painter = painterResource(auxInfo.iconRes),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text("—", fontSize = 10.sp, color = Color(0xFF555555))
                                    }
                                }
                                // Status pill
                                Surface(
                                    color = row.color.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        row.status,
                                        color = row.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuxChip(label: String, onRemove: () -> Unit) {
    val auxInfo = AuxiliaryLabels.WITH_ICONS.find { it.text == label }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SimDeepBlue.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, SimDeepBlue)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                .clickable { onRemove() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (auxInfo?.iconRes != null) {
                Image(
                    painter = painterResource(auxInfo.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(label, fontSize = 11.sp, color = SimDeepBlue, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.Close, null, tint = SimDeepBlue, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun AuxLabelPickerDialog(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Select auxiliary labels") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(AuxiliaryLabels.WITH_ICONS) { aux ->
                    val checked = selected.contains(aux.text)
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onToggle(aux.text) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        if (aux.iconRes != null) {
                            Image(
                                painter = painterResource(aux.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(aux.text, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue)) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun LabelPreview(
    patientName: String, drugName: String,
    qty: String, dose: String, direction: String,
    duration: String,
    aux: List<String>, doctor: String, date: String
) {
    // Fills the entire right pane so live changes are immediately visible.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(0.5.dp, Color(0xFFD0D0D0))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── "LIVE PREVIEW" header so the user knows updates appear here ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SimSuccess,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "● LIVE PREVIEW",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(date, fontSize = 10.sp, color = SimMuted)
            }
            Spacer(Modifier.height(8.dp))

            Text("DOSESCRIBE PHARMACY",
                fontWeight = FontWeight.Bold, color = SimDeepBlue, fontSize = 16.sp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LiveField("Patient", patientName)
            LiveField("Prescriber", doctor)
            Spacer(Modifier.height(10.dp))

            // Drug name — large
            Text("DRUG", fontSize = 10.sp, color = SimMuted, fontWeight = FontWeight.Bold)
            Text(
                drugName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A2230)
            )
            Spacer(Modifier.height(12.dp))

            LiveField("Quantity", qty)
            LiveField("Dose", dose)
            LiveField("Duration", duration)

            Spacer(Modifier.height(8.dp))
            Text("DIRECTIONS", fontSize = 11.sp,
                color = SimMuted, fontWeight = FontWeight.Bold)
            Surface(
                color = SimSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Text(
                    direction.ifBlank { "(type direction in the form)" },
                    fontSize = 14.sp,
                    color = if (direction.isBlank()) SimMuted else Color(0xFF1A2230),
                    fontWeight = if (direction.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (aux.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("AUXILIARY LABELS",
                    fontSize = 11.sp, color = SimMuted, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                aux.forEach { text ->
                    val auxInfo = AuxiliaryLabels.WITH_ICONS.find { it.text == text }
                    Surface(
                        color = SimDeepBlue.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (auxInfo?.iconRes != null) {
                                Image(
                                    painter = painterResource(auxInfo.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Text(
                                text,
                                fontSize = 12.sp,
                                color = SimDeepBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Single-line field row used by the live preview. */
@Composable
private fun LiveField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = SimMuted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        val empty = value.isBlank() || value == "—"
        Text(
            if (empty) "—" else value,
            fontSize = 14.sp,
            color = if (empty) SimMuted else Color(0xFF1A2230),
            fontWeight = if (empty) FontWeight.Normal else FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Hold / Review form
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldFormDialog(vm: SimulationViewModel, onClose: () -> Unit) {
    val available = vm.selectedDrugs.toList().ifEmpty {
        vm.case?.prescription?.items?.map { item ->
            CatalogDrug(item.expectedDrugId, item.displayName, "", DrugStorage.SHELF)
        } ?: emptyList()
    }
    var drugId by remember { mutableStateOf(available.firstOrNull()?.id ?: "") }
    var menuOpen by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Hold / Do Not Dispense") },
        text = {
            Column {
                Text("Select drug", fontSize = 11.sp, color = SimMuted)
                ExposedDropdownMenuBox(expanded = menuOpen, onExpandedChange = { menuOpen = it }) {
                    OutlinedTextField(
                        value = available.firstOrNull { it.id == drugId }?.name ?: "Select drug",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { Icon(if (menuOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null) }
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        available.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.name) },
                                onClick = { drugId = d.id; menuOpen = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason, onValueChange = { reason = it },
                    label = { Text("Reason for hold (clinical)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = available.firstOrNull { it.id == drugId } ?: return@Button
                    vm.addHold(HoldEntry(drugId = d.id, drugName = d.name, reason = reason))
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue)
            ) { Text("Save Hold") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel") } }
    )
}
