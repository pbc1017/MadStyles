package com.KAPO.madstyles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KAPO.madstyles.databinding.FragmentThreeBinding
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
    val filter: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private lateinit var binding: FragmentThreeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        binding = FragmentThreeBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerViewSearch
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        itemAdapter = ItemAdapter(items,3,resultLauncher)
        recyclerView.adapter = itemAdapter
        val kindview=arrayOf<LinearLayout> (binding.kindview1,binding.kindview2,binding.kindview3,binding.kindview4,binding.kindview5)

        val filterButton = arrayOf<Array<String>>(
            arrayOf<String>("전체","남자","여자"),
            arrayOf<String>("전체","상의","바지","아우터","신발","가방","모자"),
            arrayOf<String>("전체","검정", "흰", "빨강", "파랑", "노랑", "초록"),
            arrayOf<String>("전체","높음", "중간", "낮음"),
            arrayOf<String>("인기순","낮은 가격순","높은 가격순"),)

        val groups = arrayOf<String>("gender","kind","color","pRange","sort")
        var i = 0
        for (ll in kindview) {
            for (btnName in filterButton[i]) {
                ll.addView(createButton(btnName,groups[i]))
            }
            i += 1
        }
        filter["sort"] = mutableSetOf("인기순") // 정렬 기본값 설정

        var prevSearch = ""

        binding.searchButton.setOnClickListener() {
            var nowSearch = binding.searchText.text.toString()
            Log.d("bool",(nowSearch == prevSearch).toString())
            thread(start=true)
            {
                sendSearchRequest(nowSearch == prevSearch)
                prevSearch = nowSearch
            }
            binding.filter.visibility=View.GONE
            hideKeyboard()
        }
        binding.filterButton.setOnClickListener() {
            if (binding.filter.visibility==View.VISIBLE) {
                binding.filter.visibility=View.GONE
            } else {
                binding.filter.visibility=View.VISIBLE
            }
        }

        return binding.root
    }

    fun hideKeyboard() {
        val inputMethodManager = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        // Check if no view has focus:
        val view = activity?.currentFocus ?: View(context)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    val similarityMap = mapOf(
        "검은색" to "검정",
        "검은" to "검정",
        "검정색" to "검정",
        "남성" to "남자",
        "여성" to "여자",
        "팬츠" to "바지"
        //... add other similar words
    )

    val fieldsValues = mapOf(
        "gender" to listOf("전체","남자","여자"),
        "kind" to listOf("전체","상의","바지","아우터","신발","가방","모자"),
        "color" to listOf("전체","검정", "흰", "빨강", "파랑", "노랑", "초록"),
        "pRange" to listOf("전체","High", "Mid", "Low")
    )

    val sorts = listOf("인기순", "낮은 가격순", "높은 가격순")

    fun makeQuery(inputText: String, isSame: Boolean): JSONObject {
        val words = inputText.split(" ").toMutableList()
        val query = JSONObject()
        val wordsCopy = ArrayList(words)
        Log.d("word",words.toString())
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
        Log.d("word",words.toString())
        val remainingWords = words.joinToString(" ")
        if (remainingWords.isNotBlank()) {
            query.put("name", remainingWords)
        }

        for ((field, values) in filter) {
            if (isSame || field == "sort") {
                var array = query.optJSONArray(field) ?: JSONArray()
                if (values.contains("전체")) {
                    array = JSONArray(mutableListOf("전체"))
                } else {
                    for (value in values) {
                        if (!array.toString().contains(value)) {
                            array.put(value)
                        }
                    }
                }
                query.put(field, array)
            }
        }

        // If there's no remaining words (i.e., the search query is empty), remove the "name" key from the query
        if (remainingWords.isBlank()) {
            query.remove("name")
        }

        Log.d("QUERY", query.toString())
        return query
    }


    fun sendSearchRequest(isSame : Boolean) {
        if(!isSame) {
            // 모든 필터를 비웁니다.
            filter.clear()

            // 모든 버튼의 색깔을 회색으로 변경합니다.
            for ((_, buttons) in buttonMap) {
                buttons.forEach { it.setBackgroundColor(Color.GRAY) }
            }
            filter["sort"] = mutableSetOf("인기순")
            buttonMap["sort"]?.find { it.text == "인기순" }?.setBackgroundColor(Color.WHITE)
        }
        val query = makeQuery(binding.searchText.text.toString(), isSame)
        changeButton(query)
        serverCommu.sendRequest(query, "search", {result ->
            Log.d("Result","${result}")
            val json = result
            items.clear()
            val jsonArray = JSONArray(json)
            var pgnum = 1

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
        if (btn.text == "인기순") btn.setBackgroundColor(Color.WHITE)
        btn.setOnClickListener {
            val button = it as Button
            var currentList = filter[group] ?: mutableSetOf()
            val currentButtonList = buttonMap[group] ?: mutableListOf()
            if (group == "sort") {
                if (!currentList.contains(name)) {
                    currentList.clear()
                    currentList.add(name)
                    currentButtonList.forEach { if(it.text == name) it.setBackgroundColor(Color.WHITE) else it.setBackgroundColor(Color.GRAY)}
                }
            } else {
                if (name == "전체") {
                    if (currentList.contains("전체")) {
                        // "전체" 버튼이 눌려있을 경우 필터를 비우고 버튼 색상을 초기화합니다.
                        currentList.clear()
                        currentButtonList.forEach { it.setBackgroundColor(Color.GRAY) }
                    } else {
                        // "전체" 버튼이 눌리지 않은 경우 필터를 "전체"로 설정하고 버튼 색상을 바꿉니다.
                        currentList.clear()
                        currentList.add("전체")
                        currentButtonList.forEach { it.setBackgroundColor(Color.WHITE) }
                    }
                } else {
                    if (currentList.contains("전체")) {
                        // "전체" 버튼이 눌려있으면 다른 버튼을 눌렀을 때 "전체"를 필터에서 제거하고, 해당 버튼을 제외한 다른 버튼들을 필터에 추가합니다.
                        currentList.remove("전체")
                        currentButtonList.find { it.text == "전체" }?.setBackgroundColor(Color.GRAY)
                        for (otherButton in currentButtonList) {
                            if (otherButton.text != name && otherButton.text != "전체") {
                                currentList.add(otherButton.text.toString())
                                otherButton.setBackgroundColor(Color.WHITE)
                            }
                        }
                        button.setBackgroundColor(Color.GRAY)
                        currentButtonList.forEach {if(it.text == "전체") it.setBackgroundColor(Color.GRAY)}
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
                    if (currentButtonList.all { it.text == "전체" || currentList.contains(it.text)}) {
                        currentList = mutableSetOf("전체")
                        currentButtonList.forEach { it.setBackgroundColor(Color.WHITE)}
                    }
                }
                if (currentList.isEmpty()) {
                    filter.remove(group)
                } else {
                    filter[group] = currentList
                }
            }
            Log.d("FILTER",filter.toString())
        }
        val currentButtonList = buttonMap[group] ?: mutableListOf()
        currentButtonList.add(btn)
        buttonMap[group] = currentButtonList
        return btn
    }

    fun changeButton(newFilter: JSONObject) {
        val map: MutableMap<String, MutableSet<String>> = mutableMapOf()
        val keys: Iterator<String> = newFilter.keys()
        for (key in keys) {
            val values = newFilter.optJSONArray(key)
            val list = mutableSetOf<String>()
            if (values != null) {
                // 값이 JSONArray일 경우
                for (i in 0 until values.length()) {
                    list.add(values.getString(i))
                }
            } else {
                // 값이 문자열일 경우
                list.add(newFilter.getString(key))
            }
            map[key] = list
        }
        for ((group, values) in map) {
            if (group == "name") continue
            filter[group] = values.toList().toMutableSet()
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
        for ((group, buttons) in buttonMap) {
            if (!newFilter.has(group)) {
                buttons.forEach { it.setBackgroundColor(Color.GRAY) }
            }
        }
        //TODO: 전체가 포함되면 싹다 색칠, 싹다 색칠되면 전체로 변경
    }
}