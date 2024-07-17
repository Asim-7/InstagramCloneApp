package com.example.instagramcloneapp

import android.app.Application
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class SimpleData : Application() {

    override fun onCreate() {
        super.onCreate()

        //Firebase Offline Support
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

}