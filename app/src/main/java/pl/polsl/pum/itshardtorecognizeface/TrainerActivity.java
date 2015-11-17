package pl.polsl.pum.itshardtorecognizeface;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.List;

import pl.polsl.pum.itshardtorecognizeface.classifier.ClassifierDatabase;
import pl.polsl.pum.itshardtorecognizeface.classifier.FaceClassifier;
import pl.polsl.pum.itshardtorecognizeface.model.Face;
import pl.polsl.pum.itshardtorecognizeface.model.FaceDetector;
import pl.polsl.pum.itshardtorecognizeface.model.PictureHolder;

public class TrainerActivity extends AppCompatActivity implements CameraPreviewFragment.OnFragmentInteractionListener {

    private FaceDetector faceDetector;
    private ClassifierDatabase classifierDatabase;

    private MenuItem menuShowClassifierStatus;
    private MenuItem menuResetClassifier;
    private MenuItem menuTrainClassifier;

    private Mat lastFrameRgba;
    private Mat lastFrameGray;

    private List<Face> faces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        classifierDatabase = new ClassifierDatabase(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuShowClassifierStatus = menu.add("Show classifier status");
        menuResetClassifier = menu.add("Reset classifier");
        menuTrainClassifier = menu.add("Train classifier");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == menuShowClassifierStatus) {
            showClassifierStatus();
        } else if (item == menuResetClassifier) {
            resetClassifier();
        } else if (item == menuTrainClassifier) {
            trainClassifier();
        }
        return true;
    }

    public void processPictureClick(View view) {
        if (lastFrameRgba != null && lastFrameGray != null && !faces.isEmpty()) {
            Intent intent = new Intent(this, TrainerDatabaseActivity.class);

            PictureHolder ph = PictureHolder.getInstance();
            ph.setFrameRgba(lastFrameRgba);
            ph.setFrameGray(lastFrameGray);

            startActivity(intent);
        }
    }

    private void showClassifierStatus() {
        classifierDatabase.load();
        int examplesNumber = classifierDatabase.examplesNumber();
        int classesNumber = classifierDatabase.classesNumber();
        String text;
        if (classifierDatabase.isTrained()) {
            text = "Classifier already trained.";
        } else if (examplesNumber == 0) {
            text = "Classifier database is empty.";
        } else {
            text = "Classifier ready to train with " + examplesNumber + " examples representing " + classesNumber + " classes.";
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void resetClassifier() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    classifierDatabase.clear();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to permanently delete all examples stored in database?");
        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No", dialogClickListener);
        builder.show();
    }

    private void trainClassifier() {
        classifierDatabase.load();
        if (classifierDatabase.examplesNumber() < 2) {
            Toast.makeText(this, "Too few examples to train classifier!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (classifierDatabase.classesNumber() < 2) {
            Toast.makeText(this, "At least 2 classes are required to train classifier!", Toast.LENGTH_SHORT).show();
            return;
        }
        FaceClassifier faceClassifier = new FaceClassifier(this);
        faceClassifier.trainClassifier(classifierDatabase);
        faceClassifier.loadClassifier();
    }

    @Override
    public void loaderCallbackExtra() {
        faceDetector = new FaceDetector(TrainerActivity.this);
    }

    @Override
    public void onCameraFrameExtra(Mat frameRgba, Mat frameGray, Mat frameProcessed) {
        lastFrameRgba = frameRgba;
        lastFrameGray = frameGray;
        faces = faceDetector.detectFaces(frameGray);
        for (Face face : faces) {
            face.drawOutline(frameProcessed, new Scalar(0, 255, 0, 255), 3);
        }
    }
}
