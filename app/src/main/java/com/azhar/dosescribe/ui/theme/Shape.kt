package com.azhar.dosescribe.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Generous, calm rounding — clinical-friendly, not playful.
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chips, small tags
    small = RoundedCornerShape(12.dp),       // buttons, text fields
    medium = RoundedCornerShape(16.dp),      // cards, sheets
    large = RoundedCornerShape(20.dp),       // hero cards, dialogs
    extraLarge = RoundedCornerShape(28.dp)   // bottom sheets, big dialogs
)
