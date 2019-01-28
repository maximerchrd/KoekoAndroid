package com.wideworld.koeko.QuestionsManagement

import java.io.Serializable
import java.sql.Date
import java.time.LocalDate

class Homework : Serializable {
    var uid = ""
    var name = ""
    var idCode = ""
    var dueDate = ""
    var questions = ArrayList<String>()
    var resources = ArrayList<String>()
}