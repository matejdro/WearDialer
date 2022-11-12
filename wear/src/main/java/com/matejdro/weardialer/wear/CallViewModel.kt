package com.matejdro.weardialer.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.weardialer.model.Contact
import com.matejdro.weardialer.wear.data.WatchTransmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
   private val watchTransmitter: WatchTransmitter
) : ViewModel() {
   private var filterLetters = emptyList<String>()
   private var rawContacts: List<Contact> = emptyList()

   init {
      updateWatch()
   }

   private val _phoneNumberSelection = MutableStateFlow<List<CallEntryNumber>?>(null)
   val phoneNumberSelection: StateFlow<List<CallEntryNumber>?>
      get() = _phoneNumberSelection

   private val _finishActivity = MutableStateFlow<Boolean>(false)
   val finishActivity: StateFlow<Boolean>
      get() = _finishActivity

   val displayedContacts: Flow<List<CallEntry>>
      get() {
         return watchTransmitter.getContactsFlow().map { list ->
            rawContacts = list

            list.map { dtoContact ->
               CallEntry(
                  dtoContact.id,
                  dtoContact.name,
                  dtoContact.lastCallTimestamp?.let { Instant.ofEpochMilli(it) }
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

   fun call(number: String) {
      viewModelScope.launch {
         watchTransmitter.callNumber(number)
         _finishActivity.value = true
      }
   }

   fun activateContact(contact: CallEntry) {
      val dtoContact = rawContacts.find { it.id == contact.id } ?: return
      if (dtoContact.numbers.size == 1) {
         call(dtoContact.numbers.first().number)
      } else {
         _phoneNumberSelection.value = dtoContact.numbers.map { CallEntryNumber(it.number, it.type) } +
                 listOf(CallEntryNumber(SPECIAL_NUMBER_BACK, ""))
      }
   }

   fun activateNumber(number: CallEntryNumber) {
      if (number.number == SPECIAL_NUMBER_BACK) {
         _phoneNumberSelection.value = null
      } else {
         call(number.number)
      }
   }

   private fun updateWatch() {
      viewModelScope.launch {
         watchTransmitter.requestContacts(filterLetters)
      }
   }
}

private const val SPECIAL_NUMBER_BACK = "BACK"
