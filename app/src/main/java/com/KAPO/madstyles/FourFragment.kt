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
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.KAPO.madstyles.databinding.FragmentFourBinding
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class FourFragment : Fragment() {
    lateinit var binding: FragmentFourBinding
    lateinit var resultLauncher:ActivityResultLauncher<Intent>
    private lateinit var itemAdapter: CartItemAdapter
    val items = mutableListOf<Item>()
    val counts=mutableListOf<Int>()
    var totalcount=0
    var total=0
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

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val id = data?.getStringExtra("id")?.toInt()
                id?.let {
                    // id를 기반으로 뷰페이저 페이지를 변경
                    if (id > -1) (activity as? MainActivity)?.changeViewPagerPage(it)
                }
            }
        }

        itemAdapter= CartItemAdapter(items,counts,this)
        binding.cartview.adapter=itemAdapter
        binding.cartview.layoutManager=LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        thread(start=true){
            requestCart(id)
        }

        binding.btnDelete.setOnClickListener {
            val deletelist= mutableListOf<Int>()
            val deleteitemlist= mutableListOf<Item>()
            for(i in 0 until binding.cartview.childCount) {
                val viewholder=binding.cartview.getChildViewHolder(binding.cartview.getChildAt(i)) as CartItemAdapter.ItemViewHolder
                if(viewholder.checkbox.isChecked) {
                    deletelist.add(counts[i])
                    deleteitemlist.add(items[i])
                }
                viewholder.checkbox.isChecked=false
            }
            deletelist.forEach {
                counts.remove(it)
            }
            deleteitemlist.forEach {
                items.remove(it)
            }
            itemAdapter.notifyDataSetChanged()
            binding.cartview.post{
                for(i in 0 until binding.cartview.childCount) {
                    val viewholder=binding.cartview.getChildViewHolder(binding.cartview.getChildAt(i)) as CartItemAdapter.ItemViewHolder
                    viewholder.checkbox.isChecked=false
                }
            }
            binding.itemchk.isChecked=false
            totalcount=0
            total=0
            binding.Txttotalselected.text="총 ${totalcount}개"
            binding.Txttotalcount.text="총 ${totalcount}개"
            binding.txttotal.text="${total} 원"
            binding.btnPay.text="${total}원 결제하기"

            thread(start = true){
                updateCart()
            }

        }
        binding.itemchk.setOnClickListener {
            val checkbox = it as CheckBox
            if (checkbox.isChecked) {
                for(i in 0 until binding.cartview.childCount) {
                    val viewholder=binding.cartview.getChildViewHolder(binding.cartview.getChildAt(i)) as CartItemAdapter.ItemViewHolder
                    viewholder.checkbox.isChecked=true
                }
                totalcount=counts.sum()
                binding.Txttotalselected.text="전체 ${totalcount}개"
                binding.Txttotalcount.text="총 ${totalcount}개"
                for(i in 0 until items.size){
                    total+=items[i].price*counts[i]
                }
                binding.txttotal.text="${total} 원"
                binding.btnPay.text="${total}원 결제하기"
            } else {
                for(i in 0 until binding.cartview.childCount) {
                    val viewholder=binding.cartview.getChildViewHolder(binding.cartview.getChildAt(i)) as CartItemAdapter.ItemViewHolder
                    viewholder.checkbox.isChecked=false
                }
                totalcount=0
                total=0
                binding.Txttotalselected.text="전체 ${totalcount}개"
                binding.Txttotalcount.text="총 ${totalcount}개"
                binding.txttotal.text="${total} 원"
                binding.btnPay.text="${total}원 결제하기"
            }
        }

    }

    private fun requestCart(id:String) {
        val QueryObj= JSONObject()
        QueryObj.put("id",id)
        serverCommu.sendRequest(QueryObj, "getcartitems", {result ->
            Log.d("Result","${result}")
            json = result
            items.clear()
            counts.clear()
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
                counts.add(jsonObj.getInt("count"))
            }
            requireActivity().runOnUiThread{
                binding.itemchk.isChecked=true
                totalcount=counts.sum()
                binding.Txttotalselected.text="전체 ${totalcount}개"
                binding.Txttotalcount.text="총 ${totalcount}개"
                for(i in 0 until items.size){
                    total=items[i].price*counts[i]
                }
                binding.txttotal.text="${total}원"
                binding.btnPay.text="${total}원 결제하기"
                itemAdapter.notifyDataSetChanged()
            }
        }, {result ->
            Log.d("Result","${result}")
        })
    }

    fun updateCart()
    {
        val UpdateObj= JSONObject()
        UpdateObj.put("id",(activity as MainActivity).getID())
        val cart=JSONArray()
        for(i in 0 until items.size)
        {
            val item=JSONObject()
            item.put("id",items[i].id)
            item.put("count",counts[i])
            cart.put(item)
        }
        //cart 잘 생성
        UpdateObj.put("cart",cart)
        serverCommu.sendRequest(UpdateObj, "setcart", {result ->
            Log.d("Cartupdate result","${result}")

        }, {result ->
            Log.d("Err:","${result}")
        })
    }

    override fun onResume() {
        super.onResume()
        requestCart((activity as MainActivity).getID())
    }
}

class CartItemAdapter(private val items: MutableList<Item>,private val counts:MutableList<Int>, val context:FourFragment) : RecyclerView.Adapter<CartItemAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.item_name)
        val brand: TextView = itemView.findViewById(R.id.item_brand)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val image: ImageView = itemView.findViewById(R.id.item_image)
        val count:TextView=itemView.findViewById(R.id.Txtcount)
        val checkbox:CheckBox=itemView.findViewById(R.id.itemchk)
        val inc:Button=itemView.findViewById(R.id.increase)
        val dec:Button=itemView.findViewById(R.id.decrease)
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
        holder.count.text=counts[position].toString()
        holder.checkbox.isChecked=true
        holder.checkbox.setOnClickListener {
            val chk=it as CheckBox
            if(chk.isChecked)
            {
                context.totalcount+=counts[position]
                context.total+=item.price*counts[position]
                if(!context.binding.itemchk.isChecked)
                    context.binding.itemchk.isChecked=true
                updateNumbers()
                //해당 수량만큼 가격 수량 업뎃
            }
            else
            {
                context.totalcount-=counts[position]
                context.total-=item.price*counts[position]
                updateNumbers()
                //가격 업뎃
            }
        }
        holder.inc.setOnClickListener {
            counts[position]++
            thread(start = true){
                context.updateCart()
            }
            holder.count.text=counts[position].toString()
            if(holder.checkbox.isChecked)
            {
                context.totalcount++
                context.total+=item.price
                holder.price.text="${item.price*counts[position]}원"
                updateNumbers()
            }

        }
        holder.dec.setOnClickListener {
            if (counts[position]>0){
                counts[position]--
                thread(start = true){
                    context.updateCart()
                }

            holder.count.text=counts[position].toString()
            if(holder.checkbox.isChecked){
                context.totalcount--
                context.total-=item.price
                holder.price.text="${item.price*counts[position]}원"
                updateNumbers()

            }
            }
        }
        holder.image.setOnClickListener {
            val intent=Intent(it.context,DetailActivity::class.java)
            intent.putExtra("itemId", item.id)
            intent.putExtra("userId",(context.activity as MainActivity).getID())
            context.resultLauncher.launch(intent)
        }
    }

    override fun getItemCount() = items.size

    private fun updateNumbers(){
        context.binding.Txttotalselected.text="전체 ${context.totalcount}개"
        context.binding.Txttotalcount.text="총 ${context.totalcount}개"
        context.binding.txttotal.text="${context.total}원"
        context.binding.btnPay.text="${context.total}원 결제하기"
    }
}