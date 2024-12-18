

public class Model implements IModel {

    CalcProcess proc;

    @Override
    public void startProcess(Updatable updater) {
        if(proc != null) {
            proc.processStop();
        }
        proc = new CalcProcess(updater);
        proc.start();
    }

    @Override
    public void stopProcess() {
        proc.processStop();
    }

    @Override
    public void pauseProcess() {
        proc.processPause();
    }

    @Override
    public void resumeProcess() {
        proc.processResume();
    }

    @Override
    public boolean isAlive() {
        return proc.isAlive();
    }
}
