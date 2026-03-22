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
import android.widget.Toast
import com.example.basicviews.databinding.AdminPopupBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.database.FirebaseDatabase
class InventoryAdapter(
    private val itemList: List<BakeryItem>,
    private val userRole: String = "customer"
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.itemImage)
        val name: TextView = itemView.findViewById(R.id.itemName)
        val price: TextView = itemView.findViewById(R.id.itemPrice)
        val quantity: TextView = itemView.findViewById(R.id.itemQuantity)
        val category: TextView = itemView.findViewById(R.id.itemCategory)
        val editButton: Button = itemView.findViewById(R.id.editButton)
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

        holder.editButton.setOnClickListener {
            showEditPopup(holder.itemView.context, item)
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

            // ✅ Update the existing item using its ID
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
}