package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────
// Prescription popup — modal, vertically scrollable, persists scroll
// across re-opens within the same session.
// ─────────────────────────────────────────────────────────────────
@Composable
fun PrescriptionPopup(vm: SimulationViewModel) {
    val rx = vm.case?.prescription ?: return
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = vm.prescriptionScrollIndex,
        initialFirstVisibleItemScrollOffset = vm.prescriptionScrollOffset
    )
    DisposableEffect(Unit) {
        onDispose {
            vm.savePrescriptionScroll(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
        }
    }

    Dialog(
        onDismissRequest = { vm.closePrescription() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.55f).fillMaxHeight(0.95f),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prescription", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = Color(0xFF222222), modifier = Modifier.weight(1f))
                    IconButton(onClick = { vm.closePrescription() }) {
                        Icon(Icons.Filled.Close, "close", tint = SimMuted)
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))

                if (rx.hasError) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = SimDanger, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                rx.errorNote ?: "This prescription has a flag — review carefully.",
                                color = SimDanger, fontSize = 11.sp
                            )
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Column {
                            Text(rx.doctorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(rx.doctorQualification, fontSize = 11.sp, color = SimMuted)
                            Spacer(Modifier.height(4.dp))
                            RxIconRow(Icons.Filled.Place, rx.clinic)
                            RxIconRow(Icons.Filled.Phone, rx.phone)
                            RxIconRow(null, rx.date)
                        }
                    }
                    item { HorizontalDivider(color = Color(0xFFEEEEEE)) }
                    item {
                        Column {
                            Text("Patient Name: ${rx.patientName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Diagnosis: ${rx.diagnosis}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Spacer(Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RxChip(rx.patientAge); RxChip(rx.patientGender); RxChip(rx.patientCity)
                            }
                        }
                    }
                    item { HorizontalDivider(color = Color(0xFFEEEEEE)) }
                    items(rx.items) { item ->
                        Column {
                            Text(item.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimDeepBlue)
                            Spacer(Modifier.height(2.dp))
                            Text(item.instructions, fontSize = 11.sp, color = Color(0xFF333333))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RxIconRow(icon: androidx.compose.ui.graphics.vector.ImageVector?, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = SimMuted, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, fontSize = 11.sp, color = Color(0xFF333333))
    }
}

@Composable
private fun RxChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SimSurface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) { Text(text, fontSize = 10.sp, color = Color(0xFF555555)) }
}

