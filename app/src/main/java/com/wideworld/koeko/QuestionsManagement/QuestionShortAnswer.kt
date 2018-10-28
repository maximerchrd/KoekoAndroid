package com.wideworld.koeko.QuestionsManagement

import java.util.ArrayList
import java.util.Vector

/**
 * Created by maximerichard on 26.10.17.
 */
class QuestionShortAnswer {
    var id: String? = null
    var subject: String? = null
    var level: String? = null
    var question: String? = null
    private var IMAGE: String? = null
    var answers: ArrayList<String>? = null

    var subjects: Vector<String>? = null
    var objectives: Vector<String>? = null
    var image: String?
        get() = IMAGE
        set(iMAGE) = if (iMAGE != null && iMAGE.length == 0) {
            IMAGE = "none"
        } else {
            IMAGE = iMAGE
        }

    constructor() {
        id = "0"
        subject = ""
        level = ""
        question = ""
        IMAGE = "none"
        answers = ArrayList()
    }

    constructor(lEVEL: String, qUESTION: String, iMAGE: String) {

        level = lEVEL
        question = qUESTION
        if (iMAGE.length == 0) {
            IMAGE = "none"
        } else {
            IMAGE = iMAGE
        }
        answers = ArrayList()
    }
}
