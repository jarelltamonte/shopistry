package com.example.basicviews

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basicviews.databinding.ActivityAdminBinding
import com.example.basicviews.databinding.AdminPopupBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Admin : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // ✅ RecyclerView variables
    private lateinit var itemList: MutableList<BakeryItem>
    private lateinit var adapter: InventoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val databaseUrl = "https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app"
        database = FirebaseDatabase.getInstance(databaseUrl).getReference("Inventory")

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        itemList = mutableListOf()
        adapter = InventoryAdapter(itemList)

        binding.inventoryRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.inventoryRecyclerView.adapter = adapter

        loadItems()

        binding.fabAddItem.setOnClickListener {
            showAddPopup()
        }
    }

    private fun loadItems() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()

                for (snap in snapshot.children) {
                    val item = snap.getValue(BakeryItem::class.java)
                    if (item != null) {
                        itemList.add(item)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Admin, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val popupBinding = AdminPopupBinding.inflate(layoutInflater)
        dialog.setContentView(popupBinding.root)

        popupBinding.accordionHeader.setOnClickListener {
            val isVisible = popupBinding.accordionContent.visibility == View.VISIBLE
            popupBinding.accordionContent.visibility =
                if (isVisible) View.GONE else View.VISIBLE

            popupBinding.arrowIcon.animate()
                .rotation(if (isVisible) 0f else 180f)
                .start()
        }

        popupBinding.publishButton.setOnClickListener {

            val name = popupBinding.productInput.text.toString().trim()
            val qtyInput = popupBinding.quantityInput.text.toString().trim()
            val priceInput = popupBinding.priceInput.text.toString().trim()
            val description = popupBinding.descriptionInput.text.toString().trim()
            val imageUrl = popupBinding.imageInput.text.toString().trim()

            val selectedChipId = popupBinding.categoryChipGroup.checkedChipId
            val category = if (selectedChipId != View.NO_ID) {
                popupBinding.categoryChipGroup
                    .findViewById<Chip>(selectedChipId)
                    .text.toString()
            } else {
                "Uncategorized"
            }

            if (name.isEmpty() || qtyInput.isEmpty() || priceInput.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Please fill all fields including Image URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveDataToDatabase(
                name,
                qtyInput,
                priceInput,
                category,
                description,
                imageUrl,
                dialog
            )
        }

        dialog.show()
    }

    private fun saveDataToDatabase(
        name: String,
        qty: String,
        price: String,
        category: String,
        description: String,
        imageUrl: String,
        dialog: BottomSheetDialog
    ) {
        val itemId = database.push().key ?: return

        val newItem = BakeryItem(
            id = itemId,
            productName = name,
            quantity = qty,
            price = price,
            category = category,
            description = description,
            image = imageUrl
        )

        database.child(itemId).setValue(newItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item Published!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}