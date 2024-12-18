package production.IterationServer;

public class CalcProcess extends Thread {
    Updatable updater;
    Boolean shouldPause = false;
    Boolean shouldStop = false;

    public CalcProcess(Updatable updater) {
        this.updater = updater;
    }

    public void processStop() {
        this.shouldStop = true;
        if (this.shouldPause) {
            synchronized(this) {
                this.notify();
            }
        }

    }

    public void processPause() {
        this.shouldPause = true;
    }

    public void processResume() {
        this.shouldPause = false;
        synchronized(this) {
            this.notify();
        }
    }

    public void run() {
        for(int i = 0; i <= 1000; ++i) {
            if (this.shouldPause) {
                synchronized(this) {
                    try {
                        this.wait();
                    } catch (InterruptedException var6) {
                        InterruptedException e = var6;
                        throw new RuntimeException(e);
                    }
                }
            }

            double currentProgress = (double)i / 1000.0;
            this.updater.update(currentProgress);
            if (this.shouldStop) {
                this.updater.update(0.0);
                break;
            }

            try {
                Thread.sleep(20L);
            } catch (InterruptedException var5) {
                InterruptedException e = var5;
                throw new RuntimeException(e);
            }
        }

    }
}
