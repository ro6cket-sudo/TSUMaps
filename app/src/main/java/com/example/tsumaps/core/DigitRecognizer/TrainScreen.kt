package com.example.tsumaps.core.DigitRecognizer

import android.widget.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import kotlinx.coroutines.launch

@Composable
public fun TrainingScreen(){
    val context = LocalContext.current
    val coroutineSpace = rememberCoroutineScope()
    var isTraining by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()){
        Button(
            onClick = {
                if (!isTraining){
                    isTraining = true
                    coroutineSpace.launch {
                        coach(context)
                        isTraining = false
                    }
                }
            },
            enabled = !isTraining
        ) {
            Text(if (isTraining) "Идет обучение" else "Запустить обучение")
        }
    }
}