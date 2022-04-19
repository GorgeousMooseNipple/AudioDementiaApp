package lab.android.audiodementia.background;

public class SubmitSongEvent {

    private int position;

    public SubmitSongEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
