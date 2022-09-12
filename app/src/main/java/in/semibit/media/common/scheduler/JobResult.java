package in.semibit.media.common.scheduler;

public class JobResult<T> {
    int jobStatus = 0;
    T result;

    public JobResult(int jobStatus, T result) {
        this.jobStatus = jobStatus;
        this.result = result;
    }

    public static JobResult success(){
        return new JobResult(1,new Object());
    }

    public static JobResult failed(){
        return new JobResult(-1,new Object());
    }

}
