package com.example.basicviews

import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Dashboard : AppCompatActivity() {

    private lateinit var database: BakeryDatabase
    private lateinit var adapter: BakeryAdapter
    private var allItems: List<BakeryItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // 1. Initialize Database & Intent Data
        database = BakeryDatabase.getDatabase(this, lifecycleScope)
        val userRole = intent.getStringExtra("USER_ROLE") ?: "customer"
        val isAdmin = userRole == "admin"

        // 2. Setup RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvBakeryItems)
        rv.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter with empty list first
        adapter = BakeryAdapter(listOf(), isAdmin) { itemToDelete ->
            lifecycleScope.launch(Dispatchers.IO) {
                database.bakeryDao().deleteItem(itemToDelete)
                // Flow handles the UI update automatically!
            }
        }
        rv.adapter = adapter

        // 3. REAL-TIME LISTENER (Fixed Type Mismatch)
        lifecycleScope.launch {
            database.bakeryDao().getAllItems().collect { items ->
                // 'items' is already List<BakeryItem> from the Flow
                allItems = items
                adapter.updateData(items)
            }
        }

        // 4. Setup UI Listeners
        setupCategoryFilters()
        setupSearch()

        // 5. Handle Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupCategoryFilters() {
        // Filter for Breads
        findViewById<CardView>(R.id.categoryGadgets).setOnClickListener {
            filterList("Breads")
        }
        // Filter for Pastries
        findViewById<CardView>(R.id.categoryClothing).setOnClickListener {
            filterList("Pastries")
        }
        // Filter for Cakes
        findViewById<CardView>(R.id.categoryAccessories).setOnClickListener {
            filterList("Cakes")
        }
        // Click "Products" title to reset/show all
        findViewById<TextView>(R.id.tvProductsTitle).setOnClickListener {
            adapter.updateData(allItems)
        }
    }

    private fun setupSearch() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = allItems.filter {
                    it.itemName.contains(newText ?: "", ignoreCase = true)
                }
                adapter.updateData(filtered)
                return true
            }
        })
    }

    private fun filterList(category: String) {
        val filtered = allItems.filter { it.category == category }
        adapter.updateData(filtered)
    }
}