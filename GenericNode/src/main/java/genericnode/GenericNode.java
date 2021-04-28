/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genericnode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author wlloyd
 */
public class GenericNode {

    /**
     * @param args the command line arguments
     */
    static boolean stopped = false;
    static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            if (args[0].equals("rmis")) {
                System.out.println("RMI SERVER");
                try {
                    // insert code to start RMI Server
                    RMIServer obj = new RMIServer();
                    RemoteKV stub = (RemoteKV) UnicastRemoteObject.exportObject(obj, 0);

                    // Bind the remote object's stub in the registry
                    Registry registry = LocateRegistry.getRegistry();
                    obj.setRegistry(registry);
                    registry.bind("server", stub);

                    System.err.println("Server ready");
                } catch (Exception e) {
                    System.out.println("Error initializing RMI server.");
                    e.printStackTrace();
                }
            }
            if (args[0].equals("rmic")) {
                System.out.println("RMI CLIENT");
                String addr = args[1];
                String cmd = args[2];
                String key = (args.length > 3) ? args[3] : "";
                String val = (args.length > 4) ? args[4] : "";
                // insert code to make RMI client request
                try {
                    Registry registry = LocateRegistry.getRegistry(addr);
                    RemoteKV stub = (RemoteKV) registry.lookup("server");
                    String response = "";
                    switch (cmd) {
                        case "put":
                            response = stub.put(key, val);
                            break;
                        case "get":
                            response = stub.get(key);
                            break;
                        case "del":
                            response = stub.delete(key);
                            break;
                        case "store":
                            response = stub.store();
                            break;
                        case "exit":
                            response = stub.exit();
                            break;
                        default:
                            break;

                    }
                    System.out.println("server response: " + response);
                } catch (Exception e) {
                    System.err.println("Client exception: " + e.toString());
                    e.printStackTrace();
                }
            }
            if (args[0].equals("tc")) {
                //READ INPUT
                System.out.println("TCP CLIENT");
                String addr = args[1];
                int port = Integer.parseInt(args[2]);
                String cmd = args[3];
                String key = (args.length > 4) ? args[4] : "";
                String val = (args.length > 5) ? args[5] : "";
                Input input = new Input(cmd,key,val);
                try(Socket socket = new Socket(addr, port)) {                    
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println(input.toString());
                    out.flush();

                    String serverResponse;
                    while (true) {
                        serverResponse = br.readLine();
                        if (serverResponse == null) {
                            break;
                        }
                        System.out.println(serverResponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (args[0].equals("ts")) {

                int port = Integer.parseInt(args[1]);

                try ( ServerSocket serverSocket = new ServerSocket(port)) {
                    System.out.println("Connected To server");

                    //Create Key val store object
                    KeyValStore kv = new KeyValStore();

                    while (!threadPool.isShutdown()) {
                        Socket clientSocket;
                        try {
                            clientSocket = serverSocket.accept();
                            threadPool.execute(new WorkerTCP(clientSocket, serverSocket, kv, threadPool));
                        } catch (IOException e) {
                            if (threadPool.isShutdown()) {
                                System.out.println("Server Stopped.");
                                break;
                            }
                            throw new RuntimeException(
                                    "Error accepting client connection", e);
                        }
                    }
                    threadPool.awaitTermination(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[0].equals("uc")) {
                System.out.println("UDP CLIENT");
                String addr = args[1];
                int sendport = Integer.parseInt(args[2]);
                String cmd = args[3];
                String key = (args.length > 4) ? args[4] : "";
                String val = (args.length > 5) ? args[5] : "";
                Input input = new Input(cmd, key, val);
                try {
                    DatagramSocket ds = new DatagramSocket();

                    byte[] byteRequest = input.toString().getBytes();
                    InetAddress ia = InetAddress.getByName(addr);
                    DatagramPacket dpRequest = new DatagramPacket(byteRequest, byteRequest.length, ia, sendport);
                    ds.send(dpRequest);

                    byte[] byteResponse = new byte[65535];
                    DatagramPacket dpResponse = new DatagramPacket(byteResponse, byteResponse.length);
                    ds.receive(dpResponse);
                    String serverResponse = new String(dpResponse.getData(), dpResponse.getOffset(), dpResponse.getLength());
                    System.out.println(serverResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (args[0].equals("us")) {
                System.out.println("UDP SERVER");
                int port = Integer.parseInt(args[1]);
                System.out.println("From Server:" + port);
                // insert code to start UDP server on port
                //int newport = port + 1;

                KeyValStore kv = new KeyValStore();
                try {
                    DatagramSocket ds = new DatagramSocket(port);

                    while (!threadPool.isShutdown()) {
                        try {
                            byte[] b1 = new byte[1024];
                            DatagramPacket dp = new DatagramPacket(b1, b1.length);
                            ds.receive(dp);
                            threadPool.execute(new WorkerUDP(ds, dp, kv, threadPool));
                        } catch (Exception e) {
                            if (threadPool.isShutdown()) {
                                System.out.println("Server Stopped.");
                                break;
                            }
                            throw new RuntimeException("Error accepting client connection", e);
                        }
                    }
                    threadPool.awaitTermination(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            String msg = "GenericNode Usage:\n\n"
                    + "Client:\n"
                    + "uc/tc <address> <port> put <key> <msg>  UDP/TCP CLIENT: Put an object into store\n"
                    + "uc/tc <address> <port> get <key>  UDP/TCP CLIENT: Get an object from store by key\n"
                    + "uc/tc <address> <port> del <key>  UDP/TCP CLIENT: Delete an object from store by key\n"
                    + "uc/tc <address> <port> store  UDP/TCP CLIENT: Display object store\n"
                    + "uc/tc <address> <port> exit  UDP/TCP CLIENT: Shutdown server\n"
                    + "rmic <address> put <key> <msg>  RMI CLIENT: Put an object into store\n"
                    + "rmic <address> get <key>  RMI CLIENT: Get an object from store by key\n"
                    + "rmic <address> del <key>  RMI CLIENT: Delete an object from store by key\n"
                    + "rmic <address> store  RMI CLIENT: Display object store\n"
                    + "rmic <address> exit  RMI CLIENT: Shutdown server\n\n"
                    + "Server:\n"
                    + "us/ts <port>  UDP/TCP SERVER: run udp or tcp server on <port>.\n"
                    + "rmis  run RMI Server.\n";
            System.out.println(msg);
        }

    }

}
