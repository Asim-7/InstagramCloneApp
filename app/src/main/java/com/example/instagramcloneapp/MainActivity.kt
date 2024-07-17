package com.example.instagramcloneapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.instagramcloneapp.Fragments.HomeFragment
import com.example.instagramcloneapp.Fragments.NotificationsFragment
import com.example.instagramcloneapp.Fragments.ProfileFragment
import com.example.instagramcloneapp.Fragments.SearchFragment
import com.example.instagramcloneapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    var status: String = ""

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                moveToFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_search -> {
                moveToFragment(SearchFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_add_post -> {
                item.isChecked = false
                startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_notifications -> {
                moveToFragment(NotificationsFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_profile -> {
                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        wifiCheck()

        val fAuth: FirebaseAuth = FirebaseAuth.getInstance()

        if (fAuth.currentUser == null){
            val intent = Intent(this@MainActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        startFromHomeFragment(HomeFragment())

    }

    private fun moveToFragment(fragment: Fragment){
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace( R.id.fragment_container, fragment )
        fragmentTrans.addToBackStack("tag").commit()
    }

    private fun startFromHomeFragment(fragment: Fragment){
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace( R.id.fragment_container, fragment ).commit()
        //fragmentTrans.addToBackStack("tag").commit()
    }

    override fun onBackPressed() {

        var count : Int = supportFragmentManager.backStackEntryCount

        if (count == 0){
            //Toast.makeText(this, "Want to exit?", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        }else{
            supportFragmentManager.popBackStack()
        }

    }

    private fun wifiCheck() {
        //Wifi Check
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi enabled";
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobile data enabled";
            }
        } else {
            status = "No internet is available";
            networkDialoge()
        }
        //Toast.makeText(this, "Network Status: $status", Toast.LENGTH_SHORT).show()
        Log.d("NetStatus: ", status)
    }

    private fun networkDialoge() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.network_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
        val alertDialog = mBuilder.show()

        dialogView.findViewById<Button>(R.id.btn_restartApp).setOnClickListener {
            //finish()
            //Restart App
            val mStartActivity = Intent(this, MainActivity::class.java)
            val mPendingIntentId : Int = 123456
            val mPendingIntent : PendingIntent = PendingIntent.getActivity(this, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val mgr : AlarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis()+100, mPendingIntent)
            System.exit(0)
        }
        dialogView.findViewById<ImageView>(R.id.close_networkDialog).setOnClickListener {
            alertDialog.dismiss()
        }
    }
}
