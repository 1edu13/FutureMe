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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.futureme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    isDark: Boolean,
    onFinish: () -> Unit
) {
    // 🎨 REFERENCIAS AL TEMA
    val gold = MaterialTheme.colorScheme.primary
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = textPrimary.copy(alpha = 0.6f)

    val cardBrush = if (isDark) {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, AzulProfundo))
    } else {
        Brush.verticalGradient(listOf(ClaroBase, ClaroSuave, ClaroPrincipal))
    }

    // ✅ LÓGICA ORIGINAL INTACTA
    val pages = remember {
        listOf(
            OnboardingPage("BIENVENIDO", "FutureMe guarda mensajes para tu yo del futuro.\nEscribe hoy y ábrelo cuando llegue el momento."),
            OnboardingPage("LA IDEA", "Captura lo que piensas o sientes ahora.\nCuando se abra, lo verás con perspectiva."),
            OnboardingPage("CREAR CÁPSULA", "Pon un título, añade texto e imágenes.\nElige una fecha y deja que el tiempo pase."),
            OnboardingPage("COMPARTIR", "Si una cápsula es compartida, invita a amigos.\nTodos la veréis al mismo tiempo.")
        )
    }

    var index by remember { mutableIntStateOf(0) }
    val isLast = index == pages.lastIndex

    AppBackground(type = BackgroundType.LOGIN_IMAGE, isDark = isDark) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DotsIndicator(pages.size, index, gold, if(isDark) AzulSuperficie else ClaroPrincipal)
                TextButton(onClick = onFinish) {
                    Text("SALTAR", color = gold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            // Card Central Sólida con degradado del Theme
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                shape = RoundedCornerShape(35.dp),
                border = BorderStroke(1.5.dp, gold.copy(alpha = 0.6f)),
                shadowElevation = if (isDark) 12.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier.background(cardBrush).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pages[index].title,
                        color = if (isDark) gold else AzulProfundo,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = pages[index].body,
                        color = textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "${index + 1} / ${pages.size}",
                        color = textSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Controles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (index > 0) {
                    OutlinedButton(
                        onClick = { index-- },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.5f))
                    ) {
                        Text("ATRÁS", color = gold, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(Modifier.width(100.dp))
                }

                Button(
                    onClick = { if (isLast) onFinish() else index++ },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) gold else AzulProfundo,
                        contentColor = if (isDark) AzulProfundo else Color.White
                    ),
                    border = BorderStroke(1.2.dp, gold),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(if (isLast) "EMPEZAR" else "SIGUIENTE", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

private data class OnboardingPage(val title: String, val body: String)

@Composable
private fun DotsIndicator(count: Int, selectedIndex: Int, activeColor: Color, inactiveColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .size(if (i == selectedIndex) 12.dp else 8.dp)
                    .background(color = if (i == selectedIndex) activeColor else inactiveColor, shape = RoundedCornerShape(50))
            )
        }
    }
}