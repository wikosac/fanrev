package org.d3ifcool.wayantiara.automaticfan.ui

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.d3ifcool.wayantiara.automaticfan.controller.DevicesAdapter
import org.d3ifcool.wayantiara.automaticfan.databinding.SelectdevicesMainBinding
import org.d3ifcool.wayantiara.automaticfan.model.DevicesModel

// kelas untuk memilih perangkat arduino yang menggunakan blutut

class SelectDevicesActivityV1 : AppCompatActivity() {
    private lateinit var binding: SelectdevicesMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectdevicesMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            // Bluetooth tidak mendukung / tidak tersedia
            val view = binding.recyclerViewDevice
            val snackbar = Snackbar.make(
                view, "Bluetooth tidak mendukung atau tersedia pada perangkat ini.",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction("OK") { }
            snackbar.show()
        } else {
            // Melakukan operasi pada bluetooth yang dipilih
            val pairedDevices = if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
              else
            {
                bluetoothAdapter.bondedDevices
            }
            val deviceList: MutableList<DevicesModel> = ArrayList()

            if (pairedDevices.isNotEmpty()) {
                // Jika terhubung, akan menampilkan nama dan alamat dari masing-masing perangkat
                for (device in pairedDevices) {
                    val deviceName = device.name // Bluetooth Name
                    val deviceHardwareAddress = device.address // MAC address
                    val deviceInfoModel = DevicesModel(deviceName, deviceHardwareAddress)
                    deviceList.add(deviceInfoModel)
                }
                // Menampilkan perangkat yang terhubung menggunakan RecyclerView
                val recyclerView = binding.recyclerViewDevice
                recyclerView.layoutManager = LinearLayoutManager(this)
                val deviceListAdapter = DevicesAdapter(this, deviceList)
                recyclerView.adapter = deviceListAdapter
                recyclerView.itemAnimator = DefaultItemAnimator()
            } else {
                val view = binding.recyclerViewDevice
                val snackbar = Snackbar.make(
                    view, "Tidak ada perangkat bluetooth yang tersambung.",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction("OK") { }
                snackbar.show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}