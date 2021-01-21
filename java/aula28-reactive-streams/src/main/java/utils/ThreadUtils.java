package utils;

public class ThreadUtils {
	public static void showThread(String msg) {
		if (true) {
			System.out.println(msg + " on thread: " +
				Thread.currentThread().getName());
		}
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch(InterruptedException e) {

		}
	}
}
