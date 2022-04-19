package lab.android.audiodementia.background;

public interface Function<Returns, Accepts> {
    Returns call(Accepts arg);
}
