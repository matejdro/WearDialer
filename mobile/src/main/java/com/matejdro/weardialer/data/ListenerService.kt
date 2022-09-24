package com.matejdro.weardialer.data

import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.matejdro.weardialer.common.CommPaths
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
   private lateinit var contactFilterer: ContactFilterer

   override fun onCreate() {
      super.onCreate()

      messageClient = Wearable.getMessageClient(this)
      nodeClient = Wearable.getNodeClient(this)
      contactFilterer = ContactFilterer(this)
   }

   override fun onMessageReceived(event: MessageEvent) {
      when (event.path) {
         CommPaths.MESSAGE_REQUEST_CONTACTS -> {
            onContactRequestReceived(ContactRequest.ADAPTER.decode(event.data))
         }
         else -> {}
      }
   }

   private fun onContactRequestReceived(request: ContactRequest) {
      scope.launch {
         val contacts = contactFilterer.getContacts(request.filterLetters)
         messageClient.sendMessageToNearestClient(nodeClient, CommPaths.MESSAGE_CONTACT_DATA, Contacts(contacts).encode())
      }
   }

   override fun onDestroy() {
      super.onDestroy()
      scope.cancel()
   }
}
