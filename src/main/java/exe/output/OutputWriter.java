package exe.output;

public abstract class OutputWriter implements Runnable {

    private volatile boolean stop = false;
    private volatile boolean stopGracefully = true;


    /**
     * Writer will stop pooling from queue for new entries, however interrupt still must be called to ensure that the thread will exit.
     */
    public void setStopFlag() {
        this.stop = true;
    }

    /**
     * Writer will stop pooling from queue for new entries, however interrupt still must be called to ensure that the thread will exit.
     * @param stopGracefully If true writer will try to finish writing remaining entries from queue
     */
    public void setStopFlag(boolean stopGracefully) {
        this.stop = true;
        this.stopGracefully = stopGracefully;
    }


    protected boolean continueWriting() {
        return !stop;
    }

    protected boolean stopGracefully() {
        return stopGracefully;
    }

}
