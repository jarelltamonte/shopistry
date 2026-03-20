package com.example.basicviews

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.basicviews.databinding.ActivityInventoryItemBinding

class BakeryAdapter(private val itemList: List<BakeryItem>) : RecyclerView.Adapter<BakeryAdapter.ViewHolder>() {
    class ViewHolder(val binding: ActivityInventoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ActivityInventoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        holder.binding.apply {
            tvProductName.text = item.productName
            tvCategory.text = item.category
            tvPrice.text = "₱${item.price}"
            // Click listener to open ItemInfo activity
            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, ItemInfo::class.java)

                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = itemList.size
}