package com.example.tsumaps.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.TsuMapScreen
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.PlaceType
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.viewmodels.MapViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign

enum class SheetMode { PATHFINDING, CLUSTERING, ANTS }

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
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT)
                .show()
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
                onIncrementCluster = { viewModel.incrementClusterCount() },
                onDecrementCluster = { viewModel.decrementClusterCount() },
                onClearPathClick = { viewModel.clearPath() },
                onBuildFinalPathClick = { viewModel.buildPath() },

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
                    onDismiss = { viewModel.clearSelectedPlace() },
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
    isSearching: Boolean,
    onBuildPathClick: () -> Unit,
    onSelectionModeClick: () -> Unit,
    onObstacleClick: () -> Unit,
    onClearObstaclesClick: () -> Unit,
    isClusteringActive: Boolean,
    onClusteringClick: () -> Unit,
    clusterCount: Int,
    onIncrementCluster: () -> Unit,
    onDecrementCluster: () -> Unit,
    onClearPathClick: () -> Unit,
    onBuildFinalPathClick: () -> Unit,

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
                text = "A*",
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
                onClick = {
                    onModeSelected(if (selectedMode == SheetMode.CLUSTERING) null else SheetMode.CLUSTERING)
                }
            )
            ActionButton(
                text = "Муравьи",
                containerColor = if (selectedMode == SheetMode.ANTS) Color(0xFF4CAF50) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = {
                    onModeSelected(if (selectedMode == SheetMode.ANTS) null else SheetMode.ANTS)
                }
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
                        modifier = Modifier.weight(0.4f),
                        onClick = onObstacleClick
                    )
                    ActionButton(
                        text = "Очистить стены",
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White,
                        modifier = Modifier.weight(0.4f),
                        onClick = onClearObstaclesClick
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = selectedMode == SheetMode.CLUSTERING,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Количество кластеров: ",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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
                    text = if (isClusteringActive) "Скрыть кластеры" else "Кластеризация",
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
    clusterIndex: Int? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            if (clusterIndex != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(clusterColors[clusterIndex % clusterColors.size], CircleShape)
                    )
                    Text(
                        "Кластер ${clusterIndex + 1}",
                        fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ClusterLegend(clusterCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("K-Means", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            for (i in 0 until clusterCount) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(clusterColors[i % clusterColors.size], CircleShape)
                    )
                    Text("Кластер ${i + 1}", fontSize = 11.sp, color = Color.DarkGray)
                }
            }
        }
    }
}