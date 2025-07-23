package com.matejdro.weardialer.wear

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
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
fun DialScreen(
   entries: List<CallEntry>,
   numbersPopup: List<CallEntryNumber>? = null,
   triggerFilterButton: (String) -> Unit,
   backspace: () -> Unit,
   activateContact: (CallEntry) -> Unit,
   activateNumber: (CallEntryNumber) -> Unit
) {
   val bottomListFocusRequester = remember { FocusRequester() }
   val topListFocusRequester = remember { FocusRequester() }

   Box {
      BackgroundList(entries, bottomListFocusRequester, activateContact)
      ForegroundDialer(triggerFilterButton, backspace)
   }

   if (numbersPopup != null) {
      NumbersList(numbersPopup, topListFocusRequester, activateNumber)
   }

   LaunchedEffect(numbersPopup) {
      if (numbersPopup == null) {
         bottomListFocusRequester.requestFocus()
      } else {
         topListFocusRequester.requestFocus()
      }
   }
}

@Composable
private fun ForegroundDialer(triggerFilterButton: (String) -> Unit, backspace: () -> Unit) {
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
         DialButton("<-", backspace)
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
private fun BackgroundList(entries: List<CallEntry>, focusRequester: FocusRequester, activateContact: (CallEntry) -> Unit) {
   var selection by remember { mutableIntStateOf(0) }

   val context = LocalContext.current

   val state = rememberLazyListState()
   val scope = rememberCoroutineScope()

   BackHandler {
      val targetContact = entries.elementAtOrNull(selection)
      if (targetContact != null) {
         activateContact(targetContact)
      }
   }

   LazyColumn(state = state,
      modifier = Modifier
         .padding(16.dp)
         .onRotaryScrollEvent {
            selection = if (it.verticalScrollPixels > 0) {
               (selection + 1).coerceAtMost(entries.size - 1)
            } else {
               (selection - 1)
            }.coerceAtLeast(0)

            scope.launch {
               state.animateScrollToItem(selection, -state.layoutInfo.viewportSize.height / 3)
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
               .padding(top = 8.dp)
               .onFocusChanged { println("Key focus changed $it $entry") }
         ) {
            val zonedDate = entry.lastCallTime?.let { ZonedDateTime.ofInstant(it, ZoneId.systemDefault()) }

            Text(entry.name)
            Text(zonedDate?.let { AndroidDateTimeFormatter.ofLocalizedDateTime(context, FormatStyle.SHORT).format(it) }.orEmpty())
            Spacer(
               Modifier
                  .padding(top = 8.dp)
                  .fillMaxWidth()
                  .height(1.dp)
                  .background(Color.White)
            )
         }
      }
   }
}

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NumbersList(entries: List<CallEntryNumber>, focusRequester: FocusRequester, activateNumber: (CallEntryNumber) -> Unit) {
   var selection by remember { mutableIntStateOf(0) }

   val state = rememberLazyListState()
   val scope = rememberCoroutineScope()

   BackHandler {
      val targetNumber = entries.elementAtOrNull(selection)
      if (targetNumber != null) {
         activateNumber(targetNumber)
      }
   }

   LazyColumn(state = state,
      modifier = Modifier
         .padding(32.dp)
         .background(Color.DarkGray)
         .onRotaryScrollEvent {
            selection = if (it.verticalScrollPixels > 0) {
               (selection + 1).coerceAtMost(entries.size - 1)
            } else {
               (selection - 1)
            }.coerceAtLeast(0)

            scope.launch {
               state.animateScrollToItem(selection, -state.layoutInfo.viewportSize.height / 3)
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
               .padding(top = 8.dp)
         ) {
            Text(entry.number)
            Text(entry.label)
            Spacer(
               Modifier
                  .padding(top = 8.dp)
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
            0,
            "Mom",
            ZonedDateTime.of(
               2022, 7, 31, 19, 30, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         ),
         CallEntry(
            1,
            "Friend",
            ZonedDateTime.of(
               2022, 7, 31, 17, 45, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         ),
         CallEntry(
            2,
            "John Smith",
            ZonedDateTime.of(
               2022, 7, 20, 19, 30, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         ),
         CallEntry(
            3,
            "Omolara Johannes",
            ZonedDateTime.of(
               2022, 6, 15, 19, 30, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         ),
         CallEntry(
            4,
            "Joos Ruut",
            ZonedDateTime.of(
               2022, 6, 5, 17, 45, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         ),
         CallEntry(
            5,
            "Satu Meera",
            ZonedDateTime.of(
               2022, 5, 20, 19, 30, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         ),
      )

      DialScreen(testEntries, null, {}, {}, {}) {}
   }

}

@Composable
@Preview(device = "id:wearos_large_round", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_WATCH)
private fun NumberPickerPreview() {
   PreviewTheme {
      val testEntries = listOf(
         CallEntry(
            0,
            "Mom",
            ZonedDateTime.of(
               2022, 7, 31, 19, 30, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         ),
         CallEntry(
            1,
            "Friend",
            ZonedDateTime.of(
               2022, 7, 31, 17, 45, 0, 0, ZoneId.systemDefault()
            ).toInstant()
         )
      )

      val testNumbers = listOf(
         CallEntryNumber("123 456 789", "Home"),
         CallEntryNumber("987 654 321", "Work"),
      )

      DialScreen(testEntries, testNumbers, {}, {}, {}) {}
   }

}

data class CallEntry(val id: Long, val name: String, val lastCallTime: Instant?)
data class CallEntryNumber(val number: String, val label: String)
