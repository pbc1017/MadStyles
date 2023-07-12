package com.KAPO.madstyles

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KAPO.madstyles.databinding.FragmentFiveBinding
import com.kakao.sdk.user.UserApi
import com.kakao.sdk.user.UserApiClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

class FiveFragment : Fragment() {
    private lateinit var binding:FragmentFiveBinding
    private lateinit var itemAdapter: ItemAdapter
    var json:String=""
    var id=""
    val items= mutableListOf<Item>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentFiveBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentFiveBinding.bind(view)
        binding.btnlogout.setOnClickListener {
            UserApiClient.instance.unlink { err ->
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.putExtra("Loginagain", true)
                startActivity(intent)
            }
        }
            binding.btnimgpick.setOnClickListener {
                if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent().apply {
                        setAction(Intent.ACTION_GET_CONTENT)
                        setType("image/*")
                    }
                    activity?.startActivityForResult(intent, 4)
                }
                else{
                    Log.d("IMGPICK","Came to else")
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,),6)
                }
            }
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
        id=(activity as MainActivity).getID()
        binding.userProfile.text="${id}님"
        itemAdapter= ItemAdapter(items,1,resultLauncher, id) //item을 어떻게 가져오지?
        val linmanager= LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        binding.recentView.adapter=itemAdapter
        binding.recentView.layoutManager=linmanager
        Requestrecent(id)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            val intent = Intent().apply {
                setAction(Intent.ACTION_GET_CONTENT)
                setType("image/*")
            }
            activity?.startActivityForResult(intent, 4)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun Requestrecent(id:String) {

            val QueryObj= JSONObject()
            QueryObj.put("id",id)

            //synchronized(this) {
            thread(start=true) {
                serverCommu.sendRequest(QueryObj, "recent", {result ->
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
                        items.reverse()
                    }
                    requireActivity().runOnUiThread{
                        binding.recentView.adapter?.notifyDataSetChanged()
                        binding.recentView.scrollToPosition(0)
                    }
                }, {result ->
                    Log.d("Result","${result}")
                })

            //    }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onResume() {
        super.onResume()
        if(id!="")
        Requestrecent(id)
    }
}