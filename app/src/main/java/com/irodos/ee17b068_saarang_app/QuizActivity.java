package com.irodos.ee17b068_saarang_app;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    //enumeration
    enum SCREEN{start_screen, second_screen, question_screen,result_screen}

    Button start;
    JSONArray questionSet;
    LinearLayout MainBox;
    LinearLayout lin_layout_template;
    Button answers[] = new Button[4];
    TextView questionBox;
    TextView pointBox;
    TextView pointInc;
    int correctAnswer;
    int qno;
    boolean share_clicked; //share button click state
    boolean clickState; //Stores information if the answers for one question have already been clicked to avoid errors
    SCREEN s;
    int points;
    String difficulty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_quiz);
        MainBox = (LinearLayout)findViewById(R.id.MainBox);
        clickState = false;

        if(savedInstanceState==null)
            reset();
        else{
            s = (SCREEN) savedInstanceState.get("s");
            share_clicked = savedInstanceState.getBoolean("share_clicked");
            if(s == SCREEN.question_screen) {
                try{
                    difficulty = savedInstanceState.getString("diff");
                    points = savedInstanceState.getInt("points");
                    qno = savedInstanceState.getInt("qno");
                    clickState = savedInstanceState.getBoolean("clickState");
                    questionSet = new JSONArray(savedInstanceState.getString("questionSet"));
                    initializeQuiz();
                    runQuiz();
                }catch(Exception e){
                    s = SCREEN.start_screen;
                    reset();
                }
            }
            else if(s==SCREEN.second_screen){
                showSecondScreen();
            }
            else if(s == SCREEN.result_screen){
                points = savedInstanceState.getInt("points");
                printPoints();
            }
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        savedInstanceState.putSerializable("s", s);
        savedInstanceState.putString("diff", difficulty);
        savedInstanceState.putInt("points", points);
        savedInstanceState.putInt("qno", qno);
        savedInstanceState.putBoolean("clickState", clickState);
        savedInstanceState.putBoolean("share_clicked", share_clicked);
        try {
            savedInstanceState.putString("questionSet", questionSet.toString());
        }catch(Exception e){
            savedInstanceState.putString("questionSet", "");
        }
    }
    private View.OnClickListener starter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((LinearLayout) start.getParent()).removeView(start);
            Log.d("QuizActivity.Click", "Start Clicked");
            //Change layout to difficulty_layout
            showSecondScreen();
        }
    };


    public class retrieveData extends AsyncTask<String, Void, JSONArray> {
        String string = "";
        Context c;
        RelativeLayout load;
        public retrieveData(Context c){
            this.c = c;
        }
        @Override
        protected void onPreExecute(){
            LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            load = (RelativeLayout) inflater.inflate(R.layout.loading_questions, MainBox, false);
            MainBox.addView(load);
        }
        @Override
        protected JSONArray doInBackground(String... arg) {
            try {
                URL url = new URL(arg[0]);
                string = getDataFromHttpConnection(url);
            } catch (Exception e) {
                Log.d("QuizActivity.Https", "Https Object Problem Occurred");
                return null;
            }
            try {
                JSONObject j = new JSONObject(string);
                if (j == null)
                    Log.d("QuizActivity.Https", "Null JSONObject Made");
                JSONArray q_with_a = j.getJSONArray("results");
                if (q_with_a == null)
                    Log.d("QuizActivity.Https", "Null JSONArray Made");
                return q_with_a;
            } catch (Exception e) {
                return null;
            }
        }

        private String getDataFromHttpConnection(URL url) throws IOException {
            String str = "";
            HttpURLConnection h = (HttpURLConnection) url.openConnection();
            try {
                InputStream is = new BufferedInputStream(h.getInputStream());
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                str += (s.hasNext() ? s.next() : "");
            } catch (Exception e) {
                Log.d("QuizActivity.Input", "Input Stream error");
            } finally {
                h.disconnect();
            }
            return str;
        }

        protected void onPostExecute(JSONArray q_with_a_set) {
            MainBox.removeView(load);
            if(q_with_a_set!=null) {
                questionSet = q_with_a_set;
                initializeQuiz();
                runQuiz();
                Log.d("QuizAct", "Leaving onPostExecute()");
            }else{
                Toast.makeText(c, "Check internet connection and try again later", Toast.LENGTH_LONG).show();
                reset();
            }
        }
    }

    private View.OnClickListener AnswerListener = new View.OnClickListener() {
        public void onClick(final View v) {
            //Check if Answer is correct and Make updates
            if (clickState == false) {
                clickState = true;
                answers[correctAnswer].setBackground(getResources().getDrawable(R.drawable.right));
                if (v.getId() == answers[correctAnswer].getId()) {
                    points += 3;
                    pointInc.setTextColor(getResources().getColor(R.color.correct));
                    pointInc.setText("+3");
                } else {
                    points -= 1;
                    v.setBackground(getResources().getDrawable(R.drawable.wrong));
                    pointInc.setText("- 1");
                    pointInc.setTextColor(getResources().getColor(R.color.wrong));
                }
                qno++;
                final ViewAnimation x = new ViewAnimation(questionBox);
                new CountDownTimer(500, 10) {
                    public void onFinish() {
                        try {
                            x.circleAnimation(ViewAnimation.ANIMATION_HIDE);
                        }catch (Exception e){
                            x.v.setVisibility(View.INVISIBLE);
                        }
                    }
                    public void onTick(long millisUntilFinished) {}
                }.start();
                new CountDownTimer(1000, 10) {
                    public void onFinish() {
                        answers[correctAnswer].setBackground(getResources().getDrawable(R.drawable.button_basic));
                        v.setBackground(getResources().getDrawable(R.drawable.button_basic));
                        pointInc.setText("");
                        pointBox.setText(Integer.toString(points));
                        try {
                            x.circleAnimation(ViewAnimation.ANIMATION_SHOW);
                        }catch(Exception e){
                            x.v.setVisibility(View.VISIBLE);
                        }
                        if (questionSet.length() > (qno)) {
                            runQuiz();
                        }
                        else {
                            //Change State
                            s = SCREEN.result_screen;
                            printPoints();
                        }
                    }

                    public void onTick(long millisUntilFinished){}
                }.start();
            }
        }
    };

    //Method to Print points after all questions are done
    private void printPoints(){
        //REMOVAL OF LAYOUTS
       try {
           ((ViewGroup) lin_layout_template.getParent()).removeView(lin_layout_template);
       }catch(Exception e){}

        //SETTING Layout for results
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.result_layout, MainBox, false);
        TextView result = (TextView)v.findViewById(R.id.result);
        result.setText("Your Score: " + Integer.toString(points));
        Button playAgain = (Button)v.findViewById(R.id.playAgain);
        Button share = (Button)v.findViewById(R.id.share);
        MainBox.addView(v);

        //When 'Play Again' is clicked
        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayout) v.getParent().getParent().getParent()).removeView((LinearLayout) v.getParent().getParent());
                Log.d("QuizActivity.Click", "Play Again Clicked");
                reset();
            }
        });

        //Sharing Score
        final String textMessage = "Hey! I scored " + Integer.toString(points) + " on a quiz with " + difficulty
                                    + " difficulty on QuizApplication!\n\nYou should try it out too!!";
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!share_clicked) {
                    //has Share button been clicked
                    share_clicked = true;

                    // Create the text message with a string
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, textMessage);
                    sendIntent.setType("text/plain");

                    // Create intent to show the chooser dialog
                    Intent chooser = Intent.createChooser(sendIntent, "Share");

                    // Verify that the intent will resolve to an activity
                    if (sendIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(chooser);
                        share_clicked = false;
                    }
                }
            }
        });
    }

    //Method to setup initial screen
    private void reset(){
        s = SCREEN.start_screen; //Change State
        share_clicked = false;
        points = 0;
        qno = 0;
        difficulty = "";
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        start = (Button) inflater.inflate(R.layout.get_questions_button, MainBox, false);
        MainBox.addView(start);
        start.setOnClickListener(starter);
    }

    //Method to create suitable UI for the quiz
    private void initializeQuiz(){
        //Initializing UI
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lin_layout_template = (LinearLayout) inflater.inflate(R.layout.question_set, MainBox, false);
        questionBox = (TextView) lin_layout_template.findViewById(R.id.questionBox);
        pointBox = (TextView) lin_layout_template.findViewById(R.id.pointBox);
        pointBox.setText(Integer.toString(points));
        pointInc = (TextView) lin_layout_template.findViewById(R.id.point_inc);
        answers[0] = (Button) lin_layout_template.findViewById(R.id.answer1);
        answers[1] = (Button) lin_layout_template.findViewById(R.id.answer2);
        answers[2] = (Button) lin_layout_template.findViewById(R.id.answer3);
        answers[3] = (Button) lin_layout_template.findViewById(R.id.answer4);
        for (int i = 0; i < 4; i++) {
            answers[i].setOnClickListener(AnswerListener);
        }
        MainBox.addView(lin_layout_template);
    }
    
    //Method displaying the Second Screen, i.e., the difficulty screen
    private void showSecondScreen(){
        s = SCREEN.second_screen;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lin_layout_template = (LinearLayout)(inflater.inflate(R.layout.get_difficulty, MainBox, false))
                                .findViewById(R.id.diff_ll);

        lin_layout_template.findViewById(R.id.easy_button).setOnClickListener(DifficultyListener);
        lin_layout_template.findViewById(R.id.hard_button).setOnClickListener(DifficultyListener);
        lin_layout_template.findViewById(R.id.medium_button).setOnClickListener(DifficultyListener);
        MainBox.addView((ScrollView)lin_layout_template.getParent());
    }
    
    private View.OnClickListener DifficultyListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            String URL_String;
            if(v.getId()==R.id.easy_button){
               URL_String = "https://opentdb.com/api.php?amount=10&category=18&difficulty=easy&type=multiple";
                difficulty = "easy";
            }
            else if(v.getId()==R.id.medium_button){
               URL_String = "https://opentdb.com/api.php?amount=10&category=18&difficulty=medium&type=multiple";
               difficulty = "medium";
            }
            else{
               URL_String = "https://opentdb.com/api.php?amount=10&category=18&difficulty=hard&type=multiple";
               difficulty = "hard";
            }
            //Retrieve Questions
            new retrieveData(v.getContext()).execute(URL_String);

            //Remove Difficulty Layout
            ((LinearLayout) lin_layout_template.getParent().getParent()).removeView((ScrollView)lin_layout_template.getParent());
        }
    };
    
    //Display Question (qno) of the quiz
    private void runQuiz(){
        //Changing State
        s = SCREEN.question_screen;
        
        //Posting questions
        clickState = false;
        try {
            JSONObject q_and_a = questionSet.getJSONObject(qno);
            String text = (Integer.toString(qno + 1) + ". " + q_and_a.getString("question").replace("&quot;", "\"")
                    .replace("&#039;", "\'"));
            questionBox.setText(text);
            //new ViewAnimation(questionBox).circleChangeText(text);
            correctAnswer = new Random().nextInt(4);
            answers[correctAnswer].setText(q_and_a.getString("correct_answer").replace("&quot;", "\"").replace("&#039;","\'"));
            JSONArray wrong_ans = q_and_a.getJSONArray("incorrect_answers");
            int j = correctAnswer%3; //wrong answer pointer
            for(int i = 0; i < 4; i++){
                if(i!=correctAnswer) {
                    String s="";
                    s = wrong_ans.getString(j).replace("&quot;", "\"").replace("&#039;","\'");
                    answers[i].setText(s);
                    j = (j+1)%3;
                }
            }
        }catch(Exception e){
            //Mostly no error will take place
            Log.d("QuizAct", "Something wrong");
        }
    }
}
