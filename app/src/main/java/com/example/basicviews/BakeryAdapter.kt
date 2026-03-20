package com.example.basicviews

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.basicviews.databinding.ActivityInventoryItemBinding

class BakeryAdapter(private var itemList: MutableList<BakeryItem>) : RecyclerView.Adapter<BakeryAdapter.ViewHolder>() {
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
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun updateList(newList: List<BakeryItem>) {
        itemList = newList.toMutableList()
        notifyDataSetChanged()
    }
}