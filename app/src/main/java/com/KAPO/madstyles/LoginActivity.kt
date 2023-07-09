package com.KAPO.madstyles

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

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
            thread(start=true){
                sendLoginRequest(id, pw)
            }

        }
    }

    private fun sendLoginRequest(id: String, pw: String) {
        val JSONobj=JSONObject()
        JSONobj.put("id",id)
        JSONobj.put("password",pw)
        serverCommu.sendRequest(JSONobj, "login", {result ->
            Log.d("Result",result.toString())
            //Log.d("Result","${result}")
            if (result != "false") {
                val infotest=JSONObject(result)
                intent.putExtra("id",id)
                intent.putExtra("gender",infotest.getString("gender"))
                setResult(RESULT_OK, intent)
                finish()
            } else {
               this.runOnUiThread{Toast.makeText(this,"ID/PW가 일치하지 않습니다.",Toast.LENGTH_SHORT).show()}
            }
        }, {result ->
            Log.d("Result","${result}")
        })
    }

    override fun onBackPressed() {

    }
}