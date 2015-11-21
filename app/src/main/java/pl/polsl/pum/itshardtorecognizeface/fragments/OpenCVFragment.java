package pl.polsl.pum.itshardtorecognizeface.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class OpenCVFragment extends Fragment {

    protected Context context;
    private OnFragmentInteractionListener mListener;

    public static OpenCVFragment newInstance() {
        OpenCVFragment fragment = new OpenCVFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseLoaderCallback loaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        onOpenCVLoadedExtra();
                        mListener.onOpenCVLoaded();
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, context, loaderCallback);
    }

    protected void onOpenCVLoadedExtra() {

    }

    public interface OnFragmentInteractionListener {
        void onOpenCVLoaded();
    }
}
