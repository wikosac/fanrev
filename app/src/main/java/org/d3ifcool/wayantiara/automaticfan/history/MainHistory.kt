package org.d3ifcool.wayantiara.automaticfan.history

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.d3ifcool.wayantiara.automaticfan.R
import org.d3ifcool.wayantiara.automaticfan.databinding.ActivityMainHistoryBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class MainHistory : AppCompatActivity() {

    private lateinit var binding: ActivityMainHistoryBinding
    private lateinit var recyclerAdapter: MainAdapter
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var allHistory: List<History>

    private val viewModel by viewModels<MainViewModel>()

    companion object {
        private const val TAG = "HistoryActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressIndicator = binding.progressIndicator

        recyclerAdapter = MainAdapter(this, object : MainAdapter.Handler {
            override fun check(isEmpty: Boolean) {
                if (isEmpty) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.nointernetView.visibility = View.GONE
                    progressIndicator.hide()
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.nointernetView.visibility = View.GONE
                    progressIndicator.hide()
                }
            }
        })
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
            setHasFixedSize(true)
        }

        viewModel.getData().observe(this) { list ->
            progressIndicator.show()

            Log.d(TAG, "Response data: $list")
            allHistory = list

            recyclerAdapter.setHistoryList(this, list)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun longDateFrom(dateString: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateString, formatter)
        val date = LocalDate.parse(dateTime.toLocalDate().toString())
        return date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun longFromPicker(dateTimeString: String): Long {
        val formatter =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss 'GMT'XXX uuuu", Locale.ENGLISH)
        val dateTime = LocalDateTime.parse(dateTimeString, formatter)
        val date = dateTime.toLocalDate().atStartOfDay() // Set time to midnight (00:00:00)
        return date.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterHistory(history: List<History>, date: Long): List<History> {
        return history.filter { data ->
            val historyDate = longDateFrom(data.waktu)
            historyDate == date
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker?, yearSelected: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, yearSelected)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateLong = longFromPicker(calendar.time.toString())
                val filteredList = filterHistory(allHistory, dateLong)

                recyclerAdapter.setHistoryList(this, filteredList)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pick_date_time -> {
                showDatePicker()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}