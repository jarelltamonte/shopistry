package com.example.basicviews

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 1. USERS TABLE
 * Stores login credentials and roles (Admin vs Regular)
 */
@Entity(tableName = "users_table")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val password: String,
    val role: String // Use "admin" or "customer"
)

/**
 * 2. ITEMS TABLE
 * This is your Bakery Catalog (Croissants, Cakes, etc.)
 */
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

/**
 * 3. CART TABLE
 * Acts as a temporary bridge between a User and the Items they want to buy
 */
@Entity(tableName = "cart_table")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val cartEntryId: Int = 0,
    val userId: Int, // Links to User.userId
    val itemId: Int, // Links to BakeryItem.itemId
    val quantity: Int
)

/**
 * 4. ORDERS TABLE
 * For the final checkout history
 */
@Entity(tableName = "orders_table")
data class Order(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val userId: Int,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)