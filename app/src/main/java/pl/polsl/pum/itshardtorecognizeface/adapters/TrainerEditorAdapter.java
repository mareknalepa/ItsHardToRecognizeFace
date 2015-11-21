package pl.polsl.pum.itshardtorecognizeface.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.List;

import pl.polsl.pum.itshardtorecognizeface.R;
import pl.polsl.pum.itshardtorecognizeface.model.Face;

public class TrainerEditorAdapter extends BaseAdapter {

    private Context context;
    private List<Face> faces;
    private List<String> labels;
    private Mat frame;

    public TrainerEditorAdapter(Context context, List<Face> faces, List<String> labels, Mat frame) {
        this.context = context;
        this.faces = faces;
        this.labels = labels;
        this.frame = frame;
    }

    private final InputFilter[] filters = new InputFilter[] {
            new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if (source.equals("")) {
                        return source;
                    } else if (source.toString().matches("[a-zA-Z ]+")) {
                        return source;
                    }
                    return "";
                }
            }
    };

    @Override
    public int getCount() {
        return faces.size();
    }

    @Override
    public Object getItem(int position) {
        return faces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_trainer_editor, parent, false);

            ImageView iv = (ImageView) convertView.findViewById(R.id.trainerEditorImageView);
            EditText et = (EditText) convertView.findViewById(R.id.trainerEditorEditText);

            Face face = faces.get(position);
            Mat faceRoi = face.extractRoi(frame);
            Bitmap faceBitmap = Bitmap.createBitmap(faceRoi.cols(), faceRoi.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(faceRoi, faceBitmap);
            iv.setImageBitmap(faceBitmap);
            iv.setMinimumWidth(faceBitmap.getWidth());
            iv.setMinimumHeight(faceBitmap.getHeight());

            et.setFilters(filters);
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    labels.set(position, s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }
        return convertView;
    }
}
