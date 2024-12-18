package production.IterationServer;

public interface IModel {
    void startProcess(Updatable var1);

    void stopProcess();

    void pauseProcess();

    void resumeProcess();

    boolean isAlive();
}
