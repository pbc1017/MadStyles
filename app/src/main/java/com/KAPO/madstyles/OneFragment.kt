package com.KAPO.madstyles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KAPO.madstyles.databinding.FragmentOneBinding
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread
import java.util.concurrent.CompletableFuture


class OneFragment : Fragment() {
    private lateinit var binding: FragmentOneBinding
    private lateinit var itemAdapter1: ItemAdapter
    private lateinit var itemAdapter2: ItemAdapter
    private lateinit var itemAdapter3: ItemAdapter
    private lateinit var itemAdapter4: ItemAdapter
    var json:String=""
    val items = listOf(mutableListOf<Item>(),mutableListOf<Item>(),mutableListOf<Item>(),mutableListOf<Item>())

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
        val id = (activity as MainActivity).getID()
        val gender=(activity as MainActivity).getgender()


        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val id = data?.getStringExtra("id")?.toInt()
                id?.let {
                    // id를 기반으로 뷰페이저 페이지를 변경
                    if (id > -1) (activity as? MainActivity)?.changeViewPagerPage(it)
                }
            }
        }
        binding.txtrecommend1.text="${id}에게 추천하는 ${gender}아이템"
        itemAdapter1= ItemAdapter(items[0],1, resultLauncher, id)
        view.findViewById<RecyclerView>(R.id.recommend_view_1).adapter=itemAdapter1
        val linmanager1=LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        view.findViewById<RecyclerView>(R.id.recommend_view_1).layoutManager=linmanager1

        binding.txtrecommend2.text="${id}의 취향에 따른 추천 아이템"
        itemAdapter2=ItemAdapter(items[1],1,resultLauncher, id)
        val linmanager2=LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        view.findViewById<RecyclerView>(R.id.recommend_view_2).adapter=itemAdapter2
        view.findViewById<RecyclerView>(R.id.recommend_view_2).layoutManager=linmanager2

        itemAdapter3= ItemAdapter(items[2],1,resultLauncher, id)
        val linmanager3=LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        view.findViewById<RecyclerView>(R.id.recommend_view_3).adapter=itemAdapter3
        view.findViewById<RecyclerView>(R.id.recommend_view_3).layoutManager=linmanager3

        itemAdapter4= ItemAdapter(items[3],1,resultLauncher, id) //item을 어떻게 가져오지?
        val linmanager4=LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        view.findViewById<RecyclerView>(R.id.recent_view).adapter=itemAdapter4
        view.findViewById<RecyclerView>(R.id.recent_view).layoutManager=linmanager4

        Requestrecommend(id,0)
            .thenApply {Requestrecommend(id,1)}
            .thenAccept{Requestrecommend(id,2)}
            //.thenAccept{Requestrecommend(id,3)}

    }

    private fun Requestrecommend(id:String,kind:Int):CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync {
            val QueryObj= JSONObject()
            QueryObj.put("id",id)

            synchronized(this) {
                thread(start=true) {
                    serverCommu.sendRequest(QueryObj, "recommend/${kind}", {result ->
                        json = result
                        Log.d("RECOMMEND","${json}")
                        items[kind].clear()
                        val jsonArray = JSONArray(json)

                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            items[kind].add(
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
                            when(kind){
                                0->itemAdapter1.notifyDataSetChanged()
                                1->itemAdapter2.notifyDataSetChanged()
                                else->{
                                    binding.txtrecommend3.text="${id}님, 이런 아이템 어떠세요?"
                                    itemAdapter3.notifyDataSetChanged()}
                            }

                        }
                    }, {result ->
                        Log.d("Result","${result}")
                    })
                }
            }
        }
    }
}