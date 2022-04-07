package FrontEnd;

import Constants.Constants;
import app.FrontEndPOA;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FrontEndObj extends FrontEndPOA {

    private final int sequencerPort;

    public FrontEndObj() {
        this.sequencerPort = Constants.SEQUENCER_PORT;
        ports = new int[]{Constants.RM1_FRONTEND_PORT, Constants.RM2_FRONTEND_PORT, Constants.RM3_FRONTEND_PORT};
        fault_Port = Constants.FAULT_PORT;
        setLogger("logs/FrontEnd.txt", "FrontEnd");
        response = new String[]{"", "", ""};
        failures = new int[]{0, 0, 0};
        new Thread(() -> {
            rmResponse(ports[0]);
        }).start();
        new Thread(() -> {
            rmResponse(ports[1]);
        }).start();
        new Thread(() -> {
            rmResponse(ports[2]);
        }).start();
    }

    private final int[] ports;
    static Logger logger;
    private final int fault_Port;
    private String[] response;
    private int[] failures;

    @Override
    public String sendRequestToSequencer(String request) {
        // logger.log("here");
        String majorResponse = "";
        try {

            byte[] buf = request.getBytes();
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), this.sequencerPort);
            socket.send(packet);
            logger.info("waiting for response...");
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Getting Major Response
            majorResponse = udpRely();

        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return majorResponse;

    }

    private String udpRely() {
        String majorResponse = "";

        if (response[0].equals(response[1])) {
            if (response[0].equals(response[2])) {
                return response[0];
            }
        }
//        if (response[0].equals("")) {
//            sendFailure("Server Crash", 2, 1);
//        }
//        if (response[1].equals("")) {
//            sendFailure("Server Crash", 1, 2);
//        }
//        if (response[2].equals("")) {
//            sendFailure("Server Crash", 2, 3);
//        }

        if (response[0].equals(response[1])) {
            majorResponse = response[0];
            failures[2]++;
            if (failures[2] == 3) {
                logger.info("FRONTEND : RM2 sending to RM3");
                sendFailure("Server Bug", 2, 3);
                failures[2] = 0;
            }
        }
        if (response[0].equals(response[2])) {
            majorResponse = response[0];
            failures[1]++;
            if (failures[1] == 3) {
                logger.info("FRONTEND : RM1 sending to RM2");
                sendFailure("Server Bug", 1, 2);
                failures[1] = 0;
            }
        }
        if (response[2].equals(response[1])) {
            majorResponse = response[1];
            failures[0]++;
            if (failures[0] == 3) {
                logger.info("FRONTEND : RM2 sending to RM1");
                sendFailure("Server Bug", 2, 1);
                failures[0] = 0;
            }
        }

        return majorResponse;
    }

    private void rmResponse(int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
            byte[] buf = new byte[1000];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String data1 = new String(packet.getData(), 0, packet.getLength());
                String data = unpackJSON(data1);
                if (port == ports[0]) {
                    response[0] = data;
                    logger.info("RM 1 : " + response[0]);
                } else if (port == ports[1]) {
                    response[1] = data;
                    logger.info("RM 2 : " + response[1]);
                } else {
                    response[2] = data;
                    logger.info("RM 3 : " + response[2]);
                }
            }
        } catch (Exception ex) {
            socket.close();
            ex.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private void sendFailure(String msg, int handlePort, int port) {
        try {
            String request = msg + ":RM" + Integer.toString(handlePort) + ":RM" + Integer.toString(port);
            byte[] buf = request.getBytes();
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(Constants.MULTICAST_IP), this.fault_Port);
            socket.send(packet);
        } catch (SocketException | UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void setLogger(String location, String id) {
        try {
            logger = Logger.getLogger(id);
            FileHandler fileTxt = new FileHandler(location, true);
            SimpleFormatter formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);
        } catch (Exception err) {
            logger.info("Couldn't Initiate Logger. Please check file permission");
        }
    }
    static String unpackJSON(String jsonString) {
        Object obj = null;
        try {
            obj = new JSONParser().parse(jsonString.trim());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;
        boolean operationFlag = Boolean.parseBoolean(jsonObject.get(Constants.OPERATION_STATUS).toString().trim());
        String id = jsonObject.get(Constants.ID).toString().trim();
        String operation = jsonObject.get(Constants.ID).toString().trim();
        if (operation.equals(Constants.ADD_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            return operationFlag ? operationWiseJSONString(jsonString.trim())
                    : id + " Unable to perform Add Event Operation for  " + eventId;
        } else if (operation.equals(Constants.REMOVE_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            return operationFlag ? operationWiseJSONString(jsonString.trim())
                    : id + " Unable to perform Remove Event Operation for  " + eventId;
        } else if (operation.equals(Constants.LIST_OPERATION)) {
            String eventType = jsonObject.get(Constants.EVENT_TYPE).toString().trim();
            return operationFlag ? operationWiseJSONString(jsonString.trim())
                    : id + " : No Data Found or Might be data issue. " + eventType;
        } else if (operation.equals(Constants.BOOK_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            return operationFlag ? operationWiseJSONString(jsonString.trim())
                    : id + " Unable to book  event " + eventId;
        } else if (operation.equals(Constants.CANCEL_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            return operationFlag ? operationWiseJSONString(jsonString.trim())
                    : id + " Unable to cancel  event " + eventId;

        } else if (operation.equals(Constants.SCHEDULE_OPERATION)) {
            return operationFlag ? operationWiseJSONString(jsonString.trim())
                    : id + " No Data Found or Might be data issue.";
        } else if (operation.equals(Constants.SWAP_OPERATION)) {
            return operationFlag ? operationWiseJSONString(jsonString.trim())
                    : id + " No Data Found or Might be data issue.";
        }
        return operationFlag ? operationWiseJSONString(jsonString.trim())
                : "No Data Found or Might be data issue. Please try again";
    }

    static String operationWiseJSONString(String jsonString) {
        Object obj = null;
        try {
            obj = new JSONParser().parse(jsonString.trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;
        String operation = jsonObject.get(Constants.OPERATION).toString().trim();
        String flag = "";
        String id = jsonObject.get(Constants.ID).toString().trim();
        if (operation.equals(Constants.ADD_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            String eventType = jsonObject.get(Constants.EVENT_TYPE).toString().trim();
            String eventCapacity = jsonObject.get(Constants.EVENT_CAPACITY).toString().trim();
            flag = id + " has create event " + eventId + " of type " + eventType + " with capacity " + eventCapacity;
        } else if (operation.equals(Constants.REMOVE_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            String eventType = jsonObject.get(Constants.EVENT_TYPE).toString().trim();
            flag = id + " has remove event " + eventId + " of type " + eventType;
        } else if (operation.equals(Constants.LIST_OPERATION)) {
            String eventType = jsonObject.get(Constants.EVENT_TYPE).toString().trim();
            String listEvents = jsonObject.get(Constants.LIST_EVENT_AVAILABLE).toString().trim();
            flag = listEvents.trim().isEmpty() ? id + " : No data found for " + eventType
                    : id + "  :  " + eventType + " = " + listEvents;
        } else if (operation.equals(Constants.BOOK_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            String eventType = jsonObject.get(Constants.EVENT_TYPE).toString().trim();
            flag = id + " has book event " + eventId + " of type " + eventType;
        } else if (operation.equals(Constants.CANCEL_OPERATION)) {
            String eventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            String eventType = jsonObject.get(Constants.EVENT_TYPE).toString().trim();
            flag = id + " has cancel event " + eventId + " of type " + eventType;
        } else if (operation.equals(Constants.SCHEDULE_OPERATION)) {
            String listEvents = jsonObject.get(Constants.LIST_EVENT_SCHEDULE).toString().trim();
            flag = listEvents.trim().isEmpty() ? id + " : No data found." : id + "  :  " + listEvents;
        } else if (operation.equals(Constants.SWAP_OPERATION)) {
            String newEventId = jsonObject.get(Constants.EVENT_ID).toString().trim();
            String newEventType = jsonObject.get(Constants.EVENT_TYPE).toString().trim();
            String oldEventId = jsonObject.get(Constants.OLD_EVENT_ID).toString().trim();
            String oldEventType = jsonObject.get(Constants.OLD_EVENT_TYPE).toString().trim();
            flag = id + " has swap event " + oldEventId + " of type " + oldEventType + " with " + newEventId
                    + " of type " + newEventType;
        }
        return flag.trim().equals("") ? id + " : No Data Found or Might be data issue. Please try again" : flag;
    }
}
}
