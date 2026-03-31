package com.example.tsumaps.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.AStarFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val pathFinder = AStarFinder()

    var startPoint by mutableStateOf<Point?>(null)
    var endPoint by mutableStateOf<Point?>(null)
    var calculatedPath by mutableStateOf<List<Point>>(emptyList())
    var isSearching by mutableStateOf(false)

    init {
        loadMapData()
    }

    private fun loadMapData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = getApplication<Application>().assets
                    .open("map_array.json")
                    .bufferedReader()
                    .use { it.readText() }

                val jsonObject = JSONObject(jsonString)
                val dataArray = jsonObject.getJSONArray("data")

                val booleanGrid = BooleanArray(dataArray.length())

                for (i in 0 until dataArray.length()) {
                    booleanGrid[i] = dataArray.getInt(i) == 0
                }

                pathFinder.setBaseMap(booleanGrid)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun buildPath() {
        val start = startPoint
        val end = endPoint

        if (start != null && end != null) {
            viewModelScope.launch(Dispatchers.Default) {
                isSearching = true

                val path = pathFinder.findPath(start, end)

                withContext(Dispatchers.Main) {
                    calculatedPath = path ?: emptyList()
                    isSearching = false
                }
            }
        }
    }

    fun onMapClick(point: Point) {

        if (point.x < 0 || point.x >= MapConstants.GRID_WIDTH ||
            point.y < 0 || point.y >= MapConstants.GRID_HEIGHT) {
            return
        }

        if (startPoint == null || (startPoint != null && endPoint != null)) {
            startPoint = point
            endPoint = null
            calculatedPath = emptyList()
        } else {
            endPoint = point
        }
    }
}