package com.example.basicviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.basicviews.databinding.FragmentProfileBinding // Ensure you use ViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        loadUserData()

        return binding.root
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Use the exact database URL from your Register activity
            val database = FirebaseDatabase.getInstance("https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users")
                .child(userId)

            database.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Pulling the keys you defined in userMap ("name", "phone", "email")
                    val name = snapshot.child("name").value.toString()
                    val phone = snapshot.child("phone").value.toString()
                    val email = snapshot.child("email").value.toString()

                    // Setting the text to your XML IDs
                    binding.profileName.text = name
                    binding.profileContact.text = "Contact: $phone"
                    binding.profileEmail.text = "Email: $email"
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
