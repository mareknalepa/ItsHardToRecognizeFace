package pl.polsl.pum.itshardtorecognizeface.classifier;

import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.polsl.pum.itshardtorecognizeface.App;
import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabase;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabaseEntry;

public class FaceClassifier {

    private static FaceClassifier instance = null;

    public static FaceClassifier getInstance() {
        if (instance == null) {
            synchronized (FaceClassifier.class) {
                if (instance == null) {
                    instance = new FaceClassifier();
                }
            }
        }
        return instance;
    }

    public final String MODEL_FILE = "face_classifier_model";
    public final String LABELS_FILE = "face_classifier_labels";

    private Context context;
    private Map<Integer, String> labelsMap;
    private String modelPath;
    private boolean classifierLoaded = false;

    private FaceClassifier() {
        System.loadLibrary("face_classifier_native");
        context = App.getContext();
        labelsMap = new HashMap<>();
        modelPath = context.getFilesDir() + File.separator + MODEL_FILE;
        init();
        loadClassifier();
    }

    private void loadClassifier() {
        if (fileExists(MODEL_FILE) && fileExists(LABELS_FILE)) {
            readLabelsFile();
            load(modelPath);
            classifierLoaded = true;
        }
    }

    public boolean classifierTrained() {
        return fileExists(MODEL_FILE) && fileExists(LABELS_FILE);
    }

    public int classesNumber() {
        return labelsMap.size();
    }

    public void trainClassifier(FacesDatabase facesDatabase) {
        labelsMap.clear();
        List<Mat> images = new ArrayList<>();
        List<Integer> numericLabels = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int numericLabel = 0;
        for (FacesDatabaseEntry fde : facesDatabase.getEntries()) {
            labelsMap.put(numericLabel, fde.getLabel());
            for (Mat frame : fde.getFrames()) {
                Mat grayFrame = frame.clone();
                Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                images.add(grayFrame);
                numericLabels.add(numericLabel);
            }
            ++numericLabel;
        }

        long[] imagesAddrArray = new long[images.size()];
        for (int i = 0; i < images.size(); ++i) {
            imagesAddrArray[i] = images.get(i).getNativeObjAddr();
        }

        int[] numericLabelsArray = new int[numericLabels.size()];
        for (int i = 0; i < numericLabels.size(); ++i) {
            numericLabelsArray[i] = numericLabels.get(i);
        }

        train(imagesAddrArray, numericLabelsArray, modelPath);
        writeLabelsFile();
        classifierLoaded = true;
    }

    public void clearClassifier() {
        if (fileExists(MODEL_FILE)) {
            context.deleteFile(MODEL_FILE);
        }
        if (fileExists(LABELS_FILE)) {
            context.deleteFile(LABELS_FILE);
        }
    }

    public String recognizeFace(Mat faceFrame) {
        if (classifierLoaded) {
            int predicted = predict(faceFrame.getNativeObjAddr());
            if (labelsMap.containsKey(predicted)) {
                return labelsMap.get(predicted);
            }
        }
        return context.getResources().getString(R.string.label_unknown);
    }

    private void writeLabelsFile() {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(LABELS_FILE, Context.MODE_PRIVATE);
            for (Map.Entry<Integer, String> entry : labelsMap.entrySet()) {
                String line = entry.getKey() + ";" + entry.getValue() + System.getProperty("line.separator");
                fos.write(line.getBytes());
            }
        } catch (java.io.IOException e) {
        } finally {
            try {
                fos.close();
            } catch (java.io.IOException e) {
            }
        }
    }

    private void readLabelsFile() {
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            fis = context.openFileInput(LABELS_FILE);
            br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            while (line != null) {
                String[] parts = line.split("[;]");
                int intLabel;
                try {
                    intLabel = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    intLabel = -1;
                }
                labelsMap.put(intLabel, parts[1]);
                line = br.readLine();
            }
        } catch (java.io.IOException e) {
        } finally {
            try {
                br.close();
                fis.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean fileExists(String path) {
        File dir = context.getFilesDir();
        File file = new File(dir, path);
        return file.exists();
    }

    private static native void init();

    private static native void load(String path);

    private static native void train(long[] images, int[] labels, String path);

    private static native int predict(long image);
}
