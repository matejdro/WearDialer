package com.matejdro.weardialer.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material.Text
import com.matejdro.weardialer.wear.theme.WearAppTheme

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {
         WearAppTheme {
            Text("Hello World")
         }
      }
   }
}
