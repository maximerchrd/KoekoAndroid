package com.wideworld.koeko.QuestionsManagement;

import java.sql.Timestamp;

public class QuestionView {
    private String ID;
    private int TYPE;   //0: QMC; 1: SHRTAQ; 2: TEST
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
    private String LANGUAGE;



    private String QCM_MUID;
    private Timestamp QCM_UPD_TMS;


    public QuestionView()	{
        ID="0";
        TYPE=-1;
        SUBJECT="";
        LEVEL="-1";
        QUESTION="";
        OPTIONSNUMBER=0;
        NB_CORRECT_ANS=0;
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
        IMAGE="none";
        LANGUAGE="";
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getTYPE() {
        return TYPE;
    }

    public void setTYPE(int TYPE) {
        this.TYPE = TYPE;
    }

    public String getSUBJECT() {
        return SUBJECT;
    }

    public void setSUBJECT(String SUBJECT) {
        this.SUBJECT = SUBJECT;
    }

    public String getLEVEL() {
        return LEVEL;
    }

    public void setLEVEL(String LEVEL) {
        this.LEVEL = LEVEL;
    }

    public String getQUESTION() {
        return QUESTION;
    }

    public void setQUESTION(String QUESTION) {
        this.QUESTION = QUESTION;
    }

    public int getOPTIONSNUMBER() {
        return OPTIONSNUMBER;
    }

    public void setOPTIONSNUMBER(int OPTIONSNUMBER) {
        this.OPTIONSNUMBER = OPTIONSNUMBER;
    }

    public int getNB_CORRECT_ANS() {
        return NB_CORRECT_ANS;
    }

    public void setNB_CORRECT_ANS(int NB_CORRECT_ANS) {
        this.NB_CORRECT_ANS = NB_CORRECT_ANS;
    }

    public String getOPT0() {
        return OPT0;
    }

    public void setOPT0(String OPT0) {
        this.OPT0 = OPT0;
    }

    public String getOPT1() {
        return OPT1;
    }

    public void setOPT1(String OPT1) {
        this.OPT1 = OPT1;
    }

    public String getOPT2() {
        return OPT2;
    }

    public void setOPT2(String OPT2) {
        this.OPT2 = OPT2;
    }

    public String getOPT3() {
        return OPT3;
    }

    public void setOPT3(String OPT3) {
        this.OPT3 = OPT3;
    }

    public String getOPT4() {
        return OPT4;
    }

    public void setOPT4(String OPT4) {
        this.OPT4 = OPT4;
    }

    public String getOPT5() {
        return OPT5;
    }

    public void setOPT5(String OPT5) {
        this.OPT5 = OPT5;
    }

    public String getOPT6() {
        return OPT6;
    }

    public void setOPT6(String OPT6) {
        this.OPT6 = OPT6;
    }

    public String getOPT7() {
        return OPT7;
    }

    public void setOPT7(String OPT7) {
        this.OPT7 = OPT7;
    }

    public String getOPT8() {
        return OPT8;
    }

    public void setOPT8(String OPT8) {
        this.OPT8 = OPT8;
    }

    public String getOPT9() {
        return OPT9;
    }

    public void setOPT9(String OPT9) {
        this.OPT9 = OPT9;
    }

    public String getIMAGE() {
        return IMAGE;
    }

    public void setIMAGE(String IMAGE) {
        this.IMAGE = IMAGE;
    }

    public String getLANGUAGE() {
        return LANGUAGE;
    }

    public void setLANGUAGE(String LANGUAGE) {
        this.LANGUAGE = LANGUAGE;
    }

    public String getQCM_MUID() {
        return QCM_MUID;
    }

    public void setQCM_MUID(String QCM_MUID) {
        this.QCM_MUID = QCM_MUID;
    }

    public Timestamp getQCM_UPD_TMS() {
        return QCM_UPD_TMS;
    }

    public void setQCM_UPD_TMS(Timestamp QCM_UPD_TMS) {
        this.QCM_UPD_TMS = QCM_UPD_TMS;
    }
}

