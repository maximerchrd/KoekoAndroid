package com.LearningTracker.LearningTrackerApp.Activities.Tools;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionShortAnswer;
import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;
import com.LearningTracker.LearningTrackerApp.R;

public class ResultsListAdapter extends RecyclerView.Adapter<ResultsListAdapter.ViewHolder> {
    private String[] questions;
    private String[] evaluations;
    private String[] medalImageNames;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView questionText;
        public TextView evaluationText;
        public ImageView medalImage;
        // each data item is just a string in this case
        public ViewHolder(View v) {
            super(v);
            questionText = (TextView) v.findViewById(R.id.result_question_text);
            evaluationText = (TextView) v.findViewById(R.id.result_evaluation);
            medalImage = (ImageView) v.findViewById(R.id.medal_image_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ResultsListAdapter(String[] questions, String[] evaluations, String[] medalImageNames, Context context) {
        this.questions = questions;
        this.evaluations = evaluations;
        this.medalImageNames = medalImageNames;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ResultsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.result_row, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.questionText.setText(questions[position]);
        holder.evaluationText.setText(evaluations[position]);

        Drawable drawable = null;
        if (medalImageNames[position].contentEquals("gold-medal")) {
            drawable = context.getResources().getDrawable(R.drawable.gold_medal);
        } else if (medalImageNames[position].contentEquals("silver-medal")) {
            drawable = context.getResources().getDrawable(R.drawable.silver_medal);
        } else if (medalImageNames[position].contentEquals("bronze-medal")) {
            drawable = context.getResources().getDrawable(R.drawable.bronze_medal);
        }
        holder.medalImage.setImageDrawable(drawable);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return questions.length;
    }
}
