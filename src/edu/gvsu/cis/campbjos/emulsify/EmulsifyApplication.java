package edu.gvsu.cis.campbjos.emulsify;

import android.app.Application;
import android.content.Context;

/**
 * @author Emulsify Team
 * @version Winter 2014
 *          Some code provided by Imgur, LLC
 */
public class EmulsifyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        EmulsifyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return EmulsifyApplication.context;
    }

}
