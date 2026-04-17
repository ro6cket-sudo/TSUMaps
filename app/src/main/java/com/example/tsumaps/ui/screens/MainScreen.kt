package com.example.tsumaps.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.TsuMapScreen
import com.example.tsumaps.core.FoodItem
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.PlaceType
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.cluster.ClusterMetricType
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.viewmodels.MapViewModel

enum class SheetMode { PATHFINDING, CLUSTERING, GENETIC }

val clusterColors = listOf(
    Color.Red, Color.Yellow, Color.Green,
    Color.Magenta, Color(0xFFFFA500), Color.Cyan,
    Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFF8BC34A), Color(0xFFFF5722)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val sheetState = rememberBottomSheetScaffoldState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedMode by remember { mutableStateOf<SheetMode?>(null) }

    androidx.compose.runtime.LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContent = {
            BottomSheetContent(
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it },
                onBuildPathClick = { viewModel.onBuildPathClick() },
                isSearching = viewModel.isSearching,
                onSelectionModeClick = { viewModel.toggleSelectionMode() },
                onObstacleClick = { viewModel.toggleObstacleMode() },
                onClearObstaclesClick = { viewModel.clearObstacles() },
                isClusteringActive = viewModel.isClusteringActive,
                onClusteringClick = { viewModel.toggleClustering() },
                clusterCount = viewModel.clusterCount,
                isComputingClusters = viewModel.isComputingClusters,
                selectedMetric = viewModel.selectedMetric,
                onMetricChange = { viewModel.setClusterMetric(it) },
                onIncrementCluster = { viewModel.incrementClusterCount() },
                onDecrementCluster = { viewModel.decrementClusterCount() },
                onClearPathClick = { viewModel.clearPath() },
                onBuildFinalPathClick = { viewModel.buildPath() },
                isGeneticRunning = viewModel.isGeneticRunning,
                geneticStartPoint = viewModel.geneticStartPoint,
                selectedFoodItems = viewModel.selectedFoodItems,
                onFoodItemToggle = { viewModel.toggleFoodItem(it) },
                onSelectAllFoodItems = { viewModel.selectAllFoodItems() },
                onClearAllFoodItems = { viewModel.clearAllFoodItems() },
                onGeneticStartClick = { viewModel.startGeneticSelection() },
                onRunGeneticClick = { viewModel.runGenetic() },
                onClearGeneticClick = { viewModel.clearGenetic() },
                geneticIteration = viewModel.geneticIteration,
                geneticCost = viewModel.geneticCost
            )
        },
        sheetPeekHeight = 160.dp,
        sheetShape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
        sheetShadowElevation = 40.dp,
        sheetContainerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFDEE5ED))
        ) {
            TsuMapScreen(
                modifier = Modifier.padding(innerPadding),
                startPoint = viewModel.startPoint,
                endPoint = viewModel.endPoint,
                isPathfindingMode = selectedMode == SheetMode.PATHFINDING
            )

            if (viewModel.isClusteringActive && viewModel.clusteredPlaces.isNotEmpty()) {
                ClusterLegend(
                    metricName = viewModel.selectedMetric.label,
                    clusterCount = viewModel.clusterCount,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 200.dp)
                )
            }

            viewModel.selectedPlace?.let { place ->
                PlaceInfoCard(
                    place = place,
                    clusterIndex = viewModel.clusteredPlaces
                        .find { it.place.id == place.id }?.clusterIndex,
                    metricName = if (viewModel.isClusteringActive) viewModel.selectedMetric.label else null,
                    onDismiss = {  viewModel.clearSelectedPlace()  },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
    }
}

@Composable
fun BottomSheetContent(
    selectedMode: SheetMode?,
    onModeSelected: (SheetMode?) -> Unit,
    onBuildPathClick: () -> Unit,
    isSearching: Boolean,
    onSelectionModeClick: () -> Unit,
    onObstacleClick: () -> Unit,
    onClearObstaclesClick: () -> Unit,
    isClusteringActive: Boolean,
    onClusteringClick: () -> Unit,
    clusterCount: Int,
    isComputingClusters: Boolean,
    selectedMetric: ClusterMetricType,
    onMetricChange: (ClusterMetricType) -> Unit,
    onIncrementCluster: () -> Unit,
    onDecrementCluster: () -> Unit,
    onClearPathClick: () -> Unit,
    onBuildFinalPathClick: () -> Unit,
    isGeneticRunning: Boolean,
    geneticStartPoint: Point?,
    selectedFoodItems: Set<FoodItem>,
    onFoodItemToggle: (FoodItem) -> Unit,
    onSelectAllFoodItems: () -> Unit,
    onClearAllFoodItems: () -> Unit,
    onGeneticStartClick: () -> Unit,
    onRunGeneticClick: () -> Unit,
    onClearGeneticClick: () -> Unit,
    geneticIteration: Int,
    geneticCost: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    )
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "A* Маршрут",
                containerColor = if (selectedMode == SheetMode.PATHFINDING) Color(0xFF4CAF50) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = {
                    onModeSelected(if (selectedMode == SheetMode.PATHFINDING) null else SheetMode.PATHFINDING)
                }
            )

            ActionButton(
                text = "Кластеризация",
                containerColor = if (selectedMode == SheetMode.CLUSTERING) Color(0xFF4CAF50) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(if (selectedMode == SheetMode.CLUSTERING) null else SheetMode.CLUSTERING) }
            )
            ActionButton(
                text = "Генетика",
                containerColor = if (selectedMode == SheetMode.GENETIC) Color(0xFF4CAF50) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(if (selectedMode == SheetMode.GENETIC) null else SheetMode.GENETIC) }
            )
        }

        AnimatedVisibility(
            visible = selectedMode == SheetMode.PATHFINDING,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = if (isSearching) "Поиск..." else "Поставить точки",
                        containerColor = TsuBlue,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onSelectionModeClick
                    )
                    ActionButton(
                        text = if (isSearching) "Поиск..." else "А* с анимацией",
                        containerColor = TsuBlue,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onBuildPathClick
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = if (isSearching) "Поиск..." else "Итоговый маршрут",
                        containerColor = TsuBlue,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onBuildFinalPathClick
                    )
                    ActionButton(
                        text = "Очистить маршрут",
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onClearPathClick
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = "Добавить стены",
                        containerColor = TsuBlue,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onObstacleClick
                    )
                    ActionButton(
                        text = "Очистить стены",
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onClearObstaclesClick
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = selectedMode == SheetMode.GENETIC,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            var showCategoryDialog by remember { mutableStateOf(false) }

            if (showCategoryDialog) {
                CategoryDialog(
                    selectedItems = selectedFoodItems,
                    onToggle = onFoodItemToggle,
                    onSelectAll = onSelectAllFoodItems,
                    onClearAll = onClearAllFoodItems,
                    onDismiss = { showCategoryDialog = false }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        text = "Категории (${selectedFoodItems.size}/${FoodItem.entries.size})",
                        containerColor = TsuBlue,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = { showCategoryDialog = true }
                    )
                    ActionButton(
                        text = if (geneticStartPoint != null) "Точка ✓" else "Нач. точка",
                        containerColor = if (geneticStartPoint != null) Color(0xFF4CAF50) else TsuBlue,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onGeneticStartClick
                    )
                }
                ActionButton(
                    text = if (isGeneticRunning) "Выполняется..." else "Запустить",
                    containerColor = TsuBlue,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRunGeneticClick
                )
                if (isGeneticRunning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (geneticCost > 0) {
                    Text(
                        text = "Итерация: $geneticIteration  |  Стоимость: $geneticCost",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                ActionButton(
                    text = "Очистить маршрут",
                    containerColor = Color(0xFFF44336),
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClearGeneticClick
                )
            }
        }

        AnimatedVisibility(
            visible = selectedMode == SheetMode.CLUSTERING,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Метрика расстояния:",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClusterMetricType.entries.forEach { metric ->
                        val selected = selectedMetric == metric
                        Button(
                            onClick = { onMetricChange(metric) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) Color(0xFF1565C0) else Color(0xFFE0E0E0),
                                contentColor = if (selected) Color.White else Color.Black
                            )
                        ) {
                            Text(
                                text = metric.label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Количество кластеров:", fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onDecrementCluster,
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$clusterCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        onClick = onIncrementCluster,
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                ActionButton(
                    text = when {
                        isComputingClusters -> "Вычисление..."
                        isClusteringActive -> "Скрыть кластеры"
                        else -> "Применить (${selectedMetric.label})"
                    },
                    containerColor = if (isClusteringActive) Color(0xFF4CAF50) else TsuBlue,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClusteringClick
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text)
    }
}

@Composable
fun PlaceInfoCard(place: Place, onDismiss: () -> Unit,
                  modifier: Modifier = Modifier, clusterIndex: Int? = null,
                  metricName: String? = null) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (clusterIndex != null) {
                Box(
                    modifier = Modifier.size(12.dp).background(
                        clusterColors[clusterIndex % clusterColors.size], CircleShape
                    )
                )
                Spacer(Modifier.height(4.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                TextButton(onClick = onDismiss) {
                    Text("x", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = place.description,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (place.type) {
                    PlaceType.FOOD -> "Кафе / Ресторан"
                    PlaceType.FOOD_SHOP -> "Магазин продуктов"
                    PlaceType.UNIVERSITY_BUILDING -> "Корпус университета"
                },
                fontSize = 13.sp,
                color = TsuBlue,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${place.openTime} - ${place.closeTime}",
                fontSize = 13.sp,
                color = Color.Gray
            )
            if (clusterIndex != null && metricName != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier.size(10.dp).background(
                            clusterColors[clusterIndex % clusterColors.size], CircleShape
                        )
                    )
                    Text(
                        "Кластер ${clusterIndex + 1} · $metricName",
                        fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium
                    )
                }
            }

        }
    }
}
@Composable
fun ClusterLegend(metricName: String, clusterCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(metricName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            for (i in 0 until clusterCount) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(clusterColors[i % clusterColors.size], CircleShape))
                    Text("Кластер ${i + 1}", fontSize = 11.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun CategoryDialog(
    selectedItems: Set<FoodItem>,
    onToggle: (FoodItem) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val allItems = FoodItem.entries
    val allSelected = selectedItems.size == allItems.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите категории", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (allSelected) onClearAll() else onSelectAll() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { if (allSelected) onClearAll() else onSelectAll() }
                    )
                    Text(
                        text = if (allSelected) "Снять все" else "Выбрать все",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TsuBlue
                    )
                }
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    allItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(item) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item in selectedItems,
                                onCheckedChange = { onToggle(item) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item.label, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK (${selectedItems.size} выбрано)")
            }
        }
    )
}
