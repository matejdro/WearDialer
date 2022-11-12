package com.matejdro.weardialer.wear.data

import android.net.Uri
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.matejdro.weardialer.common.CommPaths
import com.matejdro.weardialer.model.Call
import com.matejdro.weardialer.model.Contact
import com.matejdro.weardialer.model.ContactRequest
import com.matejdro.weardialer.model.Contacts
import com.matejdro.wearutils.messages.sendMessageToNearestClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class WatchTransmitter @Inject constructor(
    private val messageClient: MessageClient,
    private val nodeClient: NodeClient,
) {
    fun getContactsFlow(): Flow<List<Contact>> {
        return callbackFlow {
            val listener = MessageClient.OnMessageReceivedListener { event ->
                trySend(Contacts.Companion.ADAPTER.decode(event.data).contacts)
            }
            messageClient.addListener(
                listener,
                Uri.parse("wear://*${CommPaths.MESSAGE_CONTACT_DATA}"),
                MessageClient.FILTER_LITERAL
            )

            awaitClose {
                messageClient.removeListener(listener)
            }
        }
    }

    suspend fun requestContacts(filterLetters: List<String>) {
        val request = ContactRequest(filterLetters)

        messageClient.sendMessageToNearestClient(
            nodeClient,
            CommPaths.MESSAGE_REQUEST_CONTACTS,
            request.encode()
        )
    }

    suspend fun callNumber(number: String) {
        val call = Call(number)

        messageClient.sendMessageToNearestClient(
            nodeClient,
            CommPaths.MESSAGE_CALL,
            call.encode()
        )
    }
}
