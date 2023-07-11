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
import android.widget.Toast
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

        binding.buyButton.setOnClickListener()
        {
            binding.buyDetail.visibility=View.VISIBLE
            binding.addCartButton.visibility=View.VISIBLE
        }

        var optionButtonState = false
        binding.optionButton.setOnClickListener()
        {
            if(optionButtonState) {
                binding.sizeButton.visibility=View.GONE
                optionButtonState = false
            } else {
                binding.sizeButton.visibility=View.VISIBLE
                optionButtonState = true
            }
        }

        var sizeButtonState = false
        var price = 0
        binding.sizeButton.setOnClickListener()
        {
            if(sizeButtonState) {
                //TODO: 토스트 이미 추가한 상품입니다.
                binding.sizeButton.visibility=View.GONE
            } else {
                binding.size.visibility=View.VISIBLE
                binding.sizeButton.visibility=View.GONE
                binding.count.text = "1"
                binding.totalCount.text = "상품 1개"
                binding.itemPrice.text = binding.priceText.text
                binding.totalPrice.text = binding.priceText.text
                sizeButtonState = true
                price = binding.itemPrice.text.toString().split("원")[0].toInt()
            }
        }

        binding.cancelButton.setOnClickListener()
        {
            binding.totalCount.text = "상품 0개"
            binding.totalPrice.text = "0원"
            binding.size.visibility=View.GONE
            sizeButtonState = false
            price = 0
        }

        binding.plusButton.setOnClickListener()
        {
            binding.count.text = (binding.count.text.toString().toInt() + 1).toString()
            binding.totalCount.text = "상품 ${binding.count.text}개"
            binding.itemPrice.text = "${price * binding.count.text.toString().toInt()}원"
            binding.totalPrice.text = binding.itemPrice.text
        }

        binding.minusButton.setOnClickListener()
        {
            if (binding.count.text.toString().toInt() > 1) {
                binding.count.text = (binding.count.text.toString().toInt() - 1).toString()
                binding.totalCount.text = "상품 ${binding.count.text}개"
                binding.itemPrice.text = "${price * binding.count.text.toString().toInt()}원"
                binding.totalPrice.text = binding.itemPrice.text
            }
        }

        binding.addCartButton.setOnClickListener()
        {
            if (sizeButtonState) {
                var json = JSONObject()
                json.put("id",userId)
                var itemJson = JSONObject()
                itemJson.put("id",itemId)
                itemJson.put("count",binding.count.text.toString().toInt())
                json.put("item",itemJson)
                requestCart(json)
                binding.buyDetail.visibility=View.GONE
                binding.addCartButton.visibility=View.GONE
                //TODO: 토스트 장바구니에 상품을 담았습니다

                binding.totalCount.text = "상품 0개"
                binding.totalPrice.text = "0원"
                binding.size.visibility=View.GONE
                sizeButtonState = false
                price = 0

            } else {
                //TODO: 토스트 장바구니에 담을 상품이 없습니다
            }
        }

        binding.closeButton.setOnClickListener()
        {
            binding.buyDetail.visibility=View.GONE
            binding.addCartButton.visibility=View.GONE
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Note: position is 0-indexed, pages are 1-indexed
                pageNumber.text = "${position + 1}/${images.size}"
            }
        })
    }
    private fun requestCart(json:JSONObject) {
        thread(start = true)
        {
            serverCommu.sendRequest(json, "addcart", { result ->
                Log.d("Result", "${result}")
            }, { result ->
                Log.d("Result", "${result}")
            })
        }
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
                    else if(url.endsWith(".png")) {
                        val modifiedUrl = url.replace("_60.png", "_500.png")
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