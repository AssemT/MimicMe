package com.example.assemtursyngalyeva.mimicme;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;


public class Speech extends Activity implements RecognitionListener {


    //  Named searches to configure the decoder
        private static final String KWS_SEARCH = "wakeup";
        private static final String FORECAST_SEARCH = "forecast";
        private static final String DIGITS_SEARCH = "digits";
        private static final String PHONE_SEARCH = "phones";
        private static final String MENU_SEARCH = "menu";

    // Keyword to activate menu
        private static final String KEYPHRASE = "computer";

        private SpeechRecognizer recognizer;
        private HashMap<String, Integer> captions;  // stores in hashmap

        @Override
        public void onCreate(Bundle state) {
            super.onCreate(state);

            // Prepare the data for user interface
            captions = new HashMap<String, Integer>();
            captions.put(KWS_SEARCH, R.string.kws_caption);
            captions.put(MENU_SEARCH, R.string.menu_caption);
            captions.put(DIGITS_SEARCH, R.string.digits_caption);
            captions.put(PHONE_SEARCH, R.string.phone_caption);
            captions.put(FORECAST_SEARCH, R.string.forecast_caption);

            setContentView(R.layout.main);
            ((TextView) findViewById(R.id.caption_text)).setText("Preparing the recognizer");

            // execution in async task is for recognizer initialization
            new AsyncTask<Void, Void, Exception>() {
                @Override
                protected Exception doInBackground(Void... params) {
                    try {
                        Assets assets = new Assets(Speech.this);
                        File assetDir = assets.syncAssets();
                        setupRecognizer(assetDir);
                    } catch (IOException e) {
                        return e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception result) {
                    if (result != null) {
                        ((TextView) findViewById(R.id.caption_text)).setText("Failed to init recognizer " + result);
                    } else {
                        switchSearch(KWS_SEARCH);
                    }
                }
            }.execute();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            recognizer.cancel();
            recognizer.shutdown();
        }

        /*
          In partial result, we get updates about current hypothesis. We need to wait
          for final result in onResult.
        */
        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            if (hypothesis == null)
                return;

            String text = hypothesis.getHypstr();
            if (text.equals(KEYPHRASE))
                switchSearch(MENU_SEARCH);
            else if (text.equals(DIGITS_SEARCH))
                switchSearch(DIGITS_SEARCH);
            else if (text.equals(PHONE_SEARCH))
                switchSearch(PHONE_SEARCH);
            else if (text.equals(FORECAST_SEARCH))
                switchSearch(FORECAST_SEARCH);
            else
                ((TextView) findViewById(R.id.result_text)).setText(text);
        }


        // callback to stop the recognizer.
        @Override
        public void onResult(Hypothesis hypothesis) {
            ((TextView) findViewById(R.id.result_text)).setText("");
            if (hypothesis != null) {
                String text = hypothesis.getHypstr();
                makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        //stop recognizer here to get a final result
        @Override
        public void onEndOfSpeech() {
            if (!recognizer.getSearchName().equals(KWS_SEARCH))
                switchSearch(KWS_SEARCH);
        }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer =  defaultSetup().setAcousticModel(new File(assetsDir, "en-us-ptm"))
                                    .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                        // to disable logging of raw audio comment out this call (takes a lot of space on the device)
                                    .setRawLogDir(assetsDir)
                        // threshold to tune for keyphrase to balance between false alarms and misses
                                    .setKeywordThreshold(1e-45f)
                        // utilize context-independent phonetic search, context-dependent is too slow for mobile
                                    .setBoolean("-allphone_ci", true)
                                    .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create language model search
        File languageModel = new File(assetsDir, "weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
}
