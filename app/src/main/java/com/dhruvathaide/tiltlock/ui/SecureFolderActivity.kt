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
    private lateinit var recyclerView: RecyclerView
    private var isGridView = true // Default to Grid
    private lateinit var prefs: android.content.SharedPreferences

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_folder)

        prefs = getSharedPreferences("secure_prefs", MODE_PRIVATE)
        isGridView = prefs.getBoolean("is_grid_view", true)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // Add View Toggle Icon programmatically or via Menu
        // Let's rely on Menu inflation, but we need to ensure the menu XML has the item.
        // Or simpler: Add a button in Toolbar programmatically for now if Menu XML is not easily editable here (it is, but let's check).
        // Actually, let's assume valid menu resource exists or we can add it.
        // Better: Add the menu item in code to be safe if we don't want to edit menu XML.
        toolbar.inflateMenu(R.menu.menu_secure_folder)
        
        // Update icon based on state (since XML defaults to something, we sync it)
        val toggleItem = toolbar.menu.findItem(R.id.action_toggle_view)
        if (toggleItem != null) {
             updateToggleIcon(toggleItem)
        }

        toolbar.setOnMenuItemClickListener { handleMenu(it) }
        toolbar.setNavigationOnClickListener { finish() } // Back button

        emptyState = findViewById(R.id.emptyState)
        setupRecyclerView()
        
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
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
        recyclerView = findViewById(R.id.recyclerView)
        adapter = SecureFileAdapter(
            onItemClick = { file -> openFile(file) },
            onDeleteClick = { file -> confirmDelete(file) }
        )
        updateLayoutManager()
        recyclerView.adapter = adapter
    }
    
    private fun confirmDelete(file: SecureFile) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete File?")
            .setMessage("Are you sure you want to delete '${file.name}'? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFile(file)
                Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateLayoutManager() {
        if (isGridView) {
            recyclerView.layoutManager = GridLayoutManager(this, 2)
        } else {
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        }
        // Force re-bind to update layout params if needed? 
        // Usually Adapter handles it, but CardView margin might layout differently.
        adapter.notifyDataSetChanged()
    }
    
    private fun toggleViewMode() {
        isGridView = !isGridView
        prefs.edit().putBoolean("is_grid_view", isGridView).apply()
        updateLayoutManager()
        
        // Update Icon
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val item = toolbar.menu.findItem(R.id.action_toggle_view)
        if (item != null) updateToggleIcon(item)
    }

    private fun updateToggleIcon(item: MenuItem) {
        if (isGridView) {
            item.setIcon(R.drawable.ic_view_list) // Show List icon to switch TO List
            item.title = "List View"
        } else {
            item.setIcon(R.drawable.ic_view_module) // Show Grid icon to switch TO Grid
            item.title = "Grid View"
        }
    }

    private fun openFile(secureFile: SecureFile) {
        lifecycleScope.launch {
            try {
                // Decrypt to cache
                val decryptedFile = viewModel.decryptFile(secureFile.file)
                
                // Open with Intent
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
        val ext = file.extension.lowercase()
        val mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        return mime ?: "*/*"
    }

    private fun handleMenu(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_view -> {
                toggleViewMode()
                true
            }
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
