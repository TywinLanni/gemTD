// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.tywinlanni.gemtd.logic.GameCell
import com.github.tywinlanni.gemtd.logic.GameField
import com.github.tywinlanni.gemtd.logic.Stone

@Composable
@Preview
fun App() {
    val gameField = GameField.createField(1, 37, 37)
    val game = GameField(1, field = gameField)
    val path = mutableStateListOf<Pair<Byte, Byte>>()
        .apply {
            game.calculateFullPath()
                ?.forEach {
                    add(it)
                }
        }

    MaterialTheme {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(820.dp),
                Arrangement.spacedBy(1.dp),
            ) {
                gameField.forEach { column ->
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .width(20.dp),
                        Arrangement.spacedBy(1.dp),
                    ) {
                        column
                            .reversed()
                            .forEach { gameCell ->
                                Cell(gameCell, path, game)
                            }
                    }
                }
            }
            Row(Modifier
                .fillMaxWidth()
                .height(820.dp)
            ) {
                Text("labirint len: ${path.size}")
            }
        }
    }
}

@Composable
fun Cell(gameCell: GameCell, path: SnapshotStateList<Pair<Byte, Byte>>, gameField: GameField) {
    val colors = remember {
        mutableStateOf(
            when {
                gameCell.isEnemyPoint ->  Color.Yellow
                path.contains(Pair(gameCell.x, gameCell.y)) -> Color.Cyan
                gameCell.isEnemyBase -> Color.Red
                gameCell.isPlayerBase -> Color.Green
                else -> Color.Gray
            }
        )
    }

    val stoneTextureVisibility = remember { mutableStateOf(gameCell.build != null) }

    Button(
        onClick = {
            if (gameCell.build != null) {
                if (gameCell.removeBuild()) {
                    stoneTextureVisibility.value = false
                    updatePath(path, gameField)
                }
            } else {
                if (gameCell.addBuild(Stone())) {
                    stoneTextureVisibility.value = true
                       if (path.contains(Pair(gameCell.x, gameCell.y)))
                        if (!updatePath(path, gameField)) {
                            gameCell.removeBuild()
                            stoneTextureVisibility.value = false
                        }
                }
            }
        },
        modifier = Modifier
            .width(20.dp)
            .height(20.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = colors.value),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        stoneTexture(stoneTextureVisibility.value)
        if (gameCell.enemyPointNumber != null)
            Box {
                Text(
                    text = "${gameCell.enemyPointNumber.number}",
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontSize = 15.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }
    }
}

private fun updatePathColors() {

}

private fun updatePath(path: SnapshotStateList<Pair<Byte, Byte>>, gameField: GameField): Boolean {
    path.apply {
        val newPath = gameField.calculateFullPath()
        if (newPath != null) {
            clear()
            newPath.forEach {
                add(it)
            }
            return true
        }
    }
    return false
}

@Composable
fun stoneTexture(isVisible: Boolean) {
    if (isVisible)
        Image(
            painter = painterResource("drawable/stone-radio-button-on.svg"),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 820.dp, height = 820.dp),
    ) {
        //test()
        App()
    }
}
