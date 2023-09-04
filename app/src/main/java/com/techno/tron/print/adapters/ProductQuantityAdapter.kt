package com.techno.tron.print.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techno.tron.bluetoothprinter.databinding.QtyAndPriceLayoutBinding

import com.techno.tron.print.utills.Util.charRepeater

class ProductQuantityAdapter:RecyclerView.Adapter<ProductQuantityAdapter.ViewHolder> (){

    private val list = mutableListOf<QtyAndPriceLayoutBinding>()
    private var size = 1
    inner class ViewHolder(val binding:QtyAndPriceLayoutBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =  QtyAndPriceLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            list.add(this)
        }
    }

    fun add(){
        size++
        notifyItemChanged(size)
    }

    fun getSubTotal():Double{
        var total = 0.0
        list.forEach {
           total += it.price.text.toString().toIntOrNull() ?: 0
        }
        return total
    }

    override fun toString(): String {
        val str = StringBuilder()
        str.append(charRepeater("-")).append('\n')
        str.append(charRepeater(" ",10)).append("products").append("\n")
        str.append(charRepeater("-")).append('\n')
        list.forEach {
            if(it.productName.text.isNotBlank()){

                str.append(it.productName.text.toString()).append("\n")
                    .append("${"Qty:%-4d            Price:%-5d".format(it.quantity.text.toString().toInt(),it.price.text.toString().toInt())}\n")
            }
        }
        return str.toString()
    }


}