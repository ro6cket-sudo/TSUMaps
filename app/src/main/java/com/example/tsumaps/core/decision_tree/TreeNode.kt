package com.example.tsumaps.core.decision_tree

class TreeNode
    (
    var isLeaf: Boolean = false,
    var predication: String? = null,
    var question: String? = null,
    var children: MutableMap<String, TreeNode> = mutableMapOf(),
    var PlanB: String = "")
