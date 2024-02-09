package org.d3ifcool.wayantiara.automaticfan.controller

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.d3ifcool.wayantiara.automaticfan.MainActivity
import org.d3ifcool.wayantiara.automaticfan.R
import org.d3ifcool.wayantiara.automaticfan.model.DevicesModel

class DevicesAdapter(private val context: Context, private val deviceList: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var textName: TextView
        var textAddress: TextView
        var linearLayout: LinearLayout

        init {
            textName = v.findViewById(R.id.textViewDeviceName)
            textAddress = v.findViewById(R.id.textViewDeviceAddress)
            linearLayout = v.findViewById(R.id.linearLayoutDeviceInfo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.devices_main, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ViewHolder
        val (getDevicesName, getDevicesHardwareAddress) = deviceList[position] as DevicesModel
        itemHolder.textName.text = getDevicesName
        itemHolder.textAddress.text = getDevicesHardwareAddress

        // ketika perangkat dipilih
        itemHolder.linearLayout.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            // mengirimkan informasi perangkat ke MainActivity
            intent.putExtra("deviceName", getDevicesName)
            intent.putExtra("deviceAddress", getDevicesHardwareAddress)
            // memanggil MainActivity
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }
}