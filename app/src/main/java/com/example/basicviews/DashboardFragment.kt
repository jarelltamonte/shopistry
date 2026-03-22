package com.example.basicviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basicviews.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    val user = FirebaseAuth.getInstance().currentUser
    val name = user?.displayName ?: "Guest"

    private lateinit var database: DatabaseReference
    private val allProductsList = mutableListOf<BakeryItem>()
    private val displayList = mutableListOf<BakeryItem>()
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root

        binding.userGreeting.text = "Good morning, $name"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val databaseUrl = "https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app"
        database = FirebaseDatabase.getInstance(databaseUrl).getReference("Inventory")

        setupRecyclerView()

        fetchInventoryData()

        setupCategoryClickListeners()
        setupSearch()
    }

    private fun setupRecyclerView() {
        binding.productsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = InventoryAdapter(displayList)
        binding.productsRecyclerView.adapter = adapter
    }

    private fun fetchInventoryData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProductsList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(BakeryItem::class.java)
                    item?.let { allProductsList.add(it) }
                }
                showFilteredList(allProductsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Use requireContext() for Toasts in Fragments
                Toast.makeText(requireContext(), "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCategoryClickListeners() {
        binding.categoryCookies.setOnClickListener { filterByCategory("Cookies") }
        binding.categoryCakes.setOnClickListener { filterByCategory("Cakes") }
        binding.categoryBreads.setOnClickListener { filterByCategory("Breads") }
        binding.categoryPastries.setOnClickListener { filterByCategory("Pastries") }
        binding.tvCategoriesTitle.setOnClickListener { showFilteredList(allProductsList) }
    }

    private fun filterByCategory(categoryName: String) {
        val filtered = allProductsList.filter {
            it.category?.contains(categoryName, ignoreCase = true) == true
        }
        showFilteredList(filtered)
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText?.lowercase() ?: ""
                val filtered = allProductsList.filter {
                    it.productName?.lowercase()?.contains(searchText) == true
                }
                showFilteredList(filtered)
                return true
            }
        })
    }

    private fun showFilteredList(newList: List<BakeryItem>) {
        displayList.clear()
        displayList.addAll(newList)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
