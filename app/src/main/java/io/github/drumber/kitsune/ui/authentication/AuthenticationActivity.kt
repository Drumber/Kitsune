package io.github.drumber.kitsune.ui.authentication

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.textfield.TextInputLayout
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ActivityAuthenticationBinding
import io.github.drumber.kitsune.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationActivity : BaseActivity(R.layout.activity_authentication, false) {

    private val viewModel: LoginViewModel by viewModel()

    private val binding: ActivityAuthenticationBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = binding.fieldUsername
        val password = binding.fieldPassword
        val login = binding.btnLogin

        viewModel.loginFormState.observe(this) { loginFormState ->
            val loginState = loginFormState ?: return@observe

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            username.error = loginState.usernameError?.let { getString(it) }
            password.error = loginState.passwordError?.let { getString(it) }
        }

        viewModel.loginResult.observe(this) {
            val loginResult = it ?: return@observe

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)

                setResult(Activity.RESULT_OK)

                // Start new clean main activity and destroy login activity once successful
                startNewMainActivity()
                finish()
            }
        }

        viewModel.isLoggingIn.observe(this) {
            binding.layoutLoading.isVisible = it
        }

        username.afterTextChanged {
            viewModel.loginDataChanged(
                username.text(),
                password.text()
            )
        }

        password.apply {
            afterTextChanged {
                viewModel.loginDataChanged(
                    username.text(),
                    password.text()
                )
            }

            editText?.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        viewModel.login(
                            username.text(),
                            password.text()
                        )
                }
                false
            }

            login.setOnClickListener {
                viewModel.login(username.text(), password.text())
            }
        }

        binding.apply {
            tvCreateAccount.movementMethod = LinkMovementMethod.getInstance()

            btnBack.setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val displayName = model.displayName
        Toast.makeText(
            applicationContext,
            getString(R.string.logged_in_success, displayName),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        binding.fieldPassword.error = getString(errorString)
        Toast.makeText(
            applicationContext,
            errorString,
            Toast.LENGTH_SHORT
        ).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun TextInputLayout.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.editText?.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun TextInputLayout.text(): String = this.editText!!.text.toString()