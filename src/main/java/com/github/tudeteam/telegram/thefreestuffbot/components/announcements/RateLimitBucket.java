package com.github.tudeteam.telegram.thefreestuffbot.components.announcements;

public class RateLimitBucket {

    /**
     * The rate limit of the bucket.
     */
    protected final int rateLimit;

    /**
     * The timestamp since the last rate limit reset.
     */
    protected long rateTimestamp = 0;

    /**
     * The current rate of the current second.
     */
    protected int currentRate = 0;

    public RateLimitBucket(int rateLimit) {
        this.rateLimit = rateLimit;
    }

    /**
     * Attempt to increase the current rate counter, if the limit is reached, it'll sleep until it resets.
     *
     * @throws InterruptedException If interrupted while waiting for the rate limit to reset.
     */
    public synchronized void consume() throws InterruptedException {
        while (true) {
            long timestamp = System.currentTimeMillis();
            if (timestamp - rateTimestamp >= 1000L) {
                rateTimestamp = timestamp;
                currentRate = 0;
            }

            if (currentRate >= rateLimit) {
                Thread.sleep(1010L - (timestamp - rateTimestamp)); //The extra 10 ms is because Thread.sleep is not perfectly accurate.
                continue;
            }

            currentRate++;
            break;
        }
    }
}
