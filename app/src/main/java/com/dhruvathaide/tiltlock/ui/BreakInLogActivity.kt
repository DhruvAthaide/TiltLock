package com.dhruvathaide.tiltlock.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.AccessLogRepository
import com.dhruvathaide.tiltlock.data.LogEntry
import com.dhruvathaide.tiltlock.data.LogType
import java.text.SimpleDateFormat
import java.util.Locale

class BreakInLogActivity : AppCompatActivity() {

    private lateinit var repository: AccessLogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_in_log)

        repository = AccessLogRepository(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLogs)
        val emptyState = findViewById<TextView>(R.id.emptyLogState)

        val logs = repository.getLogs()
        if (logs.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = LogAdapter(logs)
        }
    }

    private class LogAdapter(private val logs: List<LogEntry>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val iconType: ImageView = view.findViewById(R.id.iconType)
            val tvType: TextView = view.findViewById(R.id.tvType)
            val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_entry, parent, false)
            return LogViewHolder(view)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            val log = logs[position]
            holder.tvTimestamp.text = dateFormat.format(log.timestamp)

            if (log.type == LogType.SUCCESS) {
                holder.tvType.text = "ACCESS GRANTED"
                holder.tvType.setTextColor(Color.parseColor("#00FFEA")) // neon_cyan
                holder.iconType.setColorFilter(Color.parseColor("#00FFEA"))
            } else {
                holder.tvType.text = "ACCESS DENIED"
                holder.tvType.setTextColor(Color.parseColor("#FF0033")) // error_red
                holder.iconType.setColorFilter(Color.parseColor("#FF0033"))
            }
        }

        override fun getItemCount() = logs.size
    }
}
