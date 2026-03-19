package com.example.basicviews

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

class Tab : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tab)

        viewPager = findViewById(R.id.viewPager)
        val adapter = ViewPageAdapter(this)
        viewPager.adapter = adapter
        viewPager.currentItem = 0
        viewPager.offscreenPageLimit = 3


        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            viewPager.currentItem = 0
        }

        findViewById<LinearLayout>(R.id.navCart).setOnClickListener {
            viewPager.currentItem = 1
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            viewPager.currentItem = 2
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}