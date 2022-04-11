package cn.pax.odd.keyinjection.printer.daggerInjection;



import cn.pax.odd.keyinjection.printer.PrinterUtil;
import dagger.Module;
import dagger.Provides;

@Module
public class PrinterUtilModule {
    @Provides
    public PrinterUtil newPrinterUtil() {
        return new PrinterUtil();
    }
}
