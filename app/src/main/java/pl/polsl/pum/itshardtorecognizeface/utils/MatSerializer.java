package pl.polsl.pum.itshardtorecognizeface.utils;

import android.content.Context;
import android.util.Base64;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MatSerializer {

    public static String matToString(Mat m) throws MatSerializerException {
        if (!m.isContinuous()) {
            throw new MatSerializerException("Cannot serialize discontinuous matrices.");
        }
        String serialized = "" + m.type() + ";" + m.rows() + ";" + m.cols() + ";";
        byte[] bytes = new byte[(int) m.total() * (int) m.elemSize()];
        m.get(0, 0, bytes);
        serialized += Base64.encodeToString(bytes, Base64.DEFAULT);
        return serialized;
    }

    public static Mat matFromString(String s) throws MatSerializerException {
        Mat m;
        String[] parts = s.split("[;]");
        try {
            int type = Integer.parseInt(parts[0]);
            int rows = Integer.parseInt(parts[1]);
            int cols = Integer.parseInt(parts[2]);
            byte[] bytes = Base64.decode(parts[3], Base64.DEFAULT);
            if (bytes.length != CvType.ELEM_SIZE(type) * rows * cols) {
                throw new MatSerializerException("Invalid serialized matrix detected.");
            }
            m = new Mat(rows, cols, type);
            m.put(0, 0, bytes);
        } catch (NumberFormatException e) {
            throw new MatSerializerException("Invalid serialized matrix detected.");
        }
        return m;
    }

    public static void matToFile(Mat m, String path, Context context) throws MatSerializerException {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(path, Context.MODE_PRIVATE);
            fos.write(matToString(m).getBytes());
        } catch (java.io.IOException e) {
            throw new MatSerializerException("Cannot write matrix to file.");
        } finally {
            try {
                fos.close();
            } catch (java.io.IOException e) {
            }
        }
    }

    public static Mat matFromFile(String path, Context context) throws MatSerializerException{
        Mat m = null;
        FileInputStream fis = null;
        try {
            File dir = context.getFilesDir();
            File matFile = new File(dir, path);
            fis = context.openFileInput(path);
            byte[] buffer = new byte[(int) matFile.length()];
            fis.read(buffer);
            m = matFromString(new String(buffer));
        } catch (java.io.IOException e) {
            throw new MatSerializerException("Cannot read matrix from file.");
        } finally {
            try {
                fis.close();
            } catch (java.io.IOException e) {
            }
        }
        return m;
    }
}
