


public class CalcProcess extends Thread{

    Updatable updater;

    Boolean shouldPause = false;
    Boolean shouldStop = false;

    public CalcProcess(Updatable updater) {
        this.updater = updater;
    }

    public void processStop(){
        shouldStop = true;
        if(shouldPause)
            synchronized (this) {
                this.notify();
            }
    }

    public void processPause(){
        shouldPause = true;
    }

    public void processResume(){
        shouldPause = false;
        synchronized (this) {
            this.notify();
        }
    }

    public void run(){
        for(int i = 0; i <= 1000; i++) {

            if(shouldPause) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            double currentProgress = (double) i / 1000;


            updater.update(currentProgress);

            if(shouldStop){
                updater.update(0.0);
                break;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
