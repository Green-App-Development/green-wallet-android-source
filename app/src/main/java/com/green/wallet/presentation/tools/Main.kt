package com.green.wallet.presentation.tools

import com.example.common.tools.formatString
import java.util.Arrays

class Main {}

fun main() {



}

fun countVisitedNodes(edges: List<Int>): IntArray {

    val ans = IntArray(edges.size) { 0 }
    val visited = BooleanArray(edges.size) { false }
    val cycleStarts = dfs(0, edges, visited)
    Arrays.fill(visited, false)
    val len = markCycle(cycleStarts, edges, visited)

    for (i in 0 until edges.size) {
        if (visited[i]) {
            ans[i] = len
        }
    }

    for (i in 0 until edges.size) {
        if (visited[i]) continue
        val cur = dfs(edges, i, visited, ans)
        ans[i] = cur + len
    }

    return ans
}

fun dfs(edges: List<Int>, at: Int, visited: BooleanArray, ans: IntArray): Int {
    if (visited[at])
        return 0
    val cur = 1 + dfs(edges, edges[at], visited, ans)
    ans[at] = cur
    return ans[at]
}

fun markCycle(at: Int, edges: List<Int>, visited: BooleanArray): Int {
    if (visited[at])
        return 0
    visited[at] = true
    return 1 + markCycle(edges[at], edges, visited)
}


fun dfs(at: Int, edges: List<Int>, visited: BooleanArray): Int {
    if (visited[at])
        return at
    visited[at] = true
    return dfs(edges[at], edges, visited)
}














