package cn.pax.odd.keyinjection.printer.daggerInjection;

import cn.pax.odd.keyinjection.printer.ReceiptData;
import dagger.Module;
import dagger.Provides;

@Module
public class ReceiptModule {
    @Provides
    public ReceiptData newReceiptData() {
        return new ReceiptData();
    }
}
