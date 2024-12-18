

public interface IModel {
    void startProcess(Updatable updater);
    void stopProcess();
    void pauseProcess();
    void resumeProcess();
    boolean isAlive();
}
