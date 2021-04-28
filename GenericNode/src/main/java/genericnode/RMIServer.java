package genericnode;

import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMI Server class. Object is to be bound to RMI Registry for remote access.
 */
public class RMIServer implements RemoteKV {
    static KeyValStore map = new KeyValStore();

    static Registry myRegistry = null;

    /**
     * Put key and value to map, and returns server response string indicating command.
     * @param key Key string
     * @param value Value string
     * @return Returns string confirming command
     */
    public String put(String key, String value) {
        return map.operation(new Input("put", key, value));
    }

    /**
     * Get value of map by key. Return indicates key and value, and indicates error if no such key.
     * @param key Key string
     * @return Returns value in map linked to the key, echoing the input command and returning error
     */
    public String get(String key) {
        return map.operation(new Input("get", key, ""));
    }

    /**
     * Deletes key value pair from map.
     * @param key Key string
     * @return Returns server response echoing command
     */
    public String delete(String key)  {
        return map.operation(new Input("del", key, ""));
    }

    /**
     * Returns string with full contents of map up to 65k characters.
     * @return Returns server response holding full contents of map (pairs), truncated to 65k characters from map
     */
    public String store() {
        return map.operation(new Input("store", "", ""));
    }

    /**
     * Exits server and returns string indicating such.
     * @return Returns exit message
     */
    public String exit() {
        try {
            UnicastRemoteObject.unexportObject(this, true);
            return "Closing client...";
        } catch (Exception e) {
            e.printStackTrace();
            StackTraceElement[] stk = e.getStackTrace();
            String errorOut = "Error exiting";
            for (StackTraceElement err : stk) {
                errorOut += err.toString() + ";";
            }
            return errorOut;
        }
    }

    public void setRegistry(Registry theRegistry) {
        myRegistry = theRegistry;
    }





}