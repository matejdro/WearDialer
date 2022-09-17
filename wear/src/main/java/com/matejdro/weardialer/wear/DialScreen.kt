package com.matejdro.weardialer.wear

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.matejdro.weardialer.wear.theme.PreviewTheme
import dev.drewhamilton.androidtime.format.AndroidDateTimeFormatter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle

@Composable
fun DialScreen(entries: List<CallEntry>, triggerFilterButton: (String) -> Unit) {
   val focusRequester = remember { FocusRequester() }

   Box() {
      BackgroundList(entries, focusRequester)
      ForegroundDialer(triggerFilterButton)
   }

   LaunchedEffect(Unit) {
      focusRequester.requestFocus()
   }
}

@Composable
private fun ForegroundDialer(triggerFilterButton: (String) -> Unit) {
   Column(
      Modifier
         .fillMaxSize()
         .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
   ) {
      ButtonRow {
         DialButton("1") { triggerFilterButton(".,!1 ") }
         DialButton("2") { triggerFilterButton("abcč2") }
         DialButton("3") { triggerFilterButton("def3") }
      }

      ButtonRow {
         DialButton("4") { triggerFilterButton("ghi4") }
         DialButton("5") { triggerFilterButton("jkl5") }
         DialButton("6") { triggerFilterButton("mno6") }
      }

      ButtonRow {
         DialButton("7") { triggerFilterButton("pqrsš7") }
         DialButton("8") { triggerFilterButton("tuv8") }
         DialButton("9") { triggerFilterButton("xyzž9") }
      }

      ButtonRow {
         DialButton("<-") {}
         DialButton("OK") {}
      }
   }
}

@Composable
private fun ColumnScope.ButtonRow(content: @Composable RowScope.() -> Unit) {
   Row(
      Modifier.Companion
         .weight(1f)
         .fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      content = content
   )
}

@Composable
private fun RowScope.DialButton(text: String, onClick: () -> Unit) {
   Box(
      Modifier
         .clickable(onClick = onClick, role = Role.Button)
         .weight(1f)
         .fillMaxHeight()
         .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center,
   ) {
      Text(text, color = Color.Green, fontWeight = FontWeight.W100)
   }
}

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BackgroundList(entries: List<CallEntry>, focusRequester: FocusRequester) {
   var selection by remember { mutableStateOf(0) }

   val context = LocalContext.current

   val state = rememberLazyListState()
   val scope = rememberCoroutineScope()

   LazyColumn(state = state,
      modifier = Modifier
         .padding(16.dp)
         .onRotaryScrollEvent {
            selection = if (it.verticalScrollPixels > 0) {
               (selection + 1).coerceAtMost(entries.size - 1)
            } else {
               (selection - 1).coerceAtLeast(0)
            }

            scope.launch {
               state.animateScrollToItem(selection)
            }
            true
         }
         .focusRequester(focusRequester)
         .focusable()
   ) {
      items(entries.size) { index ->
         val entry = entries.elementAt(index)

         val highlightColor = if (index == selection) {
            Color(0x30FF8000)
         } else {
            Color.Transparent
         }

         Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
               .background(highlightColor)
               .padding(top = 16.dp)
         ) {
            val zonedDate = ZonedDateTime.ofInstant(entry.lastCallTime, ZoneId.systemDefault())

            Text(entry.name)
            Text(AndroidDateTimeFormatter.ofLocalizedDateTime(context, FormatStyle.SHORT).format(zonedDate))
            Spacer(
               Modifier
                  .padding(top = 16.dp)
                  .fillMaxWidth()
                  .height(1.dp)
                  .background(Color.White)
            )
         }

      }
   }
}

@Composable
@Preview(device = "id:wearos_large_round", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_WATCH)
private fun DialScreenPreview() {
   PreviewTheme {
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

      DialScreen(testEntries) {}
   }

}

data class CallEntry(val name: String, val lastCallTime: Instant)
