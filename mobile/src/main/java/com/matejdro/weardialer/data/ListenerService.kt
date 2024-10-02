package com.matejdro.weardialer.data

import android.R.id
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.matejdro.weardialer.R
import com.matejdro.weardialer.common.CommPaths
import com.matejdro.weardialer.model.Call
import com.matejdro.weardialer.model.ContactRequest
import com.matejdro.weardialer.model.Contacts
import com.matejdro.wearutils.companion.WearableCompanionService
import com.matejdro.wearutils.messages.sendMessageToNearestClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ListenerService : WearableCompanionService() {
    private lateinit var nodeClient: NodeClient
    private lateinit var whatsappRecentCallsStore: WhatsappRecentCallsStore
    private lateinit var contactFilterer: ContactFilterer

    override fun onCreate() {
        super.onCreate()

        nodeClient = Wearable.getNodeClient(this)
        whatsappRecentCallsStore = WhatsappRecentCallsStore(this)
        contactFilterer = ContactFilterer(this, whatsappRecentCallsStore, coroutineScope)
    }

    override fun onMessageReceived(event: MessageEvent) {
        super.onMessageReceived(event)

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
        coroutineScope.launch {
            println("contact request received")
            val contacts = try {
                contactFilterer.getContacts(request.filterLetters)
            } catch (e: Exception) {
                println("getContacts failed")
                e.printStackTrace()
                throw e
            }
            println("got contacts")
            messageClient.sendMessageToNearestClient(
                nodeClient,
                CommPaths.MESSAGE_CONTACT_DATA,
                Contacts(contacts).encode()
            )
            println("message sent")
        }
    }

    private fun onCallReceived(call: Call) {
        coroutineScope.launch {
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

    override fun createOngoingNotification(): Notification {
        createNotificationChannel()
        val notificationBuilder = NotificationCompat.Builder(this, KEY_NOTIFICATION_CHANNEL)
            .setContentTitle("Dialer active")
            .setContentText("Dialer active")
            .setSmallIcon(R.drawable.ic_dialer)

        return notificationBuilder.build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val persistentChannel = NotificationChannel(KEY_NOTIFICATION_CHANNEL,
            "Dialer active",
            NotificationManager.IMPORTANCE_MIN)
        notificationManager.createNotificationChannel(persistentChannel)
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

private const val KEY_NOTIFICATION_CHANNEL = "Service_Channel"
