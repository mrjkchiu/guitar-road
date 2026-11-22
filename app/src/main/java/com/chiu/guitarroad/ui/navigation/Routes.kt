package com.chiu.guitarroad.ui.navigation

/**
 * 导航路由字符串（契约第四节，写死）。
 * "main" 是底部 5 Tab 宿主；其余路由为同级目的地，底部栏隐藏。
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val TASK = "task/{nodeId}"
    const val TUNER = "tuner?nodeId={nodeId}"
    const val METRONOME = "metronome?nodeId={nodeId}&bpm={bpm}"
    const val CHORDS = "chords"
    const val CHORD = "chord/{chordId}"
    const val THEORY = "theory"
    const val THEORY_CARD = "theory/{cardId}"

    fun task(nodeId: String) = "task/$nodeId"
    fun tuner(nodeId: String = "none") = "tuner?nodeId=$nodeId"
    fun metronome(nodeId: String = "none", bpm: Int = 60) = "metronome?nodeId=$nodeId&bpm=$bpm"
    fun chord(chordId: String) = "chord/$chordId"
    fun theoryCard(cardId: String) = "theory/$cardId"
}
