package com.example.futureme.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.ui.theme.OroAmbar

@Composable
fun NightButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, OroAmbar),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(OroAmbar.copy(alpha = 0.12f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = OroAmbar,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
