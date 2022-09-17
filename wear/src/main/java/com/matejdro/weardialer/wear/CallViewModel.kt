package com.matejdro.weardialer.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.weardialer.wear.data.WatchTransmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
   private val watchTransmitter: WatchTransmitter
) : ViewModel() {
   private var filterLetters = emptyList<String>()

   fun filter(letters: String) {
      viewModelScope.launch {
         filterLetters = filterLetters + letters

         watchTransmitter.requestContacts(filterLetters)
      }
   }
}
