package com.wideworld.koeko.QuestionsManagement;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 26.10.17.
 */
public class QuestionShortAnswer {
    private String ID;
    private String SUBJECT;
    private String LEVEL;
    private String QUESTION;
    private String IMAGE;
    private ArrayList<String> answers;

    private Vector<String> subjects;
    private Vector<String> objectives;
    public QuestionShortAnswer()	{
        ID="0";
        SUBJECT="";
        LEVEL="";
        QUESTION="";
        IMAGE="none";
        answers = new  ArrayList<>();
    }
    public QuestionShortAnswer(String lEVEL, String qUESTION, String iMAGE) {

        LEVEL = lEVEL;
        QUESTION = qUESTION;
        if (iMAGE.length() == 0) {
            IMAGE = "none";
        } else {
            IMAGE = iMAGE;
        }
        answers = new  ArrayList<>();
    }
    public String getID()
    {
        return ID;
    }
    public String getSUBJECT() {
        return SUBJECT;
    }
    public String getLEVEL() {
        return LEVEL;
    }
    public String getQUESTION() {
        return QUESTION;
    }
    public String getIMAGE() {
        return IMAGE;
    }
    public ArrayList<String> getAnswers() {
        return answers;
    }
    public Vector<String> getSubjects() {
        return subjects;
    }
    public Vector<String> getObjectives() {
        return objectives;
    }
    public void setID(String id)
    {
        ID=id;
    }
    public void setSUBJECT(String sUBJECT) {
        SUBJECT = sUBJECT;
    }
    public void setLEVEL(String lEVEL) {
        LEVEL = lEVEL;
    }
    public void setQUESTION(String qUESTION) {
        QUESTION = qUESTION;
    }
    public void setIMAGE(String iMAGE) {
        if (iMAGE.length() == 0) {
            IMAGE = "none";
        } else {
            IMAGE = iMAGE;
        }
    }
    public void setSubjects(Vector<String> subjects) {
        this.subjects = subjects;
    }
    public void setObjectives(Vector<String> objectives) {
        this.objectives = objectives;
    }
    public void setAnswers(ArrayList<String> answers) {
        this.answers = answers;
    }
}
