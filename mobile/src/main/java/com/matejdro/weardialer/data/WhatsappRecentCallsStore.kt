package com.matejdro.weardialer.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.matejdro.weardialer.data.WhatsappDao
import com.matejdro.weardialer.model.Contact
import javax.inject.Inject

class WhatsappRecentCallsStore(private val context: Context) {
    private val db = Room.databaseBuilder(context, WhatsappDatabase::class.java, "db").build()
    private val dao = db.dao()

    suspend fun getRecent(): List<WhatsappRecentCallEntry> {
        return dao.getRecent()
    }

    suspend fun insert(entry: WhatsappRecentCallEntry) {
        return dao.insert(entry)
    }
}

@Database(entities = [WhatsappRecentCallEntry::class], version = 1)
internal abstract class WhatsappDatabase : RoomDatabase() {
    abstract fun dao(): WhatsappDao
}

@Dao
internal interface WhatsappDao {
    @Query("SELECT * FROM recent_calls ORDER BY date DESC LIMIT 20")
    suspend fun getRecent(): List<WhatsappRecentCallEntry>

    @Insert
    suspend fun insert(entry: WhatsappRecentCallEntry)
}

@Entity("recent_calls")
data class WhatsappRecentCallEntry(
    @PrimaryKey val id: Int?,
    @ColumnInfo("number") val number: String,
    @ColumnInfo("date") val date: Long,
    @ColumnInfo("contactId") val contactId: Long
)
