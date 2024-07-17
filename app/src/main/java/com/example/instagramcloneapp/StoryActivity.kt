package com.example.instagramcloneapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.instagramcloneapp.Model.Story
import com.example.instagramcloneapp.Model.User
import com.example.instagramcloneapp.databinding.ActivityStoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    var currentUserId : String = ""
    var userId : String = ""
    var counter = 0
    var pressTime = 0L
    var limit = 500L

    var imageList : List<String>? = null
    var storyIdsList : List<String>? = null
    var storiesProgressView : StoriesProgressView? = null
    val onTouchListener = View.OnTouchListener { v, event ->
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView!!.resume()
                return@OnTouchListener limit < now - pressTime
            }

        }
        false
    }

    private lateinit var storyBinding: ActivityStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyBinding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(storyBinding.root)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId")!!

        storiesProgressView = findViewById(R.id.stories_progress)

        storyBinding.layoutSeen.visibility = View.GONE
        storyBinding.storyDelete.visibility = View.GONE

        if (userId == currentUserId){
            storyBinding.layoutSeen.visibility = View.VISIBLE
            storyBinding.storyDelete.visibility = View.VISIBLE
        }
        getStories(userId)
        userInfo(userId)

        val reverse : View = findViewById(R.id.reverse)
        reverse.setOnClickListener {
            storiesProgressView!!.reverse()
        }
        reverse.setOnTouchListener(onTouchListener)

        val skip : View = findViewById(R.id.skip)
        skip.setOnClickListener {
            storiesProgressView!!.skip()
        }
        skip.setOnTouchListener(onTouchListener)

        storyBinding.seenNumber.setOnClickListener {
            val intent = Intent(this@StoryActivity, ShowUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyid", storyIdsList!![counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }

        storyBinding.storyDelete.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference
                .child("Story")
                .child(userId)
                .child(storyIdsList!![counter])
            ref.removeValue().addOnCompleteListener {
                task -> if (task.isSuccessful){
                Toast.makeText(this@StoryActivity, "Story Deleted!", Toast.LENGTH_SHORT).show()
            }
            }
        }
    }

    private fun getStories(userId: String){
        imageList = ArrayList()
        storyIdsList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                (imageList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()

                for (snapshot in p0.children){
                    val story : Story? = snapshot.getValue<Story>(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()){
                        (imageList as ArrayList<String>).add(story.getImageUrl())
                        (storyIdsList as ArrayList<String>).add(story.getStoryId())
                    }
                }
                storiesProgressView!!.setStoriesCount((imageList as ArrayList<String>).size)
                storiesProgressView!!.setStoryDuration(6000L)   //For 6 seconds
                storiesProgressView!!.setStoriesListener(this@StoryActivity)
                storiesProgressView!!.startStories(counter)
                try {
                    Picasso.get().load(imageList!!.get(counter)).placeholder(R.drawable.profile).into(storyBinding.imageStory)
                }
                catch (e: Exception) {
                    // handler
                }
                addViewToStory(storyIdsList!!.get(counter))
                seenNumber(storyIdsList!!.get(counter))
            }

        })
    }

    private fun userInfo(userId: String){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                /*if (context != null){
                    return
                }*/
                if (p0.exists()){
                    val user = p0.getValue<User>(User::class.java)

                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(storyBinding.storyProfileImage)
                    }
                    catch (e: Exception) {
                        // handler
                    }
                    storyBinding.storyUsername.text = user!!.getUsername()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }

    private fun addViewToStory(storyId : String){
        FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)
            .child(storyId)
            .child("views")
            .child(currentUserId)
            .setValue(true)
    }

    private fun seenNumber(storyId : String){
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)
            .child(storyId)
            .child("views")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                storyBinding.seenNumber.text = "" + p0.childrenCount
            }

        })
    }

    override fun onComplete() {
        finish()
    }

    override fun onPrev() {
        try {
            if (counter-1 < 0) return   //app will not crash if there are not back stories
            Picasso.get().load(imageList!![--counter]).placeholder(R.drawable.profile).into(storyBinding.imageStory)
        }
        catch (e: Exception) {
            // handler
        }
        seenNumber(storyIdsList!![counter])
    }

    override fun onNext() {
        try {
            Picasso.get().load(imageList!![++counter]).placeholder(R.drawable.profile).into(storyBinding.imageStory)
        }
        catch (e: Exception) {
            // handler
        }
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])
    }

    override fun onDestroy() {
        super.onDestroy()

        storiesProgressView!!.destroy()
    }

    override fun onResume() {
        super.onResume()

        storiesProgressView!!.resume()
    }

    override fun onPause() {
        super.onPause()

        storiesProgressView!!.pause()
    }
}
