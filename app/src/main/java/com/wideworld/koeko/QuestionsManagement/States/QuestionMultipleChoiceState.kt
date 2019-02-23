package com.wideworld.koeko.QuestionsManagement.States

import android.widget.CheckBox

class QuestionMultipleChoiceState {
    var checkboxes = arrayListOf<CheckBox>()
    var timeRemaining = 0L
    var wasAnswered = false
}