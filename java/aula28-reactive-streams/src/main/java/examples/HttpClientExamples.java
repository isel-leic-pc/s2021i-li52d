package examples;

import io.reactivex.Observable;

import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;

import static observables.ObservableUtils.fromCompletableFuture;
import static observables.ReactiveHttpClient.*;


public class HttpClientExamples {

	private static void showResponseHeaders(HttpResponse resp) {
		for(var entry : resp.headers().map().entrySet()) {
			System.out.println(entry);
		}
	}

	public static void
	simpleAsyncHttpRequestExample(String url, CountDownLatch done) {
		simpleAsyncHttpRequest(url)
		.whenComplete((resp, t) -> {
			if (t != null) {
				System.out.println(t.getMessage());
				done.countDown();
			}
		})
		.thenApply(resp -> {
			showResponseHeaders(resp);
			return resp.body();
		})
		.thenAccept(body -> {
			System.out.println(body);
			done.countDown();
		});
	}

	public static void
	bodyAsStringHttpRequestExample(String url, CountDownLatch done) {
		bodyAsString(url)
			.whenComplete((resp, t) -> {
				if (t!= null) {
					System.out.println(t.getMessage());
					done.countDown();
				}
			})
			.thenApply(resp -> {
				showResponseHeaders(resp);
				return resp.body();
			})
			.thenAccept(body -> {
				System.out.println(body);
				done.countDown();
			});
	}

	public static void isDynamicHtmlHttpRequestExample(String url, CountDownLatch done) {
		hasTextInBody(url, "</script>")
			.whenComplete((resp, t) -> {
				if (t!= null) {
					System.out.println(t.getMessage());
					done.countDown();
				}
			})
			.thenAccept(resp -> {
				System.out.println((resp ? "" : "No ") + "Dynamic Html");
				done.countDown();
			});

	}

	private static Observable<String> asObservable(String url) {
		return
			fromCompletableFuture(
				bodyAsString("https://www.isel.pt")
			)
			.map(HttpResponse::body)
			.map(s -> s.lines().iterator())
			.flatMap(it -> Observable.fromIterable(() -> it));
	}

	public static void showSriptLinesWithReactiveBodyStringExample(String url, CountDownLatch done) {

		Observable<String> bodyScriptLines =
			asObservable(url)
			.zipWith(Observable.range(1,Integer.MAX_VALUE),
						(s, i) -> String.format("%d: %s", i, s))
			.filter(s -> s.contains("<script"));



		bodyScriptLines.subscribe(
			System.out::println,
			t -> { System.out.println("error: " + t.getMessage()); done.countDown(); },
			() -> { System.out.println("completed!"); done.countDown(); }
		);
	}
}
