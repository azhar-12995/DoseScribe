package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────
// Labeling screen — form on left, LIVE label preview on right
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelingScreen(vm: SimulationViewModel, onClose: () -> Unit) {
    val rx = vm.case?.prescription ?: return
    val available = vm.selectedDrugs.toList()

    // Pre-fill drug from VM (set by Cart / Drugs / Storage entry points).
    // If editing a completed label, also pre-fill its values.
    val editing = vm.editingLabelId?.let { id -> vm.completedLabels.firstOrNull { it.id == id } }
    val initialDrugId = editing?.drugId
        ?: vm.labelingPrefillDrugId
        ?: available.firstOrNull()?.id
        ?: ""

    var selectedDrugId by remember { mutableStateOf(initialDrugId) }
    var qty by remember { mutableStateOf(editing?.quantity ?: "") }
    var dose by remember { mutableStateOf(editing?.dose ?: "") }
    var direction by remember { mutableStateOf(editing?.direction ?: "") }
    var aux by remember { mutableStateOf(editing?.auxLabels?.toSet() ?: emptySet()) }
    var auxOpen by remember { mutableStateOf(false) }

    val selectedDrug = available.firstOrNull { it.id == selectedDrugId }
    val isEditing = editing != null

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.92f),
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
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Drug", fontSize = 11.sp, color = SimMuted)
                        var drugMenuOpen by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = drugMenuOpen, onExpandedChange = { drugMenuOpen = it }) {
                            OutlinedTextField(
                                value = selectedDrug?.let { "${it.name} ${it.strength}" } ?: "Select drug",
                                onValueChange = {}, readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
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

                        OutlinedTextField(value = qty, onValueChange = { qty = it },
                            label = { Text("Quantity (e.g. 20 tablets)") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = dose, onValueChange = { dose = it },
                            label = { Text("Dose (e.g. 500 mg)") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = direction, onValueChange = { direction = it },
                            label = { Text("Direction (e.g. 1 tab PO q6h PRN)") },
                            modifier = Modifier.fillMaxWidth(), minLines = 2)

                        Text("Auxiliary labels", fontSize = 11.sp, color = SimMuted)
                        OutlinedTextField(
                            value = if (aux.isEmpty()) "Tap to choose…" else aux.joinToString(),
                            onValueChange = {}, readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { auxOpen = true },
                            trailingIcon = { Icon(Icons.Filled.ExpandMore, null) }
                        )
                        if (auxOpen) {
                            AlertDialog(
                                onDismissRequest = { auxOpen = false },
                                title = { Text("Select auxiliary labels") },
                                text = {
                                    LazyColumn {
                                        items(AuxiliaryLabels.ALL) { lbl ->
                                            val checked = aux.contains(lbl)
                                            Row(
                                                Modifier.fillMaxWidth().clickable {
                                                    aux = if (checked) aux - lbl else aux + lbl
                                                }.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(checked = checked, onCheckedChange = null)
                                                Spacer(Modifier.width(6.dp))
                                                Text(lbl, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { auxOpen = false }) { Text("Done") } }
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // ── Stack of completed labels (newest on top) ──
                        if (vm.completedLabels.isNotEmpty()) {
                            Text(
                                "Completed labels (${vm.completedLabels.size}) — tap to edit",
                                fontSize = 11.sp, color = SimMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(vm.completedLabels) { lbl ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (lbl.id == vm.editingLabelId) SimDeepBlue.copy(alpha = 0.10f)
                                        else SimSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { vm.editLabel(lbl.id) }
                                    ) {
                                        Column(Modifier.padding(8.dp)) {
                                            Text(
                                                lbl.drugName, fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SimDeepBlue
                                            )
                                            Text(
                                                "${lbl.quantity} · ${lbl.dose} · ${lbl.direction}",
                                                fontSize = 10.sp, color = Color(0xFF333333),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SimPrimaryButton(
                                label = if (isEditing) "Save Changes" else "Add Label",
                                onClick = {
                                    val d = selectedDrug ?: return@SimPrimaryButton
                                    vm.addLabel(
                                        CompletedLabel(
                                            drugId = d.id, drugName = d.name,
                                            quantity = qty, dose = dose, direction = direction,
                                            auxLabels = aux.toList()
                                        )
                                    )
                                    qty = ""; dose = ""; direction = ""; aux = emptySet()
                                }
                            )
                            OutlinedButton(onClick = { vm.openHoldForm() }, shape = RoundedCornerShape(20.dp)) {
                                Text("Hold / Review")
                            }
                        }
                    }

                    // Live preview
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SimSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        LabelPreview(
                            patientName = rx.patientName,
                            drugName = selectedDrug?.let { "${it.name} ${it.strength}" } ?: "—",
                            qty = qty.ifBlank { "—" },
                            dose = dose.ifBlank { "—" },
                            direction = direction.ifBlank { "—" },
                            aux = aux.toList(),
                            doctor = rx.doctorName,
                            date = rx.date
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelPreview(
    patientName: String, drugName: String,
    qty: String, dose: String, direction: String,
    aux: List<String>, doctor: String, date: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.85f),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(0.5.dp, Color(0xFFD0D0D0))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("DOSESCRIBE PHARMACY", fontWeight = FontWeight.Bold, color = SimDeepBlue, fontSize = 12.sp)
            Text("Rx Label · $date", fontSize = 9.sp, color = SimMuted)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            Text("Patient: $patientName", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("Prescriber: $doctor", fontSize = 10.sp, color = SimMuted)
            Spacer(Modifier.height(8.dp))
            Text(drugName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Quantity:  $qty", fontSize = 11.sp)
            Text("Dose:      $dose", fontSize = 11.sp)
            Spacer(Modifier.height(2.dp))
            Text("Directions:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(direction, fontSize = 11.sp)
            if (aux.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Auxiliary:", fontSize = 10.sp, color = SimMuted, fontWeight = FontWeight.SemiBold)
                aux.forEach { Text("• $it", fontSize = 10.sp, color = Color(0xFF333333)) }
            }
        }
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

