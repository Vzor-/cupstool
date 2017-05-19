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
    Pointer ippNewRequest(int op);
    Pointer ippGetString(Pointer attr, int element, Pointer dataLen);
    Pointer ippFirstAttribute(Pointer ipp);
    Pointer ippNextAttribute(Pointer ipp);
    Pointer ippFindAttribute(Pointer ipp, String name, int type);
    Pointer ippFindNextAttribute(Pointer ipp, String name, int type);

    String cupsServer();
    String ippTagString(int tag);
    String ippGetName(Pointer attr);
    String ippGetString(Pointer attr, int element, String language);

    int ippPort();
    int httpAssembleURI(int encoding, Memory uri, int urilen, String sceme, String username, String host, int port, String resourcef);
    int ippTagValue(String name);
    int ippEnumValue(String attrname, String enumstring);
    int ippOpValue(String name);
    int ippAddString(Pointer ipp, int group, int tag, String name, String charset, String value);
    int ippAddInteger (Pointer ipp, int group, int tag, String name, int value);
    int ippGetCount(Pointer attr);
    int ippGetValueTag(Pointer ipp);
    int ippGetInteger(Pointer attr, int element);

    void ippDelete(Pointer ipp);
    void httpClose(Pointer http);
}
