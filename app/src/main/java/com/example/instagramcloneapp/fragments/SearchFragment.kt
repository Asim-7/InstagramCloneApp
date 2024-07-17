package com.example.instagramcloneapp.fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.adapter.UserAdapter
import com.example.instagramcloneapp.model.User

import com.example.instagramcloneapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchFragment : Fragment() {

    private var recyclerView : RecyclerView? = null
    private var userAdapter : UserAdapter? = null
    private var mUser : MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        view.findViewById<TextView>(R.id.search_edit_text).addTextChangedListener(object: TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (view.findViewById<TextView>(R.id.search_edit_text).text.toString() == ""){

                }else{
                    recyclerView?.visibility = View.VISIBLE

                    retrieveUsers()

                    searchUser(s.toString().toLowerCase())
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }

        })

        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
/*
                Log.i("data key", dataSnapshot.key.toString())
                Log.i("data value", dataSnapshot.value.toString())
                Log.i("data children", dataSnapshot.children.toString())
*/
                mUser?.clear()

                for (snapshot in dataSnapshot.children){
                    /*Log.i("data snap", snapshot.toString())
                    Log.i("data snap value", snapshot.value.toString())
                    Log.i("data snap child value", snapshot.child("fullname").getValue().toString())*/

                    val user = snapshot.getValue(User::class.java)
                    if (user != null){
                        mUser?.add(user)
                    }
                }

                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    private fun retrieveUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (view?.findViewById<TextView>(R.id.search_edit_text)?.text.toString() == ""){
                    mUser?.clear()

                    for (snapshot in dataSnapshot.children){
                        val user = snapshot.getValue(User::class.java)

                        if (user != null){
                            mUser?.add(user)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }


}
