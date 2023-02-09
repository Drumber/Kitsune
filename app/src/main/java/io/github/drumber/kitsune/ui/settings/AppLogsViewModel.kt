package io.github.drumber.kitsune.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.util.LogCatReader
import kotlinx.coroutines.launch

class AppLogsViewModel : ViewModel() {

    private var _logMessages = MutableLiveData<String>()
    val logMessages: LiveData<String>
        get() = _logMessages

    init {
        viewModelScope.launch {
            val logCatMessages = LogCatReader.readAppLogs()
            _logMessages.value = logCatMessages.joinToString("\n")
        }
    }

}