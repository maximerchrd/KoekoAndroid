package com.wideworld.koeko.QuestionsManagement

class GameView {
    var gameType = -1
    var endScore = 30
    var theme = 0
    var team = 0
}

object GameType {
    const val manualSending = 0
    const val orderedAutomaticSending = 1
    const val randomAutomaticSending = 2
    const val qrCodeGame = 3
}