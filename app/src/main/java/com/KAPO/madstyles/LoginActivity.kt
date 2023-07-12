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
import com.android.volley.ClientError
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var initTime = 0L
    var kakaoid=""
    private var Loginagain=false
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
        Loginagain=intent.getBooleanExtra("Loginagain",false)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.prefer.visibility=View.INVISIBLE
        binding.kakaonickname.visibility=View.INVISIBLE
        binding.btncreateaccount.visibility=View.INVISIBLE
        val kindview=arrayOf<LinearLayout> (binding.kindview1,binding.kindview2,binding.kindview3,binding.kindview4)
        var i=0
        for (ll in kindview) {
            for (btnName in filterButton[i]) {
                ll.addView(createButton(btnName,groups[i]))
            }
            i += 1
        }
        KakaoSdk.init(this,"842a58ee39301eebd3d10d93d94bec68")
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
            binding.kakaonickname.visibility=View.GONE
        }
        binding.btncreateaccount.setOnClickListener {
            if(kakaoid=="") {//normal login
                val user = JSONObject()
                user.put("id", binding.inputId.text.toString())
                user.put("password", binding.inputPw.text.toString())
                user.put("gender", prefer["성별"]?.get(0))
                val pref = JSONObject()
                pref.put("style", prefer["스타일"]?.get(0))
                pref.put("color", prefer["색상"]?.get(0))
                pref.put("pRange", prefer["가격대"]?.get(0))
                user.put("prefer", pref)
                val cart = JSONArray()
                user.put("cart", cart)
                Log.d("Item", user.toString())
                thread(start = true) {

                    sendAccountCreateRequest(user)
                }
            }
            else
            {//kakao login
                val user = JSONObject()
                user.put("id", binding.inputNickname.text.toString())
                user.put("kakaoid",kakaoid)
                user.put("gender", prefer["성별"]?.get(0))
                val pref = JSONObject()
                pref.put("style", prefer["스타일"]?.get(0))
                pref.put("color", prefer["색상"]?.get(0))
                pref.put("pRange", prefer["가격대"]?.get(0))
                user.put("prefer", pref)
                val cart = JSONArray()
                user.put("cart", cart)
                Log.d("Item", user.toString())
                thread(start = true) {

                    sendAccountCreateRequest(user)
                }
            }
        }
//        Log.d("Test","로그인 화면으로 들어옴")

        val logincallback:(OAuthToken?,Throwable?)->Unit={token,err->
            if(err!=null){
                Log.d("KAKAO","LOGIN FAILED")
            }
            else if(token!=null){
                UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                    kakaoid = tokenInfo?.id.toString()
                    val JSONobj = JSONObject()
                    JSONobj.put("kakaoid", kakaoid)
                    thread(start=true) {
                        serverCommu.sendRequest(JSONobj, "kakaologin", { result ->
                            Log.d("Login OK", result.toString())
                            //Log.d("Result","${result}")
                            if (result != "false") {
                                val infotest = JSONObject(result)
                                if (!Loginagain) {
                                    intent.putExtra("id", infotest.getString("id"))
                                    intent.putExtra(
                                        "gender", infotest.getString("gender")
                                    )
                                    setResult(RESULT_OK, intent)
                                    finish()
                                } else {
                                    val loginIntent = Intent(this, MainActivity::class.java)
                                    loginIntent.putExtra("id", infotest.getString("id"))
                                    loginIntent.putExtra("gender", infotest.getString("gender"))
                                    loginIntent.putExtra("Loginagain", true)
                                    startActivity(loginIntent)
                                    finish()
                                }
                            } else {
                                this.runOnUiThread {
                                    Toast.makeText(this, "회원 가입을 진행합니다", Toast.LENGTH_SHORT).show()
                                    binding.prefer.visibility = View.VISIBLE
                                    binding.btncreateaccount.visibility = View.VISIBLE
                                    binding.kakaonickname.visibility = View.VISIBLE
                                }
                            }
                        }, { result ->
                            Log.d("Err:", "${result}")
                        })
                    }
                }
            }
        }

        binding.btnKakaologin.setOnClickListener {
            if(AuthApiClient.instance.hasToken()) //이미 로그인한 사용자
            {
                UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                    kakaoid=tokenInfo?.id.toString()
                    thread(start=true){
                        sendLoginRequest(kakaoid,kakaoid)
                    }
                }

            }
            else {
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                    UserApiClient.instance.loginWithKakaoTalk(this) { token, err ->
                        if (err != null) {
                            Log.e("KAKAO", "LOGIN FAILED", err)
                            UserApiClient.instance.loginWithKakaoAccount(
                                this,
                                callback = logincallback
                            )
                        } else if (token != null) {
                            //회원가입
                            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                                kakaoid=tokenInfo?.id.toString()
                                thread(start=true){
                                    val JSONobj=JSONObject()
                                    JSONobj.put("kakaoid",kakaoid)
                                    serverCommu.sendRequest(JSONobj, "kakaologin", {result ->
                                        Log.d("Login OK",result.toString())
                                        //Log.d("Result","${result}")
                                        if (result != "false") {
                                            val infotest=JSONObject(result)
                                            if(!Loginagain) {
                                                intent.putExtra("id", infotest.getString("id"))
                                                intent.putExtra("gender", infotest.getString("gender")
                                                )
                                                setResult(RESULT_OK, intent)
                                                finish()
                                            }
                                            else{
                                                val loginIntent=Intent(this,MainActivity::class.java)
                                                loginIntent.putExtra("id", infotest.getString("id"))
                                                loginIntent.putExtra("gender", infotest.getString("gender"))
                                                loginIntent.putExtra("Loginagain",true)
                                                startActivity(loginIntent)
                                                finish()
                                            }
                                        } else {
                                            this.runOnUiThread{
                                                Toast.makeText(this,"회원 가입을 진행합니다",Toast.LENGTH_SHORT).show()
                                                binding.prefer.visibility= View.VISIBLE
                                                binding.btncreateaccount.visibility=View.VISIBLE
                                                binding.kakaonickname.visibility=View.VISIBLE
                                            }
                                        }
                                    }, {result ->
                                        Log.d("Err:","${result}")
                                    })
                                }
                            }

                        }
                    }
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = logincallback)
                }
            }
        }
    }

    private fun sendLoginRequest(id: String, pw: String) {
        val JSONobj=JSONObject()
        if(kakaoid=="") {
            JSONobj.put("id",id)
            JSONobj.put("password",pw)
            serverCommu.sendRequest(JSONobj, "login", { result ->
                Log.d("Login OK", result.toString())
                //Log.d("Result","${result}")
                if (result != "false") {
                    val infotest = JSONObject(result)
                    if (!Loginagain) {
                        intent.putExtra("id", infotest.getString("id"))
                        intent.putExtra(
                            "gender", infotest.getString("gender")
                        )
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        val loginIntent = Intent(this, MainActivity::class.java)
                        loginIntent.putExtra("id", infotest.getString("id"))
                        loginIntent.putExtra("gender", infotest.getString("gender"))
                        loginIntent.putExtra("Loginagain", true)
                        startActivity(loginIntent)
                        finish()
                    }
                } else {
                    this.runOnUiThread {
                        Toast.makeText(
                            this,
                            "ID/PW가 일치하지 않습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }, { result ->
                Log.d("Err:", "${result}")
            })
        }
        else
        {
            JSONobj.put("kakaoid",kakaoid)
            serverCommu.sendRequest(JSONobj, "kakaologin", { result ->
                Log.d("Login OK", result.toString())
                //Log.d("Result","${result}")
                if (result != "false") {
                    val infotest = JSONObject(result)
                    if (!Loginagain) {
                        intent.putExtra("id", infotest.getString("id"))
                        intent.putExtra(
                            "gender", infotest.getString("gender")
                        )
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        val loginIntent = Intent(this, MainActivity::class.java)
                        loginIntent.putExtra("id", infotest.getString("id"))
                        loginIntent.putExtra("gender", infotest.getString("gender"))
                        loginIntent.putExtra("Loginagain", true)
                        startActivity(loginIntent)
                        finish()
                    }
                } else {
                    this.runOnUiThread {
                        Toast.makeText(
                            this,
                            "ID/PW가 일치하지 않습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }, { result ->
                Log.d("Err:", "${result}")
            })
        }

    }

    private fun sendAccountCreateRequest(person:JSONObject) {
        if(kakaoid=="") {
            serverCommu.sendRequest(person, "createaccount", { result ->
                if (result == "OK") {

                    if (!Loginagain) {
                        intent.putExtra("id", binding.inputId.text.toString())
                        intent.putExtra("gender", prefer["성별"]?.get(0))
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        val loginIntent = Intent(this, MainActivity::class.java)
                        loginIntent.putExtra("id", binding.inputId.text.toString())
                        loginIntent.putExtra("gender", prefer["성별"]?.get(0))
                        loginIntent.putExtra("Loginagain", true)
                        startActivity(loginIntent)
                        finish()
                    }
                } else {
                    this.runOnUiThread {
                        Toast.makeText(this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }, { result ->
                Log.d("Err:", "${result}")
            })
        }
        else{
            serverCommu.sendRequest(person, "createkakaoaccount", { result ->
                if (result == "OK") {

                    if (!Loginagain) {
                        intent.putExtra("id", binding.inputNickname.text.toString())
                        intent.putExtra("gender", prefer["성별"]?.get(0))
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        val loginIntent = Intent(this, MainActivity::class.java)
                        loginIntent.putExtra("id", binding.inputNickname.text.toString())
                        loginIntent.putExtra("gender", prefer["성별"]?.get(0))
                        loginIntent.putExtra("Loginagain", true)
                        startActivity(loginIntent)
                        finish()
                    }
                } else {
                    this.runOnUiThread {
                        Toast.makeText(this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }, { result ->
                Log.d("Err:", "${result}")
            })
        }
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
        if (System.currentTimeMillis() - initTime > 3000) {
            Toast.makeText(
                this, "종료하려면 한번 더 누르세요!",
                Toast.LENGTH_SHORT
            ).show()
            initTime = System.currentTimeMillis()
        } else {
            ActivityCompat.finishAffinity(this)
            System.exit(0)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("test","로그인 다시 들어옴")
    }
}