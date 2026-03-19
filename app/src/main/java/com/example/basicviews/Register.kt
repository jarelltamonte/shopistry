package com.example.basicviews

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.basicviews.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginLink.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val name = binding.nameInput.text.toString()
            val phone = binding.phoneInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPass = binding.confirmPasswordInput.text.toString()

            if (email.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty()){
                if (password == confirmPass && phone.length == 11){
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){
                            val userId = firebaseAuth.currentUser?.uid
                            val role = if (binding.adminCheck.isChecked) "admin" else "customer" // Assuming checkbox ID is rememberMe
                            val database = FirebaseDatabase.getInstance("https://shopistry-8df94-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")
                            val userMap = mapOf(
                                "name" to name,
                                "role" to role,
                                "email" to email,
                                "phone" to phone
                            )

                            userId?.let { uid ->
                                database.child(uid).setValue(userMap).addOnSuccessListener {
                                    val intent = Intent(this, Login::class.java)
                                    startActivity(intent)
                                    finish()
                                }.addOnFailureListener { error ->
                                    Toast.makeText(this, "DB Error: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else if (phone.length != 11){
                    Toast.makeText(this, "Phone number must be 11 digits!", Toast.LENGTH_SHORT).show()
                }

                else{
                    Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            }

        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}