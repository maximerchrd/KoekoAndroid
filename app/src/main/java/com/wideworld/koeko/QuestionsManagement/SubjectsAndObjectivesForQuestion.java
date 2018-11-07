package com.wideworld.koeko.QuestionsManagement;

public class SubjectsAndObjectivesForQuestion {
    private String[] objectives;
    private String[] subjects;
    private String questionId;

    public String[] getObjectives() {
        return objectives;
    }

    public String[] getSubjects() {
        return subjects;
    }

    public String getQuestionId() {
        return questionId;
    }

    public SubjectsAndObjectivesForQuestion(String[] objectives, String[] subjects, String questionId) {
        this.objectives = objectives;
        this.subjects = subjects;
        this.questionId = questionId;
    }
    public SubjectsAndObjectivesForQuestion() {
        this.objectives = new String[0];
        this.subjects = new String[0];
    }
}
