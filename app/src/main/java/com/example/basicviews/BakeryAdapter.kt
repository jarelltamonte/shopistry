package com.example.basicviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BakeryAdapter(
    private var items: List<BakeryItem>,
    private val isAdmin: Boolean,
    private val onDelete: (BakeryItem) -> Unit
) : RecyclerView.Adapter<BakeryAdapter.BakeryViewHolder>() {

    class BakeryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvItemName)
        val price = view.findViewById<TextView>(R.id.tvItemPrice)
        val stock = view.findViewById<TextView>(R.id.tvItemStock)
        val deleteBtn = view.findViewById<Button>(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BakeryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bakery_card, parent, false)
        return BakeryViewHolder(view)
    }

    override fun onBindViewHolder(holder: BakeryViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.itemName
        holder.price.text = "PHP ${String.format("%.2f", item.price)}"
        holder.stock.text = "In Stock: ${item.stockQuantity}"

        if (isAdmin) {
            holder.deleteBtn.visibility = View.VISIBLE
            holder.deleteBtn.setOnClickListener { onDelete(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<BakeryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}