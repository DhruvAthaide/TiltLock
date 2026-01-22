package com.dhruvathaide.tiltlock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.SecureFile

class SecureFileAdapter(
    private val onItemClick: (SecureFile) -> Unit,
    private val onDeleteClick: (SecureFile) -> Unit
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
        private val ivIcon: android.widget.ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        private val ivDelete: android.widget.ImageView = itemView.findViewById(R.id.ivDelete)

        fun bind(file: SecureFile) {
            tvName.text = file.name
            
            // Set Icon
            val iconRes = getIconForFile(file.name)
            ivIcon.setImageResource(iconRes)
            
            tvMeta.text = "SECURE • " + getExtensionLabel(file.name)
            
            itemView.setOnClickListener {
                onItemClick(file)
            }
            
            ivDelete.setOnClickListener {
                onDeleteClick(file)
            }
        }
        
        private fun getIconForFile(name: String): Int {
            val lower = name.lowercase()
            return when {
                lower.endsWith(".pdf") -> R.drawable.ic_file_pdf
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") -> R.drawable.ic_file_image
                lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".aac") -> R.drawable.ic_file_audio
                lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi") -> R.drawable.ic_file_video
                lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt") -> R.drawable.ic_file_doc
                else -> R.drawable.ic_file_doc // Default
            }
        }

        private fun getExtensionLabel(name: String): String {
            return if (name.contains(".")) name.substringAfterLast(".").uppercase() else "FILE"
        }
    }
}
