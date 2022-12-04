package com.example.pingit.controller

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pingit.Adapters.MessageAdapter
import com.example.pingit.Model.Channel
import com.example.pingit.Model.Message
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
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.appBarMain.toolbar, R.string.open_navigation, R.string.close_navigation)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(
            BROADCAST_USER_DATA_CHANGE))
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
        IntentFilter(BROADCAST_USER_DATA_CHANGE)
        )
        if (App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this){}
        }

        binding.navView.findViewById<ListView>(R.id.channel_List).setOnItemClickListener {_, _, i, _ ->
                selectedChannel = MessageService.channels[i]
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                updateWithChannel()

        }
    }
    private fun setupAdapters(){
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        binding.channelList.adapter = channelAdapter

        messageAdapter  = MessageAdapter(this, MessageService.message)
        binding.appBarMain.contentMain.messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        binding.appBarMain.contentMain.messageListView.layoutManager = layoutManager
    }

    override fun onDestroy() {
        socket.disconnect()
        super.onDestroy()
    }

    private val userDataChangeReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn){
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userNameNavHeader).text = UserDataService.name
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userMailNavHeader).text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable",
                packageName)
                binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setImageResource(resourceId)
                binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.loginButtonNavHeader).text = "Logout"

                MessageService.getChannel {complete ->
                    if (complete){
                        if (MessageService.channels.count() > 0){
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }

                    }

                }

            }
        }
    }

    fun updateWithChannel() {
        binding.appBarMain.contentMain.mainChannelName.text = "#${selectedChannel?.name}"
        if (selectedChannel != null) {
            MessageService.getMessages(selectedChannel!!.id) { complete ->
                if (selectedChannel != null) {
                    messageAdapter.notifyDataSetChanged()
                    if (messageAdapter.itemCount > 0){
                        binding.appBarMain.contentMain.messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)

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
        if (App.prefs.isLoggedIn){
            //logOut
            UserDataService.logout()
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userNameNavHeader).text = ""
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userMailNavHeader).text = ""
            binding.navView.get(R.layout.nav_header_main).findViewById<ImageView>(R.id.userImageNavHeader).setImageResource(R.drawable.userpic)
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.userImageNavHeader).setBackgroundColor(Color.TRANSPARENT)
            binding.navView.get(R.layout.nav_header_main).findViewById<TextView>(R.id.loginButtonNavHeader).text = "Login!!"
            binding.appBarMain.contentMain.mainChannelName.text = "Please Log In"
        }else{
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }
    fun addChannelClicked(view: View){
        if (App.prefs.isLoggedIn){

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("ADD", DialogInterface.OnClickListener{_, _ ->

                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                    val desTextField = dialogView.findViewById<EditText>(R.id.addChannnelDesTxt)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = desTextField.text.toString()
                    socket.emit("newChannel", channelName, channelDesc)
                })
                .setNegativeButton("CANCEL"){_, _->

                }
                .show()
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        if(App.prefs.isLoggedIn){
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[0] as String
                val channelId = args[0] as String

                val newChannel = Channel(channelName, channelDescription, channelId)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn){
            runOnUiThread {
                val channelId = args[2] as String
                if(channelId == selectedChannel?.id) {
                    val msgBody = args[0] as String
                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(
                        msgBody,
                        userName,
                        channelId,
                        userAvatar,
                        userAvatarColor,
                        id,
                        timeStamp
                    )
                    MessageService.message.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    binding.appBarMain.contentMain.messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)

                }
            }
        }
    }
    fun sentMessageBtnClicked(view: View){

        if(App.prefs.isLoggedIn && binding.appBarMain.contentMain.messaeTextView.text.isNotEmpty() && selectedChannel != null){
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", UserDataService.avatarName, UserDataService.avatarColor )
            binding.appBarMain.contentMain.messaeTextView.text.clear()
            hideKeyboard()
        }
    }
    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}