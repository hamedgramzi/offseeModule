package org.offsee.offseequestionmodule;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.offseequestionmodule.R;
import com.squareup.picasso.Picasso;

import org.offsee.offseequestionmodule.model.MyPair;
import org.offsee.offseequestionmodule.model.Question;
import org.offsee.offseequestionmodule.model.SponsorInfo;
import org.offsee.offseequestionmodule.utils.App;
import org.offsee.offseequestionmodule.utils.AudioPlayer;
import org.offsee.offseequestionmodule.utils.FontManager;
import org.offsee.offseequestionmodule.utils.circleprogress.DonutProgress;
import org.offsee.offseequestionmodule.webservice.ApiManagerInvoke;
import org.offsee.offseequestionmodule.webservice.MessageContract;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import needle.Needle;
import signalgo.client.GoResponseHandler;

public class QuestionActivity extends AppCompatActivity {
    TextView questionTextView;
    TextView[] answersText = new TextView[4];
    LinearLayout topLayout;
    ArrayList<Question> questions;
    int[] userAnswer;
    HashMap<Integer, Integer> assignAnswers;
    HashMap<Integer, Integer> assignRealAnswers;
    int rightAnswer = 0;
    CardView questionCard, sponsorCard;
    int selectedQuestion = 0;
    DonutProgress donutProgress;
    int duration;
    long useDuration = 0;
    TextView positiveTextview, scoreTextview, earnScoreTextview, numberQuestionTextview, questionScoreTextview;
    int numberOfRightAnswer = 0;
    int earnScore = 0;
    CountDownTimer countDownTimer;
    AudioPlayer audioPlayer;
    RelativeLayout layoutOnButtons, toolbar;
    public static final long delayChange = 2000;
    private static final long animateDuration = (delayChange * 9 / 10) / 2;
    ImageView sponsorLogoImageView;
    TextView sponsorNameTextview, noteSponsorTextview;
    SponsorInfo currentSponsorInfo;
    ImageView questionPic, testImageView;
    ImageView soundIcon;
    boolean soundStatus = true;
    int gamePlayId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        questions = getIntent().getParcelableArrayListExtra("qlist");
        duration = getIntent().getIntExtra("duration", 1);
        gamePlayId = getIntent().getIntExtra("gamePlay", 0);
        FontManager.instance().setTypeface(getWindow().getDecorView());
        initialize();
        fillQuestion(questions.get(selectedQuestion));
        audioPlayer = AudioPlayer.instance(R.raw.untitled8);
    }

    public void fillQuestion(Question q) {
        questionTextView.setText(q.content);
        questionScoreTextview.setText(q.score + " " + getString(R.string.toman));
        rightAnswer = assignRandomAnswers(q);
        if (questions.get(selectedQuestion).imageUrl != null) {
            questionPic.setVisibility(View.VISIBLE);
            ((RelativeLayout.LayoutParams) questionTextView.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.questionPic);
            Picasso.with(App.getContext()).load(questions.get(selectedQuestion).imageUrl).into(questionPic);
        } else {
            questionPic.setVisibility(View.GONE);
            questionPic.setImageResource(R.drawable.logo_ldpi);
            ((RelativeLayout.LayoutParams) questionTextView.getLayoutParams()).addRule(RelativeLayout.BELOW, 0);
        }
        if (questions.size() - 1 >= selectedQuestion + 1 && questions.get(selectedQuestion + 1).imageUrl != null) {
            Picasso.with(App.getContext()).load(questions.get(selectedQuestion + 1).imageUrl).into(testImageView);
        }

        numberQuestionTextview.setText((selectedQuestion + 1) + "/" + questions.size());
        if (questions.get(selectedQuestion).sponsorInfo != null) {
            currentSponsorInfo = questions.get(selectedQuestion).sponsorInfo;
            sponsorNameTextview.setText(currentSponsorInfo.name);
            Picasso.with(App.getContext()).load(currentSponsorInfo.imageUrl).into(sponsorLogoImageView);
            noteSponsorTextview.setText(currentSponsorInfo.description);
        }
    }

    public void changeQuestion() {
        selectedQuestion++;
        if (selectedQuestion == questions.size()) {
            endQuestions();
            return;
        }

        YoYo.with(Techniques.RollOut).duration(animateDuration).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                questionCard.setVisibility(View.INVISIBLE);
                fillQuestion(questions.get(selectedQuestion));
            }
        }).playOn(questionCard);
        YoYo.with(Techniques.RollOut).duration(animateDuration).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                sponsorCard.setVisibility(View.INVISIBLE);
                questionCard.setVisibility(View.VISIBLE);
                sponsorCard.requestLayout();
            }
        }).playOn(sponsorCard);
        YoYo.with(Techniques.RollIn).duration(animateDuration).delay(animateDuration).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                startTimer();
                layoutOnButtons.setVisibility(View.GONE);
            }
        }).playOn(questionCard);
        YoYo.with(Techniques.RollIn).duration(animateDuration).delay(animateDuration).playOn(sponsorCard);

        try {
            YoYo.with(Techniques.SlideOutLeft).duration(animateDuration).playOn(answersText[0]);
            YoYo.with(Techniques.SlideInLeft).delay(animateDuration).duration(animateDuration).playOn(answersText[0]);
            YoYo.with(Techniques.SlideOutRight).duration(animateDuration).playOn(answersText[1]);
            YoYo.with(Techniques.SlideInRight).delay(animateDuration).duration(animateDuration).playOn(answersText[1]);
            YoYo.with(Techniques.SlideOutLeft).duration(animateDuration).playOn(answersText[2]);
            YoYo.with(Techniques.SlideInLeft).delay(animateDuration).duration(animateDuration).playOn(answersText[2]);
            YoYo.with(Techniques.SlideOutRight).duration(animateDuration).playOn(answersText[3]);
            YoYo.with(Techniques.SlideInRight).delay(animateDuration).duration(animateDuration).playOn(answersText[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < answersText.length; i++) {
            answersText[i].setEnabled(true);
            answersText[i].setSelected(false);
        }

    }

    AnimatorSet flipIn, flipOut;

    public void initialize() {
//        topLayout = (LinearLayout) findViewById(R.id.topLayout);
        layoutOnButtons = (RelativeLayout) findViewById(R.id.layoutOnButtons);
//        topLayout.getLayoutParams().height = App.getWindowWidth(QuestionActivity.this) * 2 / 3;
        questionTextView = (TextView) findViewById(R.id.questionTextView);
        answersText[0] = (TextView) findViewById(R.id.ansTextview1);
        answersText[1] = (TextView) findViewById(R.id.ansTextview2);
        answersText[2] = (TextView) findViewById(R.id.ansTextview3);
        answersText[3] = (TextView) findViewById(R.id.ansTextview4);
        userAnswer = new int[questions.size()];
        questionCard = (CardView) findViewById(R.id.questionCard);
        questionPic = (ImageView) findViewById(R.id.questionPic);
        testImageView = (ImageView) findViewById(R.id.testImageView);
        sponsorCard = (CardView) findViewById(R.id.sponsorCard);
        toolbar = (RelativeLayout) findViewById(R.id.toolbar);
        positiveTextview = (TextView) findViewById(R.id.positiveTextview);
        scoreTextview = (TextView) findViewById(R.id.scoreTextview);
        scoreTextview.setText(earnScore + " " + getString(R.string.toman));
        //scoreTextview.setCompoundDrawablesWithIntrinsicBounds(null, null, App.getIcon(TaxcityFont.Icon.at_medal, R.color.accent), null);
        earnScoreTextview = (TextView) findViewById(R.id.earnScoreTextview);
        numberQuestionTextview = (TextView) findViewById(R.id.numberQuestionTextview);
        questionScoreTextview = (TextView) findViewById(R.id.questionScoreTextview);
        //questionScoreTextview.setCompoundDrawablesWithIntrinsicBounds(App.getIcon(TaxcityFont.Icon.at_medal, R.color.dark_gray), null, null, null);
        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
        sponsorLogoImageView = (ImageView) findViewById(R.id.sponsorLogoImageView);
        sponsorNameTextview = (TextView) findViewById(R.id.sponsorNameTextview);
        noteSponsorTextview = (TextView) findViewById(R.id.noteSponsorTextview);
        donutProgress.setProgress(duration);
        donutProgress.setMax(duration);
        useDuration = duration;
        //startTimer();
        flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.in_animation);
        flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.out_animation);


        int colorFrom = ContextCompat.getColor(this, R.color.my_green);
        int colorTo = ContextCompat.getColor(this, R.color.my_red);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(1000 * duration); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                donutProgress.setFinishedStrokeColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
        layoutOnButtons.setVisibility(View.VISIBLE);


        answersText[0].setVisibility(View.INVISIBLE);
        answersText[1].setVisibility(View.INVISIBLE);
        answersText[2].setVisibility(View.INVISIBLE);
        answersText[3].setVisibility(View.INVISIBLE);
        questionCard.setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                questionCard.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.RollIn).duration(animateDuration).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        startTimer();
                        layoutOnButtons.setVisibility(View.GONE);
                    }
                }).playOn(questionCard);
                toolbar.setVisibility(View.VISIBLE);
                answersText[0].setVisibility(View.VISIBLE);
                answersText[1].setVisibility(View.VISIBLE);
                answersText[2].setVisibility(View.VISIBLE);
                answersText[3].setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInDown).duration(animateDuration).playOn(toolbar);
                try {
                    YoYo.with(Techniques.SlideInLeft).duration(animateDuration).playOn(answersText[0]);
                    YoYo.with(Techniques.SlideInRight).duration(animateDuration).playOn(answersText[1]);
                    YoYo.with(Techniques.SlideInLeft).duration(animateDuration).playOn(answersText[2]);
                    YoYo.with(Techniques.SlideInRight).duration(animateDuration).playOn(answersText[3]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);

        soundIcon = (ImageView) findViewById(R.id.soundIcon);
        StateListDrawable states = new StateListDrawable();
        //change
        //states.addState(new int[]{android.R.attr.state_selected}, App.getIcon(TaxcityFont.Icon.icon_volume_on, R.color.white));
        //states.addState(new int[]{}, App.getIcon(TaxcityFont.Icon.icon_volume_off, R.color.white));
        soundIcon.setImageDrawable(states);
        soundStatus = App.sp.getBoolean(getString(R.string.pref_game_sound), true);
        soundIcon.setSelected(soundStatus);
        soundIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundStatus = !soundStatus;
                App.sp.edit().putBoolean(getString(R.string.pref_game_sound), soundStatus).commit();
                soundIcon.setSelected(soundStatus);
                if (soundStatus) {
                    startSound();
                } else {
                    if (audioPlayer != null) {
                        audioPlayer.pause();
                    }
                }
            }
        });


    }


    private void startTimer() {
        countDownTimer = new CountDownTimer(1000 * useDuration, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long x = millisUntilFinished / 1000;
                donutProgress.setProgress(x);
                donutProgress.setText(x + "");
                useDuration = x;
                try {
                    Field f = DonutProgress.class.getDeclaredField("textPaint");
                    f.setAccessible(true);
                    TextPaint tp = (TextPaint) f.get(donutProgress);
                    tp.setTypeface(FontManager.instance().getTypeface());
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (x == 20) {
                    AudioPlayer.instance(R.raw.thick).repeateWithRate(3f);
                }

            }

            @Override
            public void onFinish() {
                endQuestions();
            }
        };
        countDownTimer.start();

    }

    private void stopTimer() {
        countDownTimer.cancel();
        countDownTimer = null;
    }

    boolean canAnswer = true;

    public void onAnswerClick(View v) {
        if (!canAnswer) {
            return;
        }
        canAnswer = false;
        int temp = 0;
        if(v.getId() == R.id.ansTextview1){
            temp = 1;
        } else if(v.getId() == R.id.ansTextview2){
            temp = 2;
        } else if(v.getId() == R.id.ansTextview3){
            temp = 3;
        } else if(v.getId() == R.id.ansTextview4){
            temp = 4;
        }


        userAnswer[selectedQuestion] = assignAnswers.get(temp);
        layoutOnButtons.setVisibility(View.VISIBLE);
        stopTimer();
        answersText[temp - 1].setSelected(true);
        answersText[temp - 1].setEnabled(false);
        //if (questions.get(selectedQuestion).sponsorInfo != null) {
        flipOut.setTarget(questionCard);
        flipOut.start();
        sponsorCard.setVisibility(View.VISIBLE);
        sponsorCard.requestLayout();
        flipIn.setTarget(sponsorCard);
        flipIn.start();
        //}
        final int finalTemp = temp;
        ApiManagerInvoke.sendAnswerQuestion(gamePlayId, questions.get(selectedQuestion).id, userAnswer[selectedQuestion], new GoResponseHandler<MessageContract<MyPair<Integer, Integer>>>() {
            @Override
            public void onResponse(final MessageContract<MyPair<Integer, Integer>> myPairMessageContract) {
                if (myPairMessageContract != null && myPairMessageContract.isSuccess) {
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            getAnswer(assignRealAnswers.get(myPairMessageContract.data.first), finalTemp);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    changeQuestion();
                                    canAnswer = true;
                                }
                            }, delayChange);
                        }
                    });
                } else if (myPairMessageContract != null && !myPairMessageContract.isSuccess) {
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(App.getContext(), myPairMessageContract.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    final GoResponseHandler goResponseHandler = this;
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(layoutOnButtons, getString(R.string.serverError), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.tryAgain), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ApiManagerInvoke.sendAnswerQuestion(gamePlayId, questions.get(selectedQuestion).id, userAnswer[selectedQuestion], goResponseHandler);
                                }
                            }).show();
                        }
                    });
                    App.serverError(App.getContext());
                }
            }
        });

    }

    private void getAnswer(int rightAnswer, int selectAnswer) {
        for (int i = 0; i < answersText.length; i++) {
            answersText[i].setSelected(false);
            answersText[i].setEnabled(true);
        }
        if (rightAnswer == selectAnswer) {
            AudioPlayer.instance(R.raw.coin).play();
            answersText[selectAnswer - 1].setSelected(true);
            answersText[selectAnswer - 1].setEnabled(true);
            positiveTextview.setText((++numberOfRightAnswer) + " " + getString(R.string.right));
            earnScoreTextview.setVisibility(View.VISIBLE);
            Animation swingUp = AnimationUtils.loadAnimation(QuestionActivity.this, R.anim.swing_up);
            earnScoreTextview.startAnimation(swingUp);
            final int selectQuestion = selectedQuestion;
            swingUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    startCountAnimation(earnScore, earnScore + questions.get(selectQuestion).score);
                    earnScore += questions.get(selectQuestion).score;
                    //scoreTextview.setText(earnScore + " " + getString(R.string.score));
                    earnScoreTextview.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        } else {
            AudioPlayer.instance(R.raw.bump).play();
            answersText[selectAnswer - 1].setEnabled(false);
            answersText[selectAnswer - 1].setSelected(false);

            answersText[rightAnswer - 1].setSelected(true);
            answersText[rightAnswer - 1].setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (soundStatus) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startSound();
                }
            }, 500);

        }
    }

    private void startSound() {
        if (audioPlayer != null) {
            audioPlayer.repeate();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
        AudioPlayer.instance(R.raw.thick).stop();
    }

    public int assignRandomAnswers(Question q) {
        assignAnswers = new HashMap<>();
        assignRealAnswers = new HashMap<>();
        List<String> answersString = new ArrayList<>();
        answersString.add(q.answer1);
        answersString.add(q.answer2);
        answersString.add(q.answer3);
        answersString.add(q.answer4);
        List<Integer> ra = new ArrayList<>();
        for (int i = 0; i < answersText.length; i++) {
            ra.add(i + 1);
        }
        int temp = 0;
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < answersString.size(); i++) {
            int x = random.nextInt(ra.size());
            answersText[ra.get(x) - 1].setText(answersString.get(i));

            assignAnswers.put(ra.get(x), i + 1);
            assignRealAnswers.put(i + 1, ra.get(x));
//            if (q.trueAnswer == i + 1) {
//                temp = ra.get(x);
//            }
            ra.remove(x);
        }
        return temp;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("خروج از بازی")
                .setMessage("آیا برای خروج از بازی مطمئن هستید؟ با اینکار این بازی را از دست می دهید!")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
        //super.onBackPressed();

    }

    private void startCountAnimation(int from, int to) {
        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(from, to);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                scoreTextview.setText((int) animation.getAnimatedValue() + " " + getString(R.string.toman));
            }
        });
        animator.start();
    }

    public void onAlakiClick(View v) {

    }


    private void endQuestions() {

        YoYo.with(Techniques.RollOut).duration(animateDuration).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {

                Intent intent = new Intent(QuestionActivity.this, EndQuestionActivity.class);
                intent.putParcelableArrayListExtra("qList", questions);
                intent.putExtra("answers", userAnswer);
                intent.putExtra("rightAnswers", numberOfRightAnswer);
                intent.putExtra("useTime", duration - useDuration);
                intent.putExtra("earnScore", earnScore);
                intent.putExtra("gamePlay", gamePlayId);

                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        }).playOn(questionCard);
        YoYo.with(Techniques.RollOut).duration(animateDuration).playOn(sponsorCard);
//        toolbar.setVisibility(View.VISIBLE);
//        answersText[0].setVisibility(View.VISIBLE);
//        answersText[1].setVisibility(View.VISIBLE);
//        answersText[2].setVisibility(View.VISIBLE);
//        answersText[3].setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideOutUp).duration(animateDuration).playOn(toolbar);
        try {
            YoYo.with(Techniques.SlideOutLeft).duration(animateDuration).playOn(answersText[0]);
            YoYo.with(Techniques.SlideOutRight).duration(animateDuration).playOn(answersText[1]);
            YoYo.with(Techniques.SlideOutLeft).duration(animateDuration).playOn(answersText[2]);
            YoYo.with(Techniques.SlideOutRight).duration(animateDuration).playOn(answersText[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
