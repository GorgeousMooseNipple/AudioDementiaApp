package lab.android.audiodementia.background;

public interface Consumer<Accepts> {
    void call(Accepts arg);
}
