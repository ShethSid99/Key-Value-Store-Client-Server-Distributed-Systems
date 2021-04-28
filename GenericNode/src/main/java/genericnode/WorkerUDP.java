/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genericnode;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author srivatsav
 */
class WorkerUDP implements Runnable {

    private DatagramPacket dp;
    private DatagramSocket ds;
    private KeyValStore kv;
    private ExecutorService threadPool;

    public WorkerUDP(DatagramSocket ds, DatagramPacket dp, KeyValStore kv, ExecutorService threadPool) {
        this.dp = dp;
        this.ds = ds;
        this.kv = kv;
        this.threadPool = threadPool;
    }

    public void run() {
        BufferedReader br = null;
        PrintWriter out = null;
        String inputString = null;

        try {
            inputString = new String(dp.getData(), dp.getOffset(), dp.getLength());
            System.out.println("Client sent " + inputString);
            Input input = new Input(inputString);

            if (input.cmd == Command.exit) {
                threadPool.shutdown();
                String output = "shutdown";
                byte[] byteRequest = output.getBytes();
                DatagramPacket dpRequest = new DatagramPacket(byteRequest, byteRequest.length, dp.getAddress(), dp.getPort());
                ds.send(dpRequest);
                ds.close();
                return;
            }
            //If not exit, do the operation
            String output = kv.operation(input);
            byte[] byteResponse = output.getBytes();
            DatagramPacket dpResponse = new DatagramPacket(byteResponse, byteResponse.length, dp.getAddress(), dp.getPort());
            ds.send(dpResponse);
            
        } catch (Exception ex) {
            Logger.getLogger(WorkerUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
