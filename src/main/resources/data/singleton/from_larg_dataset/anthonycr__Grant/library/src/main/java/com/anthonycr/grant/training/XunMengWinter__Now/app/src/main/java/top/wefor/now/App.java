package top.wefor.now;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import top.wefor.now.utils.Toaster;

public class App extends Application {

    private static App sApp;

    private static Toaster sToaster;

    public static App getInstance() {
        return sApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        Fresco.initialize(getApplicationContext());
        Realm.init(getApplicationContext());
        if (BuildConfig.DEBUG)
            Logger.addLogAdapter(new AndroidLogAdapter());
    }

    public static void showToast(String msg) {
        if (sToaster == null)
            sToaster = new Toaster();
        sToaster.showToast(msg);
    }

    public static void showToast(int resId) {
        showToast(App.getInstance().getString(resId));
    }

}
