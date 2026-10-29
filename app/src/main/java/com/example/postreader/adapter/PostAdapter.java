package com.example.postreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.postreader.DetailActivity;
import com.example.postreader.Model.Post;
import com.example.postreader.R;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<Post> fullList = new ArrayList<>();
    private final List<Post> displayList = new ArrayList<>();
    private final Context context;
    private String currentQuery = "";

    public PostAdapter(List<Post> posts, Context context) {
        this.context = context;
        setPosts(posts);
    }

    public void setPosts(List<Post> posts) {
        fullList.clear();
        if (posts != null) {
            fullList.addAll(posts);
        }
        applyFilter();
    }

    public void filter(String query) {
        currentQuery = query != null ? query.trim().toLowerCase() : "";
        applyFilter();
    }

    private void applyFilter() {
        displayList.clear();
        if (currentQuery.isEmpty()) {
            displayList.addAll(fullList);
        } else {
            for (Post post : fullList) {
                boolean matchesTitle = post.getTitle() != null && post.getTitle().toLowerCase().contains(currentQuery);
                boolean matchesBody = post.getBody() != null && post.getBody().toLowerCase().contains(currentQuery);
                if (matchesTitle || matchesBody) {
                    displayList.add(post);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = displayList.get(position);

        holder.tvId.setText(context.getString(R.string.post_number, post.getId()));
        holder.tvTitle.setText(post.getTitle());
        holder.tvSnippet.setText(post.getBody() != null ? post.getBody().replace('\n', ' ') : "");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_POST_ID, post.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvId;
        final TextView tvTitle;
        final TextView tvSnippet;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSnippet = itemView.findViewById(R.id.tvSnippet);
        }
    }
}
