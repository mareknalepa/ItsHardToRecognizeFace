package pl.polsl.pum.itshardtorecognizeface.model;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.polsl.pum.itshardtorecognizeface.App;
import pl.polsl.pum.itshardtorecognizeface.adapters.FacesDatabaseAdapter;
import pl.polsl.pum.itshardtorecognizeface.classifier.FaceClassifier;
import pl.polsl.pum.itshardtorecognizeface.utils.MatSerializer;
import pl.polsl.pum.itshardtorecognizeface.utils.MatSerializerException;

public class FacesDatabase {

    private static FacesDatabase instance = null;

    public static FacesDatabase getInstance() {
        if (instance == null) {
            synchronized (FacesDatabase.class) {
                if (instance == null) {
                    instance = new FacesDatabase();
                }
            }
        }
        return instance;
    }

    private Context context;
    private List<FacesDatabaseEntry> entries;
    private FacesDatabaseAdapter adapter;

    public final static String IMAGES_LIST_FILE = "faces_list";

    private FacesDatabase() {
        context = App.getContext();
        entries = new ArrayList<>();
        load();
    }

    public void onDatabaseChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (entries.size() >= 2 && allEntriesHasImages()) {
            FaceClassifier.getInstance().trainClassifier(this);
        }
    }

    public void registerAdapter(FacesDatabaseAdapter fda) {
        adapter = fda;
    }

    public List<FacesDatabaseEntry> getEntries() {
        return entries;
    }

    public void clear() {
        String[] files = context.fileList();
        for (String path : files) {
            context.deleteFile(path);
        }
        entries.clear();
        FaceClassifier.getInstance().clearClassifier();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public int getLabelsNumber() {
        return entries.size();
    }

    public int getImagesNumber() {
        int number = 0;
        for (FacesDatabaseEntry fde : entries) {
            number += fde.getFrames().size();
        }
        return number;
    }

    public boolean allEntriesHasImages() {
        for (FacesDatabaseEntry fde : entries) {
            if (fde.getFrames().size() == 0) {
                return false;
            }
        }
        return true;
    }

    private void load() {
        entries.clear();

        FileInputStream fis;
        try {
            fis = context.openFileInput(IMAGES_LIST_FILE);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            while (line != null) {
                String[] parts = line.split("[;]");
                boolean insert = false;
                FacesDatabaseEntry fde = findEntry(parts[0]);
                if (fde == null) {
                    fde = new FacesDatabaseEntry(parts[0]);
                    insert = true;
                }
                try {
                    fde.addFrame(MatSerializer.matFromFile(parts[1], context));
                    if (insert) {
                        entries.add(fde);
                    }
                } catch (MatSerializerException e) {
                }
                line = br.readLine();
            }
            br.close();
            fis.close();
        } catch (java.io.IOException e) {
        }
    }

    public void add(FacesDatabaseEntry entry) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(IMAGES_LIST_FILE, Context.MODE_APPEND);
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date date = new Date();
            String path = df.format(date);
            while (pathExists(path)) {
                path += '_';
            }
            try {
                MatSerializer.matToFile(entry.getFrames().get(0), path, context);
            } catch (MatSerializerException e) {
            }

            String contents = entry.getLabel() + ";" + path + System.getProperty("line.separator");
            fos.write(contents.getBytes());
            fos.close();
        } catch (java.io.IOException e) {
        }
        FacesDatabaseEntry fed = findEntry(entry.getLabel());
        if (fed != null) {
            fed.addFrame(entry.getFrames().get(0));
        } else {
            entries.add(entry);
        }
        onDatabaseChanged();
    }

    public void delete(FacesDatabaseEntry entry) {
        entries.remove(entry);
        onDatabaseChanged();
    }

    public FacesDatabaseEntry findEntry(String label) {
        for (FacesDatabaseEntry fde : entries) {
            if (fde.getLabel().equals(label)) {
                return fde;
            }
        }
        return null;
    }

    private boolean pathExists(String path) {
        try {
            FileInputStream fis = context.openFileInput(path);
            fis.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
