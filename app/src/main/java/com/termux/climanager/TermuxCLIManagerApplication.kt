package com.termux.climanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TermuxCLIManagerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global configurations
        initializeFileSystemAccess()
        initializeCLITools()
    }
    
    private fun initializeFileSystemAccess() {
        // Set up file system access permissions and paths
    }
    
    private fun initializeCLITools() {
        // Initialize CLI tool configurations
    }
}