package com.matejdro.weardialer.data

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.matejdro.weardialer.common.CommPaths
import com.matejdro.weardialer.model.ContactRequest

class ListenerService : WearableListenerService() {
   override fun onMessageReceived(event: MessageEvent) {
      when (event.path) {
         CommPaths.MESSAGE_REQUEST_CONTACTS -> {
            onContactRequestReceived(ContactRequest.ADAPTER.decode(event.data))
         }
         else -> {}
      }
   }

   private fun onContactRequestReceived(decode: ContactRequest) {
      println("Received $decode")
   }
}
