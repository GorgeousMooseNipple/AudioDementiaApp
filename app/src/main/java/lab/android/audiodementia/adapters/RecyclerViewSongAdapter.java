package lab.android.audiodementia.adapters;

import lab.android.audiodementia.R;
import lab.android.audiodementia.model.Song;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class RecyclerViewSongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Song> songList;
    private BottomReachedListener bottomReachedListener;
    private OnRecyclerItemClickListener clickListener;

    public void setBottomReachedListener(BottomReachedListener bottomReachedListener) {
        this.bottomReachedListener = bottomReachedListener;
    }

    public void setClickListener(OnRecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public RecyclerViewSongAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public Song getItem(int position) {
        return songList.get(position);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_song_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Song song = songList.get(position);
        ((ViewHolder) viewHolder).title.setText(song.getTitle());
        ((ViewHolder) viewHolder).artist.setText(song.getArtist());
        ((ViewHolder) viewHolder).album.setText(song.getAlbum());
        long duration = song.getDuration();
        String hr_duration = String.format(Locale.ROOT, "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(duration),
                TimeUnit.SECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration))
        );
        ((ViewHolder) viewHolder).duration.setText(hr_duration);

        String smallCover = songList.get(position).getSmallCoverUrl();
        if (smallCover != null && !smallCover.isEmpty())
            Picasso.get().load(smallCover).into(((ViewHolder) viewHolder).cover);
        if (position == songList.size() - 1 && bottomReachedListener != null) {
            bottomReachedListener.onBottomReached(position);
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        ImageView cover;
        TextView title;
        TextView artist;
        TextView album;
        TextView duration;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null)
                        clickListener.onItemClick(getItem(getAdapterPosition()), getAdapterPosition());
                }
            });

            this.cover = view.findViewById(R.id.song_item_cover);
            this.title = view.findViewById(R.id.recycler_song_title);
            this.artist = view.findViewById(R.id.recycler_song_artist);
            this.album = view.findViewById(R.id.recycler_song_album);
            this.duration = view.findViewById(R.id.recycler_song_duration);
        }

    }

}
