package com.example.basicviews

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.basicviews.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var isEditingAddress = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        loadUserData()
        setupAddressEdit()

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return binding.root
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        val database = FirebaseDatabase.getInstance("https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("Users")
            .child(userId)

        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value.toString()
                val phone = snapshot.child("phone").value.toString()
                val email = snapshot.child("email").value.toString()
                val address = snapshot.child("address").value?.toString()

                binding.profileName.text = name
                binding.profileContact.text = phone
                binding.profileEmail.text = email

                // Show address if it exists, otherwise show placeholder
                if (!address.isNullOrEmpty()) {
                    binding.profileAddress.text = address
                } else {
                    binding.profileAddress.text = "Tap edit to add address"
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAddressEdit() {
        binding.editAddressBtn.setOnClickListener {
            if (!isEditingAddress) {
                binding.profileAddressEdit.setText(
                    if (binding.profileAddress.text == "Tap edit to add address") ""
                    else binding.profileAddress.text
                )
                binding.profileAddress.visibility = View.GONE
                binding.profileAddressEdit.visibility = View.VISIBLE
                binding.editAddressBtn.setImageResource(android.R.drawable.ic_menu_save)
                binding.profileAddressEdit.requestFocus()
                isEditingAddress = true
            } else {
                saveAddress()
            }
        }

        // Save on keyboard "Done"
        binding.profileAddressEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveAddress()
                true
            } else false
        }
    }

    private fun saveAddress() {
        val newAddress = binding.profileAddressEdit.text.toString().trim()

        if (newAddress.isEmpty()) {
            Toast.makeText(context, "Address cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance("https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("Users")
            .child(userId)
            .child("address")
            .setValue(newAddress)
            .addOnSuccessListener {
                binding.profileAddress.text = newAddress
                binding.profileAddressEdit.visibility = View.GONE
                binding.profileAddress.visibility = View.VISIBLE
                binding.editAddressBtn.setImageResource(android.R.drawable.ic_menu_edit)
                isEditingAddress = false

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.profileAddressEdit.windowToken, 0)

                Toast.makeText(context, "Address saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save address", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}