package edu.ucla.cens.audiosens.processing;

import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processing.AudioData.Flag;

public class CircularQueue {
	private int qMaxSize;// max queue size
	private int fp = 0;  // front pointer
	private int rp = 0;  // rear pointer
	private int qs = 0;  // size of queue
	private AudioData[] q;    // actual queue
	private AudioData[] tempQ;

	int lastLogged=0;
	int icount=0;


	public CircularQueue(int size) 
	{
		qMaxSize = size;
		fp = 0;
		rp = 0;
		qs = 0;
		q = new AudioData[qMaxSize];
	}

	public synchronized void insert(short[] data,long timestamp, Flag flag) 
	{
		synchronized(this)
		{
			//insert case; if the queue is full then we will increase size of the queue by a factor of 2
			if (!fullq()) {
				qs++;
				rp = (rp + 1)%qMaxSize;
				if(q[rp]==null)
				{
					q[rp]= new AudioData(data.clone(), timestamp, flag);
					icount++;
				}
				else
				{
					q[rp].insert(data, timestamp, flag);
				}
				// start the delete thread and start copying because there is an element
				notify(); 
			}
			else
			{
				//since queue full, double Size
				int temp_rp=rp;
				int temp_fp=fp;
				int temp_qMaxSize=qMaxSize*2;
				tempQ = new AudioData[temp_qMaxSize];

				for(int i=0;i<qs;i++)
				{
					temp_fp = (temp_fp + 1)%qMaxSize;
					tempQ[i]= q[temp_fp];
				}
				temp_fp=temp_qMaxSize-1;
				temp_rp=qs-1;

				q=tempQ;
				fp=temp_fp;
				rp=temp_rp;
				qMaxSize=temp_qMaxSize;

				Logger.i("Queue size increase to:"+temp_qMaxSize);

				if (!fullq()) {
					qs++;
					rp = (rp + 1)%qMaxSize;
					q[rp] = new AudioData(data.clone(), timestamp, flag);
					icount++;
					notify(); 
				}

				//Clearing tempQ
				tempQ=null;
			}
		}
	}


	public synchronized AudioData deleteAndHandleData() 
	{
		//means that buffer doesn't yet have appState.writeAfterThisManyValues elements so sleep
		if(emptyq())
		{
			try 
			{
				wait(100); //if nothing in 100 ms. return null
			} 
			catch(InterruptedException e) 
			{
				Logger.e("Enterrupted Exception in deleteAndHandleDat");
			}
		}
		//means there is data now
		return delete();
	}

	private synchronized AudioData delete() 
	{
		synchronized(this)
		{
			if (!emptyq()) 
			{
				//will not decrease size to avoid race condition
				qs--;
				fp = (fp + 1)%qMaxSize;
				return q[fp];
			}
			else 
			{
				return null;
			}
		}
	}

	public boolean emptyq() {
		return qs == 0;
	}

	private boolean fullq() {
		return qs == qMaxSize;
	}

	public int getQSize() {
		return qs;
	}

	public void printq() 
	{
		System.out.print("Size: " + qs +
				", rp: " + rp + ", fp: " + fp + ", q: ");
		for (int i = 0; i < qMaxSize; i++)
			System.out.print("q[" + i + "]=" 
					+ q[i] + "; ");
		System.out.println();
	}
}