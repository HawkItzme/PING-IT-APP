package com.example.pingit.controller

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
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
import com.example.pingit.Model.Channel
import com.example.pingit.R
import com.example.pingit.Services.AuthService
import com.example.pingit.Services.MessageService
import com.example.pingit.Services.UserDataService
import com.example.pingit.Utilities.BROADCAST_USER_DATA_CHANGE
import com.example.pingit.Utilities.SOCKET_URL
import com.example.pingit.databinding.ActivityMainBinding
import io.socket.client.IO
import io.socket.emitter.Emitter

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    private lateinit var binding: ActivityMainBinding
    lateinit var channelAdapter: ArrayAdapter<Channel>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        socket.connect()
        socket.on("channelCreated", onNewChannel)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.appBarMain.toolbar, R.string.open_navigation, R.string.close_navigation)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
        IntentFilter(BROADCAST_USER_DATA_CHANGE)
        )
    }
    private fun setupAdapters(){
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        binding.channelList.adapter = channelAdapter
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(
            BROADCAST_USER_DATA_CHANGE))

        super.onResume()
    }

    override fun onDestroy() {
        socket.disconnect()
        super.onDestroy()
    }

    private val userDataChangeReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            if (AuthService.isLoggedIn){
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userNameNavHeader).text = UserDataService.name
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userMailNavHeader).text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable",
                packageName)
                binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setImageResource(resourceId)
                binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.loginButtonNavHeader).text = "Logout"

                MessageService.getChannel(context){complete ->
                    if (complete){
                        channelAdapter.notifyDataSetChanged()
                    }

                }

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
        if (AuthService.isLoggedIn){

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("ADD", DialogInterface.OnClickListener{dialog, i ->

                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                    val desTextField = dialogView.findViewById<EditText>(R.id.addChannnelDesTxt)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = desTextField.text.toString()
                    socket.emit("newChannel", channelName, channelDesc)
                })
                .setNegativeButton("CANCEL"){dialog, i ->

                }
                .show()
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        runOnUiThread {
            val channelName = args[0] as String
            val channelDescription = args[0] as String
            val channelId = args[0] as String

            val newChannel = Channel(channelName, channelDescription, channelId)
            MessageService.channels.add(newChannel)
            channelAdapter.notifyDataSetChanged()
        }
    }
    fun sentMessageBtnClicked(view: View){

        hideKeyboard()
    }
    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}