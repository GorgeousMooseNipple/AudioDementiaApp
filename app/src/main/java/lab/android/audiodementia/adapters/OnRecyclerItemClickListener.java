package lab.android.audiodementia.adapters;

import lab.android.audiodementia.model.Album;
import lab.android.audiodementia.model.Genre;
import lab.android.audiodementia.model.Playlist;
import lab.android.audiodementia.model.Song;

public interface OnRecyclerItemClickListener {

    void onItemClick(Object obj);

    void onItemClick(Object obj, int position);

}
