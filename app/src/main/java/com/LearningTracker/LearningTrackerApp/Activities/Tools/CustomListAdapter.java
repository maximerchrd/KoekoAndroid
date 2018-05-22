package com.LearningTracker.LearningTrackerApp.Activities.Tools;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.R;

import java.util.Map;

public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.ViewHolder> {
    private String[] mQuestionIds;
    private String[] mQuestionTexts;
    private Map<String,QuestionMultipleChoice> idMapQmc;
    private Map<String,QuestionShortAnswer> idMapShrtaq;

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
    public CustomListAdapter(String[] myDataset, Map<String,QuestionMultipleChoice> idMapQmcArg,
             Map<String,QuestionShortAnswer> idMapShrtaqArg) {
        mQuestionIds = myDataset;
        idMapQmc = idMapQmcArg;
        idMapShrtaq = idMapShrtaqArg;
        mQuestionTexts = new String[mQuestionIds.length];

        for (int i = 0; i < mQuestionIds.length; i++) {
            QuestionMultipleChoice questionMultipleChoice = idMapQmc.get(mQuestionIds[i]);
            if (questionMultipleChoice == null) {
                QuestionShortAnswer questionShortAnswer = idMapShrtaq.get(mQuestionIds[i]);
                mQuestionTexts[i] = questionShortAnswer.getQUESTION();
            } else {
                mQuestionTexts[i] = questionMultipleChoice.getQUESTION();
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mQuestionIds.length;
    }
}
