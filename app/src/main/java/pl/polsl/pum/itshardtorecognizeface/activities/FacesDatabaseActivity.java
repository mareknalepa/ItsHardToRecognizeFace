package pl.polsl.pum.itshardtorecognizeface.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.adapters.FacesDatabaseAdapter;
import pl.polsl.pum.itshardtorecognizeface.classifier.FaceClassifier;
import pl.polsl.pum.itshardtorecognizeface.fragments.OpenCVFragment;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabase;

public class FacesDatabaseActivity extends AppCompatActivity implements OpenCVFragment.OnFragmentInteractionListener {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faces_database);
        listView = (ListView) findViewById(R.id.facesDatabaseListView);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(new OpenCVFragment(), "OpenCVFragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_faces_database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.menu_faces_database_trainer:
                intent = new Intent(this, TrainerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.menu_faces_database_status:
                showStatus();
                break;
            case R.id.menu_faces_database_clear:
                clearDatabase();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void showStatus() {
        FacesDatabase fd = FacesDatabase.getInstance();
        FaceClassifier fc = FaceClassifier.getInstance();
        String text = "Database contains " + fd.getLabelsNumber() + " different persons with " +
                fd.getImagesNumber() + " images altogether.\n";
        if (fc.classifierTrained()) {
            text += "Classifier trained with " + fc.classesNumber() + " different classes.";
        } else {
            text += "Classifier not trained.";
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void clearDatabase() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    FacesDatabase.getInstance().clear();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to permanently delete all faces stored in database?");
        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No", dialogClickListener);
        builder.show();
    }

    @Override
    public void onOpenCVLoaded() {
        FacesDatabaseAdapter facesDatabaseAdapter = new FacesDatabaseAdapter(this);
        listView.setAdapter(facesDatabaseAdapter);
    }
}
