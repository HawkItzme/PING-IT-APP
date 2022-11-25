package com.example.pingit.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.pingit.R
import com.example.pingit.Services.AuthService
import com.example.pingit.Services.UserDataService
import com.example.pingit.Utilities.BROADCAST_USER_DATA_CHANGE
import com.example.pingit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.appBarMain.toolbar, R.string.open_navigation, R.string.close_navigation)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
        IntentFilter(BROADCAST_USER_DATA_CHANGE)
        )
    }
    private val userDataChangeReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (AuthService.isLoggedIn){
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userNameNavHeader).text = UserDataService.name
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userMailNavHeader).text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable",
                packageName)
                binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setImageResource(resourceId)
                binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.loginButtonNavHeader).text = "Logout"


            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }
    fun loginButtonClicked(view: View){
        if (AuthService.isLoggedIn){
            //logOut
            UserDataService.logout()
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userNameNavHeader).text = ""
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userMailNavHeader).text = ""
            binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setImageResource(R.drawable.userpic)
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userImageNavHeader).setBackgroundColor(Color.TRANSPARENT)
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.loginButtonNavHeader).text = "Login!!"
        }else{
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }
    fun addChannelClicked(view: View){

    }
    fun sentMessageBtnClicked(view: View){

    }
}