package com.KAPO.madstyles

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.KAPO.madstyles.databinding.ActivityLoginBinding
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private fun requestPermission() {
        val permission = Manifest.permission.INTERNET
        ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            val id = binding.inputId.text.toString()
            val pw = binding.inputPw.text.toString()

            // Clear the inputs
            binding.inputId.text.clear()
            binding.inputPw.text.clear()

            val permission = Manifest.permission.INTERNET
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result == PackageManager.PERMISSION_GRANTED) {
                sendLoginRequest(id, pw)
            } else {
                requestPermission()
            }
            // Send request to the server

        }
    }

    private fun sendLoginRequest(id: String, pw: String) {
        // Assuming your server accepts POST request for login with params "username" and "password"
        val url = "http://172.10.5.149/login"

        val params = HashMap<String,String>()
        params["id"] = id
        params["password"] = pw
        val jsonObject = JSONObject(params as Map<*, *>)

        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                // TODO: handle your server response here
                // For instance, you could parse it as JSON and check some field to verify the login
                val responseObject = JSONObject(response)

//                val loginSuccessful = responseObject.getBoolean("success") // Assuming response has "success" field
                val loginSuccessful = responseObject.toString()
                Log.d("chan","${loginSuccessful}")
//                if (loginSuccessful) {
//                    // Login successful, navigate to next activity
//                    Log.d("chan","Login successful")
//                } else {
//                    // Login failed, show error message
//                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
//                }
            },
            Response.ErrorListener { error ->
                // TODO: Handle the error appropriately
                Log.e("chan", "Login request error: $error")
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return jsonObject.toString().toByteArray()
            }
        }

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }
}