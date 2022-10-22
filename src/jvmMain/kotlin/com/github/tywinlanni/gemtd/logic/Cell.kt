package com.github.tywinlanni.gemtd.logic

open class Cell(
    val x: Byte,
    val y: Byte,
    val isEnemyPoint: Boolean = false,
    val enemyPointNumber: EnemyPointNumber?,
    open val allowedMove: Boolean,
    internal var distanceFromStart: Int? = null,
)

class GameCell(
    x: Byte,
    y: Byte,
    val isEnemyBase: Boolean = false,
    val isPlayerBase: Boolean = false,
    isEnemyPoint: Boolean = false,
    enemyPointNumber: EnemyPointNumber? = null,
    private var isBuilding: Boolean = false,
) : Cell(x, y, isEnemyPoint, enemyPointNumber, true) {
    var build : Build? = null
        private set

    override val allowedMove: Boolean
        get() = (build?.buildType ?: BuildType.PEDAL) == BuildType.PEDAL

    val allowedBuild: Boolean
        get() = !isEnemyBase && !isPlayerBase && !isEnemyPoint && (build?.buildType ?: BuildType.STONE) == BuildType.STONE

    fun addBuild(newBuild: Build): Boolean {
        if (allowedBuild) {
            build = newBuild
            return true
        }
        return false
    }

    fun removeBuild(): Boolean {
        if (build?.buildType == BuildType.STONE) {
            build = null
            return true
        }
        return false
    }
}
