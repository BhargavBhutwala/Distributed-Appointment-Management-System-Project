package FrontEnd;

import app.FrontEndPOA;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class FrontEndObj extends FrontEndPOA {

    private final int sequencerPort;

    public FrontEndObj() {
        this.sequencerPort = 5000;
        ports = new int[]{1000, 2000, 3000};
        fault_Port = 4000;
        logger = new Logger();
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
    private final Logger logger;
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
        if (response[0].equals("")) {
            sendFailure("Server Crash", 2, 1);
        }
        if (response[1].equals("")) {
            sendFailure("Server Crash", 1, 2);
        }
        if (response[2].equals("")) {
            sendFailure("Server Crash", 2, 3);
        }

        if (response[0].equals(response[1])) {
            majorResponse = response[0];
            failures[2]++;
            if (failures[2] == 3) {
                sendFailure("Server Bug", 2, 3);
                failures[2] = 0;
            }
        }
        if (response[0].equals(response[2])) {
            majorResponse = response[0];
            failures[1]++;
            if (failures[1] == 3) {
                sendFailure("Server Bug", 1, 2);
                failures[1] = 0;
            }
        }
        if (response[2].equals(response[1])) {
            majorResponse = response[1];
            failures[0]++;
            if (failures[0] == 3) {
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
                String data = new String(packet.getData(), 0, packet.getLength());
                if (port == ports[0]) {
                    response[0] = data;
                } else if (port == ports[1]) {
                    response[1] = data;
                } else {
                    response[2] = data;
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
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), this.fault_Port);
            socket.send(packet);
        } catch (SocketException | UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
