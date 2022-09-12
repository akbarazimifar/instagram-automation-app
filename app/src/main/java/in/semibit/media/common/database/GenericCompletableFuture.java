package in.semibit.media.common.database;

public class GenericCompletableFuture<T> {

    private T result;
    private Exception ex;
    private GenericCompletableFutureCBX<T> onException;
    private GenericCompletableFutureCB<T> onComplete;

    void setResult(T value) {
        this.result = value;
        if (this.onComplete != null) {
            this.onComplete.onComplete(result);
        }
    }

    public void completeExceptionally(Exception exception) {
        ex = exception;
        if (onException != null) {
            T exResult = onException.onException(exception);
            setResult(exResult);

        }
    }

    public void complete(T result) {
        setResult(result);
    }

    public void thenAccept(GenericCompletableFutureCB<T> onComplete) {
        this.onComplete = onComplete;
        if (result != null || ex != null) {
            onComplete.onComplete(result);
        }
    }

    public GenericCompletableFuture<T> exceptionally(GenericCompletableFutureCBX<T> onException) {
        this.onException = onException;
        if (ex != null) {
            setResult(onException.onException(ex));
        }
        return this;
    }


    public static GenericCompletableFuture genericCompletedFuture(Object o) {
        GenericCompletableFuture completableFuture = new GenericCompletableFuture();
        completableFuture.complete(o);
        return completableFuture;
    }


    public boolean isDone() {
        return result != null || ex != null;
    }

    public T get() {
        return result;
    }
}
