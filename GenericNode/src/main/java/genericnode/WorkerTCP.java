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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author srivatsav
 */
class WorkerTCP implements Runnable {

    private final Socket clientSocket;
    private final ServerSocket ss;
    private KeyValStore kv;
    private ExecutorService threadPool;

    public WorkerTCP(Socket socket, ServerSocket ss, KeyValStore kv,ExecutorService threadPool) {
        this.clientSocket = socket;
        this.ss = ss;
        this.kv = kv;
        this.threadPool = threadPool;
    }

    public void run() {
        BufferedReader br = null;
        PrintWriter out = null;
        String inputString = null;

        try {
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            inputString = br.readLine();

            out = new PrintWriter(clientSocket.getOutputStream(), true);

            System.out.println("Client sent " + inputString);
            Input input = new Input(inputString);

            if (input.cmd == Command.exit) {
                threadPool.shutdown();
                clientSocket.close();
                ss.close();
                return;
            }
            //If not exit, do the operation
            String output = kv.operation(input);
            out.println(output);
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(WorkerTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


