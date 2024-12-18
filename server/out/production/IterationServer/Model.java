package production.IterationServer;

public class Model implements IModel {
    CalcProcess proc;

    public Model() {
    }

    public void startProcess(Updatable updater) {
        if (this.proc != null) {
            this.proc.processStop();
        }

        this.proc = new CalcProcess(updater);
        this.proc.start();
    }

    public void stopProcess() {
        this.proc.processStop();
    }

    public void pauseProcess() {
        this.proc.processPause();
    }

    public void resumeProcess() {
        this.proc.processResume();
    }

    public boolean isAlive() {
        return this.proc.isAlive();
    }
}
