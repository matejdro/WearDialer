package com.matejdro.weardialer.data

import android.R.id
import android.content.Intent
import android.net.Uri
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.matejdro.weardialer.common.CommPaths
import com.matejdro.weardialer.model.Call
import com.matejdro.weardialer.model.ContactRequest
import com.matejdro.weardialer.model.Contacts
import com.matejdro.wearutils.messages.sendMessageToNearestClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ListenerService : WearableListenerService() {
    private val scope = MainScope()

    private lateinit var messageClient: MessageClient
    private lateinit var nodeClient: NodeClient
    private lateinit var whatsappRecentCallsStore: WhatsappRecentCallsStore
    private lateinit var contactFilterer: ContactFilterer

    override fun onCreate() {
        super.onCreate()

        messageClient = Wearable.getMessageClient(this)
        nodeClient = Wearable.getNodeClient(this)
        whatsappRecentCallsStore = WhatsappRecentCallsStore(this)
        contactFilterer = ContactFilterer(this, whatsappRecentCallsStore, scope)
    }

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            CommPaths.MESSAGE_REQUEST_CONTACTS -> {
                onContactRequestReceived(ContactRequest.ADAPTER.decode(event.data))
            }

            CommPaths.MESSAGE_CALL -> {
                onCallReceived(Call.ADAPTER.decode(event.data))
            }

            else -> {}
        }
    }

    private fun onContactRequestReceived(request: ContactRequest) {
        scope.launch {
            val contacts = contactFilterer.getContacts(request.filterLetters)
            messageClient.sendMessageToNearestClient(
                nodeClient,
                CommPaths.MESSAGE_CONTACT_DATA,
                Contacts(contacts).encode()
            )
        }
    }

    private fun onCallReceived(call: Call) {
        scope.launch {
            val contact = contactFilterer.lookupNumber(call.number)
            val whatsappId = contact.id.takeIf { it != 0L }?.let {
                contactFilterer.getWhatsappNumber(it, call.number)
            }

            if (whatsappId != null) {
                val intent = Intent()
                intent.setAction(Intent.ACTION_VIEW)

                whatsappRecentCallsStore.insert(
                    WhatsappRecentCallEntry(
                        null,
                        call.number,
                        System.currentTimeMillis(),
                        contact.id
                    )
                )
                intent.setDataAndType(
                    Uri.parse("content://com.android.contacts/data/${whatsappId}"),
                    "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
                )
                intent.setPackage("com.whatsapp")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
            } else {
                val intent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:" + Uri.encode(call.number))
                ).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
