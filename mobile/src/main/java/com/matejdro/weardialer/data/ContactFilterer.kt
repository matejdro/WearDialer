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

class ContactFilterer(
    private val context: Context,
    private val whatsappRecentCallsStore: WhatsappRecentCallsStore,
    scope: CoroutineScope
) {
    @OptIn(FlowPreview::class)
    private val calLog =
        suspend { loadCallLog() }.asFlow().shareIn(scope, SharingStarted.Eagerly, 1)

    suspend fun getContacts(filterLetters: List<String>): List<Contact> {
        val callLog = this.calLog.first()

        if (filterLetters.isEmpty()) {
            return callLog.map { it.contact }
        }

        val resolver = context.contentResolver
        val selection =
            "(${buildSelection(filterLetters)}) AND ${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1"
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME} ASC LIMIT 20"
        val projection =
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)

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

        val nativeCallLog = withContext(Dispatchers.IO) {
            resolver.query(uri, projection, selection, selectionArgs, "${CallLog.Calls.DATE} DESC")
                ?.use { cursor ->
                    val callLog = ArrayList<CallLogEntry>()

                    while (cursor.moveToNext()) {
                        val number = cursor.getString(0)
                        if (number.isBlank()) {
                            continue
                        }

                        val date = cursor.getLong(1)
                        val contact = lookupNumber(number).copy(lastCallTimestamp = date)

                        callLog += CallLogEntry(number, date, contact)
                    }

                    callLog.distinctBy { it.number }
                } ?: emptyList()
        }

        val whatsappCallLog = whatsappRecentCallsStore.getRecent().map {
            with(it) {
                CallLogEntry(
                    number,
                    date,
                    lookupContact(contactId, number).copy(lastCallTimestamp = date)
                )
            }
        }

        return (nativeCallLog + whatsappCallLog).sortedByDescending { it.date }
    }

    suspend fun lookupNumber(number: String): Contact {
        println("lookupNumber $number")
        val resolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.PhoneLookup.CONTACT_ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )


        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )

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

    suspend fun lookupContact(id: Long, number: String): Contact {
        val resolver = context.contentResolver

        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)


        val uri = ContactsContract.Contacts.CONTENT_URI

        val displayName = withContext(Dispatchers.IO) {
            resolver.query(
                uri,
                projection,
                "${ContactsContract.Contacts._ID} = ?",
                arrayOf(id.toString()),
                null
            )?.use { cursor ->
                if (cursor.moveToNext()) {
                    val displayName = cursor.getString(0)

                    displayName
                } else {
                    null
                }
            }
        }

        return if (displayName != null) {
            Contact(id, displayName, numbers = listOf(Number(number = number)))
        } else {
            Contact(0, number, numbers = listOf(Number(number = number)))
        }
    }

    suspend fun getWhatsappNumber(contactId: Long, phoneNumber: String): Long? {
        val resolver = context.contentResolver

        val projection = arrayOf(ContactsContract.Data._ID, ContactsContract.Data.DATA1)

        val processedPhoneNumber = NO_NUMBER_REGEX.replace(phoneNumber, "").let {
            if (it.startsWith("0")) {
                "386" + it.drop(1)
            } else {
                it
            }
        }

        val uri = ContactsContract.Data.CONTENT_URI
        val selection =
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = \"vnd.android.cursor.item/vnd.com.whatsapp.voip.call\""
        val selectionArgs = arrayOf(contactId.toString())

        return withContext(Dispatchers.IO) {
            var resultId: Long? = null

            resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val targetNumber = cursor.getString(1).removeSuffix("@s.whatsapp.net")
                    if (processedPhoneNumber == targetNumber) {
                        resultId = id
                        break
                    }
                }
            }

            resultId
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

private val NO_NUMBER_REGEX = Regex("[^0-9]")
