package com.ljmarinscull.baubuddy.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.ljmarinscull.baubuddy.databinding.FragmentLoginBinding
import com.ljmarinscull.baubuddy.util.collectLatestLifecycleFlow
import com.ljmarinscull.baubuddy.util.visible

class LoginFragment : Fragment() {


    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var savedStateHandle: SavedStateHandle

    companion object{
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGIN_SUCCESSFUL] = false

        setupUI()
        observers()
    }

    private fun setupUI() = with(binding) {
        button.setOnClickListener {
            viewModel.onEvent(
                LoginEvent.Login(
                    usernameField.text.toString().trim(),
                    passwordField.text.toString().trim()
                )
            )
        }
    }

    private fun observers() {
        collectLatestLifecycleFlow(viewModel.state){ state ->
            handleLoading(state.isLoading)
        }

        collectLatestLifecycleFlow(viewModel.userLoggedState){ appPreferences ->
            if (appPreferences.authorization.isNotEmpty()){
                savedStateHandle[LOGIN_SUCCESSFUL] = true
                findNavController().popBackStack()
            }
        }

        collectLatestLifecycleFlow(viewModel.errorFlow){ error->
            handleError(error)
        }
    }

    private fun handleError(error: String?) {
        error?.let {
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }
    }

    private fun handleLoading(isLoading: Boolean) = with(binding) {
        loading.visible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}