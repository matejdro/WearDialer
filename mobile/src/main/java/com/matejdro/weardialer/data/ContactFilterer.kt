package com.matejdro.weardialer.data

import android.content.Context
import android.provider.ContactsContract
import com.matejdro.weardialer.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactFilterer(private val context: Context) {
   suspend fun getContacts(filterLetters: List<String>): List<Contact> {
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

               val contact = Contact(id, name)
               contacts += contact
            }

            contacts
         } ?: emptyList()
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
}
