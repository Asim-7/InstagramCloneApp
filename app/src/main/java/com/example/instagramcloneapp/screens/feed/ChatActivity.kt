package com.example.instagramcloneapp.screens.feed

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.util.OnSwipeTouchListener

class ChatActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val recyclerView : RecyclerView? = findViewById(R.id.recyclerView_chat)

        recyclerView!!.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Left")

            }

            override fun onSwipeRight() {
                Log.e("ViewSwipe", "Right")

                onBackPressed()
                //finish()
            }
        })
    }


}
