package utils;

public class Progress {

	private String format;
	private String leadingText;
	private String trailingText;
	
	private long numTicks;
	private long delta;
	private long curTick;
	
	public Progress(String leadingText, long numTicks) {
		this(leadingText, "", numTicks, 2);
	}
	
	public Progress(String leadingText, long numTicks, int numDecimalPlaces) {
		this(leadingText, "", numTicks, numDecimalPlaces);
	}
	
	public Progress(String leadingText, String trailingText, long numTicks, int numDecimalPlaces) {
		if (numDecimalPlaces < 0)
			numDecimalPlaces = 0;
		
		this.format = "%." + numDecimalPlaces + "f";
		this.leadingText = leadingText;
		this.trailingText = trailingText;
	
		this.numTicks = numTicks;
		this.curTick = 0;
		this.delta = (int)(numTicks / Math.pow(10, 2 + numDecimalPlaces)); // how often to update progress
		if (this.delta < 1)
			this.delta = 1;
		
		update();
	}
	
	private synchronized void update() {
		System.out.print("\r");
	    System.out.print(leadingText);
	    System.out.print(" [");
	    System.out.print(String.format(format, getCurPercent()));
	    System.out.print("%] ");
	    System.out.print(trailingText);
	}
	
	public double getCurPercent() {
		return curTick * 100.0 / numTicks;
	}
	
	public synchronized void tick() {
		if (curTick == numTicks)
			return;
		
		curTick ++;
		if (curTick % delta == 0)
			update();
	}
	
	public void tick(long times) {
		for (long i = 0; i < times; i++)
			tick();
	}
	
	public synchronized void done() {
		curTick = numTicks;
		update();
		System.out.println();
	}
}
