package com.azhar.dosescribe.ui.feature.simulation

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azhar.dosescribe.R

@Composable
fun StorageScreen(
    storage: DrugStorage,
    catalog: List<CatalogDrug>,
    onAdd: (CatalogDrug, Int) -> Unit,
    onClose: () -> Unit
) {
    val available = catalog.filter { it.storage == storage }
    var query by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var selectedDrug by remember { mutableStateOf<CatalogDrug?>(null) }
    var qty by remember { mutableStateOf(1) }
    var addedToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(addedToast) {
        if (addedToast != null) {
            kotlinx.coroutines.delay(2000)
            addedToast = null
        }
    }

    val filtered = available
        .filter { d ->
            (query.isBlank() || d.name.contains(query, ignoreCase = true)) &&
            (selectedLetter == null || d.name.firstOrNull()?.uppercaseChar() == selectedLetter)
        }
        .sortedBy { it.name }

    val bgRes = when (storage) {
        DrugStorage.SHELF -> R.drawable.shelf_detail
        DrugStorage.FRIDGE -> R.drawable.fridge_detail
        DrugStorage.SAFE -> R.drawable.locker_detail
    }

    val sidebarBg = Color(0xFF2C2C2C)
    val sidebarSurface = Color(0xFF4A4A4A)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A))) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onClose() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Back", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT AREA: SHELF IMAGE
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp, bottom = 12.dp)) {
                Image(
                    painter = painterResource(bgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillBounds
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { drug ->
                        DrugTileOnShelf(
                            drug = drug,
                            isSelected = selectedDrug?.id == drug.id,
                            onClick = {
                                selectedDrug = drug
                                qty = 1
                            }
                        )
                    }
                }
            }

            // RIGHT SIDEBAR
            Surface(
                modifier = Modifier.width(360.dp).fillMaxHeight().padding(end = 12.dp, bottom = 12.dp),
                color = sidebarBg,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Default State: Search & Alphabets
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            placeholder = { Text("Search Drug", color = Color.Gray, fontSize = 14.sp) },
                            trailingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Spacer(Modifier.height(12.dp))
                        Text("Alphabets", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))

                        val alphabets = ('A'..'Z').toList()
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(9),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        ) {
                            item { AlphabetButton("ALL", selectedLetter == null) { selectedLetter = null } }
                            items(alphabets) { char ->
                                AlphabetButton(char.toString(), selectedLetter == char) { selectedLetter = char }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Instructions", color = Color.White.copy(0.6f), fontSize = 13.sp)
                        Text("Select a medication from the shelf to see details.", color = Color.White.copy(0.4f), fontSize = 11.sp)
                    }

                    // Detail State Overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedDrug != null,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = sidebarBg,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            selectedDrug?.let { drug ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        IconButton(onClick = { selectedDrug = null }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Close, null, tint = Color.White)
                                        }
                                    }

                                    // Image
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        color = Color.White,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            if (drug.drawableRes != null) {
                                                Image(
                                                    painter = painterResource(drug.drawableRes),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Text(drug.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center)
                                    Text(drug.strength, color = Color.White.copy(0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)

                                    Spacer(Modifier.height(24.dp))

                                    // One Row: Quantity Selector + Add Button
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = sidebarSurface,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // --- Quantity Controls ---
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(Color.White.copy(0.1f), RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                IconButton(onClick = { if (qty > 1) qty-- }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Remove, null, tint = Color.White)
                                                }
                                                Text(
                                                    qty.toString().padStart(2, '0'),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(onClick = { qty++ }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Add, null, tint = Color.White)
                                                }
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            // --- ADD TO CART Button ---
                                            Button(
                                                onClick = {
                                                    onAdd(drug, qty)
                                                    addedToast = "${drug.name} added into inventory"
                                                    selectedDrug = null
                                                },
                                                modifier = Modifier.weight(1f).height(42.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = SimDeepBlue),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("ADD TO CART", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // SUCCESS POPUP (Center)
    androidx.compose.animation.AnimatedVisibility(
        visible = addedToast != null,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut(),
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        label = "addedToast"
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = Color(0xFF2E7D32),
                shape = RoundedCornerShape(40.dp),
                shadowElevation = 10.dp
            ) {
                Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(addedToast ?: "", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun AlphabetButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) SimDeepBlue else Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = if (label == "ALL") 8.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DrugTileOnShelf(drug: CatalogDrug, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White.copy(0.2f) else Color.Transparent)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) SimDeepBlue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (drug.drawableRes != null) {
            Image(
                painter = painterResource(drug.drawableRes),
                contentDescription = drug.name,
                modifier = Modifier.fillMaxSize().padding(6.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
