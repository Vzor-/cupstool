package com.berezin;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;

import javax.print.PrintException;
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
                else if (item == 4) getPrinterState();
                else if (item == 5) listJobs();
                else if (item == 6) getJobInfo();
                else if (item == 7) getDetailedPrinterInfo();
                else if (item == 8) breakStuff();
                else if (item == 9) break;
                else System.out.println("Invalid input");
            } catch (Exception e) {
                System.out.println("err");
            }
        }
        closeCupStuff();
    }

    static int promptInput(){
        System.out.println("1: List active subscriptions");
        System.out.println("2: Start subscription");
        System.out.println("3: End subscription");
        System.out.println("4: Get printer state");
        System.out.println("5: List jobs");
        System.out.println("6: Get detailed job info");
        System.out.println("7: Get detailed printer info");
        System.out.println("8: Break stuff");
        System.out.println("9: Exit");
        Scanner s = new Scanner(System.in);
        return s.nextInt();
    }

    static void initCupsStuff() {
        http = Cups.INSTANCE.httpConnectEncrypt(
                Cups.INSTANCE.cupsServer(),
                Cups.INSTANCE.ippPort(),
                Cups.INSTANCE.cupsEncryption());
    }

    static void closeCupStuff() {
        Cups.INSTANCE.httpClose(http);
    }

    static void listSubscriptions() {
        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Get-Subscriptions"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("uri"),
                "printer-uri",
                "",
                "ipp://localhost:" + Cups.INSTANCE.ippPort() + "/printers/");
        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Name"),
                "requesting-user-name",
                "",
                System.getProperty("user.name"));
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);

        Cups.INSTANCE.ippDelete(response);
    }

    static void startSubscription() {
        System.out.println("Enter subscription uri, or press enter for rss://localhost:8000");
        Scanner s = new Scanner(System.in);
        String uri = s.nextLine();
        if (uri == "") uri = "rss://localhost:8000";

        System.out.println("Enter subscription type: \n" +
                "1: Printer status\n" +
                "2: Job status\n" +
                "3: Both");
        s = new Scanner(System.in);
        int item = s.nextInt();

        String[] stringArray;
        if (item == 1) {
            stringArray = new String[]{"printer-state-changed"};
        } else if (item == 2) {
            stringArray = new String[]{"job-state-changed"};
        } else if (item == 3) {
            stringArray = new String[]{"printer-state-changed", "job-state-changed"};
        } else {
            System.out.println("Invalid input");
            return;
        }
        StringArray elements = new StringArray(stringArray);

        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Create-Job-Subscription"));

//      The above line is a helper function that does all of this for us
//        Pointer request = Cups.INSTANCE.ippNew();
//        Cups.INSTANCE.ippSetOperation(request, Cups.INSTANCE.ippOpValue("Create-Job-Subscription"));
//        Cups.INSTANCE.ippSetRequestId(request, SomeRandomNumber);
//        Cups.INSTANCE.ippAddString(request,
//                Cups.INSTANCE.ippTagValue("Operation"),
//                Cups.INSTANCE.ippTagValue("charset"),
//                "attributes-charset",
//                "",
//                "utf-8"
//        );
//        Cups.INSTANCE.ippAddString(request,
//                Cups.INSTANCE.ippTagValue("Operation"),
//                Cups.INSTANCE.ippTagValue("naturalLanguage"),
//                "attributes-natural-language",
//                "",
//                "en-us"
//        );

        // We listen to all printers.
        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("uri"),
                "printer-uri",
                "",
                "ipp://localhost:" + Cups.INSTANCE.ippPort() + "/");
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
        Cups.INSTANCE.ippAddStrings(request,
                Cups.INSTANCE.ippTagValue("Subscription"),
                Cups.INSTANCE.ippTagValue("Keyword"),
                "notify-events",
                stringArray.length,
                "",
                elements);
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);

        Cups.INSTANCE.ippDelete(response);
    }

    static void endSubscription() {
        System.out.println("Enter subscription id, or enter to cancel");
        Scanner s = new Scanner(System.in);
        int id;
        try {
            id = Integer.parseInt(s.nextLine());
        } catch (NumberFormatException e) {
            return;
        }

        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Cancel-Subscription"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("uri"),
                "printer-uri",
                "",
                "ipp://localhost:" + Cups.INSTANCE.ippPort() + "/");
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

        Cups.INSTANCE.ippDelete(response);
    }

    static void getPrinterState(){
        ArrayList<String> list = getPrinterNameList();
        displayList(list);
        System.out.println("Enter printer number");
        Scanner s = new Scanner(System.in);
        int id = s.nextInt();
        if ((id > 0)&&(id <= list.size())) {
            Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("CUPS-Get-Printers"));

            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Name"),
                    "requesting-user-name",
                    "",
                    System.getProperty("user.name"));
            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Name"),
                    "first-printer-name",
                    "",
                    list.get(id - 1));
            Cups.INSTANCE.ippAddInteger(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Integer"),
                    "limit",
                    1);
            Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");

            Pointer attr = Cups.INSTANCE.ippFindAttribute(response, "printer-state",
                    Cups.INSTANCE.ippTagValue("enum"));
            String message = "Status Enum: ";
            message += Cups.INSTANCE.ippEnumString("printer-state", Cups.INSTANCE.ippGetInteger(attr, 0));

            message += "\nStatus Message: ";
            attr = Cups.INSTANCE.ippFindNextAttribute(response, "printer-state-message",
                    Cups.INSTANCE.ippTagValue("TextWithoutLanguage"));
            message += parseAttr(attr);

            message += "\nStatus Reason: ";
            attr = Cups.INSTANCE.ippFindNextAttribute(response, "printer-state-reasons",
                    Cups.INSTANCE.ippTagValue("keyword"));
            message += parseAttr(attr);

            System.out.println(message);

            Cups.INSTANCE.ippDelete(response);
        } else {
            System.out.println("Input out of bounds");
        }
    }

    static void listJobs(){
        ArrayList<String> list = getPrinterNameList();
        displayList(list);
        System.out.println("Enter printer number");
        Scanner s = new Scanner(System.in);
        int id = s.nextInt();
        if ((id > 0)&&(id <= list.size())) {
            Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Get-Jobs"));

            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Name"),
                    "requesting-user-name",
                    "",
                    System.getProperty("user.name")
            );
            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("uri"),
                    "printer-uri",
                    "",
                    "ipp://localhost:" + Cups.INSTANCE.ippPort() + "/printers/" + list.get(id - 1));

            Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
            System.out.println("ipp://localhost:" + Cups.INSTANCE.ippPort() + "/printers/" + list.get(id - 1));
            parseResponse(response);

            Cups.INSTANCE.ippDelete(response);
        } else {
            System.out.println("Input out of bounds");
        }
    }

    static void getJobInfo() {
        System.out.println("Enter job number");
        Scanner s = new Scanner(System.in);
        int id = s.nextInt();

        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Get-Job-Attributes"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Name"),
                "requesting-user-name",
                "",
                System.getProperty("user.name")
        );
        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("uri"),
                "job-uri",
                "",
                "ipp://localhost:" + id + "/jobs/" + id);

        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        parseResponse(response);

        Cups.INSTANCE.ippDelete(response);
    }

    static void getDetailedPrinterInfo() {
        ArrayList<String> list = getPrinterNameList();
        displayList(list);
        System.out.println("Enter printer number");
        Scanner s = new Scanner(System.in);
        int id = s.nextInt();
        if ((id > 0)&&(id <= list.size())) {
            Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("CUPS-Get-Printers"));

            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Name"),
                    "requesting-user-name",
                    "",
                    System.getProperty("user.name"));
            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Name"),
                    "first-printer-name",
                    "",
                    list.get(id - 1));
            Cups.INSTANCE.ippAddInteger(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Integer"),
                    "limit",
                    1);
            Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
            parseResponse(response);

            Cups.INSTANCE.ippDelete(response);
        } else {
            System.out.println("Input out of bounds");
        }
    }

    static void breakStuff() {
        Pointer fileResponse = null;
        try {
            Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("Print-Job"));
            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("uri"),
                    "printer-uri",
                    "",
                    "ipp://localhost:631/printers/Zebra");
            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("Name"),
                    "requesting-user-name",
                    "",
                    System.getProperty("user.name"));
            Cups.INSTANCE.ippAddString(request,
                    Cups.INSTANCE.ippTagValue("Operation"),
                    Cups.INSTANCE.ippTagValue("mimetype"),
                    "document-format",
                    null,
                    "application/vnd.cups-raw");
            // request is automatically closed
            Pointer response = Cups.INSTANCE.cupsDoFileRequest(http, request, "/ipp/print", "/Users/kyleb/tray-kyle/assets/zpl_sample.txt");

            parseResponse(response);
        }
        catch (Throwable t) {
            System.out.println("error breaking stuff");
            System.out.println(t);
        }
    }

    static void displayList(ArrayList<String> list) {
        int counter = 0;
        for (String s : list) System.out.println("" + ++counter + ": " + s);
    }

    static ArrayList<String> getPrinterNameList() {
        ArrayList<String> list = new ArrayList<>();
        Pointer request = Cups.INSTANCE.ippNewRequest(Cups.INSTANCE.ippOpValue("CUPS-Get-Printers"));

        Cups.INSTANCE.ippAddString(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Name"),
                "requesting-user-name",
                "",
                System.getProperty("user.name"));
        Cups.INSTANCE.ippAddStrings(request,
                Cups.INSTANCE.ippTagValue("Operation"),
                Cups.INSTANCE.ippTagValue("Keyword"),
                "requested-attributes",
                1,
                "",
                new StringArray(new String[]{"printer-name"}));
        Pointer response = Cups.INSTANCE.cupsDoRequest(http, request, "/");
        Pointer attr = Cups.INSTANCE.ippFindAttribute(response, "printer-name",
                Cups.INSTANCE.ippTagValue("Name"));

        while (attr != Pointer.NULL) {
            list.add(Cups.INSTANCE.ippGetString(attr, 0, ""));
            attr = Cups.INSTANCE.ippFindNextAttribute(response, "printer-name",
                    Cups.INSTANCE.ippTagValue("Name"));
        }

        Cups.INSTANCE.ippDelete(response);
        return list;
    }

    static void parseResponse(Pointer response) {
        Pointer attr = Cups.INSTANCE.ippFirstAttribute(response);
        while (true) {
            if (attr == Pointer.NULL) {
                break;
            }
            System.out.println(parseAttr(attr));
            attr = Cups.INSTANCE.ippNextAttribute(response);
        }
        System.out.println("------------------------");
    }

    static String parseAttr(Pointer attr){
        int valueTag = Cups.INSTANCE.ippGetValueTag(attr);
        int attrCount = Cups.INSTANCE.ippGetCount(attr);
        String data = "";
        String attrName = Cups.INSTANCE.ippGetName(attr);
        for (int i = 0; i < attrCount; i++) {
            if (valueTag == Cups.INSTANCE.ippTagValue("Integer")) {
                data += Cups.INSTANCE.ippGetInteger(attr, i);
            } else if (valueTag == Cups.INSTANCE.ippTagValue("Boolean")) {
                data += (Cups.INSTANCE.ippGetInteger(attr, i) == 1);
            } else if (valueTag == Cups.INSTANCE.ippTagValue("Enum")) {
                data += Cups.INSTANCE.ippEnumString(attrName, Cups.INSTANCE.ippGetInteger(attr, i));
            } else {
                data += Cups.INSTANCE.ippGetString(attr, i, "");
            }
            if (i + 1 < attrCount) {
                data += ", ";
            }
        }

        if (attrName == null){
            return "------------------------";
        }
        return String.format("%s: %d %s {%s}", attrName, attrCount, Cups.INSTANCE.ippTagString(valueTag), data);
    }
}