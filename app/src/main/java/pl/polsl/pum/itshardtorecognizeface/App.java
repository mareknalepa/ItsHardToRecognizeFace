package pl.polsl.pum.itshardtorecognizeface;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static boolean welcomeSaid = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
