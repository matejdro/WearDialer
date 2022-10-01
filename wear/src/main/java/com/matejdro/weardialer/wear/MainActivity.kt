package com.matejdro.weardialer.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.matejdro.weardialer.wear.theme.WearAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
   private val viewModel by viewModels<CallViewModel>()
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {
         WearAppTheme {
            val entries = viewModel.displayedContacts.collectAsState(emptyList()).value
            val numbers = viewModel.phoneNumberSelection.collectAsState().value

            DialScreen(entries, numbers, viewModel::filter, viewModel::backspace, viewModel::activateContact, viewModel::activateNumber)
         }
      }
   }
}
