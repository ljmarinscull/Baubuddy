package com.ljmarinscull.baubuddy

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.ljmarinscull.baubuddy.data.datasource.local.LocalDataSource
import com.ljmarinscull.baubuddy.data.datasource.local.PreferencesRepository
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSource
import com.ljmarinscull.baubuddy.data.datasource.remote.RetrofitClient
import com.ljmarinscull.baubuddy.databinding.ActivityMainBinding
import com.ljmarinscull.baubuddy.domain.repository.Repository
import com.ljmarinscull.baubuddy.ui.login.LoginViewModel
import com.ljmarinscull.baubuddy.ui.login.LoginViewModelFactory

private const val USER_PREFERENCES_NAME = "app_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel

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
                        AppDatabase.getInstance(this).resourcesDao()
                    )
                ),
                PreferencesRepository(dataStore)
            )
        ).get(LoginViewModel::class.java)
    }
}