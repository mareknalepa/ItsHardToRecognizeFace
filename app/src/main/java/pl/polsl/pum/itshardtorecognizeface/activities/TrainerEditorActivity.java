package pl.polsl.pum.itshardtorecognizeface.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.adapters.TrainerEditorAdapter;
import pl.polsl.pum.itshardtorecognizeface.fragments.OpenCVFragment;
import pl.polsl.pum.itshardtorecognizeface.model.Face;
import pl.polsl.pum.itshardtorecognizeface.model.FaceDetector;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabase;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabaseEntry;

public class TrainerEditorActivity extends AppCompatActivity implements OpenCVFragment.OnFragmentInteractionListener {

    private Mat frameRgba;
    private Mat frameGray;
    private ListView listView;
    private List<Face> faces;
    private List<String> labels;

    private FaceDetector faceDetector;

    public TrainerEditorActivity() {
        labels = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_editor);
        listView = (ListView) findViewById(R.id.trainerEditorListView);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(new OpenCVFragment(), "OpenCVFragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trainer_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.menu_trainer_editor_faces_database:
                intent = new Intent(this, FacesDatabaseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void addToDatabaseClick(View view) {
        if (!formIsValid()) {
            Toast.makeText(this, R.string.text_trainer_editor_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < labels.size(); ++i) {
            FacesDatabaseEntry fde = new FacesDatabaseEntry(labels.get(i));
            fde.addFrame(faces.get(i).extractRoi(frameRgba));
            FacesDatabase.getInstance().add(fde);
        }
        Intent intent = new Intent(this, FacesDatabaseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private boolean formIsValid() {
        for (String label : labels) {
            if (label.isEmpty() || label.equals("") || !label.matches("[a-zA-Z]+[a-zA-Z ]+")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onOpenCVLoaded() {
        faceDetector = new FaceDetector(TrainerEditorActivity.this);

        Intent intent = getIntent();
        long frameRgbaAddr = intent.getLongExtra("frameRgbaAddr", 0);
        long frameGrayAddr = intent.getLongExtra("frameGrayAddr", 0);
        Mat temp = new Mat(frameRgbaAddr);
        frameRgba = temp.clone();
        temp = new Mat(frameGrayAddr);
        frameGray = temp.clone();

        faces = faceDetector.detectFaces(frameGray);
        for (int i = 0; i < faces.size(); ++i) {
            labels.add("");
        }

        TrainerEditorAdapter trainerEditorAdapter = new TrainerEditorAdapter(this, faces, labels, frameRgba);
        listView.setAdapter(trainerEditorAdapter);
    }
}
