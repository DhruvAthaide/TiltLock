package com.dhruvathaide.tiltlock.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.SecureFile
import com.dhruvathaide.tiltlock.data.SecureFileRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File

class SecureFolderActivity : AppCompatActivity() {

    private lateinit var viewModel: SecureFolderViewModel
    private lateinit var adapter: SecureFileAdapter
    private lateinit var emptyState: TextView

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_folder)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_secure_folder)
        toolbar.setOnMenuItemClickListener { handleMenu(it) }

        emptyState = findViewById(R.id.emptyState)
        setupRecyclerView()
        
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            // Open File Picker
            importLauncher.launch(arrayOf("*/*"))
        }

        val repo = SecureFileRepository(this)
        val factory = SecureFolderViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[SecureFolderViewModel::class.java]

        viewModel.files.observe(this) { files ->
            adapter.submitList(files)
            emptyState.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.loadFiles()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = SecureFileAdapter { file -> openFile(file) }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    private fun openFile(secureFile: SecureFile) {
        lifecycleScope.launch {
            try {
                // Decrypt to cache
                val decryptedFile = viewModel.decryptFile(secureFile.file)
                
                // Open with Intent
                // Note: Need FileProvider setup in Manifest for this to work on API 24+
                val uri = FileProvider.getUriForFile(
                    this@SecureFolderActivity,
                    "com.dhruvathaide.tiltlock.fileprovider",
                    decryptedFile
                )
                
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, getMimeType(decryptedFile))
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@SecureFolderActivity, "Error opening file", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun getMimeType(file: File): String {
        return when (file.extension) {
            "jpg", "png", "jpeg" -> "image/*"
            "pdf" -> "application/pdf"
            "mp4" -> "video/*"
            else -> "*/*"
        }
    }

    private fun handleMenu(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_gesture -> {
                startActivity(Intent(this, GestureSetupActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> false
        }
    }
}
