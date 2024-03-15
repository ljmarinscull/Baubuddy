package com.ljmarinscull.baubuddy.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.ljmarinscull.baubuddy.data.datasource.local.AppDatabase
import com.ljmarinscull.baubuddy.data.datasource.local.LocalDataSource
import com.ljmarinscull.baubuddy.data.datasource.local.PreferencesRepository
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSource
import com.ljmarinscull.baubuddy.data.datasource.remote.RetrofitClient
import com.ljmarinscull.baubuddy.dataStore
import com.ljmarinscull.baubuddy.databinding.ActivityMainBinding
import com.ljmarinscull.baubuddy.domain.repository.Repository
import com.ljmarinscull.baubuddy.ui.home.HomeViewModel
import com.ljmarinscull.baubuddy.ui.home.HomeViewModelFactory
import com.ljmarinscull.baubuddy.ui.login.LoginViewModel
import com.ljmarinscull.baubuddy.ui.login.LoginViewModelFactory
import com.ljmarinscull.baubuddy.ui.worker.RefreshResourcesWorkerManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(
                Repository(
                    remoteDataSource = RemoteDataSource(api = RetrofitClient.getInstance()),
                    localDataSource = LocalDataSource(
                        AppDatabase.getInstance(applicationContext).resourcesDao()
                    )
                ),
                PreferencesRepository(dataStore)
            )
        ).get(LoginViewModel::class.java)

        homeViewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(
                Repository(
                    remoteDataSource = RemoteDataSource(api = RetrofitClient.getInstance()),
                    localDataSource = LocalDataSource(
                        AppDatabase.getInstance(applicationContext).resourcesDao()
                    )
                ),
                PreferencesRepository(dataStore)
            )
        ).get(HomeViewModel::class.java)

        loginViewModel.userLogged.observe(this) {
            if (it.authorization.isNotEmpty())
                RefreshResourcesWorkerManager(
                    WorkManager.getInstance(applicationContext)
                ).enqueueServicesUpdate()
        }
    }
}