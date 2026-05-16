package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────
// Storage screen — used for Shelf / Fridge / Safe
// Alphabetical filter + search + product preview with Add/Discard
// ─────────────────────────────────────────────────────────────────
@Composable
fun StorageScreen(
    storage: DrugStorage,
    catalog: List<CatalogDrug>,
    onAdd: (CatalogDrug, Int) -> Unit,
    onClose: () -> Unit
) {
    val available = catalog.filter { it.storage == storage }
    var query by remember { mutableStateOf("") }
    var letter by remember { mutableStateOf<Char?>(null) }
    var preview by remember { mutableStateOf<CatalogDrug?>(null) }
    // "Drug added" confirmation toast
    var addedToast by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(addedToast) {
        if (addedToast != null) {
            kotlinx.coroutines.delay(1800)
            addedToast = null
        }
    }

    val filtered = available
        .filter { d ->
            (query.isBlank() || d.name.contains(query, ignoreCase = true)) &&
                    (letter == null || d.name.firstOrNull()?.uppercaseChar() == letter)
        }
        .sortedBy { it.name }

    val title = when (storage) {
        DrugStorage.SHELF -> "Pharmacy Shelf"
        DrugStorage.FRIDGE -> "Refrigerated Storage"
        DrugStorage.SAFE -> "Controlled Drug Safe"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SimSurface
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SimDeepBlue)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "close") }
                }

                // Search bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    placeholder = { Text("Search drug name…", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                // Alphabet row
                val letters = ('A'..'Z').toList()
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        AlphaChip(label = "All", selected = letter == null) { letter = null }
                    }
                    items(letters) { ch ->
                        AlphaChip(label = ch.toString(), selected = letter == ch) {
                            letter = if (letter == ch) null else ch
                        }
                    }
                }

                // Drug grid
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No drugs match your filter.", color = SimMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize().padding(top = 6.dp)
                    ) {
                        items(filtered) { drug ->
                            DrugTile(
                                drug = drug,
                                onClick = { preview = drug },
                                // Quick "+1" add directly from the tile.
                                onQuickAdd = {
                                    onAdd(drug, 1)
                                    addedToast = "${drug.name} added (×1)"
                                }
                            )
                        }
                    }
                }
            }

            // ── "Drug added" confirmation toast (auto-dismiss) ──
            androidx.compose.animation.AnimatedVisibility(
                visible = addedToast != null,
                enter = androidx.compose.animation.fadeIn() +
                        androidx.compose.animation.scaleIn(initialScale = 0.85f),
                exit = androidx.compose.animation.fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            ) {
                Surface(
                    color = SimSuccess,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Color.White,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(addedToast ?: "", color = Color.White,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ── Product preview ──
        if (preview != null) {
            ProductPreviewDialog(
                drug = preview!!,
                onAdd = { qty ->
                    onAdd(preview!!, qty)
                    addedToast = "${preview!!.name} added (×$qty)"
                    preview = null
                },
                onDiscard = { preview = null }
            )
        }
    }
}

@Composable
private fun AlphaChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) SimDeepBlue else Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color(0xFF333333),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun DrugTile(
    drug: CatalogDrug,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (drug.drawableRes != null) {
                    Image(
                        painter = painterResource(drug.drawableRes),
                        contentDescription = drug.name,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SimSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(drug.name.first().toString(),
                            fontSize = 44.sp, color = SimDeepBlue,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                drug.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF1A2230),
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            // Quick "+1" Add directly from the tile.
            Surface(
                onClick = onQuickAdd,
                color = SimDeepBlue,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(34.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, null, tint = Color.White,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add 1", color = Color.White,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPreviewDialog(
    drug: CatalogDrug,
    onAdd: (Int) -> Unit,
    onDiscard: () -> Unit
) {
    // Quantity starts at 1 (per spec). + button increases, − decreases (min 1).
    var qty by remember { mutableStateOf(1) }
    AlertDialog(
        onDismissRequest = onDiscard,
        title = { Text(drug.name, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // BIG image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (drug.drawableRes != null) {
                        Image(
                            painter = painterResource(drug.drawableRes),
                            contentDescription = drug.name,
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SimSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(drug.name.first().toString(), fontSize = 64.sp,
                                color = SimDeepBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(drug.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (drug.strength.isNotBlank()) {
                    Text(drug.strength, fontSize = 12.sp, color = SimMuted)
                }
                Spacer(Modifier.height(16.dp))

                // Quantity selector
                Text("Quantity", fontSize = 12.sp, color = SimMuted)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QtyBtn(icon = false, label = "−") { if (qty > 1) qty-- }
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SimSurface,
                        modifier = Modifier.width(64.dp)
                    ) {
                        Text(qty.toString(),
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.Bold, color = SimDeepBlue, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    QtyBtn(icon = true, label = "+") { qty++ }
                }
                Spacer(Modifier.height(12.dp))
                // Quick-pick chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickQtyChip("+1") { qty = 1 }
                    QuickQtyChip("+2") { qty = 2 }
                    QuickQtyChip("+5") { qty = 5 }
                    QuickQtyChip("+10") { qty = 10 }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(qty) },
                colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add to Cart")
            }
        },
        dismissButton = { TextButton(onClick = onDiscard) { Text("Discard") } }
    )
}

@Composable
private fun QuickQtyChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SimDeepBlue)
    ) {
        Text(label, fontSize = 12.sp, color = SimDeepBlue, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
private fun QtyBtn(icon: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = SimDeepBlue,
        shape = CircleShape,
        modifier = Modifier.size(38.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (icon) {
                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Filled.Remove, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

