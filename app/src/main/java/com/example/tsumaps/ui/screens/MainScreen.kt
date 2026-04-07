package com.example.tsumaps.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tsumaps.TsuMapScreen
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.viewmodels.MapViewModel


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
            isSelectionMode = viewModel.isSelectionMode,
            onSelectionModeClick = { viewModel.toggleSelectionMode() },
            isObstacleMode = viewModel.isObstacleMode,
            onObstacleClick = { viewModel.toggleObstacleMode() }
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
            TsuMapScreen(modifier = Modifier.padding(innerPadding),path = viewModel.calculatedPath,
                startPoint = viewModel.startPoint,
                endPoint = viewModel.endPoint,
                onPointSelected = { clickedPoint -> viewModel.onMapClick(clickedPoint)})
        }
    }
}

@Composable
fun BottomSheetContent(isSearching: Boolean, onBuildPathClick: () -> Unit, isSelectionMode: Boolean,
                       onSelectionModeClick: () -> Unit, isObstacleMode: Boolean, onObstacleClick: () -> Unit,) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding((16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = if (isSearching) "Поиск..." else "Построить Маршрут",
                containerColor = TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = onBuildPathClick
            )
            ActionButton(
                text = if (isSearching) "Поиск..." else "Поставить точки",
                containerColor = TsuBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = onSelectionModeClick
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
                modifier = Modifier.weight(1f),
                onClick = onObstacleClick
            )
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