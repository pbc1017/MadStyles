package com.KAPO.madstyles

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
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
    val buttonMap: MutableMap<String, MutableList<Button>> = mutableMapOf()
    val prefer: MutableMap<String, MutableList<String>> = mutableMapOf()
    val filterButton = arrayOf<Array<String>>(
        arrayOf<String>("남자","여자"),
        arrayOf<String>("전체","캐주얼","스트릿","빈티지","댄디","클래식","스포티"),
        arrayOf<String>("전체","검정", "흰색", "빨강", "파랑", "노랑", "초록"),
        arrayOf<String>("전체","낮은 가격대", "중간 가격대", "높은 가격대"),)

    val groups = arrayOf<String>("성별","스타일","색상","가격대")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.prefer.visibility=View.INVISIBLE
        binding.btncreateaccount.visibility=View.INVISIBLE
        val kindview=arrayOf<LinearLayout> (binding.kindview1,binding.kindview2,binding.kindview3,binding.kindview4)
        var i=0
        for (ll in kindview) {
            for (btnName in filterButton[i]) {
                ll.addView(createButton(btnName,groups[i]))
            }
            i += 1
        }

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
        binding.btnShowselection.setOnClickListener{
            binding.prefer.visibility= View.VISIBLE
            binding.btncreateaccount.visibility=View.VISIBLE

        }
        binding.btncreateaccount.setOnClickListener {
            val user=JSONObject()
            user.put("id",binding.inputId.text.toString())
            user.put("password",binding.inputPw.text.toString())
            user.put("gender", prefer["성별"]?.get(0))
            val pref=JSONObject()
            pref.put("style", prefer["스타일"]?.get(0))
            pref.put("color", prefer["색상"]?.get(0))
            pref.put("pRange", prefer["가격대"]?.get(0))
            user.put("prefer",pref)
            Log.d("Item",user.toString())
            thread(start=true){

                sendAccountCreateRequest(user)
            }
        }
        Log.d("Test","로그인 화면으로 들어옴")
    }

    private fun sendLoginRequest(id: String, pw: String) {
        val JSONobj=JSONObject()
        JSONobj.put("id",id)
        JSONobj.put("password",pw)
        serverCommu.sendRequest(JSONobj, "login", {result ->
            Log.d("Login OK",result.toString())
            //Log.d("Result","${result}")
            if (result != "false") {
                val infotest=JSONObject(result)
                intent.putExtra("id",infotest.getString("id"))
                intent.putExtra("gender",infotest.getString("gender"))
                setResult(RESULT_OK, intent)
                finish()
            } else {
               this.runOnUiThread{Toast.makeText(this,"ID/PW가 일치하지 않습니다.",Toast.LENGTH_SHORT).show()}
            }
        }, {result ->
            Log.d("Err:","${result}")
        })
    }

    private fun sendAccountCreateRequest(person:JSONObject) {
        serverCommu.sendRequest(person, "createaccount", {result ->
            if (result == "OK") {
                intent.putExtra("id",binding.inputId.text.toString())
                intent.putExtra("gender",prefer["성별"]?.get(0))
                setResult(RESULT_OK, intent)
                finish()
            } else {
                this.runOnUiThread{Toast.makeText(this,"이미 존재하는 아이디입니다",Toast.LENGTH_SHORT).show()}
            }
        }, {result ->
            Log.d("Err:","${result}")
        })
    }

    fun createButton(name: String, group: String): Button {
        val btn = Button(this)
        btn.text = name
        btn.layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        btn.setBackgroundColor(Color.GRAY) // 기본 버튼 색상
        btn.setOnClickListener {
            val button = it as Button
            var currentList = prefer[group] ?: mutableListOf()
            val currentButtonList = buttonMap[group] ?: mutableListOf()
            if (!currentList.contains(name)) {
                currentList.clear()
                currentList.add(name)
                currentButtonList.forEach { if(it.text == name) it.setBackgroundColor(Color.WHITE) else it.setBackgroundColor(Color.GRAY)}
            }
            if (currentList.isEmpty()) {
                prefer.remove(group)
            } else {
                prefer[group] = currentList
            }
        }
        val currentButtonList = buttonMap[group] ?: mutableListOf()
        currentButtonList.add(btn)
        buttonMap[group] = currentButtonList
        return btn
    }

    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()
        Log.d("test","로그인 다시 들어옴")
    }
}