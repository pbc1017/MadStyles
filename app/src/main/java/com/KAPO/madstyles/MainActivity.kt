package com.KAPO.madstyles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.KAPO.madstyles.FiveFragment
import com.KAPO.madstyles.FourFragment
import com.KAPO.madstyles.OneFragment
import com.KAPO.madstyles.R
import com.KAPO.madstyles.ThreeFragment
import com.KAPO.madstyles.TwoFragment
import com.KAPO.madstyles.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var initTime = 0L
    private var id: String? = ""


    // 뷰 페이저 어댑터
    class MyFragmentPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        val fragments = listOf(OneFragment(), TwoFragment(), ThreeFragment(), FourFragment(), FiveFragment())
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    fun tabSetting(tab: TabLayout.Tab, position: Int) {
        if (position == 0) {
            tab.text = "tab1"
//            tab.setIcon(R.drawable.contacts_icon)
        } else if (position == 1) {
            tab.text = "tab2"
//            tab.setIcon(R.drawable.gallery_icon)
        } else if (position == 2) {
            tab.text = "tab3"
//            tab.setIcon(R.drawable.gallery_icon)
        } else if (position == 3) {
            tab.text = "tab4"
//            tab.setIcon(R.drawable.gallery_icon)
        } else {
            tab.text = "tab5"
//            tab.setIcon(R.drawable.gpt_icon)
        }

    }
    private fun sendIdRequest(id: String) {
        // Assuming your server accepts POST request for login with params "username" and "password"
        val url = "http://143.248.193.204:4444/requestmain"
        val okHttpClient= OkHttpClient()
        val JSONobj= JSONObject()
        JSONobj.put("id",id)
        val body= JSONobj.toString().toRequestBody("application/json".toMediaType())
        val req=okhttp3.Request.Builder().url(url).post(body).build()
        okHttpClient.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ERROR",e.message.toString())
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val result = response.body!!.string().removeSurrounding("\"")
                Log.d("Result","${result}")
//                if (result == "true") {
//                    intent.putExtra("id",id)
//                    setResult(RESULT_OK, intent)
//                    finish()
//                } else {
//                    Toast.makeText(this@LoginActivity,"ID/PW가 일치하지 않습니다.",Toast.LENGTH_SHORT).show()
//                }
            }

        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: 자동로그인 여부 확인
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent,10)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewpager.adapter = MyFragmentPagerAdapter(this)

        TabLayoutMediator(binding.tabs,binding.viewpager){
                tab, position-> tabSetting(tab, position)
        }.attach()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 10 && resultCode === Activity.RESULT_OK) {
            id = data?.getStringExtra("id")
        }
        id?.let { sendIdRequest(it) }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
//            val fragment = (binding.viewpager.adapter as MyFragmentPagerAdapter).fragments[binding.viewpager.currentItem]
//            if (fragment is OneFragment) {
//                if(fragment.backButtonPressed()==1) return true
//            }
//            else if (fragment is TwoFragment) {
//                if(fragment.backButtonPressed()==1) return true
//            }
            if(System.currentTimeMillis() - initTime > 3000){
                Toast.makeText(this, "종료하려면 한번 더 누르세요!",
                    Toast.LENGTH_SHORT).show()
                initTime = System.currentTimeMillis()
                return true // 키 이벤트 무시
            }
            else
            {
                ActivityCompat.finishAffinity(this)
                System.exit(0)
            }
        }
        return super.onKeyDown(keyCode, event) // 키 이벤트 처리
    }
}