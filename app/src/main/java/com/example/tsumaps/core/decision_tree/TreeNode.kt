package com.example.tsumaps.core.decision_tree

class TreeNode
    (
    var isLeaf: Boolean = false,
    var prediction: String? = null,
    var feature: String? = null,
    var children: MutableMap<String, TreeNode> = mutableMapOf(),
    var planB: String = ""
)