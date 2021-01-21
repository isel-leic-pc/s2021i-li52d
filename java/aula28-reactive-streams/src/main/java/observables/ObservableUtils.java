package observables;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.TimeUnit;


import static utils.ThreadUtils.showThread;

public class ObservableUtils {

	/**
	 * Build an Observable from a completable future
	 * @param cf
	 * @param <T>
	 * @return
	 */
	public static <T> Observable<T> fromCompletableFuture(CompletableFuture<T> cf) {
		return Observable.create(observer -> {
			cf.whenComplete((tval, error) -> {
				if (error != null)
					observer.onError(error);
				else {
					observer.onNext(tval);
					observer.onComplete();
				}
			});
		});
	}







}
