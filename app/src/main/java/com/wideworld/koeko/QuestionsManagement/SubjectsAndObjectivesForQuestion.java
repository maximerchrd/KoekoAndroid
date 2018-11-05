package com.wideworld.koeko.QuestionsManagement;

public class SubjectsAndObjectivesForQuestion {
    private String[] objectives;
    private String[] subjects;

    public String[] getObjectives() {
        return objectives;
    }

    public String[] getSubjects() {
        return subjects;
    }

    public SubjectsAndObjectivesForQuestion(String[] objectives, String[] subjects) {
        this.objectives = objectives;
        this.subjects = subjects;
    }
    public SubjectsAndObjectivesForQuestion() {
        this.objectives = new String[0];
        this.subjects = new String[0];
    }
}
