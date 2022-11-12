package com.matejdro.weardialer.data

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import com.matejdro.weardialer.model.Contact
import com.matejdro.weardialer.model.Number
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class ContactFilterer(private val context: Context, scope: CoroutineScope) {
   @OptIn(FlowPreview::class)
   private val calLog = suspend { loadCallLog() }.asFlow().shareIn(scope, SharingStarted.Eagerly, 1)

   suspend fun getContacts(filterLetters: List<String>): List<Contact> {
      val callLog = this.calLog.first()

      if (filterLetters.isEmpty()) {
         return callLog.map { it.contact }
      }

      val resolver = context.contentResolver
      val selection = "(${buildSelection(filterLetters)}) AND ${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1"
      val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME} ASC LIMIT 20"
      val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)

      val uri = ContactsContract.Contacts.CONTENT_URI

      return withContext(Dispatchers.IO) {
         resolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            val contacts = ArrayList<Contact>()

            while (cursor.moveToNext()) {
               val id = cursor.getLong(0)
               val name = cursor.getString(1)

               val numbers = getNumbers(id)
               val lastContact = callLog.find { it.contact.id == id }

               val contact = Contact(id, name, lastContact?.date, numbers)
               contacts += contact
            }

            contacts
         } ?: emptyList()
      }
   }

   private suspend fun getNumbers(contactId: Long): List<com.matejdro.weardialer.model.Number> {
      val resolver = context.contentResolver

      val selection =
         "${ContactsContract.Data.MIMETYPE} == \"${ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}\" AND ${ContactsContract.Data.CONTACT_ID} = ?"
      val selectionArgs = arrayOf(contactId.toString())
      val projection = arrayOf(
         ContactsContract.CommonDataKinds.Phone.NUMBER,
         ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
         ContactsContract.CommonDataKinds.Phone.TYPE,
         ContactsContract.CommonDataKinds.Phone.LABEL
      )

      val uri = ContactsContract.Data.CONTENT_URI

      return withContext(Dispatchers.IO) {
         resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            val numbers = ArrayList<Pair<com.matejdro.weardialer.model.Number, Boolean>>()

            while (cursor.moveToNext()) {
               val number = cursor.getString(0)
               val isPrimary = cursor.getInt(1) != 0
               val typeKey = cursor.getInt(2)
               val customTypeLabel = cursor.getString(3).orEmpty()

               val typeText = when (typeKey) {
                  ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
                  ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
                  ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
                  ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE -> "Work Mobile"
                  else -> customTypeLabel
               }

               numbers += (com.matejdro.weardialer.model.Number(typeText, number) to isPrimary)
            }

            numbers.sortedByDescending { it.second }.map { it.first }
         } ?: emptyList()
      }

   }

   private suspend fun loadCallLog(): List<CallLogEntry> {
      val resolver = context.contentResolver

      val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE)

      val instantOneMonthAgo = ZonedDateTime.now().minusMonths(1).toInstant().toEpochMilli()
      val selection = "${CallLog.Calls.DATE} > ?"
      val selectionArgs = arrayOf(instantOneMonthAgo.toString())

      val uri = CallLog.Calls.CONTENT_URI

      return withContext(Dispatchers.IO) {
         resolver.query(uri, projection, selection, selectionArgs, "${CallLog.Calls.DATE} DESC")?.use { cursor ->
            val callLog = ArrayList<CallLogEntry>()

            while (cursor.moveToNext()) {
               val number = cursor.getString(0)
               val date = cursor.getLong(1)
               val contact = lookupNumber(number).copy(lastCallTimestamp = date)

               callLog += CallLogEntry(number, date, contact)
            }

            callLog.distinctBy { it.number }
         } ?: emptyList()
      }
   }

   private suspend fun lookupNumber(number: String): Contact {
      val resolver = context.contentResolver

      val projection = arrayOf(ContactsContract.PhoneLookup.CONTACT_ID, ContactsContract.PhoneLookup.DISPLAY_NAME)


      val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))

      val contact = withContext(Dispatchers.IO) {
         resolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToNext()) {
               val contactId = cursor.getLong(0)
               val displayName = cursor.getString(1)

               contactId to displayName
            } else {
               null
            }
         }
      }

      return if (contact != null) {
         val (contactId, displayName) = contact

         Contact(contactId, displayName, numbers = listOf(Number(number = number)))
      } else {
         Contact(0, number, numbers = listOf(Number(number = number)))
      }
   }

   private fun buildSelection(filters: List<String>): String {
      if (filters.isEmpty()) {
         return "1"
      }

      val caseInsensitiveFilters = filters.map { "${it.lowercase()}${it.uppercase()}" }

      val mergedFilters = caseInsensitiveFilters.joinToString("") { "[$it]" }

      return (ContactsContract.Contacts.DISPLAY_NAME + " GLOB \"" + mergedFilters + "*\"")
   }

   private data class CallLogEntry(val number: String, val date: Long, val contact: Contact)
}
