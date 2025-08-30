package com.cloudstorage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private val onDownloadClick: (CloudFile) -> Unit,
    private val onDeleteClick: (CloudFile) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {
    
    private val files = mutableListOf<CloudFile>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    fun updateFiles(newFiles: List<CloudFile>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }
    
    override fun getItemCount(): Int = files.size
    
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
        private val tvFileDate: TextView = itemView.findViewById(R.id.tvFileDate)
        private val btnDownload: MaterialButton = itemView.findViewById(R.id.btnDownload)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        
        fun bind(file: CloudFile) {
            tvFileName.text = file.filename
            tvFileSize.text = formatFileSize(file.size)
            tvFileDate.text = formatDate(file.date)
            
            btnDownload.setOnClickListener {
                onDownloadClick(file)
            }
            
            btnDelete.setOnClickListener {
                onDeleteClick(file)
            }
        }
        
        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
                else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            }
        }
        
        private fun formatDate(dateString: String): String {
            return try {
                // Parse the date string from the API (e.g., "2023-12-01 14:30:00")
                val apiFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = apiFormat.parse(dateString)
                date?.let { dateFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }
    }
}