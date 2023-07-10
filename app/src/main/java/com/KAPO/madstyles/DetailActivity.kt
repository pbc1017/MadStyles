package com.KAPO.madstyles

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.KAPO.madstyles.databinding.ActivityDetailBinding
import org.json.JSONObject

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener()
        {
            intent.putExtra("id","-1")
            setResult(RESULT_OK, intent)
            Log.d("finish","finish")
            finish()
        }
        binding.homeButton.setOnClickListener()
        {
            intent.putExtra("id","0")
            setResult(RESULT_OK, intent)
            Log.d("finish","finish")
            finish()
        }
        binding.searchButton.setOnClickListener()
        {
            intent.putExtra("id","2")
            setResult(RESULT_OK, intent)
            Log.d("finish","finish")
            finish()
        }
        binding.cartButton.setOnClickListener()
        {
            intent.putExtra("id","3")
            setResult(RESULT_OK, intent)
            Log.d("finish","finish")
            finish()
        }
    }
}