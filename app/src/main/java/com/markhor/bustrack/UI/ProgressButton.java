package com.markhor.bustrack.UI;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.markhor.bustrack.R;

public class ProgressButton {
    private CardView cardView;
    private ConstraintLayout layout;
    private ProgressBar progressBar;
    private TextView textView;
    private String buttonText;
    Animation fadeAnimation;
    public ProgressButton(Context context, View view, String text)  {
        fadeAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_anim);
        cardView = view.findViewById(R.id.card_view);
        layout = view.findViewById(R.id.constraint_layout);
        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.textView);
        buttonText = text;
        textView.setText(buttonText);
    }

    public void buttonActivated() {
        progressBar.setAnimation(fadeAnimation);
        progressBar.setVisibility(View.VISIBLE);
        textView.setAnimation(fadeAnimation);
        textView.setText("Please Wait...");
    }
    public void buttonStop() {
        progressBar.setVisibility(View.GONE);
        textView.setText(buttonText);
    }
    public void buttonDone() {
        progressBar.setVisibility(View.GONE);
        textView.setText("Done");
    }
}
