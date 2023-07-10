package com.KAPO.madstyles

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KAPO.madstyles.databinding.FragmentFourBinding
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class FourFragment : Fragment() {
    private lateinit var binding: FragmentFourBinding
    private lateinit var itemAdapter: CartItemAdapter
    val items = mutableListOf<Item>()
    var json=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFourBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentFourBinding.bind(view)
        val id = (activity as MainActivity).getID()
        itemAdapter= CartItemAdapter(items)
        binding.cartview.adapter=itemAdapter
        binding.cartview.layoutManager=LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        thread(start=true){
            requestRanking(id)
        }
    }

    private fun requestRanking(id:String) {
        val QueryObj= JSONObject()
        QueryObj.put("id",id)
        serverCommu.sendRequest(QueryObj, "getcartitems", {result ->
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
                        rank=jsonObj.getInt("rank")
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


}

class CartItemAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<CartItemAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.item_name)
        val brand: TextView = itemView.findViewById(R.id.item_brand)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val image: ImageView = itemView.findViewById(R.id.item_image)

    }

    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.item_name)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =LayoutInflater.from(parent.context).inflate(R.layout.cartitem_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.price.text = item.price.toString()+"원"
        Glide.with(holder.image.context)
            .load(item.imgUrl)
            .into(holder.image)
    }

    override fun getItemCount() = items.size
}