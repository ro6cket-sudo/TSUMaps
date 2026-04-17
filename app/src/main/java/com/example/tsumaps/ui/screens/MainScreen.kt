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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tsumaps.TsuMapScreen
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.PlaceType
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.viewmodels.MapViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign

private enum class SheetMode {PATHFINDING, CLUSTERING}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val sheetState = rememberBottomSheetScaffoldState()
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContent = {BottomSheetContent(
            onBuildPathClick = { viewModel.onBuildPathClick() },
            isSearching = viewModel.isSearching,
            onSelectionModeClick = { viewModel.toggleSelectionMode() },
            onObstacleClick = { viewModel.toggleObstacleMode() },
            onClearObstaclesClick = {viewModel.clearObstacles()} ,
            isClusteringActive = viewModel.isClusteringActive,
            onClusteringClick = { viewModel.toggleClustering() },
            clusterCount = viewModel.clusterCount,
            onIncrementCluster = {viewModel.incrementClusterCount()},
            onDecrementCluster = {viewModel.decrementClusterCount()},
            onClearPathClick = {viewModel.clearPath()},
            onBuildFinalPathClick = {viewModel.buildPath()}
        )},
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
            TsuMapScreen(modifier = Modifier.padding(innerPadding),
                startPoint = viewModel.startPoint,
                endPoint = viewModel.endPoint)

            viewModel.selectedPlace?.let { place ->
                PlaceInfoCard(
                    place = place,
                    onDismiss = {viewModel.clearSelectedPlace()},
                    modifier = Modifier
                        .align (Alignment.TopCenter)
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
    }
}

@Composable
fun BottomSheetContent(isSearching: Boolean, onBuildPathClick: () -> Unit,
                       onSelectionModeClick: () -> Unit, onObstacleClick: () -> Unit,
                       onClearObstaclesClick: () -> Unit, isClusteringActive: Boolean,
                       onClusteringClick: () -> Unit,clusterCount: Int, onIncrementCluster: () -> Unit,
                       onDecrementCluster: () -> Unit, onClearPathClick: () -> Unit,
                       onBuildFinalPathClick: () -> Unit) {
    var selectedMode by remember {mutableStateOf<SheetMode?>(null)}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp))
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "A* Маршрут",
                containerColor = if (selectedMode == SheetMode.PATHFINDING) Color(0xFF4CAF50) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedMode = if (selectedMode == SheetMode.PATHFINDING) null else SheetMode.PATHFINDING
                }
            )

            ActionButton(
                text = "Кластеризация",
                containerColor = if (selectedMode == SheetMode.CLUSTERING) Color(0xFF4CAF50) else TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedMode = if (selectedMode == SheetMode.CLUSTERING) null else SheetMode.CLUSTERING
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
                    modifier = Modifier,
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
                    modifier = Modifier,
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    }
}
@Composable
fun ActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
){
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
fun PlaceInfoCard(place: Place, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
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
                    Text("x", color = Color.Black,fontWeight = FontWeight.Bold)
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
                    PlaceType.UNIVERSITY_BUILDING -> "Корпус университета" },
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

        }
    }
}