package com.example.pingit.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.pingit.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
    fun loginBtnClicked(view: View){

    }

    fun signupBtnClicked(view: View){
      val createUserIntent = Intent(this, SignupActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }
}