# ItsHardToRecognizeFace
This application is designed to assist the blind with recognizing face of interlocutor and telling it in voice communication. It is developed as student project at Silesian University of Technology for Mobile Devices Programming subject.

## Technical details
Development requirements:

1.  Android Studio 1.3.x with Gradle 2.5

2.  Android NDK (at least r10e)

3.  OpenCV4Android SDK 2.4.11


To build application it is necessary to adjust some paths in configuration files:

1.  local.properties

        ndk.dir=/path/to/ndk-bundle

2.  app/src/main/jni/Android.mk, line 8:

        include /path/to/opencv-sdk-dir/sdk/native/jni/OpenCV.mk

3.  app/src/main/jni/Android.mk, line 13

        LOCAL_C_INCLUDES += /path/to/opencv-sdk-dir/sdk/native/jni/include

