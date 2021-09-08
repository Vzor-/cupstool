package com.berezin;

import com.sun.jna.*;

/**
 * Created by kyle on 4/27/17.
 */
public interface Cups extends Library {
    Cups INSTANCE = (Cups) Native.loadLibrary("cups", Cups.class);

    Pointer cupsEncryption();
    Pointer httpConnectEncrypt(String host, int port, Pointer encryption);
    Pointer cupsDoRequest(Pointer http, Pointer request, String resource);
    Pointer cupsDoFileRequest(Pointer http, Pointer request, String resource, String filename);
    Pointer ippNewRequest(int op);

    String cupsServer();

    int ippPort();
    int ippTagValue(String name);
    int ippOpValue(String name);
    int ippAddString(Pointer ipp, int group, int tag, String name, String charset, String value);
    int ippAddInteger (Pointer ipp, int group, int tag, String name, int value);
    int ippAddStrings(Pointer ipp, int group, int value_tag, String name, int num_values, String language, StringArray values);


    void ippDelete(Pointer ipp);
    void httpClose(Pointer http);
}
