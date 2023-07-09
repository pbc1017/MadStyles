package com.KAPO.madstyles

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.KAPO.madstyles.databinding.FragmentOneBinding
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class OneFragment : Fragment() {
    private lateinit var binding: FragmentOneBinding
    private lateinit var itemAdapter: ItemAdapter
    var json:String=""
    val items = mutableListOf<Item>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOneBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentOneBinding.bind(view)
        binding.txtrecommend1.text="${(activity as MainActivity).getID()}에게 추천하는 아이템"
        itemAdapter= ItemAdapter(items)
        view.findViewById<RecyclerView>(R.id.recommend_view_1).adapter=itemAdapter
        val id = (activity as MainActivity).getID()
        thread(start=true)
        {
            Requestrecommend(id)
        }


    }

    private fun Requestrecommend(id:String) {
        val QueryObj= JSONObject()
        QueryObj.put("id",id)
        serverCommu.sendRequest(QueryObj, "recommend", {result ->
            json = result
            Log.d("RECOMMEND","${json}")
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