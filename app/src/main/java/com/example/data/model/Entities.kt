package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    SELLER,
    ADMIN
}

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val id: String,
    val phone: String,
    val name: String,
    val email: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val businessName: String = "",
    val gstNumber: String = "",
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val originalPrice: Double,
    val discountPercent: Int,
    val sellingPrice: Double,
    val stock: Int,
    val sizes: String, // Comma separated, e.g. "S, M, L, XL"
    val colors: String, // Comma separated, e.g. "Crimson Red, Maroon, Navy Blue"
    val description: String,
    val imageUrls: String, // Pipe separated list of image URLs or resource names
    val sellerId: String,
    val sellerName: String,
    val rating: Float = 4.3f,
    val reviewCount: Int = 120,
    val isApproved: Boolean = true,
    val isFeatured: Boolean = false,
    val freeDelivery: Boolean = true,
    val codAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getImageList(): List<String> {
        return imageUrls.split("|").filter { it.isNotBlank() }
    }

    fun getSizeList(): List<String> {
        return sizes.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    fun getColorList(): List<String> {
        return colors.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
}

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val quantity: Int = 1,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class Address(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val houseNoStreet: String,
    val areaColony: String,
    val city: String,
    val state: String,
    val pincode: String,
    val isDefault: Boolean = false
) {
    fun formatted(): String {
        return "$fullName, $houseNoStreet, $areaColony, $city, $state - $pincode (Ph: $phone)"
    }
}

enum class OrderStatus {
    PLACED,
    ACCEPTED,
    PACKED,
    DISPATCHED,
    DELIVERED,
    CANCELLED
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val sellerId: String,
    val productId: String,
    val productName: String,
    val productImage: String,
    val quantity: Int,
    val selectedSize: String,
    val selectedColor: String,
    val itemPrice: Double,
    val totalPrice: Double,
    val deliveryAddress: String,
    val paymentMethod: String, // "Cash on Delivery", "UPI", "Credit/Debit Card"
    val status: OrderStatus = OrderStatus.PLACED,
    val trackingId: String = "",
    val courierName: String = "Shopy Express",
    val placedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "ORDER", "PROMO", "SELLER", "ADMIN"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
