package com.example.tsumaps.ui.screens.decisionTreeScreen

import androidx.collection.intSetOf
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ResultSection(place: String, path: List<String>){
    Card(
        modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xE8F5E9FF))
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ИТОГ: $place", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF1B5E20))

            Spacer(modifier = Modifier.height(8.dp))
            Text("Логика дерева:", fontWeight = FontWeight.Bold)

            path.forEachIndexed { index, step ->
                Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}