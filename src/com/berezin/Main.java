package com.berezin;

import com.sun.jna.Pointer;

import java.util.Scanner;

public class Main {

    static Pointer http;
    public static void main(String[] args) {
        initCupsStuff();
        while (true) {
            int item = promptInput();
            if (item == 1) listSubscriptions();
            else if (item == 2) startSubscription();
            else if (item == 3) endSubscription();
            else if (item == 4) break;
            else System.out.println("Invalid input");
        }
    }
    static int promptInput(){
        System.out.println("1: List active subscriptions");
        System.out.println("2: Start subscription");;
        System.out.println("3: End subscription");
        System.out.println("4: Exit");
        Scanner s = new Scanner(System.in);
        return s.nextInt();
    }
    static void initCupsStuff() {
        http = Cups.INSTANCE.httpConnectEncrypt(
                Cups.INSTANCE.cupsServer(),
                Cups.INSTANCE.ippPort(),
                Cups.INSTANCE.cupsEncryption());
    }
    static void listSubscriptions() {
        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Get-Subscriptions"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("uri"),
                "printer-uri",
                "",
                "ipp://localhost:631/printers/");
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);
    }
    static void startSubscription() {
        System.out.println("Enter subscription uri e.g. rss://localhost:8000");
        Scanner s = new Scanner(System.in);
        String uri = s.next();
        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Create-Printer-Subscription"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("uri"),
                "printer-uri",
                "",
                "ipp://localhost:631/");
        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Subscription"),
                Cups.INSTANCE.ippTagValue("uri"),
                "notify-recipient-uri",
                "",
                uri);
        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Subscription"),
                Cups.INSTANCE.ippTagValue("Keyword"),
                "notify-events",
                "",
                "printer-state-changed");
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);
    }
    static void endSubscription() {
        System.out.println("Enter subscription id, or -1 to cancel");
        Scanner s = new Scanner(System.in);
        int id = s.nextInt();
        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Cancel-Subscription"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("uri"),
                "printer-uri",
                "",
                "ipp://localhost:631/");
        Cups.INSTANCE.ippAddInteger(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Integer"),
                "notify-subscription-id",
                id);
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);
    }
    static void parseResponse(Pointer response) {
        Pointer attr = Cups.INSTANCE.ippFirstAttribute(response);
        while (true) {
            if (attr == Pointer.NULL) {
                break;
            }
            int valueTag = Cups.INSTANCE.ippGetValueTag(attr);
            String data = Cups.INSTANCE.ippTagString(valueTag);
            String attrName = Cups.INSTANCE.ippGetName(attr);
            if (valueTag == Cups.INSTANCE.ippTagValue("Integer")) {
                data = "" + Cups.INSTANCE.ippGetInteger(attr, 0);
            } else {
                data = Cups.INSTANCE.ippGetString(attr, 0, "");
            }
            if (attrName == null){
                System.out.println("------------------------");
            } else {
                System.out.printf("%s: %s\n", attrName, data);
            }
            attr = Cups.INSTANCE.ippNextAttribute(response);
        }
        System.out.println("------------------------");
    }
}
