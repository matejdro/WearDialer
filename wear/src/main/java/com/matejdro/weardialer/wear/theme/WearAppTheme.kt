package com.matejdro.weardialer.wear.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import com.matejdro.weardialer.common.AppColors


@Composable
fun WearAppTheme(
   content: @Composable () -> Unit
) {
   MaterialTheme(
      colors = colorPalette,
      content = content
   )
}

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
   WearAppTheme {
      Box(
         Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)) {
         content()
      }
   }
}

private val colorPalette = Colors(
   primary = AppColors.primary,
   primaryVariant = AppColors.primaryDark,
   secondary = AppColors.primary,
   secondaryVariant = AppColors.primaryDark,
   error = AppColors.error,
   onPrimary = AppColors.onPrimary,
   onSecondary = AppColors.onPrimary,
   onError = AppColors.onError
)
