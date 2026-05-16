package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────
// Cart panel — three structured sections
// ─────────────────────────────────────────────────────────────────
@Composable
fun CartPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Cart Review", onClose = onClose, modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 1. Selected Drugs
            item { SectionHeader("Selected Drugs (${vm.cart.size})") }
            if (vm.cart.isEmpty()) {
                item { SectionEmpty("No drugs added. Tap the shelf, fridge, or safe.") }
            } else {
                items(vm.cart) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = SimSurface),
                        shape = RoundedCornerShape(10.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.drugName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(item.strength, color = SimMuted, fontSize = 11.sp)
                            }
                            IconButton(onClick = { vm.setCartQuantity(item.drugId, item.quantity - 1) },
                                modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Remove, null, tint = SimDeepBlue,
                                    modifier = Modifier.size(16.dp))
                            }
                            Text("${item.quantity}", color = SimDeepBlue,
                                fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 6.dp))
                            IconButton(onClick = { vm.setCartQuantity(item.drugId, item.quantity + 1) },
                                modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Add, null, tint = SimDeepBlue,
                                    modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = { vm.removeFromCart(item.drugId) },
                                modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Delete, null, tint = SimDanger,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 2. Selected Labels
            item { Spacer(Modifier.height(6.dp)); SectionHeader("Selected Labels (${vm.completedLabels.size})") }
            if (vm.completedLabels.isEmpty()) {
                item { SectionEmpty("No labels saved yet. Build labels from the Labels panel.") }
            } else {
                items(vm.completedLabels) { lbl ->
                    Card(colors = CardDefaults.cardColors(containerColor = SimSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { vm.editLabel(lbl.id) }) {
                        Column(Modifier.padding(10.dp)) {
                            Text(lbl.drugName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimDeepBlue)
                            Text("Qty: ${lbl.quantity}  ·  Dose: ${lbl.dose}", fontSize = 11.sp)
                            if (lbl.duration.isNotBlank())
                                Text("Duration: ${lbl.duration}", fontSize = 11.sp, color = SimMuted)
                            Text("Direction: ${lbl.direction}", fontSize = 11.sp, color = Color(0xFF333333))
                        }
                    }
                }
            }

            // 3. Auxiliary Labels (deduped union across labels)
            val auxUnion = vm.completedLabels.flatMap { it.auxLabels }.toSortedSet()
            item { Spacer(Modifier.height(6.dp)); SectionHeader("Auxiliary Labels (${auxUnion.size})") }
            if (auxUnion.isEmpty()) {
                item { SectionEmpty("No auxiliary labels selected.") }
            } else {
                items(auxUnion.toList()) { aux ->
                    Surface(shape = RoundedCornerShape(10.dp),
                        color = SimDeepBlue.copy(alpha = 0.08f)) {
                        Text("• $aux", fontSize = 11.sp, color = Color(0xFF333333),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Review the above and tap Hand Over when ready.",
                    fontSize = 10.sp, color = SimMuted, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(label, fontSize = 12.sp, color = SimDeepBlue, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
private fun SectionEmpty(text: String) {
    Surface(color = Color(0xFFF1F3F6), shape = RoundedCornerShape(8.dp)) {
        Text(text, fontSize = 11.sp, color = SimMuted,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
// Chat panel — "Ask About" redesign
// Each chip tap IMMEDIATELY appends a Q/A pair to the chat log and
// turns the chip blue. The panel stays open and only closes when the
// user explicitly taps the X (close), Cancel, or Done button.
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(
        title = "Ask About",
        onClose = onClose,
        modifier = modifier,
        width = 0.62f
    ) {
        val askList = remember { AskAboutCatalog.DEFAULT }
        // A chip is "selected" iff its question text is already in the chatLog.
        val askedTexts = vm.chatLog.map { it.question }.toSet()

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── LEFT: chat bubble feed ──
            Surface(
                modifier = Modifier.weight(1.05f).fillMaxHeight(),
                color = SimSurface,
                shape = RoundedCornerShape(12.dp)
            ) {
                val listState = rememberLazyListState()
                LaunchedEffect(vm.chatLog.size) {
                    if (vm.chatLog.isNotEmpty())
                        listState.animateScrollToItem(vm.chatLog.size - 1)
                }
                if (vm.chatLog.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Tap a question on the right\nto start the conversation.",
                            fontSize = 12.sp, color = SimMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vm.chatLog) { ex ->
                            ChatBubble(text = ex.question, isUser = true)
                            Spacer(Modifier.height(4.dp))
                            ChatBubble(text = ex.answer, isUser = false)
                        }
                    }
                }
            }

            // ── RIGHT: Ask-About chip grid + actions ──
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text(
                    "Tap any question to ask the patient",
                    fontSize = 12.sp, color = SimMuted,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        askList.forEach { q ->
                            QuestionChip(
                                title = q.title,
                                selected = q.questionText in askedTexts,
                                onClick = { vm.askAbout(q) }   // immediate send
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { vm.clearChat(); onClose() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Cancel") }
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionChip(title: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) SimDeepBlue else Color.White
    val fg = if (selected) Color.White else Color(0xFF333333)
    // Use Box+clickable instead of Material3 Surface(onClick) — avoids any
    // ambiguity in click-event handling inside the slide-in panel.
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = if (selected) SimDeepBlue else SimDeepBlue.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.QuestionAnswer, null,
                tint = if (selected) Color.White else SimDeepBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                title, fontSize = 11.sp, color = fg,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ChatBubble(text: String, isUser: Boolean) {
    val align = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) SimDeepBlue else Color.White
    val fg = if (isUser) Color.White else Color(0xFF1A1A1A)
    val shape = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (!isUser) {
                AvatarDot(isUser = false)
                Spacer(Modifier.width(6.dp))
            }
            Surface(
                color = bg, shape = shape, shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Text(text, color = fg, fontSize = 12.sp, lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
            if (isUser) {
                Spacer(Modifier.width(6.dp))
                AvatarDot(isUser = true)
            }
        }
    }
}

@Composable
private fun AvatarDot(isUser: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isUser) SimDeepBlue else Color(0xFFE6E9EF),
        modifier = Modifier.size(22.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = if (isUser) Color.White else SimMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Drugs panel — shows the drug image + name + selected quantity.
// Tap a drug to open the Build-a-Label form pre-filled with it.
// ─────────────────────────────────────────────────────────────────
@Composable
fun DrugsPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Selected Drugs (${vm.selectedDrugs.size})", onClose = onClose, modifier = modifier) {
        if (vm.selectedDrugs.isEmpty()) {
            EmptyState("No drugs selected yet. Tap the shelf, fridge, or safe to add drugs.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(vm.selectedDrugs) { drug ->
                    val qty = vm.cart.firstOrNull { it.drugId == drug.id }?.quantity ?: 0
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SimSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.openLabelingFor(drug.id) }
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drug image (or letter fallback)
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (drug.drawableRes != null) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(drug.drawableRes),
                                        contentDescription = drug.name,
                                        modifier = Modifier.fillMaxSize().padding(2.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        drug.name.first().toString(),
                                        fontSize = 22.sp,
                                        color = SimDeepBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(drug.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Qty: $qty", color = SimDeepBlue, fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold)
                                Text("Tap to build label", color = SimMuted, fontSize = 10.sp)
                            }
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
    SlideInPanel(title = "Labels & Holds (${vm.completedLabels.size + vm.holds.size})", onClose = onClose, modifier = modifier) {
        if (vm.completedLabels.isEmpty() && vm.holds.isEmpty()) {
            EmptyState("No labels or holds yet. Open the labeling form from Drugs.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (vm.completedLabels.isNotEmpty()) {
                    item {
                        Text("Completed Labels (${vm.completedLabels.size})",
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimDeepBlue)
                    }
                    items(vm.completedLabels) { label ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SimSurface),
                            modifier = Modifier.fillMaxWidth().clickable { vm.editLabel(label.id) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(label.drugName, fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp, color = SimDeepBlue)
                                Spacer(Modifier.height(4.dp))
                                Text("Quantity:  ${label.quantity.ifBlank { "—" }}",
                                    fontSize = 12.sp, color = Color(0xFF333333))
                                Text("Dose:      ${label.dose.ifBlank { "—" }}",
                                    fontSize = 12.sp, color = Color(0xFF333333))
                                if (label.duration.isNotBlank())
                                    Text("Duration:  ${label.duration}",
                                        fontSize = 12.sp, color = Color(0xFF333333))
                                Spacer(Modifier.height(2.dp))
                                Text("Direction:", fontSize = 11.sp,
                                    color = SimMuted, fontWeight = FontWeight.SemiBold)
                                Text(label.direction.ifBlank { "—" },
                                    fontSize = 12.sp, color = Color(0xFF222222))
                                if (label.auxLabels.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Auxiliary:", fontSize = 11.sp,
                                        color = SimMuted, fontWeight = FontWeight.SemiBold)
                                    label.auxLabels.forEach {
                                        Text("• $it", fontSize = 11.sp, color = Color(0xFF333333))
                                    }
                                }
                            }
                        }
                    }
                }
                // ── Held drugs (moved here from Reports per request) ──
                if (vm.holds.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text("Held Drugs (${vm.holds.size})",
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimDanger)
                    }
                    items(vm.holds) { h ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Filled.Warning, null,
                                        tint = SimDanger, modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(h.drugName, fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp, color = SimDanger)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Reason: ${h.reason}", fontSize = 12.sp, color = Color(0xFF333333))
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
            modifier = Modifier.fillMaxSize().imePadding(),
            placeholder = { Text("Type your clinical thinking, holds, or counseling notes…", fontSize = 12.sp) },
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Reports panel — uses shared ReportsContent (same as desk hotspot)
// ─────────────────────────────────────────────────────────────────
@Composable
fun ReportsPanel(vm: SimulationViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    SlideInPanel(title = "Patient Reports", onClose = onClose, modifier = modifier) {
        ReportsContent(vm = vm)
    }
}

/** Shared "Reports" body used by both the rail panel and the desk prop. */
@Composable
fun ReportsContent(vm: SimulationViewModel) {
    val rx = vm.case?.prescription
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SimSurface),
                shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Lab Report", fontWeight = FontWeight.Bold, color = SimDeepBlue, fontSize = 13.sp)
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    LabRow("Sodium (Na+)", "138", "135–145", "mEq/L", normal = true)
                    LabRow("Potassium (K+)", "4.2", "3.5–5.0", "mEq/L", normal = true)
                    LabRow("Creatinine", "1.1", "0.6–1.2", "mg/dL", normal = true)
                    LabRow("eGFR", "82", ">60", "mL/min", normal = true)
                    LabRow("Hemoglobin", "13.8", "12–16", "g/dL", normal = true)
                    LabRow("WBC", "7.4", "4–11", "10³/µL", normal = true)
                    LabRow("Platelets", "265", "150–400", "10³/µL", normal = true)
                }
            }
        }
        if (rx?.errorNote != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(10.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Prescription Note", fontWeight = FontWeight.Bold,
                            color = SimDanger, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(rx.errorNote, fontSize = 11.sp, color = Color(0xFF333333))
                    }
                }
            }
        }
    }
}

@Composable
private fun LabRow(test: String, value: String, range: String, unit: String, normal: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(test, fontSize = 11.sp, color = Color(0xFF333333), modifier = Modifier.weight(1.4f))
        Text("$value $unit",
            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = if (normal) SimSuccess else SimDanger,
            modifier = Modifier.weight(1f))
        Text(range, fontSize = 10.sp, color = SimMuted, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
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
