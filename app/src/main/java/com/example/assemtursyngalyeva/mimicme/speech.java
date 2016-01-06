package com.example.assemtursyngalyeva.mimicme;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;

import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class Speech extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SpeechRecognizerSetup recognizer = defaultSetup()
                                        .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                                        .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                                        .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                                        .getRecognizer();
        recognizer.addListener(this);
    }

}
