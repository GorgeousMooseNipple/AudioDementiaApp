package lab.android.audiodementia.background;

import java.util.ArrayList;
import lab.android.audiodementia.model.Genre;

public class GenresUploadedEvent extends ResponseEvent<ArrayList<Genre>>{
    public GenresUploadedEvent(boolean successfull,
                                  String message,
                                  ArrayList<Genre> genres) {
        this.successful = successfull;
        this.message = message;
        this.entity = genres;
    }
}
