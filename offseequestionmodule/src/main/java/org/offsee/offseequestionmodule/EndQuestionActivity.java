package org.offsee.offseequestionmodule;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.offseequestionmodule.R;

import org.offsee.offseequestionmodule.model.MyPair;
import org.offsee.offseequestionmodule.model.Question;
import org.offsee.offseequestionmodule.utils.App;
import org.offsee.offseequestionmodule.utils.AudioPlayer;
import org.offsee.offseequestionmodule.utils.FontManager;
import org.offsee.offseequestionmodule.webservice.ApiManagerInvoke;
import org.offsee.offseequestionmodule.webservice.MessageContract;

import java.util.ArrayList;
import java.util.List;

import needle.Needle;
import signalgo.client.GoResponseHandler;

public class EndQuestionActivity extends AppCompatActivity {
    TextView statusTextview, totalScoreTextview, timeTextview, questionTextView, rightTextView, tryDescriptionTextview;
    ProgressBar progressBar;
    LinearLayout dataLayout;
    Button tryButton, startButton;
    ArrayList<Question> questions;
    int[] answrs;
    int rightAnswers;
    long useTime;
    int earnScore;
    int gamePlayId = 0;
    ImageView leftBox, rightBox, cloudLeft, cloudRight;
    RelativeLayout tunnelLayout;
    CardView topCardview;
    boolean soundStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_question);
        FontManager.instance().setTypefaceImmediate(getWindow().getDecorView());
        questions = getIntent().getParcelableArrayListExtra("qList");
        answrs = getIntent().getIntArrayExtra("answers");
        rightAnswers = getIntent().getIntExtra("rightAnswers", 0);
        useTime = getIntent().getLongExtra("useTime", 0);
        earnScore = getIntent().getIntExtra("earnScore", 0);
        gamePlayId = getIntent().getIntExtra("gamePlay", 0);
        soundStatus = App.sp.getBoolean(getString(R.string.pref_game_sound), true);
        initialize();
        sendToServer();

    }

    int chPageduration = 1000;

    public void initialize() {
        statusTextview = (TextView) findViewById(R.id.statusTextview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        dataLayout = (LinearLayout) findViewById(R.id.dataLayout);
        startButton = (Button) findViewById(R.id.startButton);
        tryButton = (Button) findViewById(R.id.tryButton);
        statusTextview.setTypeface(FontManager.instance(R.string.font_vazir_bold).getTypeface());
        tryButton.setTypeface(FontManager.instance(R.string.font_vazir_bold).getTypeface());
        totalScoreTextview = (TextView) findViewById(R.id.totalScoreTextview);
        //totalScoreTextview.setCompoundDrawablesWithIntrinsicBounds(null, App.getIcon(TaxcityFont.Icon.at_medal, R.color.accent, 40), null, null);
        timeTextview = (TextView) findViewById(R.id.timeTextview);
        questionTextView = (TextView) findViewById(R.id.questionTextView);
        rightTextView = (TextView) findViewById(R.id.rightTextView);

        //change
        //timeTextview.setCompoundDrawablesWithIntrinsicBounds(null, App.getIcon(TaxcityFont.Icon.icon_timer, R.color.dark_gray), null, null);
        //questionTextView.setCompoundDrawablesWithIntrinsicBounds(null, App.getIcon(TaxcityFont.Icon.icon_number_list, R.color.dark_gray), null, null);
        tryDescriptionTextview = (TextView) findViewById(R.id.tryDescriptionTextview);


        leftBox = (ImageView) findViewById(R.id.leftBox);
        rightBox = (ImageView) findViewById(R.id.rightBox);

        cloudLeft = (ImageView) findViewById(R.id.leftCloud);
        cloudRight = (ImageView) findViewById(R.id.rightCloud);

        topCardview = (CardView) findViewById(R.id.topCardview);
        tunnelLayout = (RelativeLayout) findViewById(R.id.tunnelLayout);

        leftBox.setVisibility(View.INVISIBLE);
        rightBox.setVisibility(View.INVISIBLE);
        cloudLeft.setVisibility(View.INVISIBLE);
        cloudRight.setVisibility(View.INVISIBLE);
        tunnelLayout.setVisibility(View.INVISIBLE);
        topCardview.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.INVISIBLE);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                leftBox.setVisibility(View.VISIBLE);
                rightBox.setVisibility(View.VISIBLE);
                cloudLeft.setVisibility(View.VISIBLE);
                cloudRight.setVisibility(View.VISIBLE);
                tunnelLayout.setVisibility(View.VISIBLE);
                topCardview.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInLeft).duration(chPageduration).playOn(leftBox);
                YoYo.with(Techniques.SlideInRight).duration(chPageduration).playOn(rightBox);
                YoYo.with(Techniques.SlideInLeft).duration(chPageduration).playOn(cloudLeft);
                YoYo.with(Techniques.SlideInRight).duration(chPageduration).playOn(cloudRight);
                YoYo.with(Techniques.SlideInUp).duration(chPageduration).playOn(tunnelLayout);
                YoYo.with(Techniques.FadeInDown).duration(chPageduration).playOn(topCardview);
                YoYo.with(Techniques.FadeIn).duration(chPageduration).playOn(startButton);
            }
        }, 500);

    }

    public void fillData() {
        if (rightAnswers >= QuestionSplashActivity.minQiestoin) {
            statusTextview.setText(R.string.win);
            statusTextview.setTextColor(ContextCompat.getColor(this, R.color.my_green));
            if (soundStatus)
                AudioPlayer.instance(R.raw.win).play();
            totalScoreTextview.setText(earnScore + " " + getString(R.string.toman));
            //change
            //App.increaseScore(earnScore);
        } else {
            earnScore = 0;
            statusTextview.setText(R.string.loss);
            statusTextview.setTextColor(ContextCompat.getColor(this, R.color.my_red));
            if (soundStatus)
                AudioPlayer.instance(R.raw.loss).play();
            totalScoreTextview.setText(getString(R.string.noScore));
        }
        timeTextview.setText(useTime + " " + getString(R.string.second));
        questionTextView.setText(questions.size() + " " + getString(R.string.question));
        rightTextView.setText(rightAnswers + " " + getString(R.string.right));

    }

    public void sendToServer() {
        List<Integer> list = new ArrayList<>();
        List<Integer> ans = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            list.add(questions.get(i).id);
            ans.add(answrs[i]);
        }

        ApiManagerInvoke.finishGamePlay(gamePlayId, new GoResponseHandler<MessageContract<MyPair<Integer, Integer>>>() {
            @Override
            public void onResponse(final MessageContract<MyPair<Integer, Integer>> myPairMessageContract) {
                if (myPairMessageContract != null && myPairMessageContract.isSuccess) {
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            dataLayout.setVisibility(View.VISIBLE);
                            startButton.setEnabled(true);
                            fillData();
                            //change
                            //App.updateCreditFromServer();
                        }
                    });
                } else if (myPairMessageContract != null && !myPairMessageContract.isSuccess) {
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(App.getContext(), myPairMessageContract.message, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            tryButton.setVisibility(View.VISIBLE);
                            tryDescriptionTextview.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    App.serverError(App.getContext());

                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            tryButton.setVisibility(View.VISIBLE);
                            tryDescriptionTextview.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

//        ApiManagerInvoke.sendAnswerQuestion(list, ans, new GoResponseHandler<MessageContract>() {
//            @Override
//            public void onResponse(final MessageContract messageContract) {
//                if (messageContract != null && messageContract.isSuccess) {
//                    Needle.onMainThread().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressBar.setVisibility(View.GONE);
//                            dataLayout.setVisibility(View.VISIBLE);
//                            startButton.setEnabled(true);
//                            fillData();
//                        }
//                    });
//                } else if (messageContract != null && !messageContract.isSuccess) {
//                    Needle.onMainThread().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(App.context, messageContract.message, Toast.LENGTH_SHORT).show();
//                            progressBar.setVisibility(View.GONE);
//                            tryButton.setVisibility(View.VISIBLE);
//                            tryDescriptionTextview.setVisibility(View.VISIBLE);
//                        }
//                    });
//                } else {
//                    App.serverError(App.context);
//                    Needle.onMainThread().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressBar.setVisibility(View.GONE);
//                            tryButton.setVisibility(View.VISIBLE);
//                            tryDescriptionTextview.setVisibility(View.VISIBLE);
//                        }
//                    });
//                }
//            }
//        });
    }

    public void onTryAgain(View v) {
        sendToServer();
        tryButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tryDescriptionTextview.setVisibility(View.GONE);
    }

    public void onStartAgainButtonClick(View v) {
        Intent intent = new Intent(EndQuestionActivity.this, QuestionSplashActivity.class);
        startActivity(intent);
        finish();
    }
}
