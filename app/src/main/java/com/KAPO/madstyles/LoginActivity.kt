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

    val groups = arrayOf<String>("성별","스타일","색상","가격","정렬")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.prefer.visibility=View.INVISIBLE
        binding.btncreateaccount.visibility=View.INVISIBLE
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
            thread(start=true){
                val user=JSONObject()
                user.put("id",binding.inputId.text.toString())
                user.put("password",binding.inputPw.text.toString())
                sendAccountCreateRequest(user)
            }

        }
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
                val infotest=JSONObject(result)
                intent.putExtra("id",infotest.getString("id"))
                intent.putExtra("gender",infotest.getString("gender"))
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
            if (name == "전체") {
                    if (currentList.contains("전체")) {
                        // "전체" 버튼이 눌려있을 경우 필터를 비우고 버튼 색상을 초기화합니다.
                        currentList.clear()
                        currentButtonList.forEach { it.setBackgroundColor(Color.GRAY) }
                    } else {
                        // "전체" 버튼이 눌리지 않은 경우 필터를 "전체"로 설정하고 버튼 색상을 바꿉니다.
                        currentList.clear()
                        currentList.add("전체")
                        currentButtonList.forEach { it.setBackgroundColor(Color.WHITE) }
                    }
                } else {
                    if (currentList.contains("전체")) {
                        // "전체" 버튼이 눌려있으면 다른 버튼을 눌렀을 때 "전체"를 필터에서 제거하고, 해당 버튼을 제외한 다른 버튼들을 필터에 추가합니다.
                        currentList.remove("전체")
                        currentButtonList.find { it.text == "전체" }?.setBackgroundColor(Color.GRAY)
                        for (otherButton in currentButtonList) {
                            if (otherButton.text != name && otherButton.text != "전체") {
                                currentList.add(otherButton.text.toString())
                                otherButton.setBackgroundColor(Color.WHITE)
                            }
                        }
                        button.setBackgroundColor(Color.GRAY)
                        currentButtonList.forEach {if(it.text == "전체") it.setBackgroundColor(Color.GRAY)}
                    } else {
                        if (currentList.contains(name)) {
                            // 이미 선택된 버튼을 다시 클릭하면 필터에서 해당 항목을 제거하고 버튼 색상을 초기화합니다.
                            currentList.remove(name)
                            button.setBackgroundColor(Color.GRAY)
                        } else {
                            // 선택되지 않은 버튼을 클릭하면 필터에 해당 항목을 추가하고 버튼 색상을 바꿉니다.
                            currentList.add(name)
                            button.setBackgroundColor(Color.WHITE)
                        }
                    }
                    // 전체 버튼을 제외한 모든 버튼이 선택되었다면 전체 버튼이 눌려진 것으로 처리합니다.
                    if (currentButtonList.all { it.text == "전체" || currentList.contains(it.text)}) {
                        currentList = mutableListOf("전체")
                        currentButtonList.forEach { it.setBackgroundColor(Color.WHITE)}
                    }
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

    fun changeButton(newFilter: Map<String, List<String>>) {
        for ((group, values) in newFilter) {
            prefer[group] = values.toMutableList()
            // 이제 필터가 변경되었으므로, 버튼의 색상도 업데이트해야 합니다.
            val currentButtonList = buttonMap[group] ?: mutableListOf()
            for (button in currentButtonList) {
                if (values.contains(button.text.toString())) {
                    button.setBackgroundColor(Color.WHITE)
                } else {
                    button.setBackgroundColor(Color.GRAY)
                }
            }
        }
    }
    override fun onBackPressed() {

    }
}