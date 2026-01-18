package com.dhruvathaide.tiltlock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.SecureFile

class SecureFileAdapter(
    private val onItemClick: (SecureFile) -> Unit
) : RecyclerView.Adapter<SecureFileAdapter.FileViewHolder>() {

    private var files: List<SecureFile> = emptyList()

    fun submitList(newFiles: List<SecureFile>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_secure_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.fileName)

        fun bind(file: SecureFile) {
            // Decrypt name if we encrypted filename too? No, filename is random/hashed usually. 
            // Repository returns UUID name. For better UX, we could store metadata.
            // For now, prompt implies "import ... and secure".
            // Let's just show "Secure File ${index}" or the stored name.
            tvName.text = "SECURE_DATA_" + file.name.take(8)
            
            itemView.setOnClickListener {
                onItemClick(file)
            }
        }
    }
}
