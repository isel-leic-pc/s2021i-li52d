package examples;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static observables.ObservableUtils.fromCompletableFuture;
import static observables.ReactiveHttpClient.httpRequest;
import static utils.ThreadUtils.showThread;

public class ObservableExamples {


	private static  void helloReactive() {
		Observable<String> words =
			Observable.just("Hello", ", world!");
		//Disposable subs = words.subscribe(System.out::println);

		words
			.subscribeOn(Schedulers.computation())
			.subscribe(s -> showThread(s));

		System.out.println("done!");
	}

	private static void firstCreateExample() {
		showThread("start create example");
		Observer createdObsObserver = new Observer<String>() {

			@Override
			public void onSubscribe( Disposable d) {
				showThread("onSubscrition");
			}

			@Override
			public void onNext(String str) {
				showThread(str + "");
			}

			@Override
			public void onError(  Throwable e) {
				showThread(e.toString());
			}

			@Override
			public void onComplete() {
				showThread("done");
			}
		};

		Observable<String> words = Observable.create(
			observer -> {
				showThread("emitting values");
				observer.onNext("Hello");
				observer.onNext(", world!");

				observer.onComplete();
			}
		);

		showThread("After observable creation");
		words
			.subscribeOn(Schedulers.computation())
			.observeOn(Schedulers.io())
			.subscribe(createdObsObserver);

		showThread("end create example");
	}

	private static void firstFlowableCreateExample() {
		showThread("start create example");
		Subscriber createdObsSubscriber = new Subscriber<String>() {
			private Subscription subscription;
			@Override
			public void onSubscribe( Subscription subscription) {
				showThread("onSubscrition");
				this.subscription = subscription;
				subscription.request(1);
			}

			@Override
			public void onNext(String str) {
				showThread(str + "");
				subscription.request(1);
			}

			@Override
			public void onError(  Throwable e) {
				showThread(e.toString());
			}

			@Override
			public void onComplete() {
				showThread("done");
			}
		};

		Flowable<String> words = Flowable.create(
			observer -> {
				showThread("emitting values");
				observer.onNext("Hello");
				observer.onNext(", world!");

				observer.onComplete();
			},
			BackpressureStrategy.BUFFER
		);

		showThread("After observable creation");
		words
			.subscribeOn(Schedulers.computation())
			.observeOn(Schedulers.io())
			.subscribe(createdObsSubscriber);

		showThread("end create example");
	}

	private static void intervalExample() {
		Observable<Long> timeEvents =
			Observable.interval(1, TimeUnit.SECONDS);

		Observable<String> zipped = Observable.zip(Observable.just("Joao", "Maria", "Diogo", "Carlos"),
			timeEvents, (s,l) -> s);


		zipped.subscribe(new Observer<String>() {

			@Override
			public void onSubscribe( Disposable d) {
				showThread("onSubscrition");
			}

			@Override
			public void onNext(String aLong) {
				showThread(aLong + "");
			}

			@Override
			public void onError(  Throwable e) {
				showThread(e.toString());
			}

			@Override
			public void onComplete() {
				showThread("done");
			}
		});
	}


	private static void show(HttpResponse resp) {
		System.out.println("status: " + resp.statusCode());
		for(Map.Entry<String, List<String>> e: resp.headers().map().entrySet()) {
			System.out.println(e.getKey() + ":" +
				e.getValue().stream().collect(Collectors.joining()));
		}
	}

	private static AsynchronousFileChannel createFileChannel(String fileName)
		throws UncheckedIOException {
		try {
			Path outFile = Path.of(fileName);
			return AsynchronousFileChannel.open(outFile,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void httpRequestExample() throws IOException {
		Observable<ByteBuffer> body =
			fromCompletableFuture(
				httpRequest("https://www.isel.pt")
					.thenApply(r -> {
						show(r);
						return r;
					}))
				.flatMap(r -> r.body());


		try(var outChannel = createFileChannel("bodyresponse.html")) {
			System.out.println("buffers count = " +
				body
					.reduce(0, (c, bb) -> c + outChannel.write(bb, c).get())
					.blockingGet()
			);
		}

	}

}
