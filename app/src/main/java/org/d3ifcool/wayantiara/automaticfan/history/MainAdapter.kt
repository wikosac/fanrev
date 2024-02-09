package org.d3ifcool.wayantiara.automaticfan.history

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.d3ifcool.wayantiara.automaticfan.R

class MainAdapter(var context: Context) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    var historyList: List<History> = listOf()
    var historyListFiltered: List<History> = listOf()

    fun setHistoryList(context: Context, historyList: List<History>) {
        this.context = context
        if (historyList == null) {
            this.historyList = historyList
            this.historyListFiltered = historyList
            notifyItemChanged(0, historyListFiltered.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return this@MainAdapter.historyList.size
                }

                override fun getNewListSize(): Int {
                    return historyList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return this@MainAdapter.historyList.get(oldItemPosition)
                        .status === historyList[newItemPosition].status
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val newHistory: History = this@MainAdapter.historyList.get(oldItemPosition)
                    val oldHistory: History = historyList[newItemPosition]
                    return newHistory.status === oldHistory.status
                }
            })
            this.historyList = historyList
            this.historyListFiltered = historyList
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.status!!.text = historyListFiltered[position].status

        val history: History = historyListFiltered[position]

        val status: String = history.status
        val waktu: String = history.waktu

        // Bind
        //holder.image!!.setImageURI((Uri.parse(images)))
        holder.status!!.text = status
        holder.waktu!!.text = waktu
    }

    override fun getItemCount(): Int {
        return if (historyList != null) {
            historyListFiltered.size
        } else {
            0
        }
    }

    fun failedGetData(){
        val snackbar =
            Snackbar.make(
                (context as Activity).findViewById(android.R.id.content),
                R.string.failed_load_data_api,
                Snackbar.LENGTH_SHORT
            )
        snackbar.setAction(R.string.Ok) {
            snackbar.dismiss()
        }
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.purple_700))
        snackbar.show()
    }

    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){

        var status: TextView? = null
        var waktu: TextView? = null

        init {
            status = itemView!!.findViewById<View>(R.id.namaTextView) as TextView
            waktu = itemView.findViewById<View>(R.id.latinTextView) as TextView
        }
    }
}