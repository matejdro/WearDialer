package com.matejdro.weardialer.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.weardialer.wear.data.WatchTransmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
   private val watchTransmitter: WatchTransmitter
) : ViewModel() {
   private var filterLetters = emptyList<String>()

   init {
      updateWatch()
   }

   val displayedContacts: Flow<List<CallEntry>>
      get() {
         return watchTransmitter.getContactsFlow().map { list ->
            list.map { dtoContact ->
               CallEntry(
                  dtoContact.name,
                  Instant.ofEpochMilli(dtoContact.lastCallTimestamp)
               )
            }
         }
      }

   fun filter(letters: String) {
      filterLetters = filterLetters + letters
      updateWatch()
   }

   fun backspace() {
      filterLetters = filterLetters.dropLast(1)
      updateWatch()
   }

   private fun updateWatch() {
      viewModelScope.launch {
         watchTransmitter.requestContacts(filterLetters)
      }
   }
}
