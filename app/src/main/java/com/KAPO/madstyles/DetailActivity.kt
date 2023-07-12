package com.KAPO.madstyles

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.KAPO.madstyles.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import org.json.JSONObject
import kotlin.concurrent.thread

data class Review(val userId: String, val rating: Int, val text: String)

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

class ReviewAdapter(private val reviews: MutableList<Review>, val binding: ActivityDetailBinding, val userId: String) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewEditButton: Button = itemView.findViewById(R.id.review_edit_button)
        val userId: TextView = itemView.findViewById(R.id.user_id)
        val rating: TextView = itemView.findViewById(R.id.rating)
        val text: TextView = itemView.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        return ReviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false))
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.userId.text = review.userId
        holder.rating.text = review.rating.toString()
        holder.text.text = review.text

        if (review.userId == userId) {
            binding.inputReview.visibility=View.GONE
            holder.reviewEditButton.visibility = View.VISIBLE
        } else {
            binding.inputReview.visibility=View.VISIBLE
            holder.reviewEditButton.visibility = View.GONE
        }

        holder.reviewEditButton.setOnClickListener() {
            binding.inputReview.visibility=View.VISIBLE
//            val starButtons = arrayOf(binding.star1,binding.star2,binding.star3,binding.star4,binding.star5)
//            starButtons.forEachIndexed { i, star ->
//                star.setColorFilter(if (i < holder.rating.text.toString().toInt()) Color.YELLOW else Color.GRAY)
//            }
            binding.reviewEditText.setText(holder.text.text.toString())
            reviews.remove(review)
            notifyDataSetChanged()
        }
    }
    override fun getItemCount(): Int = reviews.size
}

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewPager: ViewPager2
    private var images = mutableListOf<String>()
    private var adapter = ViewPagerAdapter(images)
    private var reviews = mutableListOf<Review>()
    private lateinit var reviewAdapter : ReviewAdapter

    private val starButtons: Array<ImageView> by lazy {
        arrayOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )
    }
    private var rating = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemId = intent.getIntExtra("itemId", 0)
        val userId = intent.getStringExtra("userId").toString()

        reviewAdapter = ReviewAdapter(reviews, binding, userId)

        requestDetail(itemId)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = adapter
        val pageNumber: TextView = binding.pageNumber

        val reviewRecyclerView = binding.reviewRecyclerView
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.adapter = reviewAdapter

        Log.d("ItemID","userID ${userId} itemID ${itemId}")

        val reviewEditText = binding.reviewEditText
        val submitButton = binding.submitButton

        starButtons.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                rating = index + 1
                starButtons.forEachIndexed { i, star ->
                    star.setColorFilter(if (i < rating) Color.YELLOW else Color.GRAY)
                }
            }
        }

        submitButton.setOnClickListener {
//            rating = 0
//            starButtons.forEachIndexed { i, star ->
//                Log.d("color",star.colorFilter.toString().toInt().toString())
//                if (star.colorFilter.toString().toInt() == Color.YELLOW) rating += 1
//            }
            val reviewText = reviewEditText.text.toString()
            if (reviewText.isNotEmpty() && rating > 0) {
                val review = Review(userId, rating, reviewText)
                sendReview(review, itemId)
                //TODO: hideKeyboard()
            } else {
                Toast.makeText(this, "리뷰와 별점을 모두 남겨주세요.", Toast.LENGTH_SHORT).show()
            }
        }

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
                Toast.makeText(this, "이미 추가한 상품입니다.",Toast.LENGTH_SHORT)
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
                thread(start=true) {
                    requestAIimage(userId,itemId)
                }
                binding.buyDetail.visibility=View.GONE
                binding.addCartButton.visibility=View.GONE
                Toast.makeText(this, "장바구니에 상품을 담았습니다.",Toast.LENGTH_SHORT)

                binding.totalCount.text = "상품 0개"
                binding.totalPrice.text = "0원"
                binding.size.visibility=View.GONE
                sizeButtonState = false
                price = 0

            } else {
                Toast.makeText(this, "장바구니에 담을 상품이 없습니다.",Toast.LENGTH_SHORT)
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
    private fun sendReview(review: Review, itemId: Int) {
        val json = JSONObject().apply {
            put("itemId", itemId)
            put("review", JSONObject().apply {
                put("userId", review.userId)
                put("rating", review.rating)
                put("text", review.text)
            })
        }
        thread(start = true) {
            serverCommu.sendRequest(json, "addReview", { result ->
                Log.d("Result", "${result}")
                Log.d("Review",reviews.toString())
                reviews.add(0, review)
                runOnUiThread {
                    binding.inputReview.visibility=View.GONE
                    Toast.makeText(this, "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("Review",reviews.toString())
                    reviewAdapter.notifyDataSetChanged()
                    recreate()
                }
            }, { result ->
                Log.d("Result", "${result}")
            })
        }
    }
    private fun deleteReview(review: Review, itemId: Int) {
        val json = JSONObject().apply {
            put("itemId", itemId)
            put("review", JSONObject().apply {
                put("userId", review.userId)
                put("rating", review.rating)
                put("text", review.text)
            })
        }
        thread(start = true) {
            serverCommu.sendRequest(json, "deleteReview", { result ->
                Log.d("Result", "${result}")
                Log.d("Review",reviews.toString())
                runOnUiThread {
                    binding.inputReview.visibility=View.GONE
                    Toast.makeText(this, "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("Review",reviews.toString())
                    reviewAdapter.notifyDataSetChanged()
                }
            }, { result ->
                Log.d("Result", "${result}")
            })
        }
    }
    private fun requestAIimage(userid:String,itemid:Int)
    {
        val json=JSONObject().apply{
            put("user",userid)
            put("item",itemid)
        }
        serverCommu.sendRequest(json,"createaiimage",{result->
            this.runOnUiThread {
                //Toast.makeText(,"${result} created!",Toast.LENGTH_SHORT).show()
            }
        },
            {result ->
            Log.d("Result", "${result}")})
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

                val reviewsArray = item.getJSONArray("review")
                for (i in 0 until reviewsArray.length()) {
                    val reviewObject = reviewsArray.getJSONObject(i)
                    val userId = reviewObject.getString("userId")
                    val rating = reviewObject.getInt("rating")
                    val text = reviewObject.getString("text")
                    reviews.add(Review(userId, rating, text))
                }
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    reviewAdapter.notifyDataSetChanged()
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
//TODO:검색이 완료되면 키보드 숨기기
//    fun hideKeyboard() {
//        val inputMethodManager = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//        // Check if no view has focus:
//        val view = activity?.currentFocus ?: View(context)
//        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
//    }
}