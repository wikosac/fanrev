package org.d3ifcool.wayantiara.automaticfan.history

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.d3ifcool.wayantiara.automaticfan.databinding.ActivityMainHistoryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainHistory: AppCompatActivity() {
    private lateinit var binding: ActivityMainHistoryBinding
    lateinit var recyclerView: RecyclerView
    lateinit var recyclerAdapter: MainAdapter
    private var historyList: MutableList<History> = ArrayList()
    lateinit var progressIndicator: LinearProgressIndicator

    companion object {
        private const val TAG = "HistoryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressIndicator = binding.progressIndicator

        recyclerAdapter = MainAdapter(this)
        with(binding.recyclerView){
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
            setHasFixedSize(true)
        }

        getDataFromAPI()
    }

    private fun getDataFromAPI(){
        progressIndicator.show()
        val apiInterface = HistoryApi.ApiClient.create().getHistory()

        apiInterface.enqueue(object : Callback<ResponseHistori> {
            override fun onResponse(call: Call<ResponseHistori>, response: Response<ResponseHistori>) {
                if (response.isSuccessful) {
                    if(response.body()?.result?.isEmpty() == true) { // if data null
                        binding.emptyView.visibility = View.VISIBLE
                        binding.nointernetView.visibility = View.GONE
                        progressIndicator.hide()
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.nointernetView.visibility = View.GONE
                        progressIndicator.hide()

                        historyList = (response.body()?.result ?: ArrayList()) as MutableList<History>
                        Log.d("TAG", "Response = $historyList")
                        recyclerAdapter.setHistoryList(applicationContext, historyList)
                    }
                } else {
                    recyclerAdapter.failedGetData()
                    binding.nointernetView.visibility = View.VISIBLE
                    progressIndicator.hide()
                }
            }

            override fun onFailure(call: Call<ResponseHistori>, t: Throwable) {
                Log.e(TAG, "Failed to load data from API", t)
                recyclerAdapter.failedGetData()
                binding.nointernetView.visibility = View.VISIBLE
                progressIndicator.hide()
            }
        })
    }
}