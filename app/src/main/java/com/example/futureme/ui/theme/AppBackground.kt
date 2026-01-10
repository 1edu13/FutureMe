package com.example.futureme.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.futureme.R

enum class BackgroundType { LOGIN_IMAGE, IMAGE, GRADIENT }

@Composable
fun AppBackground(
    type: BackgroundType,
    isDark: Boolean,
    @DrawableRes imageDark: Int? = null,
    @DrawableRes imageLight: Int? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(Modifier.fillMaxSize()) {

        when (type) {
            BackgroundType.LOGIN_IMAGE -> {
                val resId = if (isDark) R.drawable.login_bg_dark else R.drawable.login_bg_white

                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(
                                        Color(0xFF0A1929).copy(alpha = 0.38f),
                                        Color.Transparent,
                                        Color(0xFF0A1929).copy(alpha = 0.30f)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFFFFFFFF).copy(alpha = 0.06f),
                                        Color.Transparent,
                                        Color(0xFF0A1929).copy(alpha = 0.12f)
                                    )
                                }
                            )
                        )
                )
            }

            BackgroundType.IMAGE -> {
                val resId = if (isDark) imageDark else imageLight
                require(resId != null) { "BackgroundType.IMAGE requiere imageDark e imageLight" }

                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(
                                        Color(0xFF0A1929).copy(alpha = 0.35f),
                                        Color.Transparent,
                                        Color(0xFF0A1929).copy(alpha = 0.28f)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFFFFFFFF).copy(alpha = 0.06f),
                                        Color.Transparent,
                                        Color(0xFF0A1929).copy(alpha = 0.10f)
                                    )
                                }
                            )
                        )
                )
            }

            BackgroundType.GRADIENT -> {
                val colors = if (isDark) {
                    listOf(
                        Color(0xFF132F4C),
                        Color(0xFF0A1929)
                    )
                } else {
                    listOf(
                        Color(0xFFF4F8FC),
                        Color(0xFFCFE4F6),
                        Color(0xFF9EC2E6)
                    )
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = colors,
                                radius = 1600f
                            )
                        )
                )
            }
        }

        content()
    }
}
