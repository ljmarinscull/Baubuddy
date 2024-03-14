package com.ljmarinscull.baubuddy.util

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ljmarinscull.baubuddy.data.models.ResourceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

const val EMPTY_STRING_PLACEHOLDER = "---"
const val WHITE_COLOR_HEX = "#FFFFFF"

var View.visible : Boolean
    get() = visibility == View.VISIBLE
    set(value){
        visibility = if (value) View.VISIBLE else View.GONE
    }
fun <T> Fragment.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun <T> ComponentActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun SearchView.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            afterTextChanged.invoke(newText)
            return false
        }
    })
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit){
    this.addTextChangedListener(object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            TODO("Not yet implemented")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            TODO("Not yet implemented")
        }

        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }
    })
}

fun newResources() = listOf(
    ResourceEntity(
        task = "Task 1",
        title = "Title 1",
        description = "New Description 1",
        sort = "0",
        wageType = "WageType 1",
        businessUnitKey = "BusinessUnitKey 1",
        businessUnit = "BusinessUnit 1",
        parentTaskID = "ParentTaskID 1",
        prePlanningBoardQuickSelect = "PrePlanningBoardQuickSelect 1",
        colorCode = "#efef00",
        workingTime = "workingTime 1",
        isAvailableInTimeTrackingKioskMode = true,
    ),
    ResourceEntity(
        task = "Task 2",
        title = "Title 2",
        description = "New Description 2",
        sort = "0",
        wageType = "WageType 2",
        businessUnitKey = "BusinessUnitKey 2",
        businessUnit = "BusinessUnit 2",
        parentTaskID = "ParentTaskID 2",
        prePlanningBoardQuickSelect = "PrePlanningBoardQuickSelect 2",
        colorCode = "#ef00ef",
        workingTime = "workingTime 2",
        isAvailableInTimeTrackingKioskMode = true,
    )
)
