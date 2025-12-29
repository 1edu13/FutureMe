package com.example.futureme.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val gold = Color(0xFFD4AF37)
    val titleGold = Color(0xFFC9A84D)
    val textPrimary = Color(0xFFEDEDED)
    val textSecondary = textPrimary.copy(alpha = 0.75f)
    val bg = Color(0xFF0B0B0B)
    val cardBg = Color(0xFF0E0E0E)

    // 4 páginas
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Bienvenido",
                body = "Bienvenido a FutureMe.\nGuarda mensajes para tu yo del futuro."
            ),
            OnboardingPage(
                title = "La idea",
                body = "Escribe lo que sientes hoy.\nÁbrelo cuando llegue la fecha."
            ),
            OnboardingPage(
                title = "Crear cápsula",
                body = "Pon un título, texto e imágenes.\nElige cuándo se abrirá."
            ),
            OnboardingPage(
                title = "Cápsulas compartidas",
                body = "Invita a amigos a una cápsula.\nLa veréis cuando se abra."
            )
        )
    }

    var index by remember { mutableIntStateOf(0) }
    val isLast = index == pages.lastIndex

    Surface(color = bg, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Fondo/Overlay sutil para mantener el estilo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Header (puntos + skip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DotsIndicator(
                        count = pages.size,
                        selectedIndex = index,
                        gold = gold
                    )

                    TextButton(
                        onClick = onFinish,
                        colors = ButtonDefaults.textButtonColors(contentColor = textSecondary)
                    ) {
                        Text("Saltar")
                    }
                }

                // Card central
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    shadowElevation = 10.dp,
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = pages[index].title,
                            color = titleGold,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = pages[index].body,
                            color = textPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "${index + 1} / ${pages.size}",
                            color = textSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Controles (atrás / siguiente / empezar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Atrás
                    OutlinedButton(
                        onClick = { if (index > 0) index-- },
                        enabled = index > 0,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, gold.copy(alpha = if (index > 0) 0.35f else 0.15f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = gold
                        )
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Anterior", tint = gold)
                        Spacer(Modifier.width(8.dp))
                        Text("Atrás", color = gold, fontWeight = FontWeight.SemiBold)
                    }

                    // Siguiente / Empezar
                    Button(
                        onClick = {
                            if (isLast) onFinish() else index++
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF121212),
                            contentColor = textPrimary
                        ),
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.35f))
                    ) {
                        if (isLast) {
                            Icon(Icons.Default.Check, contentDescription = "Empezar", tint = gold)
                            Spacer(Modifier.width(8.dp))
                            Text("Empezar", fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Siguiente", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Siguiente", tint = gold)
                        }
                    }
                }
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val body: String
)

@Composable
private fun DotsIndicator(
    count: Int,
    selectedIndex: Int,
    gold: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val isSelected = i == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (isSelected) 10.dp else 8.dp)
                    .background(
                        color = if (isSelected) gold else gold.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}
