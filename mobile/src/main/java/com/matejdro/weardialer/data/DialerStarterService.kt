package com.matejdro.weardialer.data

import android.content.Intent
import com.matejdro.wearutils.companion.CompanionStarterService

class DialerStarterService: CompanionStarterService() {
    override fun createCompanionServiceIntent(): Intent {
        return Intent(this, ListenerService::class.java)
    }
}
