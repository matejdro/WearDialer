package com.matejdro.weardialer.wear.data

import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.matejdro.weardialer.common.CommPaths
import com.matejdro.weardialer.model.ContactRequest
import com.matejdro.wearutils.messages.sendMessageToNearestClient
import javax.inject.Inject

class WatchTransmitter @Inject constructor(
   private val messageClient: MessageClient,
   private val nodeClient: NodeClient,
) {
   suspend fun requestContacts(filterLetters: List<String>) {
      val request = ContactRequest(filterLetters)

      messageClient.sendMessageToNearestClient(nodeClient, CommPaths.MESSAGE_REQUEST_CONTACTS, request.encode())
   }
}
