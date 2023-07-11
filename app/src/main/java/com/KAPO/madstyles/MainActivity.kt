package com.KAPO.madstyles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.KAPO.madstyles.FiveFragment
import com.KAPO.madstyles.FourFragment
import com.KAPO.madstyles.OneFragment
import com.KAPO.madstyles.R
import com.KAPO.madstyles.ThreeFragment
import com.KAPO.madstyles.TwoFragment
import com.KAPO.madstyles.databinding.ActivityMainBinding
import com.KAPO.madstyles.serverCommu
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import com.kakao.sdk.common.util.Utility

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var initTime = 0L
    var id: String? = ""
    var gender:String?=""
    interface MainIdProvider {
        fun getID(): Int
    }
    fun getID(): String {
        return id.toString()
    }
    fun getgender(): String {
        return gender.toString()
    }
    fun changeViewPagerPage(index: Int) {
        binding.viewpager.currentItem = index
    }
    // 뷰 페이저 어댑터
    class MyFragmentPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        val fragments =
            listOf(OneFragment(), TwoFragment(), ThreeFragment(), FourFragment(), FiveFragment())

        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    fun tabSetting(tab: TabLayout.Tab, position: Int) {
        if (position == 0) {
            tab.text = "Home"
//            tab.setIcon(R.drawable.contacts_icon)
        } else if (position == 1) {
            tab.text = "Rank"
//            tab.setIcon(R.drawable.gallery_icon)
        } else if (position == 2) {
            tab.text = "Search"
//            tab.setIcon(R.drawable.gallery_icon)
        } else if (position == 3) {
            tab.text = "Cart"
//            tab.setIcon(R.drawable.gallery_icon)
        } else {
            tab.text = "My"
//            tab.setIcon(R.drawable.gpt_icon)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!intent.getBooleanExtra("Loginagain",false)) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, 10)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent.getBooleanExtra("Loginagain",false))
        {
            id = intent.getStringExtra("id")
            gender=intent.getStringExtra("gender")
            binding.viewpager.adapter = MyFragmentPagerAdapter(this)
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                tabSetting(tab, position)
            }.attach()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode === Activity.RESULT_OK) {
            id = data?.getStringExtra("id")
            gender=data?.getStringExtra("gender")
            binding.viewpager.adapter = MyFragmentPagerAdapter(this)
            TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
                tabSetting(tab, position)
            }.attach()
        }
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            val fragment = (binding.viewpager.adapter as MyFragmentPagerAdapter).fragments[binding.viewpager.currentItem]
//            if (fragment is OneFragment) {
//                if(fragment.backButtonPressed()==1) return true
//            }
//            else if (fragment is TwoFragment) {
//                if(fragment.backButtonPressed()==1) return true
//            }
            if (System.currentTimeMillis() - initTime > 3000) {
                Toast.makeText(
                    this, "종료하려면 한번 더 누르세요!",
                    Toast.LENGTH_SHORT
                ).show()
                initTime = System.currentTimeMillis()
                return true // 키 이벤트 무시
            } else {
                ActivityCompat.finishAffinity(this)
                System.exit(0)
            }
        }
        return super.onKeyDown(keyCode, event) // 키 이벤트 처리
    }

    override fun onBackPressed() {

    }
}