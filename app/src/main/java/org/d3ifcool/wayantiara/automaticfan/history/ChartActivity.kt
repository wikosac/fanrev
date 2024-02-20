package org.d3ifcool.wayantiara.automaticfan.history

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import org.d3ifcool.wayantiara.automaticfan.R
import org.d3ifcool.wayantiara.automaticfan.history.MainHistory.Companion.filterHistory
import org.d3ifcool.wayantiara.automaticfan.history.MainHistory.Companion.longFromPicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChartActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        viewModel.getData().observe(this) {
            setUpChart(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpChart(historyList: List<History>) {
        val lineChart: LineChart = findViewById(R.id.lineChart)

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())

        // Calculate the days of the last week
        val weekdays = ArrayList<String>()
        val data = ArrayList<Int>()

        for (i in 0..6) {
            Log.d(TAG, "calendarTime: ${calendar.time}")
            weekdays.add(sdf.format(calendar.time))

            val millis = longFromPicker(calendar.time.toString())
            val filtered = filterHistory(historyList, millis)
            data.add(filtered.size)

            calendar.add(Calendar.DAY_OF_YEAR, -1) // Move back one day
        }
        weekdays.reverse() // Reverse to get the days in order
        data.reverse()
        Log.d(TAG, "weekdays: $weekdays")
        Log.d(TAG, "data: $data")

        val entries: ArrayList<Entry> = ArrayList()
        for (i in data.indices) {
            entries.add(Entry(i.toFloat(), data[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, "Weekday Data")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK

        val lineData = LineData(dataSet)

        lineChart.data = lineData

        // Customizing X axis labels to display weekdays
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return weekdays[value.toInt()]
            }
        }

        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f
        lineChart.xAxis.labelCount = weekdays.size
        lineChart.xAxis.isGranularityEnabled = true
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisRight.axisMinimum = 0f

        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.invalidate()
    }

    companion object {
        private const val TAG = "ChartTest"
    }
}