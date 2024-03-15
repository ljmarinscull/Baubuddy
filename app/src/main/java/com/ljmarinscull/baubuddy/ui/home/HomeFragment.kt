package com.ljmarinscull.baubuddy.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ljmarinscull.baubuddy.R
import com.ljmarinscull.baubuddy.databinding.FragmentHomeBinding
import com.ljmarinscull.baubuddy.domain.models.Resource
import com.ljmarinscull.baubuddy.ui.login.LoginFragment
import com.ljmarinscull.baubuddy.ui.login.LoginViewModel
import com.ljmarinscull.baubuddy.ui.scanner.QrScannerActivity
import com.ljmarinscull.baubuddy.ui.scanner.RESULT
import com.ljmarinscull.baubuddy.util.afterTextChanged
import com.ljmarinscull.baubuddy.util.collectLatestLifecycleFlow

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var _adapter: RemoteResourceAdapter

    private val viewModel: HomeViewModel by activityViewModels<HomeViewModel>()
    private val loginViewModel: LoginViewModel by activityViewModels<LoginViewModel>()

    private var searchView: SearchView? = null

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                it.getStringExtra(RESULT)?.let { query ->
                    setQueryOnSearchView(query)
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                launchQrScanner()
            } else {
                Toast.makeText(requireContext(),"The app needs this permission to lauch the Qr scanner.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController()

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry){ success->
                if (!success){
                    val startDestination = navController.graph.startDestinationId
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination,true)
                        .build()
                    navController.navigate(startDestination,null,navOptions)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.userLogged.observe(viewLifecycleOwner) { preferences ->
            if (preferences.authorization.isNotEmpty()) {
                viewModel.updateValidAuthorization(true)
                setupMenu()
                initSwipeRefreshLayout()
                initRecyclerView()
                observers()
            } else {
                findNavController().navigate(R.id.navigation_login)
            }
        }
    }
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if(!menu.hasVisibleItems()) {
                    menuInflater.inflate(R.menu.app_menu, menu)

                val menuItem = menu.findItem(R.id.action_search)
                searchView = menuItem.actionView as SearchView?
                searchView?.queryHint = getString(R.string.search_hint)
                viewModel.state.value.query?.let { query ->
                    updateSearchView(query)
                }
                searchView?.afterTextChanged {
                    viewModel.filterByQuery(it)
                }}
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_qr -> {
                        onQrMenuItemTap()
                        true
                    }
                    else -> true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initSwipeRefreshLayout() = with(binding.swipeRefreshLayout) {
        setOnRefreshListener { viewModel.onRefresh() }
    }

    private fun initRecyclerView() = with(binding.recyclerView) {
        addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        layoutManager = LinearLayoutManager(requireContext())
        _adapter = RemoteResourceAdapter()
        adapter = _adapter
    }

    private fun observers() {
        collectLatestLifecycleFlow(viewModel.state) { state ->
            handleResources(state.resources)
            handleRefreshing(state.isRefreshing)
            handleAuthorization(state.validAuthorization)
        }
        collectLatestLifecycleFlow(viewModel.errorFlow){ error->
            handleError(error)
        }
    }

    private fun handleAuthorization(validAuthorization: Boolean) {
        if (!validAuthorization){
            findNavController().navigate(R.id.navigation_login)
        }
    }

    private fun handleRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    private fun handleResources(newResources: List<Resource>) {
        _adapter.addItems(newResources)
    }

    private fun handleError(error: String?) {
        error?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun setQueryOnSearchView(query: String){
        searchView?.isIconified = false
        viewModel.setQuery(query)
    }

    private fun updateSearchView(query: String, submit: Boolean = false){
        searchView?.isIconified = submit
        searchView?.setQuery(query, submit)
    }

    private fun onQrMenuItemTap() {
        val hasPermission = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if(hasPermission){
            launchQrScanner()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchQrScanner(){
        val intent = Intent(requireContext(), QrScannerActivity::class.java)
        launcher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }
}