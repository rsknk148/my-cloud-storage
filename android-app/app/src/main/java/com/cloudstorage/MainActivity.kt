package com.cloudstorage

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    
    private lateinit var apiClient: ApiClient
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var fileAdapter: FileAdapter
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnSelectFile: MaterialButton
    private lateinit var btnUpload: MaterialButton
    private lateinit var progressBarUpload: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoFiles: TextView
    
    private var selectedFileUri: Uri? = null
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            val fileName = getFileName(it)
            btnSelectFile.text = fileName ?: "File selected"
            btnUpload.isEnabled = true
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        apiClient = ApiClient(this)
        preferenceManager = PreferenceManager(this)
        
        initViews()
        setupRecyclerView()
        setupClickListeners()
        
        loadFiles()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnUpload = findViewById(R.id.btnUpload)
        progressBarUpload = findViewById(R.id.progressBarUpload)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        recyclerView = findViewById(R.id.recyclerView)
        tvNoFiles = findViewById(R.id.tvNoFiles)
        
        setSupportActionBar(toolbar)
        toolbar.title = "Welcome, ${preferenceManager.username}"
    }
    
    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onDownloadClick = { file -> downloadFile(file) },
            onDeleteClick = { file -> showDeleteConfirmation(file) }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = fileAdapter
    }
    
    private fun setupClickListeners() {
        btnSelectFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }
        
        btnUpload.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadFile(uri)
            }
        }
        
        swipeRefresh.setOnRefreshListener {
            loadFiles()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun loadFiles() {
        swipeRefresh.isRefreshing = true
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.getFiles()
                }
                
                if (result.isSuccess) {
                    val files = result.getOrNull() ?: emptyList()
                    fileAdapter.updateFiles(files)
                    
                    if (files.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        tvNoFiles.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        tvNoFiles.visibility = View.GONE
                    }
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Failed to load files")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Failed to load files")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun uploadFile(uri: Uri) {
        btnUpload.isEnabled = false
        progressBarUpload.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val tempFile = createTempFileFromUri(uri)
                    apiClient.uploadFile(tempFile)
                }
                
                if (result.isSuccess) {
                    selectedFileUri = null
                    btnSelectFile.text = getString(R.string.select_file)
                    btnUpload.isEnabled = false
                    loadFiles()
                    Toast.makeText(this@MainActivity, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Upload failed")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Upload failed")
            } finally {
                btnUpload.isEnabled = false
                progressBarUpload.visibility = View.GONE
            }
        }
    }
    
    private fun downloadFile(file: CloudFile) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.downloadFile(file.filename)
                }
                
                if (result.isSuccess) {
                    val responseBody = result.getOrNull()
                    responseBody?.let {
                        saveFileToDownloads(it, file.filename)
                        Toast.makeText(this@MainActivity, 
                            "File downloaded to Downloads folder", 
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showError("Download failed")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Download failed")
            }
        }
    }
    
    private fun showDeleteConfirmation(file: CloudFile) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete ${file.filename}?")
            .setPositiveButton("Yes") { _, _ ->
                deleteFile(file)
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun deleteFile(file: CloudFile) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.deleteFile(file.filename)
                }
                
                if (result.isSuccess) {
                    loadFiles()
                    Toast.makeText(this@MainActivity, "File deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Delete failed")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Delete failed")
            }
        }
    }
    
    private fun logout() {
        preferenceManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun getFileName(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
    
    private suspend fun createTempFileFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("Unable to open file")
        
        val fileName = getFileName(uri) ?: "temp_file"
        val tempFile = File.createTempFile("upload_", "_$fileName", cacheDir)
        
        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        
        tempFile
    }
    
    private suspend fun saveFileToDownloads(responseBody: ResponseBody, filename: String) = withContext(Dispatchers.IO) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, filename)
        
        responseBody.byteStream().use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }
}