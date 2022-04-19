package lab.android.audiodementia.adapters;

import lab.android.audiodementia.R;
import lab.android.audiodementia.model.Genre;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class RecyclerViewGenreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Genre> genreList;
    private BottomReachedListener bottomReachListener;
    private OnRecyclerItemClickListener listener;

    public void setBottomReachedListener(BottomReachedListener bottomReachListener) {
        this.bottomReachListener = bottomReachListener;
    }

    public void setListener(OnRecyclerItemClickListener listener) {
        this.listener = listener;
    }

    public RecyclerViewGenreAdapter(ArrayList<Genre> albumList) {
        this.genreList = albumList;
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public Genre getItem(int position) {
        return genreList.get(position);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_genre_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ViewHolder) viewHolder).genreTitle.setText(genreList.get(position).getGenreName());
        ((ViewHolder) viewHolder).position = position;
        if (position == genreList.size() - 1) {
            if (bottomReachListener != null)
                bottomReachListener.onBottomReached(position);
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView genreTitle;
        int position;

        ViewHolder(View view) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(getItem(position));
                }
            });
            this.genreTitle = view.findViewById(R.id.recycler_genre_title);
        }

    }

}
