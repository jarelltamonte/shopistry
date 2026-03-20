package com.example.basicviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.basicviews.databinding.FragmentDashboardBinding
import com.google.firebase.database.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var adapter: BakeryAdapter
    private val itemList = mutableListOf<BakeryItem>()

    //For the search functionality
    private val allItems = mutableListOf<BakeryItem>() // All items
    private val displayedItems = mutableListOf<BakeryItem>() // What will be displayed, gets all if walang search

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val databaseUrl = "https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app"
        database = FirebaseDatabase.getInstance(databaseUrl).getReference("Inventory")

       // Recycle view instead of card view apparently for more dynamic display of Firebase items.
        setupRecyclerView()
        fetchInventoryData()

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // monitors changes per letter & then updates
                filterList(newText)
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = BakeryAdapter(itemList)
        binding.rvInventory.layoutManager = LinearLayoutManager(context)
        binding.rvInventory.adapter = adapter
    }

    private fun fetchInventoryData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allItems.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(BakeryItem::class.java)
                    item?.let { allItems.add(it) }
                }
                // Initially show all items, then when user puts chars in search, lcive update
                filterList(binding.searchView.query.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun filterList(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allItems // Show everything if search is empty
        } else {
            allItems.filter { //This consider both ta product name and also category for convenience. If its too much (since there is category button function, remove OR argument)
                it.productName?.contains(query, ignoreCase = true) == true ||
                        it.category?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.updateList(filteredList)
    }
}