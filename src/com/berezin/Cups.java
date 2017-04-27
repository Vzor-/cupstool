package com.berezin;

import com.sun.jna.*;

/**
 * Created by kyle on 4/27/17.
 */
public interface Cups extends Library {
    Cups INSTANCE = (Cups ) Native.loadLibrary("cups", Cups.class);

    Pointer httpConnectEncrypt(String host, int port, Pointer encryption);
    String cupsServer ();
    int ippPort ();
    Pointer cupsEncryption ();
    Pointer ippNewRequest (int op);
    int httpAssembleURI (int encoding, Memory uri, int urilen, String sceme, String username, String host, int port, String resourcef);
    int ippAddString (Pointer ipp, int group, int tag, String name, String charset, String value);
    int ippAddInteger (Pointer ipp, int group, int tag, String name, int value);
    Pointer cupsDoRequest (Pointer http, Pointer request, String resource);
    Pointer ippFindAttribute (Pointer ipp, String name, int tag);
    String ippGetName(Pointer attr);
    int ippGetCount(Pointer attr);
    String ippGetString(Pointer attr, int element, String language);
    int ippGetInteger(Pointer attr, int element);
    int ippTagValue(String name);
    int ippOpValue(String name);
    String ippOpString(int op);
    Pointer ippFirstAttribute (Pointer ipp);
    Pointer ippNextAttribute (Pointer ipp);
    String ippTagString(int tag);
    int ippGetValueTag (Pointer ipp);
    void ippDelete (Pointer ipp);
    void httpClose (Pointer http);
}
