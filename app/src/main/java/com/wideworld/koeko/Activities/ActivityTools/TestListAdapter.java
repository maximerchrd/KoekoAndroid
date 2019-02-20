package com.wideworld.koeko.Activities.ActivityTools;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.Test;
import com.wideworld.koeko.R;

import java.util.Arrays;

public class TestListAdapter extends RecyclerView.Adapter<TestListAdapter.ViewHolder> {
    private String[] mQuestionTexts;
    private Test test;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView questionText;
        // each data item is just a string in this case
        public ViewHolder(View v) {
            super(v);
            questionText = (TextView) v.findViewById(R.id.question_text);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TestListAdapter(Test test) {
        this.test = test;
        mQuestionTexts = new String[test.getQuestionsIDs().size()];

        for (int i = 0; i < test.getQuestionsIDs().size(); i++) {
            QuestionMultipleChoice questionMultipleChoice = test.getIdMapQmc().get(test.getQuestionsIDs().get(i));
            if (questionMultipleChoice == null) {
                QuestionShortAnswer questionShortAnswer = test.getIdMapShrtaq().get(test.getQuestionsIDs().get(i));
                if (questionShortAnswer != null) {
                    mQuestionTexts[i] = questionShortAnswer.getQuestion();
                } else {
                    mQuestionTexts = Arrays.copyOfRange(mQuestionTexts, 0, mQuestionTexts.length - 1);
                }
            } else {
                mQuestionTexts[i] = questionMultipleChoice.getQuestion();
            }
        }
    }

    @Override
    public TestListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.test_row, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < mQuestionTexts.length) {
            holder.questionText.setText(mQuestionTexts[position]);
            if (!test.getActiveQuestionIds().contains(test.getQuestionsIDs().get(position))) {
                holder.questionText.setTextColor(Color.GRAY);
            } else if (test.getAnsweredQuestionIds().containsKey(test.getQuestionsIDs().get(position))) {
                holder.questionText.setPaintFlags(holder.questionText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.questionText.setTextColor(Color.BLACK);
            }

            if (Koeko.currentTestFragmentSingleton.testIsFinished) {
                if (test.getAnsweredQuestionIds().get(test.getActiveQuestionIds().get(position)) >= 100) {
                    holder.questionText.setTextColor(Color.GREEN);
                } else if (test.getAnsweredQuestionIds().get(test.getActiveQuestionIds().get(position)) >= 0) {
                    holder.questionText.setTextColor(Color.RED);
                }
            }
        } else if (Koeko.currentTestFragmentSingleton.testIsFinished) {
            double overallEval = 0.0;
            for (double eval : test.getAnsweredQuestionIds().values()) {
                overallEval += eval;
            }
            overallEval /= test.getAnsweredQuestionIds().size();
            holder.questionText.setText("Overall evaluation: " + overallEval + " %");
            holder.questionText.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return test.getQuestionsIDs().size();
    }
}
