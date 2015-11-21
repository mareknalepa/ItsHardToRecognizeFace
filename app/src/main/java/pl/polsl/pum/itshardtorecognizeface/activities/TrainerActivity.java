package pl.polsl.pum.itshardtorecognizeface.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.List;

import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.fragments.CameraPreviewFragment;
import pl.polsl.pum.itshardtorecognizeface.model.Face;
import pl.polsl.pum.itshardtorecognizeface.model.FaceDetector;

public class TrainerActivity extends AppCompatActivity implements CameraPreviewFragment.OnFragmentInteractionListener {

    private FaceDetector faceDetector;

    private Mat lastFrameRgba;
    private Mat lastFrameGray;

    private List<Face> faces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trainer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.menu_trainer_faces_database:
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

    public void processPictureClick(View view) {
        if (lastFrameRgba != null && lastFrameGray != null && !faces.isEmpty()) {
            Intent intent = new Intent(this, TrainerEditorActivity.class);

            long frameRgbaAddr = lastFrameRgba.getNativeObjAddr();
            long frameGrayAddr = lastFrameGray.getNativeObjAddr();
            intent.putExtra("frameRgbaAddr", frameRgbaAddr);
            intent.putExtra("frameGrayAddr", frameGrayAddr);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onOpenCVLoaded() {
        faceDetector = new FaceDetector(TrainerActivity.this);
    }

    @Override
    public void onCameraFrameExtra(Mat frameRgba, Mat frameGray, Mat frameProcessed) {
        lastFrameRgba = frameRgba.clone();
        lastFrameGray = frameGray.clone();
        faces = faceDetector.detectFaces(frameGray);
        for (Face face : faces) {
            face.drawOutline(frameProcessed, new Scalar(0, 255, 0, 255), 3);
        }
    }
}
