import java.net.MalformedURLException;
import java.rmi.*;

import java.util.Vector;
public interface MergeSort extends Remote {
	//Remote Methods implemented by  server and these are available for other servers to call the methods
	//all the servers  have the same methods in remote interface because all the servers perform same operations of parallel merge sort
int[] getArray() throws RemoteException;//returns the sub list stored in the server
void setArray(int[] arr) throws RemoteException;//stores the sub list in the server after sorting and merging at each step

int[] getRecievedArray() throws RemoteException;//returns the sorted sub list to other server when this server sends the data to other server for exchange
void setRecievedArray(int[] arr) throws RemoteException;// sets the sorted sub list to send the data to other servers and increases the exchange status
void sortArray() throws RemoteException; //performs Merge sort of the given array
int[] mergeSortedArrays(int[] A,int[] B,boolean isHigh) throws RemoteException;//performs merge on two equal length arrays A and B
boolean isSorted() throws RemoteException;//returns true if the sorting is done and false if the sorting is not done
int getStatus() throws RemoteException;//returns the status of number of exchanges with other servers
int getMergeStatus() throws RemoteException;//returns the status of number of merges with the sub lists received from other servers
}