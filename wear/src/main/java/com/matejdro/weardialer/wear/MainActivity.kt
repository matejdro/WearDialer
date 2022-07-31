package com.matejdro.weardialer.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.matejdro.weardialer.wear.theme.WearAppTheme
import java.time.ZoneId
import java.time.ZonedDateTime

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {
         WearAppTheme {
            val testEntries = listOf(
               CallEntry(
                  "Mom",
                  ZonedDateTime.of(
                     2022, 7, 31, 19, 30, 0, 0, ZoneId.systemDefault()
                  ).toInstant()
               ),
               CallEntry(
                  "Friend",
                  ZonedDateTime.of(
                     2022, 7, 31, 17, 45, 0, 0, ZoneId.systemDefault()
                  ).toInstant()
               ),
               CallEntry(
                  "John Smith",
                  ZonedDateTime.of(
                     2022, 7, 20, 19, 30, 0, 0, ZoneId.systemDefault()
                  ).toInstant()
               ),
               CallEntry(
                  "Omolara Johannes",
                  ZonedDateTime.of(
                     2022, 6, 15, 19, 30, 0, 0, ZoneId.systemDefault()
                  ).toInstant()
               ),
               CallEntry(
                  "Joos Ruut",
                  ZonedDateTime.of(
                     2022, 6, 5, 17, 45, 0, 0, ZoneId.systemDefault()
                  ).toInstant()
               ),
               CallEntry(
                  "Satu Meera",
                  ZonedDateTime.of(
                     2022, 5, 20, 19, 30, 0, 0, ZoneId.systemDefault()
                  ).toInstant()
               ),
            )

            DialScreen(testEntries)
         }
      }
   }
}
