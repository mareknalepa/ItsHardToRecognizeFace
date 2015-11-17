package pl.polsl.pum.itshardtorecognizeface;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;


public class CameraPreviewFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Context context;
    private OnFragmentInteractionListener mListener;

    private CameraBridgeViewBase ocvCameraView;

    private Mat frameRgba;
    private Mat frameGray;
    private Mat frameProcessed;

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
    public void onResume() {
        super.onResume();
        BaseLoaderCallback loaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        mListener.loaderCallbackExtra();
                        ocvCameraView.enableView();
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
        frameRgba = new Mat();
        frameGray = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        frameRgba.release();
        frameGray.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frameRgba = inputFrame.rgba();
        Core.flip(frameRgba, frameRgba, -1);

        frameGray = inputFrame.gray();
        Core.flip(frameGray, frameGray, -1);

        frameProcessed = frameRgba.clone();

        mListener.onCameraFrameExtra(frameRgba, frameGray, frameProcessed);

        return frameProcessed;
    }

    public interface OnFragmentInteractionListener {
        void loaderCallbackExtra();
        void onCameraFrameExtra(Mat frameRgba, Mat frameGray, Mat frameProcessed);
    }
}
