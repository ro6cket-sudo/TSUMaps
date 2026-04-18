package com.example.tsumaps.ui.screens.decisionTreeScreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Slider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.registerForAllProfilingResults
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.viewmodels.DecisionTreeViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun CsvInputSection(viewModel: DecisionTreeViewModel) {

    val context = LocalContext.current
    val getFileLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val text = reader.readText()
                    reader.close()
                    viewModel.csvText = text
                    Toast.makeText(context, "Файл успешно загружен", Toast.LENGTH_SHORT).show()
                } catch (error: Exception) {
                    Toast.makeText(
                        context,
                        "Ошибка чтения файла: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = viewModel.csvText,
                onValueChange = { viewModel.csvText = it },
                label = { Text("Вставьте CSV данные.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((150.dp)),
                textStyle = TextStyle(fontSize = 12.sp)
            )

            Button(
                onClick = {
                    getFileLauncher.launch("*/*")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Выбрать CSV файл с устройства.")
            }

            Text(
                "Макс. глубина дерева: ${viewModel.maxDepth.toInt()}",
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = viewModel.maxDepth,
                onValueChange = { viewModel.maxDepth = it },
                valueRange = 1f..10f,
                steps = 9

            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewModel.isOptimized,
                    onCheckedChange = { viewModel.isOptimized = it }
                )
                Text("Оптимизировать дерево (Бонус).")
            }

            Button(
                onClick = { viewModel.buildingTree() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TsuBlue)
            ) {
                Text("Построить дерево.")
            }
        }
    }
}