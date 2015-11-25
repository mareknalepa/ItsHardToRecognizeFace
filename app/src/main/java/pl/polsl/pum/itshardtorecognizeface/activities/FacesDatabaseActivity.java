package pl.polsl.pum.itshardtorecognizeface.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.adapters.FacesDatabaseAdapter;
import pl.polsl.pum.itshardtorecognizeface.classifier.FaceClassifier;
import pl.polsl.pum.itshardtorecognizeface.fragments.OpenCVFragment;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabase;

public class FacesDatabaseActivity extends AppCompatActivity implements
        OpenCVFragment.OnFragmentInteractionListener,
        AdapterView.OnCreateContextMenuListener {

    private FacesDatabase facesDatabase;
    private FacesDatabaseAdapter facesDatabaseAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faces_database);
        facesDatabase = FacesDatabase.getInstance();
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
        FaceClassifier fc = FaceClassifier.getInstance();

        Resources res = getResources();
        int labels = facesDatabase.getLabelsNumber();
        int images = facesDatabase.getImagesNumber();
        String text = res.getQuantityString(R.plurals.text_faces_database_status_classes, labels, labels);
        text += " " + res.getQuantityString(R.plurals.text_faces_database_status_images, images, images);

        if (fc.classifierTrained()) {
            text += String.format(getString(R.string.text_faces_database_status_classifier_trained), fc.classesNumber());
        } else {
            text += getString(R.string.text_faces_database_status_classifier_not_trained);
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void clearDatabase() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    facesDatabase.clear();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.text_faces_database_clear_confirm);
        builder.setPositiveButton(R.string.text_yes, dialogClickListener);
        builder.setNegativeButton(R.string.text_no, dialogClickListener);
        builder.show();
    }

    @Override
    public void onOpenCVLoaded() {
        facesDatabaseAdapter = new FacesDatabaseAdapter(this);
        listView.setAdapter(facesDatabaseAdapter);
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        if (v.getId() == R.id.facesDatabaseListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(facesDatabase.getEntries().get(info.position).getLabel());
            inflater.inflate(R.menu.context_menu_faces_database, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position = info.position;

        if (item.getItemId() == R.id.context_menu_faces_database_remove) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        facesDatabaseAdapter.remove(position);
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.text_faces_database_remove_confirm);
            builder.setPositiveButton(R.string.text_yes, dialogClickListener);
            builder.setNegativeButton(R.string.text_no, dialogClickListener);
            builder.show();
            return true;
        }

        return super.onContextItemSelected(item);
    }
}
