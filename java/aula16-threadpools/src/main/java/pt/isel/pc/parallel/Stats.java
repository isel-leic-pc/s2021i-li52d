package pt.isel.pc.parallel;

import pt.isel.pc.utils.WorkerThreadReport;

import java.util.concurrent.atomic.AtomicInteger;

public class Stats {
	private final AtomicInteger workUnits = new AtomicInteger();


	public  Stats() {
		WorkerThreadReport.clear();
		workUnits.set(0);
	}

	public void collect() {
		//int used = WorkerThreadReport.createdThreads();
		workUnits.incrementAndGet();

		//System.out.printf("used= %d, wi=%d\n", used, wi);
		WorkerThreadReport.registerWorker();
	}


	public  void show() {
		System.out.println("Total of work units: " + workUnits);
		System.out.println("Total of worker threads: " + WorkerThreadReport.createdThreads());
	}

}
