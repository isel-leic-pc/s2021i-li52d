package observables;

import io.reactivex.Emitter;
import io.reactivex.Observable;


import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.net.http.HttpResponse.BodyHandlers;


public class ReactiveHttpClient {

	/**
	 * A basic asynchronous http request
	 * @param url
	 * @return
	 */
	public static CompletableFuture<HttpResponse<String>>
	simpleAsyncHttpRequest(String url) {
		// create an HttpClient with default options
		HttpClient client = HttpClient.newHttpClient();
		URI uri = URI.create(url);
		HttpRequest req =
			HttpRequest.newBuilder(uri)
			.GET()
			.header("Accept-Charset", "utf-8")
			.build();

		return client.sendAsync(req, BodyHandlers.ofString(StandardCharsets.UTF_8) );
	}


	public static CompletableFuture<HttpResponse<String>>  bodyAsString(String url) {

		HttpClient client = HttpClient.newHttpClient();
		URI uri = URI.create(url);
		HttpRequest req = HttpRequest.newBuilder(uri).build();

		return client.sendAsync(req,
			// This lambda implements BodyHandler interface returning a BodySubscriber
			respInfo ->  new StringBodySubscriber(respInfo, StandardCharsets.UTF_8)
		);

	}

	private static class StringBodySubscriber implements HttpResponse.BodySubscriber<String> {
		private final HttpResponse.ResponseInfo respInfo;
		private final CompletableFuture<String> promise;
		private final ByteArrayOutputStream content;
		private final Charset charSet;
		private volatile byte[] buffer;

		private volatile Flow.Subscription subscription;

		private void showResponseInfo() {
			for(var entry : respInfo.headers().map().entrySet()) {
				System.out.println(entry);
			}
		}

		public StringBodySubscriber(HttpResponse.ResponseInfo respInfo,  Charset charSet) {

			this.respInfo = respInfo;
			this.charSet = charSet;
			promise = new CompletableFuture<>();
			content = new ByteArrayOutputStream();
			buffer = new byte[64*1024];
		}

		@Override
		public CompletionStage<String> getBody() {
			return promise;
		}

		@Override
		public void onSubscribe(Flow.Subscription subscription) {
			this.subscription = subscription;
			subscription.request(1);
		}

		@Override
		public void onNext(List<ByteBuffer> item) {
			for(ByteBuffer buf : item) {
				int remaining = buf.remaining();
				if (buffer.length < remaining) {
					// create a new buffer if needed
					buffer = new byte[remaining];
				}

				buf.get(buffer, buf.position(), remaining);
				try {
					content.write(buffer, 0, remaining);
				}
				catch(Exception e) {
					promise.completeExceptionally(e);
					subscription.cancel();
					return;
				}
			}
			subscription.request(1);
		}

		@Override
		public void onError(Throwable throwable) {
			promise.completeExceptionally(throwable);
		}

		@Override
		public void onComplete() {
			// complete the completable future with the string created from body byte array
			promise.complete(content.toString(charSet));
		}
	}


	/**
	 * raectivelly fing a string in body content
	 * @param url
	 * @param text
	 * @return
	 */
	public static CompletableFuture<Boolean> hasTextInBody(String url, String text) {

		HttpClient client = HttpClient.newHttpClient();
		URI uri = URI.create(url);
		HttpRequest req = HttpRequest.newBuilder(uri).build();

		StringFinder finder = new StringFinder(text);
		client.sendAsync(req,
			HttpResponse.BodyHandlers.fromLineSubscriber(finder))
			.whenComplete((resp, t) -> {
				System.out.println("send async done!");
				if (t != null) finder.onError(t);
			});
		return finder.promise();
	}

	/**
	 * the string finder subscriber
	 */
	private static class StringFinder implements Flow.Subscriber<String> {

		private final String toFind;
		private final CompletableFuture<Boolean> cf;

		private  volatile Flow.Subscription subscription;
		private AtomicBoolean completed;

		public StringFinder(String toFind) {
			this.toFind = toFind;
			cf = new CompletableFuture<>();
			completed = new AtomicBoolean(false);
		}


		@Override
		public void onSubscribe(Flow.Subscription subscription) {
			this.subscription = subscription;
			subscription.request(1);
		}

		@Override
		public void onNext(String item) {

			if (!completed.get() && item.contains(toFind)) {
				System.out.println(item + ": " + toFind);
				if (completed.compareAndSet(false, true)) {
					System.out.println("done!");
					cf.complete(true);
					// here we should cancel the subscription
					// but unfortunately this doesn't seem to work
				}

			}

			subscription.request(1);

		}

		@Override
		public void onError(Throwable throwable) {
			if (completed.compareAndSet(false, true))
				cf.completeExceptionally(throwable);
		}

		@Override
		public void onComplete() {
			System.out.println("emission completed!");
			if (completed.compareAndSet(false, true))
				cf.complete(false);
		}

		public CompletableFuture<Boolean> promise() {
			return cf;
		}

	}

	public static CompletableFuture<HttpResponse<Observable<ByteBuffer>>>  httpRequest(String location) {

		HttpClient client = HttpClient.newHttpClient();
		URI uri = URI.create(location);
		HttpRequest req = HttpRequest.newBuilder(uri).build();

		return client.sendAsync(req, respInfo ->   new RawBodySubscriber(respInfo) );

	}

	/**
	 * A simple BodySubscriber generating a reactive body in an Observable<ByteBuffer>  form
	 * Cancellation is not suported
	 */
	private static class RawBodySubscriber implements HttpResponse.BodySubscriber<Observable<ByteBuffer>> {

		private final HttpResponse.ResponseInfo respInfo;
		private volatile Flow.Subscription subscription;
		private final CompletableFuture<Observable<ByteBuffer>> cf;
		private AtomicReference<Emitter<ByteBuffer>> observerHolder;

		public RawBodySubscriber(HttpResponse.ResponseInfo respInfo) {
			this.respInfo = respInfo;
			observerHolder = new AtomicReference<>();
			cf = new CompletableFuture<>();
		}

		@Override
		public CompletionStage<Observable<ByteBuffer>> getBody() {
			return cf;
		}

		@Override
		public void onSubscribe(Flow.Subscription subscription) {
			this.subscription = subscription;

			// complete the completable future with an Observable<ByteBuffer>
			cf.complete(Observable.create(observer -> {
				if (!observerHolder.compareAndSet(null, observer))  {
					observer.onError(new InvalidParameterException("A subscriber is already in place"));
				}
				else {
					this.subscription.request(1);
				}
			}));
		}

		@Override
		public void onNext(List<ByteBuffer> item) {
				var observer = observerHolder.get();
				for(ByteBuffer bb : item) {
					observer.onNext(bb);
				}
				subscription.request(1);
		}

		@Override
		public void onError(Throwable throwable) {
			var observer = observerHolder.get();
			observer.onError(throwable);
		}

		@Override
		public void onComplete() {
			var observer = observerHolder.get();
			observer.onComplete();
		}


	}





}



