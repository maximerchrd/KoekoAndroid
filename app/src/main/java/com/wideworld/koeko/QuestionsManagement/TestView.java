package com.wideworld.koeko.QuestionsManagement;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by maximerichard on 07.09.18.
 */
public class TestView {
    private String testName;
    private String idTest;
    private String testMap;
    private Integer testMode;
    private String medalInstructions;
    private String mediaFileName;
    private String objectives;
    private String updateTime;


    //getters
    public String getTestName() {
        return testName;
    }

    public String getIdTest() {
        return idTest;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getTestMap() {
        return testMap;
    }

    public void setTestMap(String testMap) {
        this.testMap = testMap;
    }

    public Integer getTestMode() {
        return testMode;
    }

    public String getObjectives() {
        return objectives;
    }

    public String getMediaFileName() {
        return mediaFileName;
    }

    //setter
    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setIdTest(String idTest) {
        this.idTest = idTest;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setTestMode(Integer testMode) {
        this.testMode = testMode;
    }

    public String getMedalInstructions() {
        return medalInstructions;
    }

    public void setMedalInstructions(String medalInstructions) {
        this.medalInstructions = medalInstructions;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public void setMediaFileName(String mediaFileName) {
        this.mediaFileName = mediaFileName;
    }
}
