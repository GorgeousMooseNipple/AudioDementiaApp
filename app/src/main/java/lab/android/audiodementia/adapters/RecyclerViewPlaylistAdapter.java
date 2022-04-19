package lab.android.audiodementia.adapters;

import lab.android.audiodementia.R;
import lab.android.audiodementia.model.Playlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class RecyclerViewPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Playlist> playlistList;
    private OnRecyclerItemClickListener clickListener;

    public void setClickListener(OnRecyclerItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public RecyclerViewPlaylistAdapter(ArrayList<Playlist> albumList) {
        this.playlistList = albumList;
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public Playlist getItem(int position) {
        return playlistList.get(position);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ViewHolder) viewHolder).playlistTitle.setText(playlistList.get(position).getTitle());
        ((ViewHolder) viewHolder).trackCount.setText(String.valueOf(playlistList.get(position).getTrackCount()));
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView playlistTitle;
        TextView trackCount;

        ViewHolder(View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null)
                        clickListener.onItemClick(getItem(getAdapterPosition()));
                }
            });

            this.playlistTitle = view.findViewById(R.id.playlist_item_title);
            this.trackCount = view.findViewById(R.id.playlist_track_num);
        }

    }

}

