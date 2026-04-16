package com.example.tsumaps.ui.screens.decisionTreeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.theme.TsuLightBlue

@Composable
fun PredictionForm(viewModel: DecisionTreeViewModel) {
    val features = listOf("location", "budget", "time_available",
        "food_type", "queue_tolerance", "weather" )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TsuLightBlue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ваша ситуация:", style = MaterialTheme.typography.titleLarge)

            features.forEach { feature ->
                OutlinedTextField(
                    value = viewModel.userSelections[feature] ?: "",
                    onValueChange = { viewModel.userSelections[feature] = it},
                    label = {Text(feature)},
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {viewModel.makePrediction()},
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TsuBlue)
            ) {
                Text("Куда мне пойти?")
            }
        }
    }
}