package com.example.basicviews

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Include all 4 entities here
@Database(entities = [User::class, BakeryItem::class, CartItem::class, Order::class], version = 1)
abstract class BakeryDatabase : RoomDatabase() {
    abstract fun bakeryDao(): BakeryDao

    companion object {
        @Volatile
        private var INSTANCE: BakeryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BakeryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BakeryDatabase::class.java,
                    "bakery_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                val dao = getDatabase(context, scope).bakeryDao()

                                // 1. Hardcoded Users
                                dao.insertUser(User(username = "admin", password = "123", role = "admin"))
                                dao.insertUser(User(username = "jarell", password = "password", role = "customer"))

                                // 2. Hardcoded Bakery Items (Catalog)
                                dao.insertItem(BakeryItem(itemName = "Croissant", itemDescription = "Buttery & Flaky", price = 75.0, category = "Pastries", stockQuantity = 10, imageResId = 0))
                                dao.insertItem(BakeryItem(itemName = "Sourdough", itemDescription = "Freshly Fermented", price = 150.0, category = "Breads", stockQuantity = 5, imageResId = 0))
                                dao.insertItem(BakeryItem(itemName = "Red Velvet Cupcake", itemDescription = "Cream cheese frosting", price = 95.0, category = "Cakes", stockQuantity = 8, imageResId = 0))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}