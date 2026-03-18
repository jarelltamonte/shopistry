package com.example.basicviews

import androidx.room.Entity
import androidx.room.PrimaryKey

/**USERS TABLE**/
@Entity(tableName = "users_table")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val password: String,
    val role: String // Use "admin" or "customer"
)

/**ITEMS TABLE**/
@Entity(tableName = "items_table")
data class BakeryItem(
    @PrimaryKey(autoGenerate = true) val itemId: Int = 0,
    val itemName: String,
    val itemDescription: String,
    val price: Double,
    val category: String, // e.g., "Pastries", "Breads"
    val stockQuantity: Int,
    val imageResId: Int // Stores the ID of the image in your 'drawable' folder
)


/**CART TABLE**/
@Entity(tableName = "cart_table")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val cartEntryId: Int = 0,
    val userId: Int, // Links to User.userId
    val itemId: Int, // Links to BakeryItem.itemId
    val quantity: Int
)


/**ORDER TABLE**/
@Entity(tableName = "orders_table")
data class Order(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val userId: Int,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)