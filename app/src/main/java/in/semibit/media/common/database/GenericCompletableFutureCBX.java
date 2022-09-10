package in.semibit.media.common.database;

public interface GenericCompletableFutureCBX<T>{
    T onException(Exception exception);
}
