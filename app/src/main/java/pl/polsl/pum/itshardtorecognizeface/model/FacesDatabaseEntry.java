package pl.polsl.pum.itshardtorecognizeface.model;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class FacesDatabaseEntry {

    private String label;

    private List<Mat> frames;

    private List<Bitmap> images;

    public FacesDatabaseEntry() {
        label = "";
        frames = new ArrayList<>();
        images = new ArrayList<>();
    }

    public FacesDatabaseEntry(String label) {
        this.label = label;
        frames = new ArrayList<>();
        images = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Mat> getFrames() {
        return frames;
    }

    public List<Bitmap> getImages() {
        return images;
    }

    public void addFrame(Mat frame) {
        Bitmap bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bitmap);
        frames.add(frame);
        images.add(bitmap);
    }
}
