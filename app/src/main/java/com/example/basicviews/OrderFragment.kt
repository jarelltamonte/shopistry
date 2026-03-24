package com.example.basicviews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.basicviews.databinding.FragmentOrderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!
    private val cartList = mutableListOf<BakeryItem>()
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InventoryAdapter(cartList, "customer", isCartView = true)
        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter

        loadCartItems()

        binding.btnCheckout.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("ORDER_DEBUG", "Button Clicked. UID: $currentUserId | Cart Size: ${cartList.size}")

            if (currentUserId == null) {
                Toast.makeText(requireContext(), "Auth Error: Please re-login", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cartList.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            processCheckout(currentUserId)
        }
    }

    private fun loadCartItems() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance("https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        db.child("Carts").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartList.clear()
                var total = 0.0
                for (snap in snapshot.children) {
                    val item = snap.getValue(BakeryItem::class.java)
                    item?.let {
                        cartList.add(it)
                        val p = it.price?.replace(Regex("[^\\d.]"), "")?.toDoubleOrNull() ?: 0.0
                        val q = it.quantity?.toIntOrNull() ?: 0
                        total += (p * q)
                    }
                }
                adapter.notifyDataSetChanged()
                binding.tvTotalAmount.text = "₱${String.format("%.2f", total)}"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun processCheckout(uid: String) {
        val db = FirebaseDatabase.getInstance("https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        val currentCart = ArrayList(cartList)

        for (cartItem in currentCart) {
            val pid = cartItem.id
            if (pid == null) {
                Log.e("ORDER_DEBUG", "Item ID is null for ${cartItem.productName}")
                continue
            }

            val buyQty = cartItem.quantity?.toIntOrNull() ?: 0
            val invRef = db.child("Inventory").child(pid)

            invRef.child("quantity").get().addOnSuccessListener { snapshot ->
                val rawStock = snapshot.value?.toString() ?: "0"
                val currentStock = rawStock.replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0

                if (currentStock >= buyQty) {
                    val newStock = (currentStock - buyQty).toString()

                    invRef.child("quantity").setValue(newStock).addOnSuccessListener {
                        db.child("Carts").child(uid).child(pid).removeValue().addOnSuccessListener {
                            Log.d("ORDER_DEBUG", "Purchased $pid successfully.")
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Not enough stock for ${cartItem.productName}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Log.e("ORDER_DEBUG", "Failed to fetch stock for $pid: ${it.message}")
            }
        }

        Toast.makeText(requireContext(), "Order Placed! Our driver will contact you soon.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}