package pl.polsl.pum.itshardtorecognizeface.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabase;
import pl.polsl.pum.itshardtorecognizeface.model.FacesDatabaseEntry;

public class FacesDatabaseAdapter extends BaseAdapter {

    private Context context;
    private FacesDatabase facesDatabase;

    public FacesDatabaseAdapter(Context context) {
        this.context = context;
        facesDatabase = FacesDatabase.getInstance();
        facesDatabase.registerAdapter(this);
    }

    @Override
    public int getCount() {
        return facesDatabase.getLabelsNumber();
    }

    @Override
    public Object getItem(int position) {
        return facesDatabase.getEntries().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_faces_database, parent, false);

            TextView label = (TextView) convertView.findViewById(R.id.labelView);
            GridLayout gridLayout = (GridLayout) convertView.findViewById(R.id.facesDatabaseGridLayout);

            FacesDatabaseEntry entry = (FacesDatabaseEntry) getItem(position);
            label.setText(entry.getLabel());

            for (Bitmap bitmap : entry.getImages()) {
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setImageBitmap(bitmap);
                gridLayout.addView(imageView);
            }
        }
        return convertView;
    }
}
