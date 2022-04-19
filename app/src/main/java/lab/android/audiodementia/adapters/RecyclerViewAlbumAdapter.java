package lab.android.audiodementia.adapters;

import lab.android.audiodementia.R;
import lab.android.audiodementia.model.Album;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class RecyclerViewAlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Album> albumList;
    private BottomReachedListener bottomReachedListener;
    private OnRecyclerItemClickListener clickListener;

    public void setClickListener(OnRecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setBottomReachedListener(BottomReachedListener bottomReachedListener) {
        this.bottomReachedListener = bottomReachedListener;
    }

    public RecyclerViewAlbumAdapter(ArrayList<Album> albumList) {
        this.albumList = albumList;
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public Album getItem(int position) {
        return albumList.get(position);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_album_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ViewHolder) viewHolder).albumTitle.setText(albumList.get(position).getTitle());
        ((ViewHolder) viewHolder).artist.setText(albumList.get(position).getArtist());

        String coverUrl = albumList.get(position).getMediumCover();
        if (coverUrl != null && !coverUrl.isEmpty())
            Picasso.get().load(coverUrl).into(((ViewHolder) viewHolder).cover);
        if (position == albumList.size() - 1 && bottomReachedListener != null)
            bottomReachedListener.onBottomReached(position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView albumTitle;
        TextView artist;
        ImageView cover;
        int position;

        ViewHolder(View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null)
                        clickListener.onItemClick(getItem(getAdapterPosition()));
                }
            });

            this.albumTitle = view.findViewById(R.id.recycler_album_title);
            this.artist = view.findViewById(R.id.recycler_album_artist);
            this.cover = view.findViewById(R.id.recycler_album_cover);
        }

    }

}
