package com.LearningTracker.LearningTrackerApp.Activities.Tools;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.R;

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
                mQuestionTexts[i] = questionShortAnswer.getQUESTION();
            } else {
                mQuestionTexts[i] = questionMultipleChoice.getQUESTION();
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TestListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.test_row, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.questionText.setText(mQuestionTexts[position]);
        if (!test.getActiveQuestionIds().contains(test.getQuestionsIDs().get(position))) {
            holder.questionText.setTextColor(Color.GRAY);
        } else if (test.getAnsweredQuestionIds().contains(test.getQuestionsIDs().get(position))) {
            holder.questionText.setPaintFlags(holder.questionText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.questionText.setTextColor(Color.BLACK);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return test.getQuestionsIDs().size();
    }
}
