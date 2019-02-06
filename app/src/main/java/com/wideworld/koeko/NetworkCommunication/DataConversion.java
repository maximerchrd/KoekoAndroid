package com.wideworld.koeko.NetworkCommunication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.QuestionView;
import com.wideworld.koeko.QuestionsManagement.SubjectsAndObjectivesForQuestion;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.QuestionsManagement.TestView;
import com.wideworld.koeko.Tools.StringTools;
import com.wideworld.koeko.database_management.DbHelper;
import com.wideworld.koeko.database_management.DbTableLearningObjective;
import com.wideworld.koeko.database_management.DbTableRelationQuestionObjective;
import com.wideworld.koeko.database_management.DbTableRelationQuestionQuestion;
import com.wideworld.koeko.database_management.DbTableRelationQuestionSubject;
import com.wideworld.koeko.database_management.DbTableRelationTestObjective;
import com.wideworld.koeko.database_management.DbTableSubject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by maximerichard on 10/02/17.
 * Class to transform data from one format to another
 * e.g.: byte array to MultipleChoiceQuestion
 */
public class DataConversion {
    static public int prefixSize = 80;
    Context mContext = null;
    private String lastConvertedQuestionText = "";
    public DataConversion(Context arg_context) {
        mContext = arg_context;
    }

    public QuestionView bytearrayToQuestionView(byte[] buffer_for_whole_question) {
        QuestionView questionView = new QuestionView();
        ObjectMapper mapper = new ObjectMapper();
        String stringJson = "";
        try {
            stringJson = new String(buffer_for_whole_question, "UTF-8");
            questionView = mapper.readValue(stringJson, QuestionView.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return questionView;
    }
    public QuestionShortAnswer bytearrayvectorToShortAnswerQuestion(byte[] buffer_for_whole_question) {
        QuestionShortAnswer question_to_return = new QuestionShortAnswer();

        byte [] buffer_for_prefix = new byte[prefixSize];
        for (int i = 0; i < prefixSize; i++) {
            buffer_for_prefix[i] = buffer_for_whole_question[i];
        }
        String sizes = null;
        try {
            sizes = new String(buffer_for_prefix, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int size_of_image = Integer.parseInt(sizes.split(":")[1]);
        int size_of_text = Integer.parseInt(sizes.split(":")[2].replaceAll("\\D+",""));

        byte [] buffer_for_text = new byte[size_of_text];
        for (int i = 0; i < size_of_text; i++) {
            buffer_for_text[i] = buffer_for_whole_question[i+prefixSize];
        }

        byte [] buffer_for_image = new byte[size_of_image];
        for (int i = 0; i < size_of_image; i++) {
            buffer_for_image[i] = buffer_for_whole_question[i+prefixSize+size_of_text];
        }

        ByteArrayInputStream imageStream = new ByteArrayInputStream(buffer_for_image);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

        String question_text = "";
        try {
            question_text =  new String(buffer_for_text, "UTF-8");
            lastConvertedQuestionText = "SHRTA" + "///";
            lastConvertedQuestionText += question_text;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        question_to_return.setQuestion(question_text.split("///")[0]);
        String ID_string = "";
        if (question_text.split("///").length > 1) {
            ID_string = question_text.split("///")[1];
            question_to_return.setId(ID_string);
        } else { Log.w("reading quest buffer", "no ID"); }
        if (question_text.split("///").length > 2) {
            String[] answers = question_text.split("///")[2].split("\\|\\|\\|");
            ArrayList<String> answersList = new ArrayList<>();
            for (int i = 0; i < answers.length; i++) {
                answersList.add(answers[i]);
            }
            question_to_return.setAnswers(answersList);
        } else { Log.w("reading quest buffer", "no answers"); }
        if (question_text.split("///").length > 5) {
            question_to_return.setImage(question_text.split("///")[5]); //because inbetween come subjects and objectives
            SaveImageFile(bitmap, question_text.split("///")[5]);
        } else { Log.w("reading quest buffer", "no image indication"); }
        if (question_text.split("///").length > 3) {
            //deal with subjects
            String subjectsText = question_text.split("///")[3];
            String[] subjects = subjectsText.split("\\|\\|\\|");
            for (int i = 0; i < subjects.length; i++) {
                try {
                    DbTableSubject.addSubject(subjects[i]);
                    DbTableRelationQuestionSubject.addRelationQuestionSubject(ID_string, subjects[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else { Log.w("reading quest buffer", "no subject indication"); }

        if (question_text.split("///").length > 4) {
            //deal with learning objectives
            String learningObjectivesText = question_text.split("///")[4];
            String[] learningObjectives = learningObjectivesText.split("\\|\\|\\|");
            for (int i = 0; i < learningObjectives.length; i++) {
                try {
                    DbTableLearningObjective.addLearningObjective(learningObjectives[i], -1);
                    DbTableRelationQuestionObjective.addQuestionObjectiverRelation(learningObjectives[i], ID_string);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else { Log.w("reading quest buffer", "no objectives indication"); }

        return question_to_return;
    }

    public SubjectsAndObjectivesForQuestion bytearrayvectorToSubjectsNObjectives(byte[] byteData) {
        SubjectsAndObjectivesForQuestion sNo = null;
        ObjectMapper mapper = new ObjectMapper();
        String stringJson;
        try {
            stringJson = new String(byteData, "UTF-8");
            sNo = mapper.readValue(stringJson, SubjectsAndObjectivesForQuestion.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sNo;
    }

    public static QuestionMultipleChoice textToQuestionMultipleChoice(String questionText) {
        questionText = questionText.substring(8);
        QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
        questionMultipleChoice.setQuestion(questionText.split("///")[0]);
        if (questionText.split("///").length > 15) {
            questionMultipleChoice.setOpt0(questionText.split("///")[1]);
            questionMultipleChoice.setOpt1(questionText.split("///")[2]);
            questionMultipleChoice.setOpt2(questionText.split("///")[3]);
            questionMultipleChoice.setOpt3(questionText.split("///")[4]);
            questionMultipleChoice.setOpt4(questionText.split("///")[5]);
            questionMultipleChoice.setOpt5(questionText.split("///")[6]);
            questionMultipleChoice.setOpt6(questionText.split("///")[7]);
            questionMultipleChoice.setOpt7(questionText.split("///")[8]);
            questionMultipleChoice.setOpt8(questionText.split("///")[9]);
            questionMultipleChoice.setOpt9(questionText.split("///")[10]);
            String ID_string = questionText.split("///")[11];
            questionMultipleChoice.setId(ID_string);
            questionMultipleChoice.setNB_CORRECT_ANS(Integer.parseInt(questionText.split("///")[12]));
            questionMultipleChoice.setImage(questionText.split("///")[15]); //14 because inbetween come subjects and objectives

            //deal with subjects
            String subjectsText = questionText.split("///")[13];
            String[] subjects = subjectsText.split("\\|\\|\\|");
            for (int i = 0; i < subjects.length; i++) {
                try {
                    DbTableSubject.addSubject(subjects[i]);
                    DbTableRelationQuestionSubject.addRelationQuestionSubject(ID_string, subjects[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //deal with learning objectives
            String learningObjectivesText = questionText.split("///")[14];
            String[] learningObjectives = learningObjectivesText.split("\\|\\|\\|");
            for (int i = 0; i < learningObjectives.length; i++) {
                try {
                    DbTableLearningObjective.addLearningObjective(learningObjectives[i], -1);
                    DbTableRelationQuestionObjective.addQuestionObjectiverRelation(learningObjectives[i], ID_string);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else { Log.w("reading mcq buffer", "question text not complete (array after split is too short)"); }

        return questionMultipleChoice;
    }

    public static QuestionShortAnswer textToQuestionShortAnswere(String questionText) {
        questionText = questionText.substring(8);
        QuestionShortAnswer questionShortAnswer = new QuestionShortAnswer();
        questionShortAnswer.setQuestion(questionText.split("///")[0]);
        if (questionText.split("///").length > 5) {
            String ID_string = questionText.split("///")[1];
            questionShortAnswer.setId(ID_string);
            ArrayList<String> answers = new ArrayList<>(Arrays.asList(questionText.split("///")[2].split("\\|\\|\\|")));
            questionShortAnswer.setAnswers(answers);

            questionShortAnswer.setImage(questionText.split("///")[5]); //14 because inbetween come subjects and objectives

            //deal with subjects
            String subjectsText = questionText.split("///")[3];
            String[] subjects = subjectsText.split("\\|\\|\\|");
            for (int i = 0; i < subjects.length; i++) {
                try {
                    DbTableSubject.addSubject(subjects[i]);
                    DbTableRelationQuestionSubject.addRelationQuestionSubject(ID_string, subjects[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //deal with learning objectives
            String learningObjectivesText = questionText.split("///")[4];
            String[] learningObjectives = learningObjectivesText.split("\\|\\|\\|");
            for (int i = 0; i < learningObjectives.length; i++) {
                try {
                    DbTableLearningObjective.addLearningObjective(learningObjectives[i], -1);
                    DbTableRelationQuestionObjective.addQuestionObjectiverRelation(learningObjectives[i], ID_string);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else { Log.w("reading mcq buffer", "question text not complete (array after split is too short)"); }

        return questionShortAnswer;
    }

    public Test byteToTest(byte[] byteArray) {
        TestView testView = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String stringJson;
        try {
            stringJson = new String(byteArray, "UTF-8");
            testView = mapper.readValue(stringJson, TestView.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Test test = new Test();
        test.setIdGlobal(Long.valueOf(testView.getIdTest()));
        test.setTestName(testView.getTestName());
        if (testView.getMediaFileName() != null) {
            test.setMediaFileName(testView.getMediaFileName());
        } else {
            test.setMediaFileName("");
        }
        test.setMedalsInstructionsString(testView.getMedalInstructions());
        test.setTestUpdate(Timestamp.valueOf(testView.getUpdateTime()));

        //read objectives
        try {
            String[] objectives = testView.getObjectives().split("\\|\\|\\|");
            for (String objective : objectives) {
                DbTableRelationTestObjective.insertRelationTestObjective(String.valueOf(test.getIdGlobal()), objective);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("WifiCommunication", "ArrayOutOfBound when parsing objectives from: " + testView.getObjectives());
            e.printStackTrace();
        }

        //build test map
        String[] questionRelation = testView.getTestMap().split("\\|\\|\\|");
        for (String relation : questionRelation) {
            String[] relationSplit = relation.split(";;;");
            String questionId = relationSplit[0];
            test.getQuestionsIDs().add(questionId);
            for (int i = 1; i < relationSplit.length; i++) {
                try {
                    String[] array = relationSplit[i].split(":::");
                    DbTableRelationQuestionQuestion.insertRelationQuestionQuestion(StringTools.stringToLongID(questionId),
                            StringTools.stringToLongID(array[0]), test.getTestName(),
                            array[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    //ERROR HERE
                    Log.e("WifiCommunication", "Array out of bound when inserting the condition for insertRelationQuestionQuestion");
                    e.printStackTrace();
                }
            }
        }

        return test;
    }

    public ArrayList<String> bytesToIdsList(byte[] data) {
        ArrayList<String> idsList = new ArrayList<>();
        try {
            String stringIds = new String(data, "UTF-8");
            idsList.addAll(new ArrayList<>(Arrays.asList(stringIds.split("\\|"))));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return idsList;
    }

    static public byte[] getPrefixFromString(String prefix) {
        byte[] prefixBytes = new byte[DataConversion.prefixSize];
        for (int i = 0; i < prefix.getBytes().length && i < 80; i++) {
            prefixBytes[i] = prefix.getBytes()[i];
        }
        return prefixBytes;
    }

    private void SaveImageFile(Bitmap imageToSave, String fileName) {

        File directory = new File(mContext.getFilesDir(),"images");
        String path = mContext.getFilesDir().getAbsolutePath();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory,fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            if (imageToSave != null) {
                FileOutputStream out = new FileOutputStream(file);
                imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } else {
                Log.v("Writing image to file", "Bitmap is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getLastConvertedQuestionText() {
        return lastConvertedQuestionText;
    }

}
