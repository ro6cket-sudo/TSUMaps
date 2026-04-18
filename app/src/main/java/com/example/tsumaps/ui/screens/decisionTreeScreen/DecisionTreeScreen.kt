package com.example.tsumaps.ui.screens.decisionTreeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.viewmodels.DecisionTreeViewModel

@Composable
fun DecisionTreeScreen(viewModel: DecisionTreeViewModel = viewModel()) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            "ТГУ: Где сегодня трапезничаем?",
            style = MaterialTheme.typography.headlineMedium,
            color = TsuBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        CsvInputSection(viewModel)

        viewModel.errorMassage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        viewModel.tree?.root?.let { rootNode ->
            Text("Структура дерева решений:", style = MaterialTheme.typography.titleMedium)
            TreeVisualizer(rootNode)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.tree != null) {
            PredictionForm(viewModel)
        }

        viewModel.predictionResult?.let { (place, path) ->
            ResultSection(place, path)
        }
    }
}