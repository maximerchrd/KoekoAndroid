package com.wideworld.koeko.QuestionsManagement

import java.util.ArrayList
import java.util.Vector

/**
 * Created by maximerichard on 26.10.17.
 */
class QuestionMultipleChoice {
    var id: String? = null
    var subject: String? = null
    var level: String? = null
    var question: String? = null

    /**
     * OPTIONSNUMBER: total number of choices for the answer
     */
    var optionsnumber: Int = 0
    /**
     * NB_CORRECT_ANS: number of correct answers
     */
    var nB_CORRECT_ANS: Int = 0
    var opt0: String? = null
    var opt1: String? = null
    var opt2: String? = null
    var opt3: String? = null
    var opt4: String? = null
    var opt5: String? = null
    var opt6: String? = null
    var opt7: String? = null
    var opt8: String? = null
    var opt9: String? = null
    var timerSeconds: Int = -1
    private var IMAGE: String? = null
    var subjects: Vector<String>? = null
    var objectives: Vector<String>? = null
    var image: String?
        get() = IMAGE
        set(iMAGE) = if (iMAGE != null && iMAGE.isEmpty()) {
            IMAGE = "none"
        } else {
            IMAGE = iMAGE
        }

    val possibleAnswers: ArrayList<String>
        get() {
            val answers = ArrayList<String>()
            answers.add(opt0 ?: "")
            answers.add(opt1 ?: "")
            answers.add(opt2 ?: "")
            answers.add(opt3 ?: "")
            answers.add(opt4 ?: "")
            answers.add(opt5 ?: "")
            answers.add(opt6 ?: "")
            answers.add(opt7 ?: "")
            answers.add(opt8 ?: "")
            answers.add(opt9 ?: "")

            for (i in answers.indices.reversed()) {
                if (answers[i].length <= 0) {
                    answers.removeAt(i)
                }
            }

            return answers
        }

    constructor() {
        id = "0"
        subject = ""
        level = ""
        question = ""
        optionsnumber = 0
        nB_CORRECT_ANS = 1
        opt0 = ""
        opt1 = ""
        opt2 = ""
        opt3 = ""
        opt4 = ""
        opt5 = ""
        opt6 = ""
        opt7 = ""
        opt8 = ""
        opt9 = ""
        IMAGE = "none"
    }

    constructor(lEVEL: String, qUESTION: String, oPT0: String, oPT1: String, oPT2: String, oPT3: String, oPT4: String,
                oPT5: String, oPT6: String, oPT7: String, oPT8: String, oPT9: String, iMAGE: String) {

        level = lEVEL
        question = qUESTION
        opt0 = oPT0
        opt1 = oPT1
        opt2 = oPT2
        opt3 = oPT3
        opt4 = oPT4
        opt5 = oPT5
        opt6 = oPT6
        opt7 = oPT7
        opt8 = oPT8
        opt9 = oPT9
        if (iMAGE.length == 0) {
            IMAGE = "none"
        } else {
            IMAGE = iMAGE
        }
        var i = 1
        if (oPT1.length > 0) i++
        if (oPT2.length > 0) i++
        if (oPT3.length > 0) i++
        if (oPT4.length > 0) i++
        if (oPT5.length > 0) i++
        if (oPT6.length > 0) i++
        if (oPT7.length > 0) i++
        if (oPT8.length > 0) i++
        if (oPT9.length > 0) i++
        optionsnumber = i
    }
}
