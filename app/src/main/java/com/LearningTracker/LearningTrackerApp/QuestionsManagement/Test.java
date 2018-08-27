package com.LearningTracker.LearningTrackerApp.QuestionsManagement;

import android.app.Activity;
import android.util.Log;
import android.widget.Chronometer;

import com.LearningTracker.LearningTrackerApp.LTApplication;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableQuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.database_management.DbTableRelationQuestionQuestion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import static android.content.ContentValues.TAG;

/**
 * Created by maximerichard on 15.05.18.
 */
public class Test {
    private Long idGlobal = 0L;
    private String testName = "";
    private String testType = "";
    private Vector<String> questionsIDs;
    private Map<String, String> idMapRelation;
    private Map<String,QuestionMultipleChoice> idMapQmc;
    private Map<String,QuestionShortAnswer> idMapShrtaq;
    private Vector<String> activeQuestionIds;
    private Vector<String> answeredQuestionIds;
    private String medalsInstructionsString = "";
    private Vector<Vector<String>> medalsInstructions;

    public Test() {
        questionsIDs = new Vector<>();
        idMapRelation = new LinkedHashMap<>();
        idMapQmc = new LinkedHashMap<>();
        idMapShrtaq = new LinkedHashMap<>();
        activeQuestionIds = new Vector<>();
        answeredQuestionIds = new Vector<>();
        medalsInstructions = new Vector<>();
    }

    //getters
    public Long getIdGlobal() {
        return idGlobal;
    }
    public String getTestName() {
        return testName;
    }
    public String getTestType() {
        return testType;
    }
    public Vector<String> getQuestionsIDs() {
        return questionsIDs;
    }
    public Map<String, String> getIdMapRelation() {
        return idMapRelation;
    }
    public Map<String, QuestionMultipleChoice> getIdMapQmc() {
        return idMapQmc;
    }
    public Map<String, QuestionShortAnswer> getIdMapShrtaq() {
        return idMapShrtaq;
    }
    public Vector<String> getActiveQuestionIds() {
        return activeQuestionIds;
    }
    public Vector<String> getAnsweredQuestionIds() {
        return answeredQuestionIds;
    }
    public String getMedalsInstructionsString() {
        return medalsInstructionsString;
    }

    //setters
    public void setIdGlobal(Long idGlobal) {
        this.idGlobal = idGlobal;
    }
    public void setTestName(String testName) {
        this.testName = testName;
    }
    public void setTestType(String testType) {
        this.testType = testType;
    }
    public void setQuestionsIDs(Vector<String> questionsIDs) {
        this.questionsIDs = questionsIDs;
    }
    public void setIdMapRelation(Map<String, String> idMapRelation) {
        this.idMapRelation = idMapRelation;
    }
    public void setIdMapQmc(Map<String, QuestionMultipleChoice> idMapQmc) {
        this.idMapQmc = idMapQmc;
    }
    public void setIdMapShrtaq(Map<String, QuestionShortAnswer> idMapShrtaq) {
        this.idMapShrtaq = idMapShrtaq;
    }
    public void setActiveQuestionIds(Vector<String> activeQuestionIds) {
        this.activeQuestionIds = activeQuestionIds;
    }
    public void setAnsweredQuestionIds(Vector<String> answeredQuestionIds) {
        this.answeredQuestionIds = answeredQuestionIds;
    }
    public void setMedalsInstructionsString(String medalsInstructionsString) {
        this.medalsInstructionsString = medalsInstructionsString;
    }
    public void setMedalsInstructions(Vector<Vector<String>> medalsInstructions) {
        this.medalsInstructions = medalsInstructions;
    }

    public String serializeQuestionIDs() {
        String IDs = "";

        for (String id : questionsIDs) {
            IDs += id + "|";
        }

        return IDs;
    }

    public String[] arrayOfQuestionIDs() {
        String[] IDs = new String[questionsIDs.size()];

        for (int i = 0; i < questionsIDs.size(); i++) {
            IDs[i] = questionsIDs.get(i);
        }

        return IDs;
    }

    public Vector<Vector<String>> getMedalsInstructions() {
        if (medalsInstructions.size() == 0) {
            parseMedalsInstructions();
        }
        return medalsInstructions;
    }

    private void parseMedalsInstructions() {
        String[] instructions = medalsInstructionsString.split(";");
        if (instructions.length == 3) {
            for (String instruction : instructions) {
                Vector<String> couple = new Vector<>();
                couple.add(instruction.split(":")[1].split("/")[0]);
                couple.add(instruction.split(":")[1].split("/")[1]);
                medalsInstructions.add(couple);
            }
        }
    }

    /**
     * loads the map of the test from the db into the "Map" objects. After this method,
     * you should be able to have find the objects with the ids in idMapQmc and idMapShrtaq,
     * and the relation for each question in idMapRelation
     */
    public void loadMap() {
        //loading into idMapRelation
        Vector<String[]> testMap = DbTableRelationQuestionQuestion.getTestMapForTest(testName);
        for (String[] relation : testMap) {
            if (relation[3].length() > 0) {
                idMapRelation.put(relation[0] + "|" + relation[1], relation[3]);
            }
        }

        //loading into idMapQmc and idMapShrtaq
        for (String id : questionsIDs) {
            QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionWithId(id);
            if (questionMultipleChoice.getQUESTION().length() <= 0) {
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id);
                idMapShrtaq.put(id, questionShortAnswer);
            } else {
                idMapQmc.put(id, questionMultipleChoice);
            }
        }

        initializeActiveIds();
    }

    private void initializeActiveIds() {
        activeQuestionIds = (Vector<String>) questionsIDs.clone();
        for (Map.Entry<String, String> entry : idMapRelation.entrySet()) {
            if (entry.getValue().length() > 0) {
                activeQuestionIds.remove(entry.getKey().split("\\|")[1]);
            }
        }
    }

    public void addResultAndRefreshActiveIDs(String id, String result) {
        for (Map.Entry<String, String> entry : idMapRelation.entrySet()) {
            if (entry.getKey().split("\\|")[0].contentEquals(id)) {
                if (entry.getValue().contains("EVALUATION<")) {
                    Double threshold = Double.valueOf(entry.getValue().split("<")[1]);
                    if (Double.valueOf(result) < threshold) {
                        activeQuestionIds.add(entry.getKey().split("\\|")[1]);
                    }
                }
            }
        }

        LTApplication.currentTestActivitySingleton.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LTApplication.currentTestActivitySingleton.getmAdapter().notifyDataSetChanged();
            }
        });

    }
}