package com.LearningTracker.LearningTrackerApp.QuestionsManagement;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 26.10.17.
 */
public class QuestionMultipleChoice {
    private int ID;
    private String SUBJECT;
    private String LEVEL;
    private String QUESTION;

    /**
     * OPTIONSNUMBER: total number of choices for the answer
     */
    private int OPTIONSNUMBER;
    /**
     * NB_CORRECT_ANS: number of correct answers
     */
    private int NB_CORRECT_ANS;
    private String OPT0;
    private String OPT1;
    private String OPT2;
    private String OPT3;
    private String OPT4;
    private String OPT5;
    private String OPT6;
    private String OPT7;
    private String OPT8;
    private String OPT9;
    private String IMAGE;
    private String TRIAL0;
    private String TRIAL1;
    private String TRIAL2;
    private String TRIAL3;
    private String TRIAL4;
    private String TRIAL5;
    private String TRIAL6;
    private String TRIAL7;
    private String TRIAL8;
    private String TRIAL9;
    private Vector<String> subjects;
    private Vector<String> objectives;
    public QuestionMultipleChoice()	{
        ID=0;
        SUBJECT="";
        LEVEL="";
        QUESTION="";
        OPTIONSNUMBER=0;
        NB_CORRECT_ANS=1;
        OPT0="";
        OPT1="";
        OPT2="";
        OPT3="";
        OPT4="";
        OPT5="";
        OPT6="";
        OPT7="";
        OPT8="";
        OPT9="";
        TRIAL0 = "0";
        TRIAL1 = "0";
        TRIAL2 = "0";
        TRIAL3 = "0";
        TRIAL4 = "0";
        TRIAL5 = "0";
        TRIAL6 = "0";
        TRIAL7 = "0";
        TRIAL8 = "0";
        TRIAL9 = "0";
        IMAGE="none";
    }
    public QuestionMultipleChoice(String lEVEL, String qUESTION, String oPT0, String oPT1, String oPT2, String oPT3, String oPT4,
                                  String oPT5, String oPT6, String oPT7, String oPT8, String oPT9, String iMAGE) {

        LEVEL = lEVEL;
        QUESTION = qUESTION;
        OPT0 = oPT0;
        OPT1 = oPT1;
        OPT2 = oPT2;
        OPT3 = oPT3;
        OPT4 = oPT4;
        OPT5 = oPT5;
        OPT6 = oPT6;
        OPT7 = oPT7;
        OPT8 = oPT8;
        OPT9 = oPT9;
        TRIAL0 = "0";
        TRIAL1 = "0";
        TRIAL2 = "0";
        TRIAL3 = "0";
        TRIAL4 = "0";
        TRIAL5 = "0";
        TRIAL6 = "0";
        TRIAL7 = "0";
        TRIAL8 = "0";
        TRIAL9 = "0";
        if (iMAGE.length() == 0) {
            IMAGE = "none";
        } else {
            IMAGE = iMAGE;
        }
        int i = 1;
        if (oPT1.length() > 0) i++;
        if (oPT2.length() > 0) i++;
        if (oPT3.length() > 0) i++;
        if (oPT4.length() > 0) i++;
        if (oPT5.length() > 0) i++;
        if (oPT6.length() > 0) i++;
        if (oPT7.length() > 0) i++;
        if (oPT8.length() > 0) i++;
        if (oPT9.length() > 0) i++;
        OPTIONSNUMBER = i;
    }
    public int getID()
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
    public int getOPTIONSNUMBER() {
        return OPTIONSNUMBER;
    }

    public String getOPT0() {
        return OPT0;
    }
    public String getOPT1() {
        return OPT1;
    }
    public String getOPT2() {
        return OPT2;
    }
    public String getOPT3() {
        return OPT3;
    }
    public String getOPT4() {
        return OPT4;
    }
    public String getOPT5() {
        return OPT5;
    }
    public String getOPT6() {
        return OPT6;
    }
    public String getOPT7() {
        return OPT7;
    }
    public String getOPT8() {
        return OPT8;
    }
    public String getOPT9() {
        return OPT9;
    }
    public String getTRIAL0() {
        return TRIAL0;
    }
    public String getTRIAL1() {
        return TRIAL1;
    }
    public String getTRIAL2() {
        return TRIAL2;
    }
    public String getTRIAL3() {
        return TRIAL3;
    }
    public String getTRIAL4() {
        return TRIAL4;
    }
    public String getTRIAL5() {
        return TRIAL5;
    }
    public String getTRIAL6() {
        return TRIAL6;
    }
    public String getTRIAL7() {
        return TRIAL7;
    }
    public String getTRIAL8() {
        return TRIAL8;
    }
    public String getTRIAL9() {
        return TRIAL9;
    }
    public int getNB_CORRECT_ANS() {
        return NB_CORRECT_ANS;
    }
    public String getIMAGE() {
        return IMAGE;
    }
    public Vector<String> getSubjects() {
        return subjects;
    }
    public Vector<String> getObjectives() {
        return objectives;
    }
    public void setID(int id)
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
    public void setOPTIONSNUMBER(int oPTIONSNUMBER) {
        OPTIONSNUMBER = oPTIONSNUMBER;
    }
    public void setOPT0(String oPT0) {
        OPT0 = oPT0;
    }
    public void setOPT1(String oPT1) {
        OPT1 = oPT1;
    }
    public void setOPT2(String oPT2) {
        OPT2 = oPT2;
    }
    public void setOPT3(String oPT3) {
        OPT3 = oPT3;
    }
    public void setOPT4(String oPT4) {
        OPT4 = oPT4;
    }
    public void setOPT5(String oPT5) {
        OPT5 = oPT5;
    }
    public void setOPT6(String oPT6) {
        OPT6 = oPT6;
    }
    public void setOPT7(String oPT7) {
        OPT7 = oPT7;
    }
    public void setOPT8(String oPT8) {
        OPT8 = oPT8;
    }
    public void setOPT9(String oPT9) {
        OPT9 = oPT9;
    }
    public void setTRIAL0(String tRIAL0) {
        TRIAL0 = tRIAL0;
    }
    public void setTRIAL1(String tRIAL1) {
        TRIAL1 = tRIAL1;
    }
    public void setTRIAL2(String tRIAL2) {
        TRIAL2 = tRIAL2;
    }
    public void setTRIAL3(String tRIAL3) {
        TRIAL3 = tRIAL3;
    }
    public void setTRIAL4(String tRIAL4) {
        TRIAL4 = tRIAL4;
    }
    public void setTRIAL5(String tRIAL5) {
        TRIAL5 = tRIAL5;
    }
    public void setTRIAL6(String tRIAL6) {
        TRIAL6 = tRIAL6;
    }
    public void setTRIAL7(String tRIAL7) {
        TRIAL7 = tRIAL7;
    }
    public void setTRIAL8(String tRIAL8) {
        TRIAL8 = tRIAL8;
    }
    public void setTRIAL9(String tRIAL9) {
        TRIAL9 = tRIAL9;
    }
    public void setNB_CORRECT_ANS(int NB_CORRECT_ANS) {
        this.NB_CORRECT_ANS = NB_CORRECT_ANS;
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

    public ArrayList<String> getPossibleAnswers() {
        ArrayList<String> answers = new ArrayList<>();
        answers.add(OPT0);
        answers.add(OPT1);
        answers.add(OPT2);
        answers.add(OPT3);
        answers.add(OPT4);
        answers.add(OPT5);
        answers.add(OPT6);
        answers.add(OPT7);
        answers.add(OPT8);
        answers.add(OPT9);

        for (int i = answers.size() - 1; i >= 0; i--) {
            if (answers.get(i).length() <= 0) {
                answers.remove(i);
            }
        }

        return  answers;
    }
}
