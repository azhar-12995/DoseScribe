package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onAdd: (CatalogDrug) -> Unit,
    onClose: () -> Unit
) {
    val available = catalog.filter { it.storage == storage }
    var query by remember { mutableStateOf("") }
    var letter by remember { mutableStateOf<Char?>(null) }
    var preview by remember { mutableStateOf<CatalogDrug?>(null) }

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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
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
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(top = 6.dp)
                ) {
                    items(filtered) { drug ->
                        DrugTile(drug = drug, onClick = { preview = drug })
                    }
                }
            }
        }

        // ── Product preview ──
        if (preview != null) {
            ProductPreviewDialog(
                drug = preview!!,
                onAdd = {
                    onAdd(preview!!)
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
private fun DrugTile(drug: CatalogDrug, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SimSurface),
                contentAlignment = Alignment.Center
            ) { Text(drug.name.first().toString(), fontSize = 24.sp, color = SimDeepBlue, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(6.dp))
            Text(drug.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(drug.strength, color = SimMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ProductPreviewDialog(
    drug: CatalogDrug,
    onAdd: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDiscard,
        title = { Text(drug.name, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SimSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(drug.name.first().toString(), fontSize = 56.sp, color = SimDeepBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Text("Strength: ${drug.strength}", fontSize = 13.sp)
                Text("Storage: ${drug.storage.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 12.sp, color = SimMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = onAdd,
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

