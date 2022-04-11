package cn.pax.odd.keyinjection.daggerInjection;


import cn.pax.odd.keyinjection.MainActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeYourAndroidInjector();
}
