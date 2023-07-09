package com.KAPO.madstyles

import android.app.DownloadManager.Query
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

data class Item(
    val id: Int,
    val name: String,
    val color:String,
    val brand: String,
    val price: Int,
    val imgUrl: String,
    val rank:Int
)

class ItemAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rank:TextView=itemView.findViewById(R.id.item_rank)
        val name: TextView = itemView.findViewById(R.id.item_name)
        val brand: TextView = itemView.findViewById(R.id.item_brand)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val image: ImageView = itemView.findViewById(R.id.item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.rank.text=item.rank.toString()
        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.price.text = item.price.toString()
        Glide.with(holder.image.context)
            .load(item.imgUrl)
            .into(holder.image)

        // To load image from URL, you may need a library like Picasso or Glide
        // Here is an example with Picasso:
        // Picasso.get().load(item.imageUrl).into(holder.image)
    }

    override fun getItemCount() = items.size
}

class TwoFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    val items = mutableListOf<Item>()
    var json: String=""
    var pgnum:Int=1
    var kind:String="전체"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_two, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_ranking)
        val kindview=view.findViewById<LinearLayout>(R.id.kindview)

        kindview.addView(createButton("전체"))
        kindview.addView(createButton("상의"))
        kindview.addView(createButton("바지"))
        kindview.addView(createButton("아우터"))
        kindview.addView(createButton("신발"))
        kindview.addView(createButton("가방"))
        kindview.addView(createButton("모자"))

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        itemAdapter = ItemAdapter(items)
        recyclerView.adapter = itemAdapter
        val btnrank= view.findViewById<Button>(R.id.btngendertoggle)
        val btnprev=view.findViewById<Button>(R.id.btnprev)
        val btnnext=view.findViewById<Button>(R.id.btnnext)
        btnprev.isVisible=false
       btnrank.setOnClickListener {
            val id = (activity as MainActivity).getID()
            btnrank.text=genderchange(btnrank.text.toString())
            pgnum=1
            //var gender=(activity as MainActivity).getgender()
            thread(start=true)
            {
                requestRanking(btnrank.text.toString())
            }
        }
        btnprev.setOnClickListener{
            pgnum--
            thread(start=true)
            {
                requestRanking(btnrank.text.toString())
            }
            if(pgnum<=1)
                btnprev.isVisible=false
        }
        btnnext.setOnClickListener{
            pgnum++
            btnprev.isVisible=true
            thread(start=true)
            {
                requestRanking(btnrank.text.toString())
            }
        }
        return view
    }
    private fun requestRanking(gender:String) {
        val QueryObj=JSONObject()
        QueryObj.put("gender",gender)
        QueryObj.put("kind",kind)
        serverCommu.sendRequest(QueryObj, "ranking/${pgnum}", {result ->
            Log.d("Result","${result}")
            json = result
            items.clear()
            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                items.add(
                    Item(
                        id = jsonObj.getInt("id"),
                        name = jsonObj.getString("name"),
                        brand = jsonObj.getString("brand"),
                        price = jsonObj.getInt("price"),
                        imgUrl = jsonObj.getString("imageUrl"),
                        color=jsonObj.getString("color"),
                        rank=20*(pgnum-1)+i+1
                    )
                )
            }
            requireActivity().runOnUiThread{
                itemAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(0)
            }
        }, {result ->
            Log.d("Result","${result}")
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = (activity as MainActivity).getID()
        val gender=(activity as MainActivity).getgender()
        view.findViewById<Button>(R.id.btngendertoggle).text=gender
        thread(start=true)
        {
            requestRanking(gender)
        }
    }

    fun createButton(name:String):View{
        val btn=Button(context)
        //btn.id=ViewCompat.generateViewId()
        btn.text=name
        btn.layoutParams=LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        btn.setOnClickListener{
            kind=name
            thread(start=true)
            {
                pgnum=1
                requestRanking(view?.findViewById<Button>(R.id.btngendertoggle)?.text.toString())
            }

        }
        btn.id=ViewCompat.generateViewId()
        return btn
    }
    fun genderchange(gender:String):String{
        if (gender=="남자")
            return "여자"
        else
            return "남자"
    }
}