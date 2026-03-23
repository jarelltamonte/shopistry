package com.example.basicviews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.basicviews.databinding.ActivityItemInfoBinding

class ItemInfo : AppCompatActivity() {

    private lateinit var binding: ActivityItemInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Get Data from Intent
        val name = intent.getStringExtra("PRODUCT_NAME")
        val price = intent.getStringExtra("PRODUCT_PRICE")
        val description = intent.getStringExtra("PRODUCT_DESC")
        val stock = intent.getStringExtra("PRODUCT_STOCK")
        val imageUrl = intent.getStringExtra("PRODUCT_IMAGE")

        // 2. Map to the specific IDs in your XML
        binding.apply {
            textView4.text = name             // Product Name
            tvPrice.text = "₱$price"          // Price
            textView5.text = description      // Description
            tvStockCount.text = "Available: $stock units" // Stock count

            // Load Image into imageView2
            Glide.with(this@ItemInfo)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imageView2)

            // Back Button
            btnBack.setOnClickListener { finish() }

            // Quantity buttons logic (Optional)
            var quantity = 1
            btnIncrease.setOnClickListener {
                quantity++
                tvQuantity.text = quantity.toString()
            }
            btnDecrease.setOnClickListener {
                if (quantity > 1) {
                    quantity--
                    tvQuantity.text = quantity.toString()
                }
            }
        }
    }
}