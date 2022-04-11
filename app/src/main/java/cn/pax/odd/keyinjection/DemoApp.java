package cn.pax.odd.keyinjection;

import javax.inject.Inject;

import cn.pax.odd.keyinjection.daggerInjection.DaggerDemoAppComponent;
import cn.pax.odd.keyinjection.daggerInjection.DemoAppComponent;
import cn.pax.odd.keyinjection.sdk.InjectApp;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

/**
 * @author ligq
 * @date 2018/1/31
 */

public class DemoApp extends InjectApp implements HasAndroidInjector {
    @Inject
    DispatchingAndroidInjector<Object> dispatchingAndroidInjector;
    private DemoAppComponent mDemoAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mDemoAppComponent = DaggerDemoAppComponent.builder().build();
        mDemoAppComponent.inject(this);
        saveLogs();
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }
}
