package com.example.pingit.controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.pingit.R
import com.example.pingit.Services.AuthService
import com.example.pingit.databinding.ActivityLoginBinding
import com.example.pingit.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginSpinner.visibility = View.INVISIBLE
    }
    fun loginBtnClicked(view: View){
        enableSpinner(true)
        val email = binding.loginEmailText.text.toString()
        val password = binding.loginPasswordText.text.toString()
        hideKeyboard()
        if (email.isNotEmpty() && password.isNotEmpty()){
            AuthService.loginUser(this, email, password){loginSuccess ->
                if (loginSuccess){
                    AuthService.findUserByEmail(this){findSuccess ->
                        if (findSuccess){
                            finish()
                        }else{
                            errorToast()
                        }

                    }
                }else{
                    errorToast()
                }

            }
        }else{
            Toast.makeText(this, "Please fill in both email and Password!!", Toast.LENGTH_SHORT).show()
        }

    }

    fun signupBtnClicked(view: View){
      val createUserIntent = Intent(this, SignupActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }
    fun errorToast(){
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean){
        if (enable){
            binding.loginSpinner.visibility = View.VISIBLE
        }else{
            binding.loginSpinner.visibility = View.INVISIBLE
        }
        binding.loginBtn.isEnabled = !enable
        binding.signupBtn.isEnabled = !enable
    }
    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}