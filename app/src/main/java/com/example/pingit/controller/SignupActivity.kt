package com.example.pingit.controller

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.pingit.Services.AuthService
import com.example.pingit.Services.UserDataService
import com.example.pingit.databinding.ActivityMainBinding
import com.example.pingit.databinding.ActivitySignupBinding
import kotlin.random.Random

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    fun generateUserAvatar(view: View){
      val random = Random
        val color = random.nextInt(2)
        val avatar = random.nextInt(6)
        if(color == 0){
            userAvatar = "light$avatar"
        }else{
            userAvatar = "dark$avatar"
        }
        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        binding.createAvatarImageView.setImageResource(resourceId)

    }
    fun generateColorClicked(view: View){
        val random = Random
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        binding.createAvatarImageView.setBackgroundColor(Color.rgb(r, g, b))
        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"


    }
    fun createUserClicked(view: View){
        val userName = binding.createUsernameText.text.toString()
        val email = binding.createEmailText.text.toString()
        val password = binding.createPasswordText.text.toString()
        AuthService.registerUser(this, email, password){registerSuccess ->
            if(registerSuccess){
                AuthService.loginUser(this, email, password){loginSuccess ->
                    if (loginSuccess){
                       AuthService.createUser(this, userName, email, userAvatar, avatarColor){
                           createSuccess ->
                           if (createSuccess){
                               println(UserDataService.avatarName)
                               println(UserDataService.avatarColor)
                               println(UserDataService.name)
                               finish()
                           }
                       }
                    }
                }
            }
        }

    }

}