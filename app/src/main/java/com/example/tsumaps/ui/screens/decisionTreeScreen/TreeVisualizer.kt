package com.example.tsumaps.ui.screens.decisionTreeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tsumaps.core.decision_tree.TreeNode
import com.example.tsumaps.ui.theme.TsuBlue

@Composable
fun TreeVisualizer(node: TreeNode, depth: Int = 0) {
    Column(modifier = Modifier.padding(start = (depth * 12).dp, top = 4.dp)) {
        if (node.isLeaf) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.Green)
                Text(
                    " Рекомендация: ${node.prediction}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        } else {
            Column {
                Text(
                    "Признак: ${node.feature}",
                    color = TsuBlue,
                    fontWeight = FontWeight.Medium
                )
                node.children.forEach { (value, child) ->
                    Text(
                        "если '$value':",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    TreeVisualizer(child, depth + 1)
                }
            }
        }
    }
}