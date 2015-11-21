package pl.polsl.pum.itshardtorecognizeface.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import pl.polsl.pum.itshardtorecognizeface.R;


public class CameraPreviewFragment extends OpenCVFragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    private OnFragmentInteractionListener mListener;

    private CameraBridgeViewBase ocvCameraView;

    public static CameraPreviewFragment newInstance() {
        CameraPreviewFragment fragment = new CameraPreviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        ocvCameraView = (CameraBridgeViewBase) view.findViewById(R.id.cameraPreview);
        ocvCameraView.setCvCameraViewListener(this);
        return view;
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
        if (ocvCameraView != null) {
            ocvCameraView.disableView();
        }
        mListener = null;
    }

    @Override
    protected void onOpenCVLoadedExtra() {
        ocvCameraView.enableView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ocvCameraView != null) {
            ocvCameraView.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (ocvCameraView != null) {
            ocvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frameRgba = inputFrame.rgba();
        Core.flip(frameRgba, frameRgba, -1);

        Mat frameGray = inputFrame.gray();
        Core.flip(frameGray, frameGray, -1);

        Mat frameProcessed = frameRgba.clone();

        mListener.onCameraFrameExtra(frameRgba, frameGray, frameProcessed);

        frameRgba.release();
        frameGray.release();

        return frameProcessed;
    }

    public interface OnFragmentInteractionListener extends OpenCVFragment.OnFragmentInteractionListener {
        void onCameraFrameExtra(Mat frameRgba, Mat frameGray, Mat frameProcessed);
    }
}
