package account_service.infrastructure;

public class Synchronizer {

    private boolean syncDone;

    public Synchronizer() {
        syncDone = false;
    }

    public synchronized void awaitSync() throws InterruptedException {
        while (!syncDone) {
            wait();
        }
    }

    public synchronized void notifySync() {
        syncDone = true;
        notifyAll();
    }
}
