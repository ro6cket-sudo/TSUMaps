package com.example.tsumaps.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.tsumaps.TsuMapScreen
import com.example.tsumaps.core.DigitRecognizer.DigitNeuralNetwork
import com.example.tsumaps.core.FoodItem
import com.example.tsumaps.ui.screens.decisionTreeScreen.DecisionTreeScreen
import com.example.tsumaps.ui.screens.scoreScreec.Draw as DrawOnGrid
import com.example.tsumaps.ui.screens.scoreScreec.addDraw as addDrawOnGrid
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.PlaceType
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.cluster.ClusterMetricType
import com.example.tsumaps.core.algorithms.genetic.CrossoverType
import com.example.tsumaps.core.algorithms.genetic.MutationType
import com.example.tsumaps.core.algorithms.genetic.SelectionType
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.viewmodels.MapViewModel

enum class SheetMode { PATHFINDING, CLUSTERING, ANTS, GENETIC,DECISION_TREE}

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
    val savedNeuralResults = remember { androidx.compose.runtime.mutableStateMapOf<Int, Int>() }

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
                onModeSelected = {
                    selectedMode = it
                    val shouldBeAntsMode = it == SheetMode.ANTS
                    if (viewModel.isAntsMode != shouldBeAntsMode) viewModel.toggleAntsMode()
                },
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
                geneticCost = viewModel.geneticCost,
                selectedMutation = viewModel.selectedMutation,
                onMutationChange = { viewModel.setMutation(it) },
                selectedCrossover = viewModel.selectedCrossover,
                onCrossoverChange = { viewModel.setCrossover(it) },
                selectedSelection = viewModel.selectedSelection,
                onSelectionChange = { viewModel.setSelection(it) },

                antsAllPlaces = viewModel.landmarkPlaces,
                antsSelectedPlaces = viewModel.antsSelectedPlaces,
                antsIsPlaceSelected = { viewModel.antsIsPlaceSelected(it) },
                antsOnTogglePlace = { viewModel.antsTogglePlace(it) },
                antsOnClearSelection = { viewModel.antsClearSelection() },
                antsStartPoint = viewModel.antsStartPoint,
                antsIsPickingStart = viewModel.isAntsPickingStart,
                antsOnPickStartClick = {
                    if (viewModel.isAntsPickingStart) viewModel.antsCancelPickingStart()
                    else viewModel.antsStartPickingStart()
                },
                antsCanRun = viewModel.antsCanRun,
                antsIsRunning = viewModel.antsIsRunning,
                antsIsAnimating = viewModel.antsIsAnimating,
                antsOnRunClick = { viewModel.antsRun() },
                antsOnRunAnimatedClick = { viewModel.antsRunAnimated() },
                antsOnClearAllClick = { viewModel.antsClearAll() },
                antsRoute = viewModel.antsRoute,
                antsTotalDistanceMeters = viewModel.antsTotalDistanceMeters
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
                    clusterIndex = viewModel.clusteredPlaces.find { it.place.id == place.id }?.clusterIndex,
                    metricName = if (viewModel.isClusteringActive) viewModel.selectedMetric.label else null,
                    onDismiss = { viewModel.clearSelectedPlace() },
                    savedNeuralResult = savedNeuralResults[place.id],
                    onSaveNeuralResult = { savedNeuralResults[place.id] = it },
                    onClearNeuralResult = { savedNeuralResults.remove(place.id) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = selectedMode == SheetMode.DECISION_TREE,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            DecisionTreeScreen()
            Button(
                onClick = { selectedMode = null },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TsuBlue)
            ) {
                Text("← Назад", color = Color.White)
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
    geneticCost: Int,
    selectedMutation: MutationType,
    onMutationChange: (MutationType) -> Unit,
    selectedCrossover: CrossoverType,
    onCrossoverChange: (CrossoverType) -> Unit,
    selectedSelection: SelectionType,
    onSelectionChange: (SelectionType) -> Unit,
    antsAllPlaces: List<Place>,
    antsSelectedPlaces: List<Place>,
    antsIsPlaceSelected: (Place) -> Boolean,
    antsOnTogglePlace: (Place) -> Unit,
    antsOnClearSelection: () -> Unit,
    antsStartPoint: com.example.tsumaps.core.Point?,
    antsIsPickingStart: Boolean,
    antsOnPickStartClick: () -> Unit,
    antsCanRun: Boolean,
    antsIsRunning: Boolean,
    antsIsAnimating: Boolean,
    antsOnRunClick: () -> Unit,
    antsOnRunAnimatedClick: () -> Unit,
    antsOnClearAllClick: () -> Unit,
    antsRoute: List<Place>,
    antsTotalDistanceMeters: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                text = "Кластеры",
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
        ActionButton(
            text = "Дерево решений",
            containerColor = if (selectedMode == SheetMode.DECISION_TREE) Color(0xFF4CAF50) else TsuBlue,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth(),
            onClick = { onModeSelected(if (selectedMode == SheetMode.DECISION_TREE) null else SheetMode.DECISION_TREE) }
        )

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
                OperatorSelectorRow(
                    label = "Мутация:",
                    options = MutationType.entries,
                    selected = selectedMutation,
                    labelOf = { it.label },
                    onSelect = onMutationChange
                )
                OperatorSelectorRow(
                    label = "Скрещивание:",
                    options = CrossoverType.entries,
                    selected = selectedCrossover,
                    labelOf = { it.label },
                    onSelect = onCrossoverChange
                )
                OperatorSelectorRow(
                    label = "Отбор:",
                    options = SelectionType.entries,
                    selected = selectedSelection,
                    labelOf = { it.label },
                    onSelect = onSelectionChange
                )
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
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

        AnimatedVisibility(
            visible = selectedMode == SheetMode.ANTS,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            AntsPanel(
                allPlaces = antsAllPlaces,
                selectedPlaces = antsSelectedPlaces,
                isPlaceSelected = antsIsPlaceSelected,
                onTogglePlace = antsOnTogglePlace,
                onClearSelection = antsOnClearSelection,
                startPoint = antsStartPoint,
                isPickingStart = antsIsPickingStart,
                onPickStartClick = antsOnPickStartClick,
                canRun = antsCanRun,
                isRunning = antsIsRunning,
                isAnimating = antsIsAnimating,
                onRunClick = antsOnRunClick,
                onRunAnimatedClick = antsOnRunAnimatedClick,
                onClearAllClick = antsOnClearAllClick,
                route = antsRoute,
                totalDistanceMeters = antsTotalDistanceMeters
            )
        }
    }
}

@Composable
fun <T> OperatorSelectorRow(
    label: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Button(
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF1565C0) else Color(0xFFE0E0E0),
                        contentColor = if (isSelected) Color.White else Color.Black
                    )
                ) {
                    Text(
                        text = labelOf(option),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun AntsPanel(
    allPlaces: List<Place>,
    selectedPlaces: List<Place>,
    isPlaceSelected: (Place) -> Boolean,
    onTogglePlace: (Place) -> Unit,
    onClearSelection: () -> Unit,
    startPoint: com.example.tsumaps.core.Point?,
    isPickingStart: Boolean,
    onPickStartClick: () -> Unit,
    canRun: Boolean,
    isRunning: Boolean,
    isAnimating: Boolean,
    onRunClick: () -> Unit,
    onRunAnimatedClick: () -> Unit,
    onClearAllClick: () -> Unit,
    route: List<Place>,
    totalDistanceMeters: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Муравьиный алгоритм (TSP)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val startText = when {
                isPickingStart -> "Тапните по карте…"
                startPoint != null -> "Старт: (${startPoint.x}, ${startPoint.y})"
                else -> "Старт не выбран"
            }
            Text(
                text = startText,
                fontSize = 13.sp,
                color = if (startPoint != null) Color(0xFF2E7D32) else Color.DarkGray,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = if (isPickingStart) "Отмена" else "Выбрать старт",
                containerColor = if (isPickingStart) Color(0xFFF44336) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier,
                onClick = onPickStartClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Выбрано мест: ${selectedPlaces.size} / ${allPlaces.size}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )
            TextButton(onClick = onClearSelection) {
                Text("Сбросить выбор", fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp, max = 240.dp)
                .background(Color(0xFFF3F5F8), RoundedCornerShape(10.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                items(allPlaces, key = { it.id }) { place ->
                    val selected = isPlaceSelected(place)
                    val indexInRoute = route.indexOfFirst { it.id == place.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = { onTogglePlace(place) }
                        )
                        if (indexInRoute >= 0) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(TsuBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${indexInRoute + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = place.name,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (isAnimating) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(
                    text = "Муравьи ищут маршрут…",
                    fontSize = 13.sp,
                    color = Color(0xFF6A1B9A)
                )
            }
        }

        val notReady = !canRun
        val hintText = when {
            startPoint == null -> "Нет старта"
            selectedPlaces.size < 2 -> "Нужно ≥ 2 мест"
            else -> ""
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = when {
                        isRunning && !isAnimating -> "Поиск…"
                        notReady -> hintText.ifEmpty { "Запустить" }
                        else -> "Запустить"
                    },
                    containerColor = if (canRun && !isRunning) Color(0xFF2E7D32) else Color(0xFFB0BEC5),
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = { if (canRun && !isRunning) onRunClick() }
                )
                ActionButton(
                    text = when {
                        isAnimating -> "Анимация…"
                        isRunning -> "Поиск…"
                        notReady -> hintText.ifEmpty { "Анимировать" }
                        else -> "Анимировать"
                    },
                    containerColor = if (canRun && !isRunning) Color(0xFF6A1B9A) else Color(0xFFB0BEC5),
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = { if (canRun && !isRunning) onRunAnimatedClick() }
                )
            }
            ActionButton(
                text = "Очистить всё",
                containerColor = Color(0xFFF44336),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
                onClick = onClearAllClick
            )
        }

        if (route.isNotEmpty()) {
            val cardBg = if (isAnimating) Color(0xFFF3E5F5) else Color(0xFFE8F5E9)
            val textColor = if (isAnimating) Color(0xFF4A148C) else Color(0xFF1B5E20)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (isAnimating) "Текущий кандидат (поиск идёт…)"
                        else "Итоговый маршрут · $totalDistanceMeters м",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(Modifier.height(4.dp))
                    val orderText = buildString {
                        append("Старт")
                        route.forEachIndexed { i, p ->
                            append(" → ${i + 1}. ${p.name}")
                        }
                        append(" → Старт")
                    }
                    Text(text = orderText, fontSize = 12.sp, color = textColor)
                }
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
fun PlaceInfoCard(
    place: Place,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    clusterIndex: Int? = null,
    metricName: String? = null,
    savedNeuralResult: Int? = null,
    onSaveNeuralResult: (Int) -> Unit = {},
    onClearNeuralResult: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val gridSize = 50
    val digitRecognizer = remember { DigitNeuralNetwork(context) }
    val grid = remember {
        mutableStateListOf<MutableList<Int>>().apply {
            repeat(gridSize) { add(mutableStateListOf<Int>().apply { repeat(gridSize) { add(0) } }) }
        }
    }
    var showNeuralNet by remember { mutableStateOf(false) }
    var lastX by remember { mutableStateOf(-1f) }
    var lastY by remember { mutableStateOf(-1f) }
    var score by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp, 8.dp)) {
            if (clusterIndex != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(clusterColors[clusterIndex % clusterColors.size], CircleShape)
                )
                Spacer(Modifier.height(2.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onDismiss, contentPadding = PaddingValues(4.dp)) {
                    Text("✕", color = Color.Gray, fontSize = 13.sp)
                }
            }
            Text(text = place.description, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (place.type) {
                    PlaceType.FOOD -> "Кафе / Ресторан"
                    PlaceType.FOOD_SHOP -> "Магазин продуктов"
                    PlaceType.UNIVERSITY_BUILDING -> "Корпус университета"
                    PlaceType.LANDMARK -> "ДОСТОПРИМЕЧАТЕЛЬНОСТИ"
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
            Text(text = place.description, fontSize = 12.sp, color = Color.DarkGray)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (place.type) {
                        PlaceType.FOOD -> "Кафе / Ресторан"
                        PlaceType.FOOD_SHOP -> "Магазин продуктов"
                        PlaceType.UNIVERSITY_BUILDING -> "Корпус"
                    },
                    fontSize = 11.sp,
                    color = TsuBlue,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${place.openTime}–${place.closeTime}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            if (clusterIndex != null && metricName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.size(8.dp).background(
                            clusterColors[clusterIndex % clusterColors.size], CircleShape
                        )
                    )
                    Text(
                        "Кластер ${clusterIndex + 1} · $metricName",
                        fontSize = 11.sp, color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            ActionButton(
                text = if (showNeuralNet) "Скрыть нейросеть" else "Нейросеть",
                containerColor = if (showNeuralNet) Color(0xFF4CAF50) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
                onClick = { showNeuralNet = !showNeuralNet }
            )

            AnimatedVisibility(visible = showNeuralNet) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = "Нарисуйте цифру:",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFDFE6E9), RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (offset.x > 1f && offset.y > 1f) {
                                            lastX = offset.x
                                            lastY = offset.y
                                            DrawOnGrid(offset.x, offset.y, gridSize, grid, size.width)
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        val x = change.position.x
                                        val y = change.position.y
                                        change.consume()
                                        if (x > 1f && y > 1f && lastX != -1f) {
                                            addDrawOnGrid(lastX, x, lastY, y, gridSize, grid, size.width)
                                            lastX = x
                                            lastY = y
                                        }
                                    },
                                    onDragEnd = {
                                        lastX = -1f
                                        lastY = -1f
                                        score = digitRecognizer.claccify(grid)
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellSize = size.width / gridSize
                            for (i in 0 until gridSize) {
                                val p = i * cellSize
                                drawLine(Color(0xFFF1F2F6), Offset(p, 0f), Offset(p, size.height), 1f)
                                drawLine(Color(0xFFF1F2F6), Offset(0f, p), Offset(size.width, p), 1f)
                            }
                            for (i in 0 until gridSize) {
                                for (j in 0 until gridSize) {
                                    if (grid[i][j] == 1) {
                                        drawRect(
                                            color = TsuBlue,
                                            topLeft = Offset(i * cellSize, j * cellSize),
                                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.width(180.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ИИ:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = score?.toString() ?: "?",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TsuBlue
                                )
                            }
                        }
                        ActionButton(
                            text = "Сброс",
                            containerColor = Color(0xFFF44336),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                score = null
                                for (x in 0 until gridSize) {
                                    for (y in 0 until gridSize) { grid[x][y] = 0 }
                                }
                            }
                        )
                    }

                    if (score != null && savedNeuralResult == null) {
                        ActionButton(
                            text = "Сохранить результат ($score)",
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            modifier = Modifier.width(180.dp),
                            onClick = { score?.let { onSaveNeuralResult(it) } }
                        )
                    }

                    if (savedNeuralResult != null) {
                        Row(
                            modifier = Modifier.width(180.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = TsuBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = savedNeuralResult.toString(),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            ActionButton(
                                text = "Удалить",
                                containerColor = Color(0xFFF44336),
                                contentColor = Color.White,
                                modifier = Modifier.weight(2f),
                                onClick = onClearNeuralResult
                            )
                        }
                    }
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
