package com.example.data.repository

import android.content.Context
import com.example.data.api.GeminiClient
import com.example.data.database.AiPlanEntity
import com.example.data.database.BookingEntity
import com.example.data.database.ChatMessageEntity
import com.example.data.database.TajribahDatabase
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TajribahRepository(context: Context) {
    private val db = TajribahDatabase.getDatabase(context)
    private val dao = db.tajribahDao()

    // Bookings Flow
    fun getBookings(): Flow<List<BookingEntity>> = dao.getAllBookingsFlow()

    suspend fun createBooking(itemTitle: String, category: String, date: String, price: Double, quantity: Int) {
        val qrCodeValue = "TAJRIBAH-${itemTitle.hashCode().coerceAtLeast(0)}-${System.currentTimeMillis()}"
        val booking = BookingEntity(
            itemTitle = itemTitle,
            category = category,
            date = date,
            price = price,
            quantity = quantity,
            status = "مؤكد",
            qrCode = qrCodeValue
        )
        dao.insertBooking(booking)
    }

    suspend fun cancelBooking(id: Int) {
        dao.deleteBookingById(id)
    }

    // Chat Flow
    fun getChatMessages(partner: String): Flow<List<ChatMessageEntity>> = dao.getMessagesForPartnerFlow(partner)

    suspend fun sendChatMessage(partner: String, sender: String, text: String) {
        val message = ChatMessageEntity(
            sender = sender,
            message = text,
            timestamp = System.currentTimeMillis(),
            chatPartner = partner
        )
        dao.insertChatMessage(message)
    }

    // AI Plans Flow
    fun getAiPlans(): Flow<List<AiPlanEntity>> = dao.getAllAiPlansFlow()

    suspend fun generateAndSaveAiPlan(
        city: String,
        budget: String,
        peopleCount: String,
        duration: String,
        interests: String
    ): String {
        val planText = GeminiClient.generateTravelPlan(
            city = city,
            budget = budget,
            peopleCount = peopleCount,
            duration = duration,
            interests = interests
        )
        
        val aiPlan = AiPlanEntity(
            city = city,
            budget = budget,
            peopleCount = peopleCount,
            duration = duration,
            interests = interests,
            planText = planText,
            timestamp = System.currentTimeMillis()
        )
        dao.insertAiPlan(aiPlan)
        return planText
    }
}
