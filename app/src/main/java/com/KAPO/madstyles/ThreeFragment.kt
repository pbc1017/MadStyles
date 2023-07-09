package com.KAPO.madstyles

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class ThreeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    val items = mutableListOf<Item>()
    var json: String=""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_three, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        itemAdapter = ItemAdapter(items)
        recyclerView.adapter = itemAdapter

        return view
    }
    val similarityMap = mapOf(
        "검은색" to "검정",
        "검은" to "검정",
        "검정색" to "검정"
        //... add other similar words
    )

    val fieldsValues = mapOf(
        "gender" to listOf("전체","남자","여자"),
        "kind" to listOf("전체","상의","바지","아우터","신발","가방","모자"),
        "color" to listOf("전체","검정", "흰", "빨강", "파랑", "노랑", "초록"),
        "pRange" to listOf("전체","High", "Mid", "Low")
    )

    val sorts = listOf("인기순", "낮은 가격순", "높은 가격순")

    fun sendSearchRequest(inputText: String, filter: Map<String, List<String>>, sort: String):String {
        val words = inputText.split(" ").toMutableList()
        val query = JSONObject()
        val wordsCopy = ArrayList(words)
        for (word in wordsCopy) {
            val realWord = similarityMap[word] ?: word
            for ((field, values) in fieldsValues) {
                if (values.contains(realWord)) {
                    val array = query.optJSONArray(field) ?: JSONArray()
                    array.put(realWord)
                    query.put(field, array)
                    words.remove(word)
                    break
                }
            }
        }
        val remainingWords = words.joinToString(" ")
        query.put("brand", remainingWords)
        query.put("name", remainingWords)

        for ((field, values) in filter) {
            if (values.contains("전체")) continue
            val array = query.optJSONArray(field) ?: JSONArray()
            for (value in values) {
                array.put(value)
            }
            query.put(field, array)
        }

        when (sort) {
            "인기순" -> query.put("sort", JSONArray().put("rank down"))
            "낮은 가격순" -> query.put("sort", JSONArray().put("price up"))
            "높은 가격순" -> query.put("sort", JSONArray().put("price down"))
        }

        return query.toString()
        // use the returned items to update your RecyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = (activity as MainActivity).getID()
        val gender=(activity as MainActivity).getgender()
//        view.findViewById<Button>(R.id.btngendertoggle).text=gender
//        thread(start=true)
//        {
//            requestRanking(id,gender)
//        }
    }
}