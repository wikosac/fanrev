package org.d3ifcool.wayantiara.automaticfan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.d3ifcool.wayantiara.automaticfan.databinding.ActivityMainBinding
import org.d3ifcool.wayantiara.automaticfan.history.HistoryApi
import org.d3ifcool.wayantiara.automaticfan.history.MainHistory
import org.d3ifcool.wayantiara.automaticfan.history.ResponseHistori
import org.d3ifcool.wayantiara.automaticfan.ui.SelectDevicesActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var deviceName: String? = null
    private var deviceAddress: String? = null
    private var textSuhu: TextView? = null
    private var textHum: TextView? = null
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {

        // Menghilangkan Title Bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchOtomatis.setOnClickListener { switchUi() }

        // Insialisasi UI
        val buttonConnect = binding.buttonConnect
        val toolbar = binding.toolbar
        val progressBar = binding.progressBar
        progressBar.visibility = View.GONE
        val buttonToggle = binding.buttonToggle
        buttonToggle.isEnabled = false
        textSuhu = binding.suhu
        textHum = binding.hum

        binding.buttonHistori.setOnClickListener {
            val intent = Intent(this, MainHistory::class.java)
            startActivity(intent)
        }

        // Jika perangkat bluetooth telah dipilih dari SelectDeviceActivity
        deviceName = intent.getStringExtra("deviceName")
        if (deviceName != null) {
            // Dapatkan alamat perangkat untuk membuat Koneksi Bluetooth
            deviceAddress = intent.getStringExtra("deviceAddress")
            // Tampilkan progress dan status koneksi
            toolbar.subtitle = "Menghubungkan $deviceName..."
            progressBar.visibility = View.VISIBLE
            buttonConnect.isEnabled = false

            /*
            Ketika "devicesName" ditemukanvkode tersebut akan memanggil thread baru
            untuk membuat koneksi bluetooth ke perangkat yang dipilih.
             */
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            createConnectThread = CreateConnectThread(bluetoothAdapter, deviceAddress)
            createConnectThread!!.start()
        }

        // Pengatur GUI
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    CONNECTING_STATUS -> when (msg.arg1) {
                        1 -> {
                            toolbar.subtitle = "$deviceName Terhubung."
                            progressBar.visibility = View.GONE
                            buttonConnect.isEnabled = true
                            buttonToggle.isEnabled = true
                            isConnected = true
                            binding.buttonConnect.text = "Putuskan BT"
                            binding.switchOtomatis.isEnabled = true
                            autoStartManual()
                        }

                        -1 -> {
                            toolbar.subtitle = "Perangkat gagal terhubung."
                            progressBar.visibility = View.GONE
                            buttonConnect.isEnabled = true
                        }
                    }

                    MESSAGE_READ -> {
                        val arduinoMsg = msg.obj.toString() // Membaca pesan dari Arduino



                        if (arduinoMsg.startsWith("Suhu: ")) {
                            val temperature = arduinoMsg.substringAfter("Suhu: ").trim()
                            textSuhu?.text = "Suhu: $temperature"
                        } else if (arduinoMsg.startsWith("Hum: ")) {
                            val hum = arduinoMsg.substringAfter("Hum: ").trim()
                            textHum?.text = "Kelembapan: $hum"
                        } else if (arduinoMsg.startsWith("Fan turned on")) {
                            startFanAnimation()
                            buttonToggle.text = "Matikan Kipas"

                            // nyalakan tombol jika kipas tidak dihidupkan
                            binding.button1.isEnabled = true
                            binding.button2.isEnabled = true
                            binding.button3.isEnabled = true

                            // nyalakan tombol jika kipas dihidupkan
//                            binding.button1.setBackgroundColor(
//                                ContextCompat.getColor(applicationContext, R.color.colorOn)
//                            )
//                            binding.button2.setBackgroundColor(
//                                ContextCompat.getColor(applicationContext, R.color.colorOn)
//                            )
//                            binding.button3.setBackgroundColor(
//                                ContextCompat.getColor(applicationContext, R.color.colorOn)
//                            )
                        } else if (arduinoMsg.startsWith("Fan turned off")) {
                            stopFanAnimation()
                            buttonToggle.text = "Nyalakan Kipas"

                            // matikan tombol jika kipas tidak dihidupkan
                            binding.button1.isEnabled = false
                            binding.button2.isEnabled = false
                            binding.button3.isEnabled = false

                            // matikan warna tombol
                            binding.button2.setBackgroundColor(Color.GRAY)
                            binding.button1.setBackgroundColor(Color.GRAY)
                            binding.button3.setBackgroundColor(Color.GRAY)
                        }
                    }
                }
            }
        }

        // mengatur UI untuk koneksi blutut
        buttonConnect.setOnClickListener {
            if (!isConnected) { // jika belum konek blutut
                connectBluetooth()
            } else {
                disconnectBluetooth() // jika sudah terkoneksi blutut
            }
        }

        // Tombol untuk Menyalakan/Matikan Kipas pada Arduino Board
        buttonToggle.setOnClickListener {
            var cmdText: String? = null
            val btnState = buttonToggle.text.toString().lowercase(Locale.getDefault())
            when (btnState) {
                "nyalakan kipas" -> {
                    startFanAnimation()
                    buttonToggle.text = "Matikan Kipas"
                    // Perintah untuk menyalakan kipas pada Arduino
                    cmdText = "turn on\n"

                    insertToHistory("Kipas Dinyalakan")

                    // nyalakan tombol jika kipas tidak dihidupkan
                    binding.button1.isEnabled = true
                    binding.button2.isEnabled = true
                    binding.button3.isEnabled = true

                    // nyalakan tombol jika kipas dihidupkan
//                    binding.button1.setBackgroundColor(
//                        ContextCompat.getColor(
//                            this,
//                            R.color.colorOn
//                        )
//                    )
//                    binding.button2.setBackgroundColor(
//                        ContextCompat.getColor(
//                            this,
//                            R.color.colorOn
//                        )
//                    )
//                    binding.button3.setBackgroundColor(
//                        ContextCompat.getColor(
//                            this,
//                            R.color.colorOn
//                        )
//                    )
                }

                "matikan kipas" -> {
                    stopFanAnimation()
                    buttonToggle.text = "Nyalakan Kipas"
                    // Perintah untuk mematikan kipas pada Arduino
                    cmdText = "turn off\n"

                    insertToHistory("Kipas Dimatikan")

                    // matikan tombol jika kipas tidak dihidupkan
                    binding.button1.isEnabled = false
                    binding.button2.isEnabled = false
                    binding.button3.isEnabled = false

                    // matikan warna tombol
                    binding.button2.setBackgroundColor(Color.GRAY)
                    binding.button1.setBackgroundColor(Color.GRAY)
                    binding.button3.setBackgroundColor(Color.GRAY)
                }
            }
            // Mengirimkan perintah ke Arduino Board
            connectedThread!!.write(cmdText)
        }

//        if (connectedThread == null || !connectedThread!!.getIsStatus()) {
            binding.button1.setOnClickListener {
                binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.cream))
                binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                Snackbar.make(binding.root, "Level 1 Dipilih", Snackbar.LENGTH_SHORT).show()

                var cmdText: String? = null
                // Perintah untuk speed pelan
                cmdText = "lvl1\n"
                // Mengirimkan perintah ke Arduino Board
                connectedThread!!.write(cmdText)
            }

            binding.button2.setOnClickListener {
                binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.cream))
                binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                Snackbar.make(binding.root, "Level 2 Dipilih", Snackbar.LENGTH_SHORT).show()

                var cmdText: String? = null
                // Perintah untuk speed sedang
                cmdText = "lvl2\n"
                // Mengirimkan perintah ke Arduino Board
                connectedThread!!.write(cmdText)
            }

            binding.button3.setOnClickListener {
                binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.cream))
                binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
                Snackbar.make(binding.root, "Level 3 Dipilih", Snackbar.LENGTH_SHORT).show()

                var cmdText: String? = null
                // Perintah untuk speed kencang
                cmdText = "lvl3\n"
                // Mengirimkan perintah ke Arduino Board
                connectedThread!!.write(cmdText)
            }
//        } else {
//            Log.d("sendbt", "sedang menunggu pesan silahkan tunggu")
//            // todo give snackbar here
//            Handler().postDelayed({
//                connectedThread!!.setIsStatus(false)
//            }, 2000)
//            // todo give snackbar here
//        }


    }

    private fun autoStartManual() {
        val splashTime: Long = 1000 // 2 detik

        Handler(Looper.getMainLooper()).postDelayed({
            connectedThread!!.write("manual\n")
        }, splashTime)
    }

    // mencatat ke history
    private fun insertToHistory(status: String) {
        val call = HistoryApi.ApiClient.create().createHistory(status)
        call.enqueue(object : Callback<ResponseHistori> {
            override fun onResponse(
                call: Call<ResponseHistori>,
                response: Response<ResponseHistori>,
            ) {
                Log.d("RETRO", "response : " + response.body().toString())
                when (response.body()?.kode) {
                    "1" -> {
                        Log.d("RETRO", "response : " + response.body().toString())
                    }

                    "2" -> {
                        Log.d("RETRO", "response : " + response.body().toString())
                    }

                    else -> {
                        Log.d("RETRO", "response : " + response.body().toString())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseHistori>, t: Throwable) {
                Log.e(TAG, "terjadi kesalahan!", t)
            }
        })
    }

    // konek blutut
    private fun connectBluetooth() {
        val intent = Intent(this, SelectDevicesActivity::class.java)
        startActivity(intent)
    }

    // diskonek blutut
    private fun disconnectBluetooth() {
        connectedThread!!.write("turn off\n")
        createConnectThread?.cancel()
        connectedThread?.cancel()
        binding.toolbar.subtitle = ""
        isConnected = false
        binding.buttonConnect.text = "Hubungkan BT"
        binding.switchOtomatis.isChecked = false
        binding.buttonToggle.visibility = View.VISIBLE
        binding.txtLevel.visibility = View.VISIBLE
        binding.llBtnLvl.visibility = View.VISIBLE
        binding.switchOtomatis.isEnabled = false

        stopFanAnimation()
        binding.buttonToggle.isEnabled = false
        binding.buttonToggle.text = "Nyalakan Kipas"
        textSuhu?.text = "Suhu: "
        textHum?.text = "Kelembapan: "

        // matikan tombol jika kipas tidak dihidupkan
        binding.button1.isEnabled = false
        binding.button2.isEnabled = false
        binding.button3.isEnabled = false

        // matikan warna tombol
        binding.button2.setBackgroundColor(Color.GRAY)
        binding.button1.setBackgroundColor(Color.GRAY)
        binding.button3.setBackgroundColor(Color.GRAY)
    }

    private fun startFanAnimation() {
        // Load the animation
        val rotationAnimation = AnimationUtils.loadAnimation(this, R.anim.fan)

        // Set the animation to the ImageView
        //binding.imageView.startAnimation(rotationAnimation)

        binding.imageView.postDelayed({
            binding.imageView.startAnimation(rotationAnimation)
        }, 2000)
    }

    private fun stopFanAnimation() {
        //binding.imageView.clearAnimation()
        binding.imageView.postDelayed({
            binding.imageView.clearAnimation()
        }, 2000)
    }

    private fun switchUi() {
        if (binding.switchOtomatis.isChecked) {
            with(binding) {
                connectedThread!!.write("otomatis\n")
                insertToHistory("Kipas Otomatis Diaktifkan")
                startFanAnimation()
                buttonToggle.visibility = View.GONE
                txtLevel.visibility = View.GONE
                llBtnLvl.visibility = View.GONE
            }
        } else {
            with(binding) {

                connectedThread!!.write("manual\n")

                Handler(Looper.getMainLooper()).postDelayed({
                    connectedThread!!.write("turn off\n")
                }, 2000)


                insertToHistory("Kipas Manual Diaktifkan")

                buttonToggle.visibility = View.VISIBLE
                txtLevel.visibility = View.VISIBLE
                llBtnLvl.visibility = View.VISIBLE

                stopFanAnimation()
                buttonToggle.text = "nyalakan kipas"

                // matikan tombol jika kipas tidak dihidupkan
                binding.button1.isEnabled = false
                binding.button2.isEnabled = false
                binding.button3.isEnabled = false

                // matikan warna tombol
                binding.button2.setBackgroundColor(Color.GRAY)
                binding.button1.setBackgroundColor(Color.GRAY)
                binding.button3.setBackgroundColor(Color.GRAY)
            }
        }
    }

    /* ============================ Thread untuk Membuat Koneksi Bluetooth =================================== */
    @SuppressLint("MissingPermission")
    class CreateConnectThread(bluetoothAdapter: BluetoothAdapter, address: String?) : Thread() {
        init {
            /*
            Gunakan objek sementara yang kemudian ditetapkan ke mmSocket
            karena mmSocket sudah final.
             */
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            var tmp: BluetoothSocket? = null
            val uuid = bluetoothDevice.uuids[0].uuid
            try {
                /*
                Mendapatkan BluetoothSocket untuk terhubung dengan Perangkat Bluetooth yang diberikan.
                Karena variasi perangkat Android, metode di bawah ini mungkin tidak berfungsi untuk perangkat yang berbeda.
                mungkin harus mencoba menggunakan metode lain yaitu:
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Socket's create() method failed", e)
            }
            mmSocket = tmp
        }

        @SuppressLint("MissingPermission")
        override fun run() {
            // Batalkan pencarian bluetooth karena akan memperlambat koneksi.
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter.cancelDiscovery()
            try {
                // Hubungkan ke perangkat jarak jauh melalui soket. Panggilan ini diblokir
                // sampai berhasil atau memunculkan pengecualian.
                mmSocket!!.connect()
                Log.e("Status", "Device connected")
                handler!!.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget()
            } catch (connectException: IOException) {
                // Tidak dapat terhubung; tutup soket dan kembalikan.
                try {
                    mmSocket!!.close()
                    Log.e("Status", "Cannot connect to device")
                    handler!!.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget()
                } catch (closeException: IOException) {
                    Log.e(ContentValues.TAG, "Could not close the client socket", closeException)
                }
                return
            }

            // Upaya koneksi berhasil. Melakukan pekerjaan yang berhubungan dengan
            // koneksi di thread terpisah.
            connectedThread = ConnectedThread(mmSocket)
            connectedThread!!.run()
        }

        // Menutup soket klien dan menyebabkan thread selesai.
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the client socket", e)
            }
        }
    }

    /* =============================== Thread untuk Data Transfer =========================================== */
    class ConnectedThread(private val mmSocket: BluetoothSocket?) : Thread() {
        private val mmInStream: InputStream?
        private var isStatus = false


        private val mmOutStream: OutputStream?

        private val timer = Timer()

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Dapatkan aliran input dan output, menggunakan objek temp karena
            // aliran anggota bersifat final
            try {
                tmpIn = mmSocket!!.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            checkStatusCommand()
        }

        // Schedular
        private fun checkStatusCommand() {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    isStatus = true
                    write("check status\n")
                }
            }, 0, 5000)
        }

        fun getIsStatus(): Boolean {
            return isStatus
        }

        fun setIsStatus(status: Boolean) {
            isStatus = status;
        }

        private fun stopAutoSendCommand() {
            timer.cancel()
        }

        override fun interrupt() {
            super.interrupt()
            stopAutoSendCommand()
        }

        override fun run() {
            val buffer = ByteArray(1024) // penyimpanan buffer untuk streaming
            var bytes = 0 // byte dikembalikan dari read()
            // Terus dengarkan InputStream hingga terjadi pengecualian
            while (true) {
                try {
                    /*
                    Baca dari InputStream dari Arduino hingga karakter terminasi tercapai.
                    Kemudian kirim seluruh pesan String ke GUI Handler.
                     */
                    buffer[bytes] = mmInStream!!.read().toByte()
                    var readMessage: String
                    if (buffer[bytes] == '\n'.code.toByte()) {
                        readMessage = String(buffer, 0, bytes)
                        Log.e("Arduino Message", readMessage)
                        handler!!.obtainMessage(MESSAGE_READ, readMessage).sendToTarget()
                        bytes = 0
                    } else {
                        bytes++
                    }
                    isStatus = false
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }

        /* Sebut ini dari aktivitas utama untuk mengirim data ke perangkat jarak jauh */
        fun write(input: String?) {
            try {
                val messageBuffer = StringBuilder()
                mmOutStream?.let {
                    // append the input to the buffer
                    messageBuffer.append(input)

                    // check if the buffer contains a complete message (ends with newline)
                    val bufferString = messageBuffer.toString()
                    if (bufferString.endsWith("\n")) {
                        it.write(bufferString.toByteArray())
                        messageBuffer.setLength(0) // clear the buffer after sending
                        isStatus = true
                    }
                }
            } catch (e: IOException) {
                Log.e("Send Error", "Unable to send message", e)
            }
        }

        /* Sebut ini dari aktivitas utama untuk mematikan koneksi */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
            }
        }
    }

    /* ============================ Mengakhiri koneksi pada BackPress ====================== */
    override fun onBackPressed() {
        super.onBackPressed()
        // Mengakhiri koneksi bluetooth dan keluar dari aplikasi
        if (createConnectThread != null) {
            createConnectThread!!.cancel()
        }
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    companion object {
        var handler: Handler? = null
        var mmSocket: BluetoothSocket? = null
        var connectedThread: ConnectedThread? = null
        var createConnectThread: CreateConnectThread? = null

        // digunakan dalam pengendali bluetooth untuk mengidentifikasi status pesan
        private const val CONNECTING_STATUS = 1

        // digunakan dalam pengendali bluetooth untuk mengidentifikasi pembaruan pesan
        private const val MESSAGE_READ = 2
        private const val AUTO_REFRESH_INTERVAL = 1000L
    }
}