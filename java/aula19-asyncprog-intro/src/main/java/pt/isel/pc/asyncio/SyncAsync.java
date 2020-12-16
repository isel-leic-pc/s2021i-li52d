package pt.isel.pc.asyncio;





public class SyncAsync {
	private class AsyncOper<T> {
		public T get() throws Exception {
			return null;
		}
	}

	private interface OperationCompleted<T> {
		public void onComplete(Exception e, T value);
	}

	private class Device {

		public int read(char buffer[]) {
			return 0; // número de bytes lidos
		}

		public AsyncOper<Integer> async_read(char buffer[]) {
			return null;
		}

		public void async_read(char buffer[],
		                       OperationCompleted<Integer> completed) {

		}

	}


}
