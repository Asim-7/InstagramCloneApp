package com.example.instagramcloneapp.fragments


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.adapter.PostAdapter
import com.example.instagramcloneapp.adapter.StoryAdapter
import com.example.instagramcloneapp.screens.feed.ChatActivity
import com.example.instagramcloneapp.model.Post
import com.example.instagramcloneapp.model.Story
import com.example.instagramcloneapp.util.OnSwipeTouchListener

import com.example.instagramcloneapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var postAdapter : PostAdapter? = null
    private var storyAdapter : StoryAdapter? = null
    private var storyList : MutableList<Story>?= null
    private var postList : MutableList<Post>? = null
    private var followingList : MutableList<String>? = null
    private var progessDialog: ProgressDialog? = null
    private var status = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        progessDialog= ProgressDialog(context)
        progessDialog!!.setTitle("Loading")
        progessDialog!!.setMessage("Working on it, please wait!")
        progessDialog!!.setCanceledOnTouchOutside(false)
        progessDialog!!.show()

        wifiCheck()

        Log.i("Where", "In Home Fragment")

        var recyclerView : RecyclerView? = null
        var recyclerViewStory : RecyclerView? = null
        recyclerView = view.findViewById(R.id.recycler_view_home)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory.layoutManager = linearLayoutManager2

        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        recyclerViewStory.adapter = storyAdapter

        var chatLogo : ImageView? = null
        chatLogo = view.findViewById(R.id.chat_pic)

        chatLogo.setOnClickListener {
            val intentChat = Intent(context, ChatActivity::class.java)
            context!!.startActivity(intentChat)
        }

        recyclerView.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                Log.e("ViewSwipe", "Left")

                val intentChat = Intent(context, ChatActivity::class.java)
                context!!.startActivity(intentChat)
            }

            override fun onSwipeRight() {
                Log.e("ViewSwipe", "Right")
            }
        })

        checkFollowings()

        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")

        //sync for offline support
        followingRef.keepSynced(true)

        followingRef.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {
                progessDialog!!.dismiss()
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){

                    (followingList as ArrayList<String>).clear()

                    for (snapshot in p0.children){
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }

                    retrievePosts()

                    retrieveStories()

                }
            }

        })
    }

    private fun retrievePosts() {

        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        //sync for offline support
        postsRef.keepSynced(true)

        postsRef.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {
                progessDialog!!.dismiss()
            }

            override fun onDataChange(p0: DataSnapshot) {

                postList?.clear()

                for (snapshot in p0.children){

                    val post = snapshot.getValue(Post::class.java)

                    for (id in (followingList as ArrayList<String>)){

                        if (post!!.getPublisher() == id){

                            postList!!.add(post)
                        }
                        progessDialog!!.dismiss()
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

        })

    }

    private fun wifiCheck() {
        //Wifi Check
        val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi enabled";
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobile data enabled";
            }
        } else {
            status = "No internet available";

            progessDialog!!.dismiss()
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }
        //Toast.makeText(this, "Network Status: $status", Toast.LENGTH_SHORT).show()
        Log.d("NetStatus: ", status)
    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")

        //sync for offline support
        storyRef.keepSynced(true)

        storyRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).
                    add(Story("", 0, 0, "",
                        FirebaseAuth.getInstance().currentUser!!.uid))
                //show stories of only those whome user follows
                for (id in followingList!!){
                    var countStory = 0
                    var story:Story? = null

                    for (snapshot in dataSnapshot.child(id).children){
                        story = snapshot.getValue(Story::class.java)
                        //Retrieve stories that are within 24 hours
                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()){
                            countStory++
                        }
                    }
                    if (countStory > 0){
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

        })
    }

}
