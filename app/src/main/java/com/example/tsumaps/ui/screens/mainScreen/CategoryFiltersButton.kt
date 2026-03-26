package com.example.tsumaps.ui.screens.mainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tsumaps.ui.theme.TsuBlue

@Composable
fun CategoryFilters(){
    val categories = listOf(
        "Где поесть",
        "Коворкинги"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ){
            items(categories) { name ->
                var selected by remember { mutableStateOf(false) }
                FilterChip(
                    onClick = {selected = !selected},
                    label = { Text(name) },
                    selected = selected,
                    shape = RoundedCornerShape(24.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = TsuBlue,
                        selectedLabelColor = Color.White,
                        labelColor = TsuBlue,
                    ),
                    elevation = FilterChipDefaults.filterChipElevation(
                        elevation = 5.dp,
                        pressedElevation = 10.dp
                    ),
                    border  = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Gray,
                        selectedBorderColor = Color.White,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp,
                        enabled = true,
                        selected = selected
                    )
                )}
        }
    }
}