package in.semibit.media.common.database;

import java.util.List;

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
            if (exResult != null) {
                setResult(exResult);
            }
        }
    }

    public void complete(T result) {
        setResult(result);
    }

    public void thenAccept(GenericCompletableFutureCB<T> onComplete) {
        this.onComplete = onComplete;
        if (result != null) {
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
}
