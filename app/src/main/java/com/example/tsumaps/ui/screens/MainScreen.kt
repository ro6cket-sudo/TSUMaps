package com.example.tsumaps.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.tsumaps.R
import com.example.tsumaps.ui.theme.TsuBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val sheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = sheetState,
//        sheetDragHandle = null,
        sheetContent = {BottomSheetContent()},
        sheetPeekHeight = 50.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetShadowElevation = 20.dp,
        sheetContainerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFDEE5ED))
        ) {/* ВОТ СЮДА НУЖНО ЗАПИХНУТЬ КАРТУ*/
            TsuMapScreen()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 5.dp)
        ) {
            CategoryFilters()
        }
    }
}

@Composable
fun BottomSheetContent(){
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
                text = "Построить Маршрут",
                containerColor = TsuBlue,
                contentColor = Color.White,
                modifier = Modifier,
                onClick = {/* Зпуск А* */}
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
            FloatingWhiteButton(name, {})
        }
        }
    }
}

@Composable
fun FloatingWhiteButton(name: String, onClick: () -> Unit){
//    FloatingActionButton(
//        onClick = onClick,
//        containerColor = Color.White,
//        contentColor = TsuBlue,
//        shape = CircleShape
//    ) {
//        Text(
//            text = name,
//            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
//        )
//    }
    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = TsuBlue,
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun TsuMapScreen(modifier: Modifier = Modifier) {
    //для зума (пока не робит)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = state),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.tsu_map),
            contentDescription = "Карта ТГУ",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(1f, scale),
                    scaleY = maxOf(1f, scale),
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}