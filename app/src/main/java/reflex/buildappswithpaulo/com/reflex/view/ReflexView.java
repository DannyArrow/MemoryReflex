package reflex.buildappswithpaulo.com.reflex.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

import reflex.buildappswithpaulo.com.reflex.R;

/**
 * Created by paulodichone on 11/9/17.
 */

public class ReflexView extends View {
    public static final int INITIAL_ANIMATION_DURATION = 12000; //12 SECOND
    public static final Random random = new Random();
    public static final int SPOT_DIAMETER = 200;
    public static final float SCALE_X = 0.25f;
    public static final float SCALE_Y = 0.25f;
    public static final int INITIAL_SPOTS = 5;
    public static final int SPOT_DELAY = 500;
    public static final int LIVES = 3;
    public static final int MAX_LIVES = 7;
    public static final int NEW_LEVEL = 10;
    public static final int HIT_SOUND_ID = 1;
    public static final int MISS_SOUND_ID = 2;
    public static final int DISAPPEAR_SOUND_ID = 3;
    public static final int SOUND_PRIORITY = 1;
    public static final int SOUND_QUALITY = 100;
    public static final int MAX_STREAMS = 4;
    //Static instance variables
    private static final String HIGH_SCORE = "HIGH_SCORE";
    //Collections types for our circles/spots (imageviews) and Animators
    private final Queue<ImageView> spots = new ConcurrentLinkedDeque<>();
    private final Queue<Animator> animators = new ConcurrentLinkedDeque<>();
    private SharedPreferences preferences;
    //Variables that manage the game
    private int spotsTouched;
    private int next;
    private int coundownofspot;
    private int score;
    private int level = 2;
    private int viewWidth;
    private int viewHeight;
    private long animationTime;
    private boolean gameOver = true;
    private boolean gamePaused;
    private boolean dialogDisplayed;
    private int highScore;
    private TextView highScoreTextView;
    private TextView currentScoreTextView;
    private TextView levelTextView;
    private LinearLayout livesLinearLayout;
    private RelativeLayout relativeLayout;
    private Resources resources;
    private LayoutInflater layoutInflater;
    private Handler spotHandler;
    private SoundPool soundPool;
    private int check;
    private int volume;
    private LinearLayout alert;
    private Map<Integer, Integer> soundMap;
    private ArrayList<Integer> redorblue;
    private Context context;
    Toast toast = null;
    private int count = 0;
    private int count2 = 0;
    private int randomnumber;
    private String mode;
    private LinearLayout layout2;
    private LinearLayout layout3;

    private LinearLayout parent;
    public AlertDialog dialog;


    public ReflexView(Context context, SharedPreferences sharedPreferences, RelativeLayout parentLayout) {
        super(context);
        this.context = context;

        preferences = sharedPreferences;
        highScore = preferences.getInt(HIGH_SCORE, 0);

        //save resources for loading external values
        resources = context.getResources();


        //save LayoutInflater
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        //Setup UI components
        relativeLayout = parentLayout;
        // alert = relativeLayout.findViewById(R.id.alert);

        livesLinearLayout = relativeLayout.findViewById(R.id.lifeLinearLayout);
        highScoreTextView = relativeLayout.findViewById(R.id.highScoreTextView);
        currentScoreTextView = relativeLayout.findViewById(R.id.scoreTextView);
        levelTextView = relativeLayout.findViewById(R.id.levelTextview);

        redorblue = new ArrayList<Integer>();

        spotHandler = new Handler();


    }


    // store SpotOnView's width/height
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        viewWidth = width; // save the new width
        viewHeight = height; // save the new height
    } // end method onSizeChanged

    // called by the SpotOn Activity when it receives a call to onPause
    public void pause() {
        gamePaused = true;
        soundPool.release(); // release audio resources
        soundPool = null;
        cancelAnimations(); // cancel all outstanding animations
    } // end method pause

    // cancel animations and remove ImageViews representing spots
    private void cancelAnimations() {
        // cancel remaining animations
        for (Animator animator : animators)
            animator.cancel();

        // remove remaining spots from the screen
        for (ImageView view : spots)
            relativeLayout.removeView(view);

        //spotHandler.removeCallbacks(addSpotRunnable);
        animators.clear();
        spots.clear();
    } // end method cancelAnimations

    // called by the SpotOn Activity when it receives a call to onResume
    public void resume(Context context) {

        gamePaused = false;
        initializeSoundEffects(context); // initialize app's SoundPool
             // resume the game
              resume_game();

    } // end method resume

    private void resume_game() {
        if(dialog != null) {
            parent = null;
            dialog.dismiss();
        }

        livesLinearLayout.removeAllViews();
        for (int i = 0; i < LIVES; i++) {
            // add life indicator to screen
            livesLinearLayout.addView(
                    (ImageView) layoutInflater.inflate(R.layout.life, null));
        }
        for (int i = 0; i < check; i++){
            score -= 10 * level;
        }
        spotsTouched = 0;
        next = 0;
        check = 0;
        redorblue.clear();
        animationTime = INITIAL_ANIMATION_DURATION;
        cancelAnimations();
        spots.clear();
        animators.clear();
        displayScores();

        create_RandomCircle_dialog();

    }

    // start a new game
    public void resetGame() {
        if (score > highScore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(HIGH_SCORE, score);
            editor.apply(); // store the new high score
            highScore = score;
        }
        if(gameOver) {
            score = 0;
            level = 2;
        }
        displayScores();
        gamePaused = false;
        count2 = 0;
        count = 0;
        coundownofspot = level;
        next = 0;
        check = 0;
        redorblue.clear();
        cancelAnimations();
        spots.clear(); // empty the List of spots
        animators.clear(); // empty the List of Animators
        livesLinearLayout.removeAllViews(); // clear old lives from screen

        animationTime = INITIAL_ANIMATION_DURATION; // init animation length
        spotsTouched = 0; // reset the number of spots touched
        // reset the level
        gameOver = true; // the game is not over
        displayScores(); // display scores and level

        // add lives
        for (int i = 0; i < LIVES; i++) {
            // add life indicator to screen
            livesLinearLayout.addView(
                    (ImageView) layoutInflater.inflate(R.layout.life, null));
        } // end for

        create_RandomCircle_dialog();


        // add INITIAL_SPOTS new spots at SPOT_DELAY time intervals in ms

    } // end method resetGame



    private void create_RandomCircle_dialog(){
         parent = new LinearLayout(getContext());

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);


        layout2 = new LinearLayout(getContext());
        layout2.setGravity(Gravity.CENTER_HORIZONTAL);
        layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        layout3 = new LinearLayout(getContext());
        layout3.setGravity(Gravity.CENTER_HORIZONTAL);
        layout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // sets the number of color circle to layout2 and layout3
        setblocks();
        parent.addView(layout2);

        parent.addView(layout3);



        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(parent);
        builder.setCancelable(false);
        builder.setPositiveButton("start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 1; j <= level; ++j) {
                    //spotHandler.postDelayed(addSpotRunnable, j * SPOT_DELAY);
                    addNewSpot();


                }

            }
        });

         dialog = builder.create();

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);


        dialog.show();

    }

    private void setblocks() {


        for (int i = 0; i < level; i++) {
            ImageView blocks =
                    (ImageView) layoutInflater.inflate(R.layout.block_images, null);
            blocks.setImageResource(R.drawable.red_spot);
            blocks.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
            if(i<8) {
                set_color_to_circler(blocks);
                layout2.addView(blocks); // is the first line of circle display in the dialog
            }
            else {
                for (int j = level-1; j < level; j++) {
                    set_color_to_circler(blocks);
                    layout3.addView(blocks); // is the second line of circle display in the dialog
                }
            }
        }
    }

    private void set_color_to_circler(ImageView circle){
        randomnumber = random.nextInt(4);
        switch (randomnumber) {
            case 0:
                circle.setColorFilter(Color.RED);
                break;

            case 1:
                circle.setColorFilter(Color.BLACK);
                break;

            case 2:
                circle.setColorFilter(Color.YELLOW);
                break;
            case 3:
                circle.setColorFilter(Color.BLUE);
                break;

        }
        redorblue.add(randomnumber);
    }

    // create the app's SoundPool for playing game audio
    private void initializeSoundEffects(Context context) {


        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SOUND_QUALITY);


        //set sound effect volume
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);


        //Create a sound map
        soundMap = new HashMap<>();
        soundMap.put(HIT_SOUND_ID, soundPool.load(context, R.raw.hit, SOUND_PRIORITY));
        soundMap.put(MISS_SOUND_ID, soundPool.load(context, R.raw.wrong, SOUND_PRIORITY));
        soundMap.put(DISAPPEAR_SOUND_ID, soundPool.load(context, R.raw.disappear, SOUND_PRIORITY));

    } // end method initializeSoundEffect

    // display scores and level
    private void displayScores() {
        // display the high score, current score and level
        highScoreTextView.setText(
                resources.getString(R.string.high_score) + " " + highScore);
        currentScoreTextView.setText(
                resources.getString(R.string.score) + " " + score);
        levelTextView.setText(
                resources.getString(R.string.level) + " " + level);
    } // end function displayScores

    // Runnable used to add new spots to the game at the start
//    private Runnable addSpotRunnable = new Runnable()
//    {
//        @Override
//        public void run()
//        {
//            addNewSpot(); // add a new spot to the game
//        } // end method run
//    }; // end Runnable

    // adds a new spot at a random location and starts its animation
    public void addNewSpot() {
        count++ ;
        // choose two random coordinates for the starting and ending points
        int x = random.nextInt(viewWidth - SPOT_DIAMETER);
        int y = random.nextInt(viewHeight - SPOT_DIAMETER);
        int x2 = random.nextInt(viewWidth - SPOT_DIAMETER);
        int y2 = random.nextInt(viewHeight - SPOT_DIAMETER);
        // create new spot
        final ImageView spot =
                (ImageView) layoutInflater.inflate(R.layout.untouched, null);
        spots.add(spot); // add the new spot to our list of spots
        spot.setLayoutParams(new RelativeLayout.LayoutParams(
                SPOT_DIAMETER, SPOT_DIAMETER));
        spot.setImageResource(R.drawable.red_spot);

        switch (redorblue.get(next)){
            case 0:
                spot.setColorFilter(Color.RED);
                break;

            case 1:
                spot.setColorFilter(Color.BLACK);
                break;

            case 2:
                spot.setColorFilter(Color.YELLOW);
                break;
            case 3:
                spot.setColorFilter(Color.BLUE);
                break;

        }
        spot.setTag(redorblue.get(next));

        next++;
        spot.setX(x); // set spot's starting x location
        spot.setY(y); // set spot's starting y location
        spot.setOnClickListener( // listens for spot being clicked
                new OnClickListener() {
                    public void onClick(View v) {
                        if (toast != null) {
                            toast.cancel();
                        }
                        touchedSpot(spot); // handle touched spot

                    } // end method onClick
                } // end OnClickListener
        ); // end call to setOnClickListener
        relativeLayout.addView(spot); // add spot to the screen

        // configure and start spot's animation
        spot.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y)
                .setDuration(animationTime).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        animators.add(animation); // save for possible cancel
                    } // end method onAnimationStart

                    public void onAnimationEnd(Animator animation) {
                        animators.remove(animation); // animation done, remove

                        if (!gamePaused && spots.contains(spot)) // not touched
                        {
                            //since there multiple spots at the same time, there needs to be a tracker
                            if(count == level) {
                                if(count2 == 0) {
                                    count2++;
                                    gameOver = true;
                                    cancelAnimations();
                                    resetGame();

                                }

                            }
                           // missedSpot(spot); // lose a life
                        } // end if
                    } // end method onAnimationEnd
                } // end AnimatorListenerAdapter
        ); // end call to setListener
    } // end addNewSpot method

    private void touchedSpot(ImageView spot) {
        //toast = new Toast(context);
        //toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);

        if (spot.getTag() == redorblue.get(check)) {
          //  toast.makeText(context, "Correct order", Toast.LENGTH_LONG).show();
            check++;
            coundownofspot--;
            relativeLayout.removeView(spot); // remove touched spot from screen
            spots.remove(spot); // remove old spot from list

            ++spotsTouched; // increment the number of spots touched
            score += 10 * level; // increment the score

            // play the hit sounds
            if (soundPool != null)
                soundPool.play(HIT_SOUND_ID, volume, volume,
                        SOUND_PRIORITY, 0, 1f);

            if (spotsTouched == redorblue.size()) {
                level++;
                gameOver = false;
                resetGame();
            }


            displayScores(); // update score/level on the screen
        } else {
           // toast.makeText(context, "Incorrect order, try again", Toast.LENGTH_LONG).show();
            livesLinearLayout.removeViewAt(
                    livesLinearLayout.getChildCount() - 1);
            if (soundPool != null)
                soundPool.play(MISS_SOUND_ID, volume, volume,
                        SOUND_PRIORITY, 0, 1f);
            gamePaused = false;
            if (livesLinearLayout.getChildCount() == 0) {
                gameOver = true;
                resetGame();
                cancelAnimations();
               // resetGame();

                //builder.show();

            }
        }

    }
/*
    public void showmodedialog(){

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Game Mode:");
        alert.setNeutralButton("Easy Mode", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mode = "easy";
            }
        });

        alert.setNeutralButton("Hard Mode", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mode = "hard";

            }
        });
    }

    // end method touchedSpot

    // called when a spot finishes its animation without being touched

    public void missedSpot(ImageView spot) {

//             // play the disappear sound effect
//             if (soundPool != null) {
//                 soundPool.play(DISAPPEAR_SOUND_ID, volume, volume,
//                         SOUND_PRIORITY, 0, 1f);
//
//             }
//             // if the game has been lost
//
//
//             // if the last game's score is greater than the high score
//             if (score > highScore) {
//                 SharedPreferences.Editor editor = preferences.edit();
//                 editor.putInt(HIGH_SCORE, score);
//                 editor.apply(); // store the new high score
//                 highScore = score;
//             }
//             gameOver = false;

             // end if

//            cancelAnimations();
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle("Game Over");
//            builder.setMessage("Score: " + score);
//            builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    displayScores();
//                    dialogDisplayed = false;
//                    resetGame();
//
//                }
//            });
//            dialogDisplayed = true;
//            builder.show();
//
//
//
//
//        }else {
//            livesLinearLayout.removeViewAt(
//                    livesLinearLayout.getChildCount() - 1
//            );
//            addNewSpot();
//        }

             // end method missedSpot
         }
         */

    }

