package cn.pax.odd.keyinjection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

/**
 * @author ligq
 * @date 2018/1/17
 */

public abstract class BaseActivity extends AppCompatActivity implements HasAndroidInjector {
    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initListeners();
        initData();
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }

    protected abstract void initData();

    protected abstract void initListeners();

    protected abstract void initView();

    protected abstract int getLayoutId();
}
