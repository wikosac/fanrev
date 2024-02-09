package org.d3ifcool.wayantiara.automaticfan.history

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel: ViewModel() {

    fun getData(): LiveData<List<History>> {
        val data = MutableLiveData<List<History>>()
        val apiInterface = HistoryApi.ApiClient.create().getHistory()

        apiInterface.enqueue(object : Callback<ResponseHistori> {
            override fun onResponse(
                call: Call<ResponseHistori>,
                response: Response<ResponseHistori>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.result?.isNotEmpty() == true) {
                        data.value = response.body()!!.result
                        Log.d(TAG, "Response = $data")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseHistori>, t: Throwable) {
                Log.e(TAG, "Failed to load data from API", t)
            }
        })
        return data
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}