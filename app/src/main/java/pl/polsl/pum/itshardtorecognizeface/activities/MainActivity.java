package pl.polsl.pum.itshardtorecognizeface.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.polsl.pum.itshardtorecognizeface.App;
import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.classifier.FaceClassifier;
import pl.polsl.pum.itshardtorecognizeface.fragments.CameraPreviewFragment;
import pl.polsl.pum.itshardtorecognizeface.fragments.TextToSpeechFragment;
import pl.polsl.pum.itshardtorecognizeface.model.Face;
import pl.polsl.pum.itshardtorecognizeface.model.FaceDetector;


public class MainActivity extends AppCompatActivity implements
        CameraPreviewFragment.OnFragmentInteractionListener,
        TextToSpeechFragment.OnFragmentInteractionListener {

    private FaceDetector faceDetector;
    private FaceClassifier faceClassifier;
    private HashMap<String, Integer> facesHistory;
    private List<String> facesInFrame;
    private TextToSpeechFragment ttsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        ttsFragment = new TextToSpeechFragment();
        fm.beginTransaction().add(ttsFragment, "TextToSpeechFragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.menu_main_trainer:
                intent = new Intent(this, TrainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.menu_main_faces_database:
                intent = new Intent(this, FacesDatabaseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.menu_main_about:
                Toast.makeText(getApplicationContext(), R.string.text_about, Toast.LENGTH_LONG).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onOpenCVLoaded() {
        faceDetector = new FaceDetector(MainActivity.this);
        faceClassifier = FaceClassifier.getInstance();
        facesHistory = new HashMap<>();
        facesInFrame = new ArrayList<>();
    }

    @Override
    public Mat onCameraFrameExtra(Mat frameRgba, Mat frameGray) {
        facesInFrame.clear();
        List<Face> faces = faceDetector.detectFaces(frameGray);
        for (Face face : faces) {
            face.drawOutline(frameRgba, new Scalar(0, 255, 0, 255), 3);
            Mat faceImage = face.extractRoi(frameGray);
            String label = faceClassifier.recognizeFace(faceImage);
            face.drawLabel(frameRgba, label, new Scalar(0, 255, 0, 255));
            if (!facesHistory.containsKey(label)) {
                facesHistory.put(label, 100);
            }
            facesInFrame.add(label);
        }
        HashMap<String, Integer> newFacesHistory = new HashMap<>();
        for (Map.Entry<String, Integer> facesHistoryEntry : facesHistory.entrySet()) {
            if (facesInFrame.contains(facesHistoryEntry.getKey())) {
                facesHistoryEntry.setValue(facesHistoryEntry.getValue() + 5);
            } else {
                facesHistoryEntry.setValue(facesHistoryEntry.getValue() - 1);
            }
            if (facesHistoryEntry.getValue() > 250) {
                ttsFragment.say(facesHistoryEntry.getKey());
                facesHistoryEntry.setValue(facesHistoryEntry.getValue() - 200);
            }
            if (facesHistoryEntry.getValue() > 0) {
                newFacesHistory.put(facesHistoryEntry.getKey(), facesHistoryEntry.getValue());
            }
        }
        facesHistory = newFacesHistory;
        return frameRgba;
    }

    @Override
    public void onTtsActive() {
        if (!App.welcomeSaid) {
            ttsFragment.say(getString(R.string.text_voice_welcome));
            App.welcomeSaid = true;
        }
    }
}
