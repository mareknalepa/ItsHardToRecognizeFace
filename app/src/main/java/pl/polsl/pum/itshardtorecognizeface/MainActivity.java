package pl.polsl.pum.itshardtorecognizeface;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.List;

import pl.polsl.pum.itshardtorecognizeface.classifier.FaceClassifier;
import pl.polsl.pum.itshardtorecognizeface.model.Face;
import pl.polsl.pum.itshardtorecognizeface.model.FaceDetector;


public class MainActivity extends AppCompatActivity implements CameraPreviewFragment.OnFragmentInteractionListener {

    private MenuItem menuTrainer;
    private MenuItem menuFacesDatabase;
    private MenuItem menuAbout;

    private FaceDetector faceDetector;
    private FaceClassifier faceClassifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuTrainer = menu.add("Open trainer");
        menuFacesDatabase = menu.add("Show faces database");
        menuAbout = menu.add("About application");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == menuTrainer) {
            Intent intent = new Intent(this, TrainerActivity.class);
            startActivity(intent);
        } else if (item == menuFacesDatabase) {

        } else if (item == menuAbout) {
            Toast.makeText(getApplicationContext(),
                    "ItsHardToRecognizeFace\nApplication developed at Silesian University of Technology\n2015",
                    Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void loaderCallbackExtra() {
        faceDetector = new FaceDetector(MainActivity.this);
        faceClassifier = new FaceClassifier(this);
        faceClassifier.loadClassifier();
    }

    @Override
    public void onCameraFrameExtra(Mat frameRgba, Mat frameGray, Mat frameProcessed) {
        List<Face> faces = faceDetector.detectFaces(frameGray);
        for (Face face : faces) {
            face.drawOutline(frameProcessed, new Scalar(0, 255, 0, 255), 3);
            Mat faceImage = face.extractRoi(frameGray);
            String label = faceClassifier.recognizeFace(faceImage);
            face.drawLabel(frameProcessed, label, new Scalar(0, 255, 0, 255));
        }
    }
}
