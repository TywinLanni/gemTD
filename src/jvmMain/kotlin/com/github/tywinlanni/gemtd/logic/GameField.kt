package com.github.tywinlanni.gemtd.logic

import java.lang.Exception

class GameField(val playersValue: Byte = 1, private val field: List<List<GameCell>>) {
    private val maxX: Int
        get() = this.field.count() - 1

    private val maxY: Int
        get() = this.field[0].count() - 1

    private val enemyPath: MutableList<Pair<Byte, Byte>> = calculateFullPath()!!.toMutableList()

    companion object {
        fun createField(playersValue: Byte, maxX: Byte, maxY: Byte) : List<List<GameCell>> {
            val field = mutableListOf<List<GameCell>>()
            for (x in (0 until maxX)) {
                val column = mutableListOf<GameCell>()
                for (y in (0 until maxY)) {
                    column.add(
                        when {
                            x == 4 && y == maxY - 4 -> GameCell(x.toByte(), y.toByte(), isEnemyBase = true, isEnemyPoint = true, enemyPointNumber = EnemyPointNumber.START_POINT)
                            x == maxX - 5 && y == 4 -> GameCell(x.toByte(), y.toByte(), isPlayerBase = true, isEnemyPoint = true, enemyPointNumber = EnemyPointNumber.END_POINT)
                            x < 9 && y > maxY - 10 -> GameCell(x.toByte(), y.toByte(), isEnemyBase = true)
                            x > maxX - 10 && y < 9 -> GameCell(x.toByte(), y.toByte(), isPlayerBase = true)
                            x == 4 && y == (maxY - 1) / 2 -> GameCell(x.toByte(), y.toByte(), isEnemyPoint = true, enemyPointNumber = EnemyPointNumber.POINT_1)
                            x == maxX - 5 && y == (maxY - 1) / 2 -> GameCell(x.toByte(), y.toByte(), isEnemyPoint = true, enemyPointNumber = EnemyPointNumber.POINT_2)
                            x == maxX - 5 && y == maxY - 5 -> GameCell(x.toByte(), y.toByte(), isEnemyPoint = true, enemyPointNumber = EnemyPointNumber.POINT_3)
                            x == (maxX - 1) / 2 && y == maxY - 5  -> GameCell(x.toByte(), y.toByte(), isEnemyPoint = true, enemyPointNumber = EnemyPointNumber.POINT_4)
                            x == (maxX - 1) / 2 && y == 4 -> GameCell(x.toByte(), y.toByte(), isEnemyPoint = true, enemyPointNumber = EnemyPointNumber.POINT_5)
                            else -> GameCell(x.toByte(), y.toByte())
                        }
                    )
                }
                field.add(column)
            }
            val startStones = mutableListOf<Pair<Int, Int>>()
            if (playersValue < 3)
                startStones.addAll(listOf(
                    Pair(0, (maxY - 1) / 2),
                    Pair(1, (maxY - 1) / 2),
                    Pair((maxX - 4), (maxY - 1) / 2),
                    Pair((maxX - 3), (maxY - 1) / 2),
                    Pair((maxX - 1) / 2, 0),
                    Pair((maxX - 1) / 2, 1),
                    Pair((maxX - 1) / 2, (maxY - 1)),
                    Pair((maxX - 1) / 2, (maxY - 2)),
                ))
            if (playersValue < 2)
                startStones.addAll(listOf(
                    Pair(2, (maxY - 1) / 2),
                    Pair(3, (maxY - 1) / 2),
                    Pair((maxX - 2), (maxY - 1) / 2),
                    Pair((maxX - 1), (maxY - 1) / 2),
                    Pair((maxX - 1) / 2, 2),
                    Pair((maxX - 1) / 2, 3),
                    Pair((maxX - 1) / 2, (maxY - 3)),
                    Pair((maxX - 1) / 2, (maxY - 4)),
                ))
            startStones.forEach {
                field[it.first][it.second].addBuild(Stone())
            }
            return field
        }
    }

    fun calculateFullPath(): List<Pair<Byte, Byte>>? {
        val newEnemyPath = mutableListOf<Pair<Byte, Byte>>()
        mutableListOf<Pair<EnemyPointNumber, EnemyPointNumber>>()
            .apply {
                if (field.any { it.any { gameCell -> gameCell.enemyPointNumber == EnemyPointNumber.PLAYER_POINT } }) {
                    add(Pair(EnemyPointNumber.START_POINT, EnemyPointNumber.PLAYER_POINT))
                    add(Pair(EnemyPointNumber.PLAYER_POINT, EnemyPointNumber.POINT_1))
                }
                else
                    add(Pair(EnemyPointNumber.START_POINT, EnemyPointNumber.POINT_1))
                add(Pair(EnemyPointNumber.POINT_1, EnemyPointNumber.POINT_2))
                add(Pair(EnemyPointNumber.POINT_2, EnemyPointNumber.POINT_3))
                add(Pair(EnemyPointNumber.POINT_3, EnemyPointNumber.POINT_4))
                add(Pair(EnemyPointNumber.POINT_4, EnemyPointNumber.POINT_5))
                add(Pair(EnemyPointNumber.POINT_5, EnemyPointNumber.END_POINT))
            }
            .forEach {
                // add concurrent
                try {
                    newEnemyPath.addAll(
                        if (it.first == EnemyPointNumber.START_POINT)
                            calculatePath(it.first, it.second)
                        else
                            calculatePath(it.first, it.second).drop(0)
                    )
                } catch (e: Exception) {
                    return null
                }
            }
        enemyPath.apply {
            clear()
            addAll(newEnemyPath)
        }
        return newEnemyPath
    }

    private fun calculatePath(startPoint: EnemyPointNumber, endPoint: EnemyPointNumber): List<Pair<Byte, Byte>> {
        val path = mutableListOf<Pair<Byte, Byte>>()
        val fieldCopy = field.map { it.map { gameCell -> Cell(gameCell.x, gameCell.y, gameCell.isEnemyPoint, gameCell.enemyPointNumber, gameCell.allowedMove) } }
        var pathLen = 0
        val endPointCell = fieldCopy
            .first { it.any { cell: Cell -> cell.enemyPointNumber == endPoint } }
            .first { cell: Cell -> cell.enemyPointNumber == endPoint }
        fieldCopy
            .first { it.any { cell: Cell -> cell.enemyPointNumber == startPoint } }
            .first { cell: Cell -> cell.enemyPointNumber == startPoint }
            .distanceFromStart = 0
        while (endPointCell.distanceFromStart == null) {
            findAllCellWithDistance(pathLen, fieldCopy)
                .forEach { cell: Cell ->
                    markCellDistance(cell.x, (cell.y + 1).toByte(), pathLen + 1, fieldCopy)
                    markCellDistance(cell.x, (cell.y - 1).toByte(), pathLen + 1, fieldCopy)
                    markCellDistance((cell.x + 1).toByte(), cell.y, pathLen + 1, fieldCopy)
                    markCellDistance((cell.x - 1).toByte(), cell.y, pathLen + 1, fieldCopy)
                }
            pathLen++
            if (fieldCopy.none { it.any { cell: Cell -> cell.distanceFromStart == pathLen } })
                throw Exception("123")
        }
        path.add(Pair(endPointCell.x, endPointCell.y))
        var currentCell = endPointCell
        while (pathLen != 0) {
            currentCell = choiceCellToPath(currentCell.x, (currentCell.y + 1).toByte(), pathLen, fieldCopy)
                ?: choiceCellToPath(currentCell.x, (currentCell.y - 1).toByte(), pathLen, fieldCopy)
                ?: choiceCellToPath((currentCell.x + 1).toByte(), currentCell.y, pathLen, fieldCopy)
                ?: choiceCellToPath((currentCell.x - 1).toByte(), currentCell.y, pathLen, fieldCopy)
                ?: throw Exception("456")
            path.add(Pair(currentCell.x, currentCell.y))
            pathLen--
        }

        return path
    }

    private fun choiceCellToPath(x: Byte, y: Byte, distance: Int, fieldCopy: List<List<Cell>>): Cell? {
        if (x < 0 || x > maxX)
            return null
        if (y < 0 || y > maxY)
            return null
        val targetCell = fieldCopy[x.toInt()][y.toInt()]
        if (targetCell.distanceFromStart != null && targetCell.distanceFromStart!! < distance)
            return targetCell
        return null
    }

    private fun findAllCellWithDistance(distance: Int, fieldCopy: List<List<Cell>>): List<Cell> {
        val cells = mutableListOf<Cell>()
        fieldCopy.forEach { column ->
            column.forEach { cell: Cell ->
                if (cell.distanceFromStart == distance)
                    cells.add(cell)
            }
        }
        return cells
    }

    private fun markCellDistance(x: Byte, y: Byte, distance: Int, fieldCopy: List<List<Cell>>) {
        if (x < 0 || x > maxX)
            return
        if (y < 0 || y > maxY)
            return
        val targetCell = fieldCopy[x.toInt()][y.toInt()]
        if ((targetCell.distanceFromStart == null || (targetCell.distanceFromStart ?: 0) > distance) && targetCell.allowedMove)
            targetCell.distanceFromStart = distance
    }
}
