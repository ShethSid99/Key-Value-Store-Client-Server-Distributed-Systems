/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genericnode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author srivatsav
 */

       
class KeyValStore {

    final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
    final int MAXSIZE = 65000;

    public String operation(Input input) {
        StringBuilder sb = new StringBuilder("");
        try {

            Command cmd = input.cmd;
            String key = input.key;
            String val = input.val;

            switch (cmd) {
                case put:
                    map.put(key, val);
                    sb.append("server response: put key= " + key);
                    break;
                case get:
                    String v = map.get(key);
                    sb.append("server response: get key= " + key + " Get val: " + v);
                    break;
                case del:
                    map.remove(key);
                    sb.append("server response: delete key= " + key);
                    break;
                case store:
                    int entryCount = 0;
                    sb.append("server response:\n");
                    for (ConcurrentMap.Entry<String, String> entry : map.entrySet()) {
                        if (sb.length() >= MAXSIZE) {
                            sb.insert(0, "TRIMMED:\n");
                            break;
                        }
                        sb.append("key:" + entry.getKey() + ":" + "value:" + entry.getValue());
                        entryCount++;

                        if (entryCount < map.size()) {
                            sb.append("\n");
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();

    }
}
