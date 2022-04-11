package cn.pax.odd.keyinjection.sdk;

public interface InjectionProcess {
    /**
     * @param keyType   key type
     * @param srcIndex  source index
     * @param destIndex destination index
     */
    void inject(String keyType, byte srcIndex, byte destIndex);
}
