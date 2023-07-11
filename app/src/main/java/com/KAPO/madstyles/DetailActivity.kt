package com.KAPO.madstyles

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.KAPO.madstyles.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import org.json.JSONObject
import kotlin.concurrent.thread

class ViewPagerAdapter(private val images: List<String>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_pager_item, parent, false))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(images[position])
            .into(holder.img)
    }

    override fun getItemCount(): Int = images.size
}

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewPager: ViewPager2
    var images = mutableListOf<String>()
    val adapter = ViewPagerAdapter(images)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = adapter
        val pageNumber: TextView = binding.pageNumber

        val itemId = intent.getIntExtra("itemId", 0)
        val userId = intent.getStringExtra("userId")
        requestDetail(itemId)

        Log.d("ItemID","userID ${userId} itemID ${itemId}")

        binding.backButton.setOnClickListener()
        {
            intent.putExtra("id","-1")
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.homeButton.setOnClickListener()
        {
            intent.putExtra("id","0")
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.searchButton.setOnClickListener()
        {
            intent.putExtra("id","2")
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.cartButton.setOnClickListener()
        {
            intent.putExtra("id","3")
            setResult(RESULT_OK, intent)
            finish()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Note: position is 0-indexed, pages are 1-indexed
                pageNumber.text = "${position + 1}/${images.size}"
            }
        })
    }

    private fun requestDetail(itemId:Int) {
        val QueryObj = JSONObject()
        QueryObj.put("id", itemId)
        thread(start = true)
        {
            serverCommu.sendRequest(QueryObj, "getDetail", { result ->
                Log.d("Result", "${result}")
                val json = JSONObject(result)
                val imgSrcs = json.getJSONArray("imgSrcs")
                for (i in 0 until imgSrcs.length()) {
                    val url = imgSrcs.getString(i)

                    // Check if url ends with ".jpg"
                    if (url.endsWith(".jpg")) {
                        val modifiedUrl = url.replace("_60.jpg", "_500.jpg")
                        images.add("https:$modifiedUrl")
                    }
                }
                val item = json.getJSONObject("result")
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    binding.kindText.text = item.getString("kind").toString()
                    binding.nameText.text = item.getString("name").toString()
                    binding.priceText.text = "${item.getInt("price")}원"
                    binding.brandText.text = "${item.getString("id")} / ${item.getString("brand")}"
                    binding.genderText.text = "2023 SS / ${item.getString("gender")}"
                }
            }, { result ->
                Log.d("Result", "${result}")
            })
        }
    }
}