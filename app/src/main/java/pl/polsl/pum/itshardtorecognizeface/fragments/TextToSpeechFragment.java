package pl.polsl.pum.itshardtorecognizeface.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TextToSpeechFragment extends Fragment implements TextToSpeech.OnInitListener {

    public final static int TTS_CHECK_CODE = 999;

    private OnFragmentInteractionListener mListener;
    private TextToSpeech tts;
    private int utterance = 0;
    private boolean ttsActive = false;

    public static TextToSpeechFragment newInstance() {
        TextToSpeechFragment fragment = new TextToSpeechFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, TTS_CHECK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(getActivity(), this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = Locale.getDefault();
            int result = tts.isLanguageAvailable(locale);
            if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                tts.setLanguage(locale);
            } else {
                tts.setLanguage(Locale.US);
            }
            ttsActive = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    @SuppressLint("NewApi")
    public void say(String text) {
        if (ttsActive && !tts.isSpeaking()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "" + utterance++);
        }
    }

    public interface OnFragmentInteractionListener {
        void onTtsActive();
    }
}
