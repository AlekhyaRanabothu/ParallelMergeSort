
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;

import java.util.Vector;
public class MergeSortServer implements MergeSort {
	//methods of remote interface of a server are implemented here
	private int[] machineArray;//sub list containing n/p elements in a server where p is number of processes/servers and n is the number of elements 
								//in the input array/list
	private int[] recievedArray;// sub list received from other server when exchanged data
	private int status=0;// incremented  when the server sends data to other server for exchanging 
	private int mergeStatus=0;//incremented if the server has completed the merging of the sub list it already has and the sub list received from other server 
								// when data is exchanged between two servers
	private boolean isSorted=false;// to check if the server has done the internal sorting

	MergeSortServer(int arr[]){//constructor of the class 
		machineArray=arr;		//sets the sub array data in the server when the coordinating process splits the input and sends the sub list 
	}
	public int[] getArray(){//returns the sub list stored in the server
		return machineArray;
	}
	public int[] getRecievedArray(){//returns the sorted sub list to other server when this server sends the data to other server for exchange  
		return recievedArray;
	}
	public int getStatus(){//returns the status of number of exchanges with other servers
		return status;
	}
	public int getMergeStatus(){//returns the status of number of merges with the sub lists received from other servers
		return mergeStatus;
	}
	public void setRecievedArray(int[] arr){// sets the sorted sub list to send the data to other servers and increases the exchange status
		recievedArray=arr;
		status++;
	}
	public void setArray(int[] arr){//stores the sub list in the server after sorting and merging at each step
		machineArray=arr;
		
	}

	public void sortArray(){//perform merge sort on the sub list stored in the server
		Sort(machineArray);
		
		isSorted=true;
	}
	public boolean isSorted(){//returns true if the sorting is done and false if the sorting is not done
		return isSorted;
	}
	
	
	void merge(int arr[], int l, int m, int r) 
	{ 
		// the sizes of 2 sub arrays are as below
		//Value in variable "first" is the size of first sub array
		//Value in variable "second" is the size of second sub array
		int first = m - l + 1; 
		int second = r - m; 

		//creating 2 arrays with above 2 sizes
		int L[] = new int [first]; 
		int R[] = new int [second]; 

		//copying elements to from original array to sub arrays created above
		for (int i=0; i<first; ++i) 
			L[i] = arr[l + i]; 
		for (int j=0; j<second; ++j) 
			R[j] = arr[m + 1+ j]; 
		//Merging the arrays
		// Initial indexes of first and second subarrays 
		int i = 0, j = 0; 

		// Initial index of merged subarry array 
		int k = l; 
		while (i < first && j < second) 
		{ 
			if (L[i] <= R[j]) 
			{ 
				arr[k] = L[i]; 
				i++; 
			} 
			else
			{ 
				arr[k] = R[j]; 
				j++; 
			} 
			k++; 
		} 

		/* Copy remaining elements of L[] if any */
		while (i < first) 
		{ 
			arr[k] = L[i]; 
			i++; 
			k++; 
		} 

		/* Copy remaining elements of R[] if any */
		while (j < second) 
		{ 
			arr[k] = R[j]; 
			j++; 
			k++; 
		} 
	} 

	// Main function that sorts arr[l..r] using 
	// merge() 
	void msort(int arr[], int l, int r) 
	{ 
		if (l < r) 
		{ 
			// Find the middle point 
			int m = (l+r)/2; 

			// Sort first and second halves 
			msort(arr, l, m); 
			msort(arr , m+1, r); 

			// Merge the sorted halves 
			merge(arr, l, m, r); 
		} 
	} 

    public int[] Sort(int[] arr) {//Performs merge sort on a given array of elements
    	
    	msort(arr, 0, arr.length-1);
        return arr;
    }
    public int[] mergeSortedArrays(int[] A,int[] B,boolean isHigh){//performs merge on two equal length arrays A and B, if 'isHigh' is true the data is sent from high numbered
    							//server(A) to low numbered server(B).If 'isHigh' is false then data is from low numbered server(A) to high numbered server(B) 
    	//Always high numbered server will store the maximum n/p elements and low numbered server will 
    							// store minimum n/p elements,discarding the remaining(n=number of elements in the input unsorted list and p=number of processes)
    	//increments the merge status which indicates the number of merges the sever has performed with the sub arrays of other servers
    	machineArray=new int[A.length];
    	int count=0;
    	if(isHigh){
    		int k = 0; 
    		int i=A.length-1;
    		int j=A.length-1;
    		while (k!=A.length) 
    		{ 
    			if (A[i] >= B[j]) 
    			{ 
    				machineArray[A.length-1-k] = A[i]; 
    				i--; 
    			} 
    			else
    			{ 
    				machineArray[A.length-1-k] = B[j]; 
    				j--; 
    			} 
    			k++; 
    		} 

    	}
    	else{
    		int k = 0; 
    		int i=0;
    		int j=0;
    		while (k!=A.length) 
    		{ 
    			if (A[i] <= B[j]) 
    			{ 
    				machineArray[k] = A[i]; 
    				i++; 
    			} 
    			else
    			{ 
    				machineArray[k] = B[j]; 
    				j++; 
    			} 
    			k++; 
    		} 
    	}
    	mergeStatus++;
		    	return machineArray;
    }
    //This method is a local method to coordinating process and cannot be accessed by other servers
   //The input unsorted array is split in to  parts equal to the number of processes(here p =4) and each sub array is sent to a server by 
    //RMI invocations to each server in parallel( by using 4 threads.Each thread performs one RMI call to each server initially). 
   public void splitAndStart(final int arr[],final int n) throws MalformedURLException, RemoteException, NotBoundException{
	   
	   System.out.println("Parallel merge sort using 4 servers"+"\n");
    	final int k=arr.length;
    
    	//split the input array to 4 eual parts
	    int[] subarr1=new int[k/4];
	    int[] subarr2=new int[k/4];
	    int[] subarr3=new int[k/4];
	    int[] subarr4=new int[(k/4)];
	    System.arraycopy(arr,0,subarr1,0,subarr1.length);
	    System.arraycopy(arr,k/4,subarr2,0,subarr2.length);
	    System.arraycopy(arr,(k/4)+(k/4),subarr3,0,subarr3.length);
	    System.arraycopy(arr,(k/4)+(k/4)+(k/4),subarr4,0,subarr4.length);
	    //each server creates an instance of the class which implements the methods of the remote interface
        MergeSort obj1 = new MergeSortServer(subarr1);
        MergeSort serverStub1=null;
        MergeSort obj2 = new MergeSortServer(subarr2);
        MergeSort serverStub2 =null;

        
        MergeSort obj3 = new MergeSortServer(subarr3);
        MergeSort serverStub3 = null;

        
        MergeSort obj4 = new MergeSortServer(subarr4);
        MergeSort serverStub4 =null;
        

        try {//exportObject is used to make the above created instance available to receive RMI invocations by an anonymous TCP port.
        	//In this implementation,ports are specified for severs and binding is done using createRegistry and rebind methods
			serverStub1 = (MergeSort) UnicastRemoteObject.exportObject(obj1, 0);
			serverStub2 = (MergeSort) UnicastRemoteObject.exportObject(obj2, 0);
			serverStub3 = (MergeSort) UnicastRemoteObject.exportObject(obj3, 0);
			serverStub4 = (MergeSort) UnicastRemoteObject.exportObject(obj4, 0);
		} catch (RemoteException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

              
        try {
        	 try {
        		 //Creates and exports a Registry instance on the local host that accepts requests on the specified port(eg:1901) on which the registry accepts requests
				LocateRegistry.createRegistry(1901);//server1
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {//This method is used by a server to register the identifier of a remote object(eg:serverStub1) by name(URL of server.eg.'rmi://localhost:1901/mergeSort')
				Naming.rebind("rmi://localhost:1901/mergeSort", serverStub1);//server1
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        

       	 try {
			LocateRegistry.createRegistry(1902);//server2
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			try {
				Naming.rebind("rmi://localhost:1902/mergeSort", serverStub2);//server2
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        

       	 try {
			LocateRegistry.createRegistry(1903);//server3
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			try {
				Naming.rebind("rmi://localhost:1903/mergeSort", serverStub3);//server3
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        

       	 try {
			LocateRegistry.createRegistry(1904);//server4
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			try {
				Naming.rebind("rmi://localhost:1904/mergeSort", serverStub4);//server4
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
 //client or other servers uses 'Naming.lookup(String serverURL)'to look up the remote object reference of a remote object of a server mentioned by server URL
        /// and these stubs are used to send RMI to a particular remote object
        final MergeSort stub1=(MergeSort) Naming.lookup("rmi://localhost:1901/mergeSort");//for server1
			final MergeSort stub2=(MergeSort) Naming.lookup("rmi://localhost:1902/mergeSort");//for server2
			final MergeSort stub3=(MergeSort) Naming.lookup("rmi://localhost:1903/mergeSort");// for server 3
			final MergeSort stub4=(MergeSort) Naming.lookup("rmi://localhost:1904/mergeSort");// for server4
		    	

		    final long startTime=System.currentTimeMillis();
	    Thread t1=new Thread(new Runnable(){
        	

	 			@Override
	 			public void run() {

	 				try {

						
						stub1.sortArray();//server1 sorts the sub array sent by the coordinating process

						System.out.println("Initial sorted sub array in server1:"+Arrays.toString(stub1.getArray())+"\n");
						
						while(!stub2.isSorted()){//waits till server 2 sub array is sorted 
							
						}
						stub2.setRecievedArray(stub1.getArray());//server1 sends the data to server2
						while(stub1.getStatus()!=1){// server 1 waits till it receives data from server2
													//in server 1,status = 1 implies server 2 has sent the data to server 1,
							//as the first exchange of data for server 1 is with server2
							
						}
						stub1.mergeSortedArrays(stub1.getArray(), stub1.getRecievedArray(), false);//server 1 merges the sub list of server 1 and sub list received from server 2
						// and stores minimum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 1 after exchange with server 2 and merging:"+Arrays.toString(stub1.getArray())+"\n");
						
						while(stub3.getMergeStatus()!=1){//waits till the merging is done between sub lists of server 3 and server4
							//in server 3,MergeStatus=1 implies merge is done between server3 and server4 sub lists
						}
						stub3.setRecievedArray(stub1.getArray());//server1 sends data to server3
						while(stub1.getStatus()!=2){// server 1 waits till it receives data from server3
							//In server 1,status = 2 implies server 3 has sent the data to server 1 ,as the second exchange of data for server 1 is with server3
							
						}
						stub1.mergeSortedArrays(stub1.getArray(), stub1.getRecievedArray(), false);//server 1 merges the sub list of server 1 and sub list received from server 3
						// and stores minimum n/4 elements,discarding the remaining
						System.out.println("final sorted sub array in server 1 after exchange with server 3 and merging:"+Arrays.toString(stub1.getArray())+"\n");
						
						
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 			}
	         	
	         });
	    Thread t2=new Thread(new Runnable(){
        	

	 			@Override
	 			public void run() {

	 				try {
											
						stub2.sortArray();//server2 sorts the sub array sent by the coordinating process
						System.out.println("Initial sorted sub array in server2:"+Arrays.toString(stub2.getArray())+"\n");
						
						
						while(!stub1.isSorted()){
							//waits till server 1 sub array is sorted
						}
						stub1.setRecievedArray(stub2.getArray());//server2 sends the data to server1
						while(stub2.getStatus()!=1){// server 2 waits till it receives data from server1
							//in server 2,status = 1 implies server 1 has sent the data to server 2,
							//as the first exchange of data for server 2 is with server1
							
						}
						stub2.mergeSortedArrays(stub2.getArray(), stub2.getRecievedArray(), true);//server 2 merges the sub list of server 2 and sub list received from server 1
						// and stores maximum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 2 after exchange with server 1 and merging:"+Arrays.toString(stub2.getArray())+"\n");
						
						
						while(stub4.getMergeStatus()!=1){//waits till the merging is done between sub lists of server 3 and server4
							//in server 4,MergeStatus=1 implies merge is done between server3 and server4 sub lists
							
						}
						stub4.setRecievedArray(stub2.getArray());//server2 sends data to server4
						while(stub2.getStatus()!=2){// server 2 waits till it receives data from server4
							//In server 2,status = 2 implies server 4 has sent the data to server 2 ,as the second exchange of data for server 2 is with server4
							
						}
						stub2.mergeSortedArrays(stub2.getArray(), stub2.getRecievedArray(), false);//server 2 merges the sub list of server 2 and sub list received from server 4
																									// and stores minimum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 2 after exchange with server 4 and merging:"+Arrays.toString(stub2.getArray())+"\n");
						
						while(stub3.getMergeStatus()!=2){//waits till the merging is done between sub lists of server 1 and server3
							//in server 3,MergeStatus=2 implies merge is done between server1 and server3 sub lists as the second exchange for server3 is with server1
							
						}
						stub3.setRecievedArray(stub2.getArray());//server2 sends data to server3
						while(stub2.getStatus()!=3){// server 2 waits till it receives data from server3
							//In server 2,status = 3 implies server 3 has sent the data to server 2 ,as the third exchange of data for server 2 is with server3
							
						}
						stub2.mergeSortedArrays(stub2.getArray(), stub2.getRecievedArray(), false);//server 2 merges the sub list of server 2 and sub list received from server 3
						// and stores minimum n/4 elements,discarding the remaining
						System.out.println("final sorted sub array in server 2 after exchange with server 3 and merging:"+Arrays.toString(stub2.getArray())+"\n");
						
						while(stub1.getMergeStatus()!=2 || stub2.getMergeStatus()!=3 || stub3.getMergeStatus()!=3 ||stub4.getMergeStatus()!=2){
							
						} //coordinating process waits till all the exchanges and parallel merge sort is completed in all servers  
// and concatenate all the sorted sub lists from all the 4 servers to a final single sorted list

						
				    	System.arraycopy(stub1.getArray(),k-n,arr,0,(k/4)-(k-n));   	
				    	System.arraycopy(stub2.getArray(),0,arr,(k/4)-(k-n),k/4);
				    	System.arraycopy(stub3.getArray(),0,arr,(k/2)-(k-n),k/4);
				    	System.arraycopy(stub4.getArray(),0,arr,(3*k/4)-(k-n),k/4);


				    	final long endTime=System.currentTimeMillis();

				    	System.out.println("Time for merge sort using 4 processes: "+(endTime-startTime)+"\n");
				    	System.out.println("Final Sorted Array : "+Arrays.toString(arr)+"\n");


						
					}  catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
	 			}
	 			
	 			
	         	
	         });
	    Thread t3=new Thread(new Runnable(){
        	

	 			@Override
	 			public void run() {
 				

	 				try {
											
						stub3.sortArray();//server3 sorts the sub array sent by the coordinating process
						System.out.println("Initial sorted sub array in server3:"+Arrays.toString(stub3.getArray())+"\n");

						
						
						while(!stub4.isSorted()){//waits till server 4 sub array is sorted
							
						}
						stub4.setRecievedArray(stub3.getArray());//server3 sends the data to server4
						while(stub3.getStatus()!=1){// server 3 waits till it receives data from server4
							//in server 3,status = 1 implies server 4 has sent the data to server 3,
	//as the first exchange of data for server 3 is with server4
							
						}
						stub3.mergeSortedArrays(stub3.getArray(), stub3.getRecievedArray(), false);//server 3 merges the sub list of server 3 and sub list received from server 4
						// and stores minimum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 3 after exchange with server 4 and merging:"+Arrays.toString(stub3.getArray())+"\n");
						
						
						while(stub1.getMergeStatus()!=1){//waits till the merging is done between sub lists of server 1 and server2
							//in server 1,MergeStatus=1 implies merge is done between server1 and server2 sub lists
							
						}
						stub1.setRecievedArray(stub3.getArray());//server3 sends data to server1
						while(stub3.getStatus()!=2){// server 3 waits till it receives data from server1
							//In server 3,status = 2 implies server 1 has sent the data to server 3 ,as the second exchange of data for server 3 is with server1
							
						}
						stub3.mergeSortedArrays(stub3.getArray(), stub3.getRecievedArray(), true);//server 3 merges the sub list of server 3 and sub list received from server 1
						// and stores maximum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 3 after exchange with server 1 and merging:"+Arrays.toString(stub3.getArray())+"\n");
						
						while(stub2.getMergeStatus()!=2){//waits till the merging is done between sub lists of server 2 and server4
							//in server 2,MergeStatus=2 implies merge is done between server2 and server4 sub lists as the second exchange of server 2 is with server4
							
						}
						stub2.setRecievedArray(stub3.getArray());//server3 sends data to server2
						while(stub3.getStatus()!=3){// server 3 waits till it receives data from server2
							//In server 3,status = 3 implies server 2 has sent the data to server 3 ,as the third exchange of data for server 3 is with server2
							
							
						}
						stub3.mergeSortedArrays(stub3.getArray(), stub3.getRecievedArray(), true);//server 3 merges the sub list of server 3 and sub list received from server 2
						// and stores maximum n/4 elements,discarding the remaining
						System.out.println("final sorted sub array in server 3 after exchange with server 2 and merging:"+Arrays.toString(stub3.getArray())+"\n");
						
						
						
						
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 				}
	         	

	         	
	         });
	    Thread t4=new Thread(new Runnable(){
        	

	 			@Override
	 			public void run() {
	 				try {
								
						stub4.sortArray();//server4 sorts the sub array sent by the coordinating process
						System.out.println("Initial sorted sub array in server4:"+Arrays.toString(stub4.getArray())+"\n");
						while(!stub3.isSorted()){//waits till server 3 sub array is sorted
							
						}
						stub3.setRecievedArray(stub4.getArray());//server4 sends the data to server3
						while(stub4.getStatus()!=1){// server 4 waits till it receives data from server3
							//in server 4,status = 1 implies server 3 has sent the data to server 4,
	//as the first exchange of data for server 4 is with server3
							
						}
						stub4.mergeSortedArrays(stub4.getArray(), stub4.getRecievedArray(), true);//server 4 merges the sub list of server 4 and sub list received from server 3
						// and stores maximum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 4 after exchange with server 3 and merging:"+Arrays.toString(stub4.getArray())+"\n");
						
						while(stub2.getMergeStatus()!=1){//waits till the merging is done between sub lists of server 1 and server2
							//in server 2,MergeStatus=1 implies merge is done between server1 and server2 sub lists
							
						}
						stub2.setRecievedArray(stub4.getArray());//server4 sends data to server2
						while(stub4.getStatus()!=2){// server 4 waits till it receives data from server2
							//In server 4,status = 2 implies server 2 has sent the data to server 4 ,as the second exchange of data for server 4 is with server2
							
						}
						stub4.mergeSortedArrays(stub4.getArray(), stub4.getRecievedArray(), true);//server 4 merges the sub list of server 4 and sub list received from server 2
						// and stores maximum n/4 elements,discarding the remaining
						System.out.println("final sorted sub array in server 4 after exchange with server 2 and merging:"+Arrays.toString(stub4.getArray())+"\n");
						
						
					}  catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 				}
	         	

	         	
	         });
	    
	
	    t1.start();
	    t2.start();
	    t3.start();
	    t4.start();
	      
}
 //This method is a local method to coordinating process and cannot be accessed by other servers 
   //The input unsorted array is split in to  parts equal to the number of processes(here p =2) and each sub array is sent to a server by 
    //RMI invocations to each server in parallel( by using 2 threads.Each thread performs one RMI call to each server initially). 
   public void splitAndStart_2Process(final int arr[],final int n) throws MalformedURLException, RemoteException, NotBoundException{
	
	   
   	final int k=arr.length;
   
  //split the input array to 2 equal parts
	    int[] subarr1=new int[k/2];
	    int[] subarr2=new int[k/2];

	    System.arraycopy(arr,0,subarr1,0,subarr1.length);
	    System.arraycopy(arr,k/2,subarr2,0,subarr2.length);
	  //each server creates an instance of the class which implements the methods of the remote interface
       MergeSort obj5 = new MergeSortServer(subarr1);
       MergeSort serverStub5=null;
       MergeSort obj6 = new MergeSortServer(subarr2);
       MergeSort serverStub6 =null;


       

       try {//exportObject is used to make the above created instance available to receive RMI invocations by an anonymous TCP port.
       	//In this implementation,ports are specified for severs and binding is done using createRegistry and rebind methods
			serverStub5 = (MergeSort) UnicastRemoteObject.exportObject(obj5, 0);
			serverStub6 = (MergeSort) UnicastRemoteObject.exportObject(obj6, 0);

		} catch (RemoteException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

            
       	 try {//Creates and exports a Registry instance on the local host that accepts requests on the specified port(eg:1905) on which the registry accepts requests
				LocateRegistry.createRegistry(1905);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {//This method is used by a server to register the identifier of a remote object(eg:serverStub5) by name(URL of server.eg.'rmi://localhost:1905/mergeSort')
				Naming.rebind("rmi://localhost:1905/mergeSort", serverStub5);//server5
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        

      	 try {
			LocateRegistry.createRegistry(1906);//server6
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			try {
				Naming.rebind("rmi://localhost:1906/mergeSort", serverStub6);//server6
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        

 
      //client or other servers uses 'Naming.lookup(String serverURL)'to look up the remote object reference of a remote object of a server mentioned by server URL
	        // and these stubs are used to send RMI to a particular remote object
       final MergeSort stub5=(MergeSort) Naming.lookup("rmi://localhost:1905/mergeSort");
			final MergeSort stub6=(MergeSort) Naming.lookup("rmi://localhost:1906/mergeSort");

		    	
			System.out.println("Parallel merge sort using 2 servers\n");
		    final long startTime=System.currentTimeMillis();
	    Thread t5=new Thread(new Runnable(){
       	

	 			@Override
	 			public void run() {

	 				try {

						
						stub5.sortArray();//server5 sorts the sub array sent by the coordinating process

						System.out.println("Initial sorted sub array in server5:"+Arrays.toString(stub5.getArray())+"\n");
						
						while(!stub6.isSorted()){//waits till server 6 sub array is sorted
							
						}
						stub6.setRecievedArray(stub5.getArray());//server5 sends the data to server6
						while(stub5.getStatus()!=1){// server 5 waits till it receives data from server6
							//in server 5,status = 1 implies server 6 has sent the data to server 5
	
							
						}
						stub5.mergeSortedArrays(stub5.getArray(), stub5.getRecievedArray(), false);//server 5 merges the sub list of server 5 and sub list received from server 6
						// and stores minimum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 5 after exchange with server 6 and merging:"+Arrays.toString(stub5.getArray())+"\n");

	
						
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 			}
	         	
	         });
	    Thread t6=new Thread(new Runnable(){
       	

	 			@Override
	 			public void run() {

	 				try {
											
						stub6.sortArray();//server6 sorts the sub array sent by the coordinating process
						System.out.println("Initial sorted sub array in server6:"+Arrays.toString(stub6.getArray())+"\n");
						
						
						while(!stub5.isSorted()){//waits till server 5 sub array is sorted
							
						}
						stub5.setRecievedArray(stub6.getArray());//server6 sends the data to server5
						while(stub6.getStatus()!=1){// server 6 waits till it receives data from server5
							//in server 6,status = 1 implies server 5 has sent the data to server 6
							
						}
						stub6.mergeSortedArrays(stub6.getArray(), stub6.getRecievedArray(), true);//server 6 merges the sub list of server 6 and sub list received from server 5
						// and stores maximum n/4 elements,discarding the remaining
						System.out.println("sorted sub array in server 6 after exchange with server 5 and merging:"+Arrays.toString(stub6.getArray())+"\n");
						
						

						
						while(stub5.getMergeStatus()!=1 || stub6.getMergeStatus()!=1 ){//coordinating process waits till all the exchanges and parallel merge sort is completed in all servers  
							// and concatenate all the sorted sub lists from the 2 servers to a final single sorted list

							
						}



				    	System.arraycopy(stub5.getArray(),k-n,arr,0,(k/2)-(k-n));
				    	
				    	System.arraycopy(stub6.getArray(),0,arr,(k/2)-(k-n),k/2);

				    	final long endTime=System.currentTimeMillis();

				    	System.out.println("Time for sorting using 2 processes: "+(endTime-startTime)+"\n");
				    	System.out.println("Final Sorted Array using 2 processes: "+Arrays.toString(arr)+"\n");


						
					}  catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
	 			}
	 			
	 			
	         	
	         });

	    
	    t5.start();
	    t6.start();
       }

   
    public static void main(String args[]) throws RemoteException, MalformedURLException, NotBoundException {
    	   Scanner sc=new Scanner(System.in);
           System.out.println("Enter the number of elements : ");//number of elements to be randomly generated in unsorted list
           int n=sc.nextInt();
           Random rand=new Random();//genereate random numbers 
           int k=n;
        // if the number of elements generated is not a multiple of 4,then zeros are appended to the 
	   		//list to make the number a multiple of 4 
	   		//these extra zeros are removed after sorting
           if(n%4!=0){
        	   k=n-(n%4)+4;
           }
   	    int[] arr=new int[k];
   	    int[] arr2=new int[k];
   	    int[] arr3=new int[k];
   	    for(int i=k-n;i<k;i++){
   	        int r=rand.nextInt(100);
   	    	arr[i]=r;
   	    	arr2[i]=r;
   	    	arr3[i]=r;
   	    }
   	    if(k>n){
   	    	for(int i=0;i<k-n;i++){
   	    		arr[i]=0;
   	    		arr2[i]=0;
   	    		arr3[i]=0;
   	    	}
   	    }
   	    
  	    System.out.println("Initial unsorted array :"+Arrays.toString(arr)+"\n");
    	MergeSortServer m=new MergeSortServer(new int[]{});
    	
    
    	long t=System.currentTimeMillis();
     	m.Sort(arr2);//sequential merge sort

    	System.out.println("Final sorted array using sequential merge sort :"+Arrays.toString(arr2)+"\n");
       	long t1=System.currentTimeMillis();

       	System.out.println("Time taken for sequential Merge sort : "+(t1-t)+"\n");
	m.splitAndStart(arr,n);// parallel merge sort using 4 servers
	m.splitAndStart_2Process(arr3, n);// parallel merge sort using 2 servers
		

    	
    }
}
