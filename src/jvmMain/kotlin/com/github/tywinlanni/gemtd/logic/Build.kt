package com.github.tywinlanni.gemtd.logic

interface Build {
    val buildType: BuildType
    val texture: String
}

class Stone : Build {
    override val buildType = BuildType.STONE
    override val texture = "drawable/stone-radio-button-on.svg"
}
