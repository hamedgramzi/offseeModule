package org.offsee.offseequestionmodule;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import org.offsee.offseequestionmodule.model.GamePlayInfo;
import org.offsee.offseequestionmodule.model.Question;
import org.offsee.offseequestionmodule.utils.App;
import org.offsee.offseequestionmodule.utils.FontManager;
import org.offsee.offseequestionmodule.utils.PermissionUtils;
import org.offsee.offseequestionmodule.webservice.ApiManagerInvoke;
import org.offsee.offseequestionmodule.webservice.Core;
import org.offsee.offseequestionmodule.webservice.MessageContract;

import java.util.ArrayList;
import java.util.List;

import needle.Needle;
import signalgo.client.GoResponseHandler;

public class QuestionSplashActivity extends AppCompatActivity {
    TextView totalScoreTextview, timeTextview, questionTextView, descripGameTextview;
    ArrayList<Question> list;
    ProgressBar progressBar;
    LinearLayout dataLayout;
    public static int maxQuestion = 10;
    public static int timePerQuestion = 10;
    public static int minQiestoin = maxQuestion / 2 + 1;
    Button tryButton, startButton;
    MediaPlayer mediaPlayer;
    ImageView leftBox, rightBox, cloudLeft, cloudRight;
    RelativeLayout tunnelLayout;
    CardView topCardview;
    ImageView soundIcon;
    boolean soundStatus = true;
    ProgressBar startProgress;
    OnGameStatusListener onGameStatusListener;
    boolean isSelfFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_splash);
        App.setContext(this);
        OffseeApi.registerActivity(this);
        try {
            FontManager.instance().setTypefaceImmediate(getWindow().getDecorView());
        } catch (Exception e) {

        }
        onGameStatusListener = OffseeApi.getOnGameStatusListener();
        //offseeApi = (OffseeApi) getIntent().getSerializableExtra("offseeApi");
        leftBox = (ImageView) findViewById(R.id.leftBox);
        rightBox = (ImageView) findViewById(R.id.rightBox);

        cloudLeft = (ImageView) findViewById(R.id.leftCloud);
        cloudRight = (ImageView) findViewById(R.id.rightCloud);

        topCardview = (CardView) findViewById(R.id.topCardview);
        tunnelLayout = (RelativeLayout) findViewById(R.id.tunnelLayout);
        startProgress = (ProgressBar) findViewById(R.id.startProgress);
//        ThreeBounce doubleBounce = new ThreeBounce();
//        doubleBounce.setBounds(0, 0, 60, 60);
//        doubleBounce.setColor(getResources().getColor(R.color.dark_gray));
//        startProgress.setIndeterminateDrawable(doubleBounce);

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
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                    }
                }
            }
        });
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setTypeface(FontManager.instance(R.string.font_vazir_bold).getTypeface());
        totalScoreTextview = (TextView) findViewById(R.id.totalScoreTextview);
        //totalScoreTextview.setCompoundDrawablesWithIntrinsicBounds(null, App.getIcon(TaxcityFont.Icon.at_medal, R.color.accent, 40), null, null);
        timeTextview = (TextView) findViewById(R.id.timeTextview);
        questionTextView = (TextView) findViewById(R.id.questionTextView);
        descripGameTextview = (TextView) findViewById(R.id.descripGameTextview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        dataLayout = (LinearLayout) findViewById(R.id.dataLayout);
        tryButton = (Button) findViewById(R.id.tryButton);
        //change
        //timeTextview.setCompoundDrawablesWithIntrinsicBounds(null, App.getIcon(TaxcityFont.Icon.icon_timer, R.color.dark_gray), null, null);
        //questionTextView.setCompoundDrawablesWithIntrinsicBounds(null, App.getIcon(TaxcityFont.Icon.icon_number_list, R.color.dark_gray), null, null);


        if (enableMyLocation())
            getQuestion();
    }

    int gamePlayId = 0;

    private void getQuestion() {
        Location location = App.getLastKnownLocation();
        if (location == null) {
            Toast.makeText(QuestionSplashActivity.this, "دسترسی به موقعیت شما وجود ندارد", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            tryButton.setVisibility(View.VISIBLE);
            startButton.setEnabled(false);
            return;
        }
        ApiManagerInvoke.addGamePlayQOK(location, new GoResponseHandler<MessageContract<GamePlayInfo>>() {
            @Override
            public void onResponse(final MessageContract<GamePlayInfo> listMessageContract) {
                if (listMessageContract != null && listMessageContract.isSuccess) {
                    gamePlayId = listMessageContract.data.id;
                    int temp = listMessageContract.data.sumOfScores;
                    final int finalTemp = temp;
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            totalScoreTextview.setText("جایزه : " + finalTemp + " " + getString(R.string.toman));
                            progressBar.setVisibility(View.GONE);
                            dataLayout.setVisibility(View.VISIBLE);
                            timeTextview.setText(maxQuestion * timePerQuestion + " " + getString(R.string.second));
                            questionTextView.setText(maxQuestion + " " + getString(R.string.question));
                            descripGameTextview.setText(descripGameTextview.getText().toString().replace("ss", minQiestoin + ""));
                            startButton.setEnabled(true);
                        }
                    });

                } else if (listMessageContract != null && !listMessageContract.isSuccess) {
                    if (listMessageContract.errorCode == 61) {
//                        Needle.onMainThread().execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                AlertDialog.
//                                App.getMaterialDialog(QuestionSplashActivity.this)
//                                        .title("سقف بازی روزانه")
//                                        .content("شما به سقف بازی روزانه رسیدید، مجددا فردا برای بازی مراجعه کنید و یا با گرفتن سرویس جدید تاکسیتی دوباره بازی کنید.")
//                                        .positiveText(R.string.ok)
//                                        .dismissListener(new DialogInterface.OnDismissListener() {
//                                            @Override
//                                            public void onDismiss(DialogInterface dialog) {
//                                                finish();
//                                            }
//                                        })
//                                        .show();
//                            }
//                        });

                    } else {
                        Needle.onMainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(QuestionSplashActivity.this, listMessageContract.message, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                tryButton.setVisibility(View.VISIBLE);
                                startButton.setEnabled(false);
                            }
                        });
                    }
                } else {
                    App.serverError(QuestionSplashActivity.this);
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            tryButton.setVisibility(View.VISIBLE);
                            startButton.setEnabled(false);
                        }
                    });
                }
            }
        });
    }

    int LOCATION_PERMISSION_REQUEST_CODE = 12;

    private boolean enableMyLocation() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing.
                PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION, true);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    @RequiresApi(api = 24)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            getQuestion();

        } else {
            Toast.makeText(QuestionSplashActivity.this, "دسترسی به موقعیت شما وجود ندارد", Toast.LENGTH_SHORT).show();
            // Display the missing permission error dialog when the fragments resume.

        }
    }

    int chPageduration = 1000;

    public void onStartButtonClick(View v) {
        startButton.setText("");
        startProgress.setVisibility(View.VISIBLE);
        startButton.setEnabled(false);
        ApiManagerInvoke.startGamePlay(gamePlayId, new GoResponseHandler<MessageContract<List<Question>>>() {
            @Override
            public void onResponse(final MessageContract<List<Question>> messageContract) {
                if (messageContract != null && messageContract.isSuccess) {
                    list = (ArrayList<Question>) messageContract.data;
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            onGameStatusListener.onStart(false, "");
                            YoYo.with(Techniques.SlideOutLeft).duration(chPageduration).playOn(leftBox);
                            YoYo.with(Techniques.SlideOutRight).duration(chPageduration).playOn(rightBox);
                            YoYo.with(Techniques.SlideOutLeft).duration(chPageduration).playOn(cloudLeft);
                            YoYo.with(Techniques.SlideOutRight).duration(chPageduration).playOn(cloudRight);
                            YoYo.with(Techniques.SlideOutDown).duration(chPageduration).playOn(tunnelLayout);
                            YoYo.with(Techniques.FadeOutUp).duration(chPageduration).playOn(topCardview);
                            YoYo.with(Techniques.FadeOutUp).duration(chPageduration).playOn(soundIcon);

                            YoYo.with(Techniques.FadeOut).duration(chPageduration).onEnd(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {

                                    Intent intent = new Intent(QuestionSplashActivity.this, QuestionActivity.class);
                                    intent.putParcelableArrayListExtra("qlist", list);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    intent.putExtra("gamePlay", gamePlayId);
                                    //intent.putExtra("offseeApi", offseeApi);
                                    intent.putExtra("duration", maxQuestion * timePerQuestion);
                                    isSelfFinish = true;
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                    finish();
                                }
                            }).playOn(startButton);
                        }
                    });

                } else if (messageContract != null && !messageContract.isSuccess) {
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            onGameStatusListener.onStart(true, messageContract.message);
                            Toast.makeText(App.getContext(), messageContract.message, Toast.LENGTH_SHORT).show();
                            startButton.setText(getString(R.string.startGame));
                            startButton.setEnabled(true);
                            startProgress.setVisibility(View.GONE);
                        }
                    });
                } else {
                    App.serverError(App.getContext());
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setText(getString(R.string.startGame));
                            startButton.setEnabled(true);
                            startProgress.setVisibility(View.GONE);
                            onGameStatusListener.onStart(true, getString(R.string.serverError));

                        }
                    });
                }
            }
        });

    }

    public void onTryAgain(View v) {
        progressBar.setVisibility(View.VISIBLE);
        tryButton.setVisibility(View.GONE);
        getQuestion();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (soundStatus) {
            startSound();
        }
    }

    private void startSound() {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.overworld);
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } else {
            mediaPlayer.start();
        }

    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (!isSelfFinish) {
            onGameStatusListener.onCancel();
        }
        OffseeApi.unregisterActivity(this);
    }
}