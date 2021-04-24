package me.bakumon.ugank;

import android.app.Application;

import com.github.anzewei.parallaxbacklayout.ParallaxHelper;
import com.squareup.leakcanary.LeakCanary;

import org.litepal.LitePal;

/**
 * App
 *
 * @author bakumon https://bakumon.me
 * @date 2016/12/8 17:18
 */
public class App extends Application {
    private static App INSTANCE;

    public static App getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        initThemeManage();
        initConfigManage();
        initLeakCanary();
        initLitePal();
        registerParallaxBack();
    }

    /**
     * 滑动返回
     */
    private void registerParallaxBack() {
        registerActivityLifecycleCallbacks(ParallaxHelper.getInstance());
    }

    /**
     * 初始化主题色
     */
    private void initThemeManage() {
        ThemeManage.INSTANCE.initColorPrimary(getResources().getColor(R.color.colorPrimary));
    }

    /**
     * 初始化配置管理器
     */
    private void initConfigManage() {
        ConfigManage.INSTANCE.initConfig(this);
    }

    /**
     * 初始化 LeakCanary
     */
    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    /**
     * 初始化 LitePal
     */
    private void initLitePal() {
        LitePal.initialize(this);
    }
}
