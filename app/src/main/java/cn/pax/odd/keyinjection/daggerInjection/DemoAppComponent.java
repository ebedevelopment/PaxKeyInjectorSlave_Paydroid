package cn.pax.odd.keyinjection.daggerInjection;


import cn.pax.odd.keyinjection.DemoApp;
import cn.pax.odd.keyinjection.printer.daggerInjection.PrinterUtilModule;
import cn.pax.odd.keyinjection.printer.daggerInjection.ReceiptModule;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@Component(modules = {AndroidSupportInjectionModule.class,
        ActivityModule.class,
        PrinterUtilModule.class,
        ReceiptModule.class})
public interface DemoAppComponent {
    void inject(DemoApp application);
}
