package genericnode;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteKV extends Remote {
    String put(String key, String value) throws RemoteException;

    String get(String key) throws RemoteException;

    String delete(String key) throws RemoteException;

    String store() throws RemoteException;

    String exit() throws RemoteException;

}