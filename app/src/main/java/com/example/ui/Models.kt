package com.example.ui

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

// --- Main Data Models for Tajribah Platform ---

data class ReviewItem(
    val id: Int,
    val userName: String,
    val rating: Float,
    val comment: String,
    val date: String,
    val likes: Int,
    val hostReply: String? = null
)

data class ExperienceItem(
    val id: Int,
    val title: String,
    val hostName: String,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val price: Double,
    val duration: String,
    val city: String,
    val description: String,
    val isFavorite: Boolean = false,
    val reviews: List<ReviewItem> = emptyList()
)

data class TripItem(
    val id: Int,
    val title: String,
    val description: String,
    val city: String,
    val duration: String,
    val price: Double,
    val rating: Float,
    val isSurprise: Boolean = false
)

data class GuideItem(
    val id: Int,
    val name: String,
    val specialty: String,
    val city: String,
    val bio: String,
    val pricePerDay: Double,
    val rating: Float,
    val reviewsCount: Int
)

data class CarItem(
    val id: Int,
    val name: String,
    val type: String, // SUV, Sedan, Luxury
    val pricePerDay: Double,
    val transmission: String, // Automatic, Manual
    val fuelType: String,
    val rating: Float
)

data class GiftItem(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val pointsRequired: Int
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

data class ComplaintItem(
    val id: Int,
    val userName: String,
    val email: String,
    val subject: String,
    val content: String,
    val date: String,
    val status: String // "Pending", "Resolved"
)

// --- Navigation Screens Sealed Class ---
sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Register : Screen()
    object MainDashboard : Screen()
    object AiPlanner : Screen()
    data class ExperienceDetail(val experience: ExperienceItem) : Screen()
    object CarRental : Screen()
    object GuidesList : Screen()
    object AdminDashboard : Screen()
    object TechnicalSupport : Screen()
    object Gifts : Screen()
    object AboutApp : Screen()
    object Notifications : Screen()
}

// --- Dashboard Sub-Tabs ---
enum class DashboardTab {
    Home,         // الرئيسية
    Experiences,  // التجارب
    Trips,        // الرحلات
    Bookings,     // الحجوزات
    Profile       // الملف الشخصي
}
