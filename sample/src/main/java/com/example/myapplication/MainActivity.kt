package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.github.hylkeb.retrocache.state.RequestState

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)) {
                    MainScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    Column {
        Button(onClick = viewModel::fetchHolidays) {
            Text(text = "Fetch holidays")
        }
        LazyColumn {
            items(viewModel.holidays) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)) {
                    Text(text = "${it.date}: ${it.name} (${it.localName})")
                }
            }
        }

        Text(text = "Cached variant")
        Button(onClick = viewModel::refreshCachedHolidays) {
            Text(text = "Refresh holidays")
        }
        val holidayState = viewModel.holidayState.collectAsState().value
        when (holidayState) {
            is RequestState.Data.RefreshFailed -> { Text("Refresh failed") }
            is RequestState.Fetching,
            is RequestState.Data.Refreshing -> { CircularProgressIndicator() }
            is RequestState.Error -> { Text("Fetch failed") }
            else -> {}
        }
        if (holidayState is RequestState.Data) {
            holidayState.dateTimeMillis
            Text(text = "Date of cached response: ${java.lang.String.format("%1\$TH:%1\$TM:%1\$TS", holidayState.dateTimeMillis)}")
            Text(text = "Date of cached response: ${holidayState.dateTimeMillis}")
            Text(text = "From cache?: ${holidayState.fromCache}")
            LazyColumn {
                items(holidayState.result) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)) {
                        Text(text = "${it.date}: ${it.name} (${it.localName})")
                    }
                }
            }
        }
    }
}