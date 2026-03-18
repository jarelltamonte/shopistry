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

                                val initialItems = listOf(
                                    BakeryItem(0, "Butter Croissant", "Flaky and buttery", 75.0, "Pastries", 20, 0),
                                    BakeryItem(0, "Sourdough Loaf", "Freshly fermented", 180.0, "Breads", 10, 0),
                                    BakeryItem(0, "Chocolate Muffin", "Rich Belgian chocolate", 95.0, "Cakes", 15, 0),
                                    BakeryItem(0, "Baguette", "Crispy French crust", 120.0, "Breads", 12, 0),
                                    BakeryItem(0, "Red Velvet Cupcake", "Cream cheese frosting", 85.0, "Cakes", 18, 0),
                                    BakeryItem(0, "Blueberry Danish", "Sweet and tangy", 110.0, "Pastries", 8, 0),
                                    BakeryItem(0, "Whole Wheat Bread", "Healthy and fiber-rich", 140.0, "Breads", 15, 0),
                                    BakeryItem(0, "Eclair", "Cream filled chocolate", 130.0, "Pastries", 10, 0),
                                    BakeryItem(0, "Pandan Cake", "Filipino classic flavor", 450.0, "Cakes", 5, 0),
                                    BakeryItem(0, "Cheese Ensaymada", "Sweet brioche with cheese", 65.0, "Pastries", 25, 0)
                                )

                                initialItems.forEach { dao.insertItem(it) }
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