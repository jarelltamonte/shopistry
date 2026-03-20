package com.example.basicviews

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.basicviews.databinding.ActivityAdminBinding
import com.example.basicviews.databinding.AdminPopupBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Admin : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

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

        binding.fabAddItem.setOnClickListener {
            showAddPopup()
        }
    }

    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val popupBinding = AdminPopupBinding.inflate(layoutInflater)
        dialog.setContentView(popupBinding.root)

        popupBinding.accordionHeader.setOnClickListener {
            val isVisible = popupBinding.accordionContent.visibility == View.VISIBLE
            popupBinding.accordionContent.visibility = if (isVisible) View.GONE else View.VISIBLE
            popupBinding.arrowIcon.animate().rotation(if (isVisible) 0f else 180f).start()
        }

        popupBinding.publishButton.setOnClickListener {
            val name = popupBinding.productInput.text.toString().trim()
            val qtyInput = popupBinding.quantityInput.text.toString().trim()
            val priceInput = popupBinding.priceInput.text.toString().trim()

            val description = popupBinding.descriptionInput.text.toString().trim()

            val selectedChipId = popupBinding.categoryChipGroup.checkedChipId
            val category = if (selectedChipId != View.NO_ID) {
                popupBinding.categoryChipGroup.findViewById<Chip>(selectedChipId).text.toString()
            } else {
                "Uncategorized"
            }

            if (name.isEmpty() || qtyInput.isEmpty() || priceInput.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                saveDataToDatabase(
                    name,
                    qtyInput.toInt(),
                    priceInput.toDouble(),
                    category,
                    description,
                    dialog
                )
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun saveDataToDatabase(
        name: String,
        qty: Int,
        price: Double,
        category: String,
        description: String,
        dialog: BottomSheetDialog
    ) {
        val itemId = database.push().key ?: return

        val newItem = BakeryItem(
            id = itemId,
            productName = name,
            quantity = qty.toString(),
            price = price.toString(),
            category = category,
            description = description
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
