package org.d3ifcool.wayantiara.automaticfan.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.d3ifcool.wayantiara.automaticfan.R
import org.d3ifcool.wayantiara.automaticfan.controller.DevicesAdapter
import org.d3ifcool.wayantiara.automaticfan.databinding.SelectdevicesMainBinding
import org.d3ifcool.wayantiara.automaticfan.model.DevicesModel

// kelas untuk memilih perangkat arduino yang menggunakan blutut

class SelectDevicesActivity : AppCompatActivity() {
    private lateinit var binding: SelectdevicesMainBinding
    private val bluetoothPermission = Manifest.permission.BLUETOOTH
    private val bluetoothAdminPermission = Manifest.permission.BLUETOOTH_ADMIN
    private val bluetoothConnectPermission = Manifest.permission.BLUETOOTH_CONNECT
    private val bluetoothScanPermission = Manifest.permission.BLUETOOTH_SCAN
    private val requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectdevicesMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.title = getString(R.string.title_daftar_perangkat)

        if (hasBluetoothPermissions()) {
            checkBluetooth()
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        // Periksa izin di Android 12 dan lebih tinggi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, bluetoothPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, bluetoothAdminPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, bluetoothConnectPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, bluetoothScanPermission) == PackageManager.PERMISSION_GRANTED
        } else {
            // Untuk Android 11 dan yang lebih lama, akan menggunakan ShouldShowRequestPermissionRationale
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, bluetoothPermission) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, bluetoothAdminPermission)

            // Kembalikan nilai true jika izin diberikan, atau jika kita harus menampilkan dialog alasan khusus
            return shouldShowRationale || (
                    ContextCompat.checkSelfPermission(this, bluetoothPermission) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, bluetoothAdminPermission) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, bluetoothConnectPermission) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, bluetoothScanPermission) == PackageManager.PERMISSION_GRANTED
                    )
        }
    }

    private fun requestBluetoothPermissions() {
        // Minta izin langsung di Android 12 dan lebih tinggi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(bluetoothPermission, bluetoothAdminPermission, bluetoothConnectPermission, bluetoothScanPermission),
                requestCode
            )
        } else {
            // Untuk Android 11 dan yang lebih lama, dapat menampilkan dialog alasan khusus jika diperlukan
            // lalu meminta izin.
            // Tampilkan dialog alasan khusus di sini jika `shouldShowRationale` benar.
            // Setelah menampilkan dialog, dapat meminta izin saat pengguna berinteraksi dengannya.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(bluetoothPermission, bluetoothAdminPermission),
                requestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.requestCode -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    checkBluetooth()
                } else {
                    showPermissionDeniedSnackbar()
                }
            }
        }
    }

    private fun showPermissionDeniedSnackbar() {
        val snackbar =
            Snackbar.make(
                this.findViewById(android.R.id.content),
                "Izinkan Bluetooth agar aplikasi ini dapat mengendalikan kipas anginmu!.",
                Snackbar.LENGTH_LONG
            )
        snackbar.setAction("IZINKAN BLUETOOTH") {
            goToSettings()
        }
        snackbar.show()
    }

    private fun goToSettings() {
        val myAppSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${this.packageName}")
        )
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
        myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivityForResult(myAppSettings, requestCode)
    }

    private fun checkBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            showBluetoothNotSupportedSnackbar()
        } else {
            loadPairedDevices(bluetoothAdapter)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadPairedDevices(bluetoothAdapter: BluetoothAdapter) {
        val deviceList: MutableList<DevicesModel> = ArrayList()

        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                val deviceName = device.name // Bluetooth Name
                val deviceHardwareAddress = device.address // MAC address
                val deviceInfoModel = DevicesModel(deviceName, deviceHardwareAddress)
                deviceList.add(deviceInfoModel)
            }

            val recyclerView = binding.recyclerViewDevice
            recyclerView.layoutManager = LinearLayoutManager(this)
            val deviceListAdapter = DevicesAdapter(this, deviceList)
            recyclerView.adapter = deviceListAdapter
            recyclerView.itemAnimator = DefaultItemAnimator()
        } else {
            showNoPairedDevicesSnackbar()
        }
    }

    private fun showBluetoothNotSupportedSnackbar() {
        val view = binding.recyclerViewDevice
        val snackbar = Snackbar.make(
            view, "Bluetooth tidak mendukung atau tersedia pada perangkat ini.",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("OK") { }
        snackbar.show()
    }

    private fun showNoPairedDevicesSnackbar() {
        val view = binding.recyclerViewDevice
        val snackbar = Snackbar.make(
            view, "Tidak ada perangkat bluetooth yang tersambung dan pastikan Bluetooth aktif.",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("OK") { }
        snackbar.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}