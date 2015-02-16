package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.GridLayout;

import java.util.ArrayList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.testing.TestQuestion;
import org.eyeseetea.malariacare.testing.TestTab;
import org.eyeseetea.malariacare.utils.PopulateDB;
import java.util.Random;


import java.util.List;
//import org.eyeseetea.malariacare.database.MalariaCareDbHelper;


public class MainActivity extends ActionBarActivity {

    protected List<TestQuestion> getQuestionSet(int size) {
        // Just to be able to work, I need a question simulator, in order to emulate the DB entries
        Random r = new Random();
        String optionSets[] = {
                "asked",
                "done",
                "yesno",
                "yesNoNA",
                "yesNoAsked",
                "yesNoUnkasked",
                "gender",
                "officer",
                "malResults",
                "malDiagnose",
                "malSpecies",
                "result"
        };
        Log.i(".MainActivity", "optionSet created");
        List<TestQuestion> testQuestionSimulator = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Log.i(".MainActivity", "creating question " + i);
            // The text of the question will be fixed
            String statement = "Question number " + Integer.toString(i);
            // Te OptionSet will be randomized
            String optionSet = optionSets[r.nextInt(optionSets.length)];
            Log.i(".MainActivity", "optionSet " + optionSet);
            // We finally add both to the question array
            testQuestionSimulator.add(new TestQuestion(statement, optionSet));
            Log.i(".MainActivity", "question finished");
        }
        Log.i(".MainActivity", "questions simulated");
        return testQuestionSimulator;
    }

    protected List<TestTab> getTabSet(int size){
        // Just to be able to work, I need a question simulator, in order to emulate the DB entries
        Random r = new Random();
        String tabTypes [] = {
                "Score",
                "GNR1",
                "Microscopy1",
                "RDT1",
                "GNR2",
                "Microscopy2",
                "RDT2",
                "GNR3",
                "Microscopy3",
                "GNR3"
        };
        Log.i(".MainActivity","tabSet created");
        List<TestTab> tabSimulator = new ArrayList<>();
        for (int i=0; i<size; i++) {
            Log.i(".MainActivity", "creating tab " + i);
            // Te OptionSet will be randomized
            String tabType = tabTypes[r.nextInt(tabTypes.length)];
            Log.i(".MainActivity", "tabType " + tabType);
            // We finally add both to the question array
            tabSimulator.add(new TestTab(tabType));
            Log.i(".MainActivity", "tab finished");
        }
        Log.i(".MainActivity","tabs simulated");
        return tabSimulator;
    }

    protected int insertLayout(TestQuestion testQuestion, GridLayout parent){
        String layout = testQuestion.getOptionSet();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;
        TextView statement;
        int child = -1;
        switch(layout) {
            case "asked":
                child = R.layout.asked;
                break;
            case "done":
                child = R.layout.done;
                break;
            case "yesno":
                child = R.layout.yesno;
                break;
            case "yesNoNA":
                child = R.layout.yesnona;
                break;
            case "yesNoAsked":
                child = R.layout.yesnonotanswered;
                break;
            case "yesNoUnkasked":
                child = R.layout.yesnounkasked;
                break;
            case "gender":
                child = R.layout.gender;
                break;
            case "officer":
                child = R.layout.officer;
                break;
            case "malResults":
                child = R.layout.malresults;
                break;
            case "malDiagnose":
                child = R.layout.maldiagnose;
                break;
            case "malSpecies":
                child = R.layout.malspecies;
                break;
            case "result":
                child = R.layout.result;
                break;
         }
        Log.i(".MainActivity", "question statement: " + testQuestion.getStatement());
        v = inflater.inflate(child, parent, false);
        statement = (TextView) v.findViewById(R.id.statement);
        Log.i(".MainActivity", "previous statement: " + statement.getText());
        statement.setText(testQuestion.getStatement());
        Log.i(".MainActivity", "later statement: " + statement.getText());
        parent.addView(v);
        // For not found layout, child will be -1
        return child;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*      //-------NACHO STUFF---------//
        Log.i(".MainActivity", "App started");
        setContentView(R.layout.main_layout);

        // We get a set of questions for our layout
        List<TestQuestion> questions;
        questions = getQuestionSet(100);

        // We get a set of tabs
        List<TestTab> tabs;
        tabs = getTabSet(3);

        // We take the Layouts for adding the content
        GridLayout body = (GridLayout) this.findViewById(R.id.Body);

        // We add the questions
        for (Question question : questions) {
            // Each optionSet has its own layout defined by <optionSetString>.xml
            //   With the insertLayout function, we're trying to insert the question layout into the
            //    parent layout. The function returns the question layout. We assert is always != -1

            assertTrue(insertLayout(question, body) != -1);
        }
*/
        //-------ADRI STUFF---------//
//        File dbFile = getDatabasePath("malariacare.db");
//        adb pull /data/data/org.eyeseetea.malariacare/databases/malariacare.db ~/malariacare.db

        if (Tab.count(Tab.class, null, null)==0) {
            AssetManager assetManager = getAssets();
            PopulateDB.populateDB(assetManager);
        }

        List<Tab> tabList2 = Tab.listAll(Tab.class);
        for (Tab tabItem : tabList2){
            //codigo
            System.out.println(tabItem.toString());
        }

        // Creating a new LinearLayout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        linearLayout.setWeightSum(6f);
        linearLayout.setLayoutParams(layoutParams);


        TextView tv;

        //"Profile"
        Tab currentTab = Tab.findById(Tab.class, 10L);
        List<Header> headerList = currentTab.getHeaders();
        for (Header header : headerList){
            //codigo
            System.out.println(header.toString());
            List<Question> questionList = header.getQuestions();
            for (Question question : questionList){
                //codigo

                System.out.println(question.toString());
                System.out.println("Hijos");
                System.out.println(question.getQuestion());
                // Creating a new TextView
                tv = new TextView(this);
                tv.setText(question.getForm_name());
                tv.setLayoutParams(layoutParams);
                linearLayout.addView(tv);
            }
        }


        // Creating a new EditText
        EditText et=new EditText(this);
        et.setLayoutParams(layoutParams);
        linearLayout.addView(et);

        setContentView(linearLayout, layoutParams);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendMessage(View view) {
        Log.i(".MainActivity", "Button pressed");
    }
}
