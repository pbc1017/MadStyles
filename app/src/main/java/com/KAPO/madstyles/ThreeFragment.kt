package com.KAPO.madstyles

import android.graphics.Color
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
    val buttonMap: MutableMap<String, MutableList<Button>> = mutableMapOf()
    val filter: MutableMap<String, MutableList<String>> = mutableMapOf()

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
        val kindview=arrayOf<LinearLayout> (
            view.findViewById<LinearLayout>(R.id.kindview1),
            view.findViewById<LinearLayout>(R.id.kindview2),
            view.findViewById<LinearLayout>(R.id.kindview3),
            view.findViewById<LinearLayout>(R.id.kindview4),
            view.findViewById<LinearLayout>(R.id.kindview5))

        val filterButton = arrayOf<Array<String>>(
            arrayOf<String>("전체","남자","여자"),
            arrayOf<String>("전체","상의","바지","아우터","신발","가방","모자"),
            arrayOf<String>("전체","검정", "흰", "빨강", "파랑", "노랑", "초록"),
            arrayOf<String>("전체","높음", "중간", "낮음"),
            arrayOf<String>("인기순","낮은 가격순","높은 가격순"),)

        val groups = arrayOf<String>("성별","종류","색상","가격","정렬")
        var i = 0
        for (ll in kindview) {
            for (btnName in filterButton[i]) {
                ll.addView(createButton(btnName,groups[i]))
            }
            i += 1
        }
        filter["정렬"] = mutableListOf("인기순") // 정렬 기본값 설정
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


    fun createButton(name: String, group: String): Button {
        val btn = Button(context)
        btn.text = name
        btn.layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        btn.setBackgroundColor(Color.GRAY) // 기본 버튼 색상
        btn.setOnClickListener {
            val button = it as Button
            val currentList = filter[group] ?: mutableListOf()
            val currentButtonList = buttonMap[group] ?: mutableListOf()
            if (name == "전체") {
                if (currentList.contains("전체")) {
                    // "전체" 버튼이 눌려있을 경우 필터를 비우고 버튼 색상을 초기화합니다.
                    filter.remove(group)
                    currentList.clear()
                    currentButtonList.forEach { it.setBackgroundColor(Color.GRAY) }
                } else {
                    // "전체" 버튼이 눌리지 않은 경우 필터를 "전체"로 설정하고 버튼 색상을 바꿉니다.
                    filter[group] = mutableListOf("전체")
                    currentButtonList.forEach { it.setBackgroundColor(Color.WHITE) }
                }
            } else {
                if (currentList.contains("전체")) {
                    // "전체" 버튼이 눌려있으면 다른 버튼을 눌렀을 때 "전체"를 필터에서 제거하고, 해당 버튼을 제외한 다른 버튼들을 필터에 추가합니다.
                    currentList.remove("전체")
                    currentButtonList.find { it.text == "전체" }?.setBackgroundColor(Color.GRAY)
                    for (otherButton in currentButtonList) {
                        if (otherButton.text != name && !currentList.contains(otherButton.text.toString())) {
                            currentList.add(otherButton.text.toString())
                            otherButton.setBackgroundColor(Color.WHITE)
                        }
                    }
                    button.setBackgroundColor(Color.GRAY)
                } else {
                    if (currentList.contains(name)) {
                        // 이미 선택된 버튼을 다시 클릭하면 필터에서 해당 항목을 제거하고 버튼 색상을 초기화합니다.
                        currentList.remove(name)
                        button.setBackgroundColor(Color.GRAY)
                    } else {
                        // 선택되지 않은 버튼을 클릭하면 필터에 해당 항목을 추가하고 버튼 색상을 바꿉니다.
                        currentList.add(name)
                        button.setBackgroundColor(Color.WHITE)
                    }
                }
                // 전체 버튼을 제외한 모든 버튼이 선택되었다면 전체 버튼이 눌려진 것으로 처리합니다.
                if (currentButtonList.all { it.text == "전체" || it.currentTextColor == Color.WHITE }) {
                    filter[group] = mutableListOf("전체")
                    currentButtonList.forEach { it.setBackgroundColor(if(it.text == "전체") { Color.WHITE} else {Color.GRAY })}
                }
            }
            Log.d("FILTER",filter.toString())
        }
        val currentButtonList = buttonMap[group] ?: mutableListOf()
        currentButtonList.add(btn)
        buttonMap[group] = currentButtonList
        return btn
    }

    fun changeButton(newFilter: Map<String, List<String>>) {
        for ((group, values) in newFilter) {
            filter[group] = values.toMutableList()
            // 이제 필터가 변경되었으므로, 버튼의 색상도 업데이트해야 합니다.
            val currentButtonList = buttonMap[group] ?: mutableListOf()
            for (button in currentButtonList) {
                if (values.contains(button.text.toString())) {
                    button.setBackgroundColor(Color.WHITE)
                } else {
                    button.setBackgroundColor(Color.GRAY)
                }
            }
        }
    }
}