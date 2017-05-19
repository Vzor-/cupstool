package com.berezin;

import com.sun.jna.Pointer;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static Pointer http;
    public static void main(String[] args) {
        initCupsStuff();
        while (true) {
            try {
                int item = promptInput();
                if (item == 1) listSubscriptions();
                else if (item == 2) startSubscription();
                else if (item == 3) endSubscription();
                else if (item == 4) getState();
                else if (item == 5) break;
                else System.out.println("Invalid input");
            } catch (Exception e) {
                System.out.println("err");
            }
        }
    }
    static int promptInput(){
        System.out.println("1: List active subscriptions");
        System.out.println("2: Start subscription");
        System.out.println("3: End subscription");
        System.out.println("4: Get state");
        System.out.println("5: Exit");
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
        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Name"),
                "requesting-user-name",
                "",
                System.getProperty("user.name"));
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);
    }
    static ArrayList<String> listPrinters() {
        ArrayList<String> list = new ArrayList<>();
        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("CUPS-Get-Printers"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Name"),
                "requesting-user-name",
                "",
                System.getProperty("user.name"));
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        Pointer attr = Cups.INSTANCE.ippFindAttribute(response, "printer-state-reasons",
                Cups.INSTANCE.ippTagValue("keyword"));

        int counter = 1;
        while (attr != Pointer.NULL) {
            list.add(Cups.INSTANCE.ippGetString(attr, 0, ""));
            attr = Cups.INSTANCE.ippFindNextAttribute(response, "printer-name",
                    Cups.INSTANCE.ippTagValue("Name"));
            System.out.println(counter++ + ": " + Cups.INSTANCE.ippGetString(attr, 0, ""));
            attr = Cups.INSTANCE.ippFindNextAttribute(response, "printer-state-reasons",
                    Cups.INSTANCE.ippTagValue("keyword"));
        }
        return list;
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
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Name"),
                "requesting-user-name",
                "",
                System.getProperty("user.name"));
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
        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Name"),
                "requesting-user-name",
                "",
                System.getProperty("user.name"));
        Cups.INSTANCE.ippAddInteger(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Integer"),
                "notify-subscription-id",
                id);
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);
    }
    static void getState(){
        ArrayList<String> list = listPrinters();
        System.out.println("Enter printer number");
        Scanner s = new Scanner(System.in);
        int id = s.nextInt();
        if ((id > 0)&&(id <= list.size())) {
            System.out.println(list.get(id - 1));
        } else {
            System.out.println("Input out of bounds");
        }
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
