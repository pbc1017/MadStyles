package com.KAPO.madstyles

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

data class Item(
    val id: String,
    val name: String,
    val brand: String,
    val price: Double,
    val imgUrl: String,
    val rank:String
)

class ItemAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.price.text = item.price.toString()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_two, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        itemAdapter = ItemAdapter(items)
        recyclerView.adapter = itemAdapter
        return view
    }
    private fun sendIdRequest(id: String) {
        val JSONobj= JSONObject()
        JSONobj.put("id",id)
        serverCommu.sendRequest(JSONobj, "requestmain", {result ->
            Log.d("Result","${result}")
            json = result

            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                items.add(
                    Item(
                        id = jsonObj.getString("id"),
                        name = jsonObj.getString("name"),
                        brand = jsonObj.getString("brand"),
                        price = jsonObj.getDouble("price"),
                        imgUrl = jsonObj.getString("imgUrl"),
                        rank = jsonObj.getString("rank")
                    )
                )
            }
            requireActivity().runOnUiThread{
                itemAdapter.notifyDataSetChanged()
            }
        }, {result ->
            Log.d("Result","${result}")
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = (activity as MainActivity).getID()
        thread(start=true)
        {
            sendIdRequest(id)
        }
    }
}