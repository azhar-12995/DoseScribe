package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────
// Cart panel
// ─────────────────────────────────────────────────────────────────
@Composable
fun CartPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Cart (${vm.cart.size})", onClose = onClose, modifier = modifier) {
        if (vm.cart.isEmpty()) {
            EmptyState("Cart is empty. Add drugs from the shelf, fridge, or safe.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.cart) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SimSurface),
                        modifier = Modifier.clickable { vm.openLabelingFor(item.drugId) }
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.drugName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(item.strength, color = SimMuted, fontSize = 11.sp)
                                Text("Tap to build label", color = SimDeepBlue, fontSize = 10.sp)
                            }
                            Text("x${item.quantity}", color = SimDeepBlue, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            IconButton(onClick = { vm.removeFromCart(item.drugId) }) {
                                Icon(Icons.Filled.Delete, null, tint = SimDanger, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Chat panel — pre-authored questions
// ─────────────────────────────────────────────────────────────────
@Composable
fun ChatPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Ask the Patient", onClose = onClose, modifier = modifier) {
        val questions = vm.case?.chatQuestions ?: emptyList()

        Text("Conversation", fontSize = 12.sp, color = SimMuted)
        Spacer(Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(vm.chatLog) { ex ->
                Column {
                    Box(
                        Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SimDeepBlue)
                            .padding(8.dp)
                    ) { Text(ex.question, color = Color.White, fontSize = 11.sp) }
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier
                            .align(Alignment.Start)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SimSurface)
                            .padding(8.dp)
                    ) { Text(ex.answer, color = Color(0xFF222222), fontSize = 11.sp) }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Spacer(Modifier.height(6.dp))
        Text("Tap a question to ask:", fontSize = 12.sp, color = SimMuted)
        Spacer(Modifier.height(6.dp))
        questions.forEach { qa ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clickable { vm.ask(qa.question) },
                shape = RoundedCornerShape(8.dp),
                color = SimSurface
            ) {
                Text(qa.question, fontSize = 11.sp, color = Color(0xFF333333),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Drugs panel — drugs the user has selected
// ─────────────────────────────────────────────────────────────────
@Composable
fun DrugsPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Selected Drugs (${vm.selectedDrugs.size})", onClose = onClose, modifier = modifier) {
        if (vm.selectedDrugs.isEmpty()) {
            EmptyState("No drugs selected yet. Tap the shelf, fridge, or safe to add drugs.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(vm.selectedDrugs) { drug ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SimSurface),
                        modifier = Modifier.clickable { vm.openLabelingFor(drug.id) }
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(drug.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                drug.strength + " · " + drug.storage.name.lowercase(),
                                color = SimMuted, fontSize = 11.sp
                            )
                            Text("Tap to build label", color = SimDeepBlue, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        SimPrimaryButton("New Label", icon = Icons.Filled.Add, onClick = { vm.openLabeling() })
    }
}

// ─────────────────────────────────────────────────────────────────
// Labels panel — newest on top
// ─────────────────────────────────────────────────────────────────
@Composable
fun LabelsPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Completed Labels (${vm.completedLabels.size})", onClose = onClose, modifier = modifier) {
        if (vm.completedLabels.isEmpty()) {
            EmptyState("No labels built yet. Open the labeling form from Drugs.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.completedLabels) { label ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SimSurface),
                        modifier = Modifier.clickable { vm.editLabel(label.id) }
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(label.drugName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimDeepBlue)
                            Text("Qty: ${label.quantity}", fontSize = 11.sp, color = Color(0xFF333333))
                            Text("Dose: ${label.dose}", fontSize = 11.sp, color = Color(0xFF333333))
                            Text("Direction: ${label.direction}", fontSize = 11.sp, color = Color(0xFF333333))
                            if (label.auxLabels.isNotEmpty()) {
                                Spacer(Modifier.height(2.dp))
                                Text("Aux: " + label.auxLabels.joinToString(),
                                    fontSize = 10.sp, color = SimMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Notes panel
// ─────────────────────────────────────────────────────────────────
@Composable
fun NotesPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Session Notes", onClose = onClose, modifier = modifier) {
        OutlinedTextField(
            value = vm.notes,
            onValueChange = { vm.updateNotes(it) },
            modifier = Modifier.fillMaxSize(),
            placeholder = { Text("Type your clinical thinking, holds, or counseling notes…", fontSize = 12.sp) },
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Reports panel — running summary of session
// ─────────────────────────────────────────────────────────────────
@Composable
fun ReportsPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Session Report", onClose = onClose, modifier = modifier) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { ReportRow("Drugs selected", vm.selectedDrugs.size.toString()) }
            item { ReportRow("Labels built", vm.completedLabels.size.toString()) }
            item { ReportRow("Holds raised", vm.holds.size.toString()) }
            item { ReportRow("Calculator uses", vm.calculatorLog.size.toString()) }
            item { ReportRow("Chat questions asked", vm.chatLog.size.toString()) }
            item { ReportRow("Notes length", "${vm.notes.length} chars") }
            item { HorizontalDivider() }
            item { Text("Holds", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            items(vm.holds) { h ->
                Card(colors = CardDefaults.cardColors(containerColor = SimSurface)) {
                    Column(Modifier.padding(8.dp)) {
                        Text(h.drugName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(h.reason, fontSize = 11.sp, color = Color(0xFF333333))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = SimMuted)
        Text(value, fontSize = 12.sp, color = SimDeepBlue, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = SimMuted, fontSize = 12.sp) }
}

// ─────────────────────────────────────────────────────────────────
// (Exit dialog removed — Hand Over replaces Exit as the only way out.)
// ─────────────────────────────────────────────────────────────────


