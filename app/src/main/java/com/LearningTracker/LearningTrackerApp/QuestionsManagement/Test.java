package com.LearningTracker.LearningTrackerApp.QuestionsManagement;

import java.util.Vector;

/**
 * Created by maximerichard on 15.05.18.
 */
public class Test {
    private Long idGlobal = 0L;
    private String testName = "";
    private Vector<String> questionsIDs;

    Test() {
        questionsIDs = new Vector<>();
    }

    //getters
    public Long getIdGlobal() {
        return idGlobal;
    }
    public String getTestName() {
        return testName;
    }
    public Vector<String> getQuestionsIDs() {
        return questionsIDs;
    }

    //setters
    public void setIdGlobal(Long idGlobal) {
        this.idGlobal = idGlobal;
    }
    public void setTestName(String testName) {
        this.testName = testName;
    }
    public void setQuestionsIDs(Vector<String> questionsIDs) {
        this.questionsIDs = questionsIDs;
    }

    public String serializeQuestionIDs() {
        String IDs = "";

        for (String id : questionsIDs) {
            IDs += id + "|";
        }

        return IDs;
    }
}