package com.wideworld.koeko.NetworkCommunication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.database_management.DbTableLearningObjective;
import com.wideworld.koeko.database_management.DbTableRelationQuestionObjective;
import com.wideworld.koeko.database_management.DbTableRelationQuestionSubject;
import com.wideworld.koeko.database_management.DbTableSubject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by maximerichard on 10/02/17.
 * Class to transform data from one format to another
 * e.g.: byte array to MultipleChoiceQuestion
 */
public class DataConversion {
    Context mContext = null;
    private String lastConvertedQuestionText = "";
    public DataConversion(Context arg_context) {
        mContext = arg_context;
    }

    public QuestionMultipleChoice bytearrayvectorToMultChoiceQuestion(byte[] buffer_for_whole_question) {
        QuestionMultipleChoice question_to_return = new QuestionMultipleChoice();

        byte [] buffer_for_prefix = new byte[80];
        for (int i = 0; i < 80; i++) {
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
            buffer_for_text[i] = buffer_for_whole_question[i+80];
        }

        byte [] buffer_for_image = new byte[size_of_image];
        for (int i = 0; i < size_of_image; i++) {
            buffer_for_image[i] = buffer_for_whole_question[i+80+size_of_text];
        }

        ByteArrayInputStream imageStream = new ByteArrayInputStream(buffer_for_image);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);



        String question_text = "";
        try {
            question_text =  new String(buffer_for_text, "UTF-8");
            lastConvertedQuestionText = "MULTQ///";
            lastConvertedQuestionText += question_text;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        question_to_return.setQUESTION(question_text.split("///")[0]);
        if (question_text.split("///").length > 15) {
            question_to_return.setOPT0(question_text.split("///")[1]);
            question_to_return.setOPT1(question_text.split("///")[2]);
            question_to_return.setOPT2(question_text.split("///")[3]);
            question_to_return.setOPT3(question_text.split("///")[4]);
            question_to_return.setOPT4(question_text.split("///")[5]);
            question_to_return.setOPT5(question_text.split("///")[6]);
            question_to_return.setOPT6(question_text.split("///")[7]);
            question_to_return.setOPT7(question_text.split("///")[8]);
            question_to_return.setOPT8(question_text.split("///")[9]);
            question_to_return.setOPT9(question_text.split("///")[10]);
            String ID_string = question_text.split("///")[11];
            question_to_return.setID(ID_string);
            question_to_return.setNB_CORRECT_ANS(Integer.parseInt(question_text.split("///")[12]));
            question_to_return.setIMAGE(question_text.split("///")[15]); //14 because inbetween come subjects and objectives
            SaveImageFile(bitmap, question_text.split("///")[15]);

            //deal with subjects
            String subjectsText = question_text.split("///")[13];
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
            String learningObjectivesText = question_text.split("///")[14];
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

        return question_to_return;
    }
    public QuestionShortAnswer bytearrayvectorToShortAnswerQuestion(byte[] buffer_for_whole_question) {
        QuestionShortAnswer question_to_return = new QuestionShortAnswer();

        byte [] buffer_for_prefix = new byte[80];
        for (int i = 0; i < 80; i++) {
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
            buffer_for_text[i] = buffer_for_whole_question[i+80];
        }

        byte [] buffer_for_image = new byte[size_of_image];
        for (int i = 0; i < size_of_image; i++) {
            buffer_for_image[i] = buffer_for_whole_question[i+80+size_of_text];
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
        question_to_return.setQUESTION(question_text.split("///")[0]);
        String ID_string = "";
        if (question_text.split("///").length > 1) {
            ID_string = question_text.split("///")[1];
            question_to_return.setID(ID_string);
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
            question_to_return.setIMAGE(question_text.split("///")[5]); //because inbetween come subjects and objectives
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

    public static QuestionMultipleChoice textToQuestionMultipleChoice(String questionText) {
        questionText = questionText.substring(8);
        QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
        questionMultipleChoice.setQUESTION(questionText.split("///")[0]);
        if (questionText.split("///").length > 15) {
            questionMultipleChoice.setOPT0(questionText.split("///")[1]);
            questionMultipleChoice.setOPT1(questionText.split("///")[2]);
            questionMultipleChoice.setOPT2(questionText.split("///")[3]);
            questionMultipleChoice.setOPT3(questionText.split("///")[4]);
            questionMultipleChoice.setOPT4(questionText.split("///")[5]);
            questionMultipleChoice.setOPT5(questionText.split("///")[6]);
            questionMultipleChoice.setOPT6(questionText.split("///")[7]);
            questionMultipleChoice.setOPT7(questionText.split("///")[8]);
            questionMultipleChoice.setOPT8(questionText.split("///")[9]);
            questionMultipleChoice.setOPT9(questionText.split("///")[10]);
            String ID_string = questionText.split("///")[11];
            questionMultipleChoice.setID(ID_string);
            questionMultipleChoice.setNB_CORRECT_ANS(Integer.parseInt(questionText.split("///")[12]));
            questionMultipleChoice.setIMAGE(questionText.split("///")[15]); //14 because inbetween come subjects and objectives

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
        questionShortAnswer.setQUESTION(questionText.split("///")[0]);
        if (questionText.split("///").length > 5) {
            String ID_string = questionText.split("///")[1];
            questionShortAnswer.setID(ID_string);
            ArrayList<String> answers = new ArrayList<>(Arrays.asList(questionText.split("///")[2].split("\\|\\|\\|")));
            questionShortAnswer.setAnswers(answers);

            questionShortAnswer.setIMAGE(questionText.split("///")[5]); //14 because inbetween come subjects and objectives

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
