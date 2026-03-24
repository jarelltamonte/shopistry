package com.example.basicviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Context
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.example.basicviews.databinding.AdminPopupBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class InventoryAdapter(
    private val itemList: List<BakeryItem>,
    private val userRole: String = "customer",
    private val isCartView: Boolean = false
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.itemImage)
        val name: TextView = itemView.findViewById(R.id.itemName)
        val price: TextView = itemView.findViewById(R.id.itemPrice)
        val quantity: TextView = itemView.findViewById(R.id.itemQuantity)
        val category: TextView = itemView.findViewById(R.id.itemCategory)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        holder.name.text = item.productName
        holder.price.text = "₱${item.price}"
        holder.quantity.text = "${item.quantity} items left"
        holder.category.text = item.category

        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.image)

        holder.editButton.visibility = if (userRole == "customer") View.GONE else View.VISIBLE
        holder.deleteButton.visibility = if (isCartView) View.VISIBLE else View.GONE

        holder.deleteButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null && item.id != null) {
                FirebaseDatabase.getInstance("https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("Carts").child(uid).child(item.id).removeValue()
            }
        }

        holder.editButton.setOnClickListener {
            showEditPopup(holder.itemView.context, item)
        }

        holder.itemView.setOnClickListener {
            if (userRole == "customer") {
                showItemPopup(holder.itemView.context, item)
            }
        }
    }

    private fun showEditPopup(context: Context, item: BakeryItem) {
        val dialog = BottomSheetDialog(context)
        val popupBinding = AdminPopupBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(popupBinding.root)

        popupBinding.productInput.setText(item.productName)
        popupBinding.quantityInput.setText(item.quantity)
        popupBinding.priceInput.setText(item.price)
        popupBinding.imageInput.setText(item.image)
        popupBinding.descriptionInput.setText(item.description)

        for (i in 0 until popupBinding.categoryChipGroup.childCount) {
            val chip = popupBinding.categoryChipGroup.getChildAt(i) as? Chip
            if (chip?.text.toString() == item.category) {
                chip?.isChecked = true
                break
            }
        }

        popupBinding.publishButton.text = "Save Changes"

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
            val qty = popupBinding.quantityInput.text.toString().trim()
            val price = popupBinding.priceInput.text.toString().trim()
            val description = popupBinding.descriptionInput.text.toString().trim()
            val imageUrl = popupBinding.imageInput.text.toString().trim()

            val selectedChipId = popupBinding.categoryChipGroup.checkedChipId
            val category = if (selectedChipId != View.NO_ID) {
                popupBinding.categoryChipGroup
                    .findViewById<Chip>(selectedChipId)
                    .text.toString()
            } else {
                item.category ?: "Uncategorized"
            }

            if (name.isEmpty() || qty.isEmpty() || price.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val databaseUrl = "https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app"
            val database = FirebaseDatabase.getInstance(databaseUrl).getReference("Inventory")

            val updatedItem = BakeryItem(
                id = item.id,
                productName = name,
                quantity = qty,
                price = price,
                category = category,
                description = description,
                image = imageUrl
            )

            database.child(item.id!!).setValue(updatedItem)
                .addOnSuccessListener {
                    Toast.makeText(context, "Item updated!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun showItemPopup(context: Context, item: BakeryItem) {
        val dialog = BottomSheetDialog(context)
        val popupBinding = com.example.basicviews.databinding.ActivityItemInfoBinding
            .inflate(LayoutInflater.from(context))
        dialog.setContentView(popupBinding.root)

        // ✅ Force the bottom sheet to expand fully
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.requestLayout()
            }
        }

        var selectedQty = 1
        val maxStock = item.quantity?.replace(Regex("[^\\d]"), "")?.toIntOrNull() ?: 0

        popupBinding.tvProductName.text = item.productName
        popupBinding.tvPrice.text = "₱${item.price}"
        popupBinding.tvDescription.text = item.description ?: "No description available."
        popupBinding.tvStockCount.text = "Available: $maxStock units"

        if (maxStock > 0) {
            popupBinding.tvStockStatus.text = "In Stock"
            popupBinding.tvStockStatus.setBackgroundColor(
                android.graphics.Color.parseColor("#388E3C")
            )
        } else {
            popupBinding.tvStockStatus.text = "Out of Stock"
            popupBinding.tvStockStatus.setBackgroundColor(
                android.graphics.Color.parseColor("#D32F2F")
            )
        }

        Glide.with(context)
            .load(item.image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(popupBinding.productImage)

        popupBinding.tvQuantity.text = selectedQty.toString()

        popupBinding.btnIncrease.setOnClickListener {
            if (selectedQty < maxStock) {
                selectedQty++
                popupBinding.tvQuantity.text = selectedQty.toString()
            } else {
                Toast.makeText(context, "Max stock reached", Toast.LENGTH_SHORT).show()
            }
        }

        popupBinding.btnDecrease.setOnClickListener {
            if (selectedQty > 1) {
                selectedQty--
                popupBinding.tvQuantity.text = selectedQty.toString()
            } else {
                Toast.makeText(context, "Minimum order is 1", Toast.LENGTH_SHORT).show()
            }
        }

        popupBinding.btnBack.setOnClickListener { dialog.dismiss() }

        popupBinding.addCart.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (item.id == null) return@setOnClickListener

            val cartRef = FirebaseDatabase.getInstance(
                "https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app"
            ).getReference("Carts").child(userId).child(item.id)

            val cartItem = mapOf(
                "id" to item.id,
                "productName" to item.productName,
                "quantity" to selectedQty.toString(),
                "price" to item.price,
                "image" to item.image
            )

            cartRef.setValue(cartItem).addOnSuccessListener {
                Toast.makeText(context, "${item.productName} added to cart!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }
        }

        popupBinding.viewButton.setOnClickListener {
            if (context is Tab) {
                context.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager).currentItem = 1
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}