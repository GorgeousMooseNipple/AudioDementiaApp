package lab.android.audiodementia.background;

import java.util.ArrayList;

import lab.android.audiodementia.model.Song;

public class ResponseEvent<Entity> {

    boolean successful;
    String message;
    Entity entity;

    public boolean isSuccessful() {
        return  successful;
    }

    public String getMessage() {
        return message;
    }

    public Entity getEntity() {
        return entity;
    }
}
