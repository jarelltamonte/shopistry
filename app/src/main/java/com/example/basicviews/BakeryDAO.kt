package com.example.basicviews

import androidx.room.*

@Dao
interface BakeryDao {
    // --- USERS ---
    @Query("SELECT * FROM users_table WHERE username = :uname AND password = :pword LIMIT 1")
    suspend fun login(uname: String, pword: String): User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    // --- ITEMS ---
    @Query("SELECT * FROM items_table")
    suspend fun getAllItems(): List<BakeryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BakeryItem)

    // --- CART ---
    @Insert
    suspend fun addToCart(cartItem: CartItem)

    @Query("SELECT * FROM cart_table WHERE userId = :uid")
    suspend fun getUserCart(uid: Int): List<CartItem>

    @Query("DELETE FROM cart_table WHERE userId = :uid")
    suspend fun clearCart(uid: Int)

    // --- ORDERS ---
    @Insert
    suspend fun placeOrder(order: Order)

    @Query("SELECT * FROM orders_table WHERE userId = :uid")
    suspend fun getOrderHistory(uid: Int): List<Order>
}