package pt.isel.pc.asyncio;

public class AggregateException extends Exception{
	private final Throwable[] aggregate;

	public AggregateException(Throwable[] agg ) {
		this.aggregate = agg.clone();
	}

	public  Throwable[] get_exceptions() {
		return aggregate;
	}
}
