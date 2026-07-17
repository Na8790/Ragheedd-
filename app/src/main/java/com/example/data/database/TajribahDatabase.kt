package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemTitle: String,
    val category: String,
    val date: String,
    val price: Double,
    val quantity: Int,
    val status: String, // "مؤكد", "قيد الانتظار", "ملغي"
    val qrCode: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user", "host", "guide"
    val message: String,
    val timestamp: Long,
    val chatPartner: String // e.g. "العم أحمد (نحال)"
)

@Entity(tableName = "ai_plans")
data class AiPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val budget: String,
    val peopleCount: String,
    val duration: String,
    val interests: String,
    val planText: String,
    val timestamp: Long
)

// --- Room DAO ---

@Dao
interface TajribahDao {
    // Bookings
    @Query("SELECT * FROM bookings ORDER BY id DESC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: Int)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE chatPartner = :partner ORDER BY timestamp ASC")
    fun getMessagesForPartnerFlow(partner: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    // AI Plans
    @Query("SELECT * FROM ai_plans ORDER BY timestamp DESC")
    fun getAllAiPlansFlow(): Flow<List<AiPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiPlan(plan: AiPlanEntity)
}

// --- Room Database ---

@Database(
    entities = [BookingEntity::class, ChatMessageEntity::class, AiPlanEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TajribahDatabase : RoomDatabase() {
    abstract fun tajribahDao(): TajribahDao

    companion object {
        @Volatile
        private var INSTANCE: TajribahDatabase? = null

        fun getDatabase(context: Context): TajribahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TajribahDatabase::class.java,
                    "tajribah_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
