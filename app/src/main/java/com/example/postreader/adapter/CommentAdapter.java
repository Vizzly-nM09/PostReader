package com.example.postreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.postreader.Model.Comment;
import com.example.postreader.R;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<Comment> commentList = new ArrayList<>();

    public CommentAdapter(List<Comment> comments) {
        setComments(comments);
    }

    public void setComments(List<Comment> comments) {
        commentList.clear();
        if (comments != null) {
            commentList.addAll(comments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.tvInitials.setText(comment.getInitials());
        holder.tvName.setText(comment.getName());
        holder.tvEmail.setText(comment.getEmail());
        holder.tvBody.setText(comment.getBody());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitials;
        final TextView tvName;
        final TextView tvEmail;
        final TextView tvBody;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvCommentInitials);
            tvName = itemView.findViewById(R.id.tvCommentName);
            tvEmail = itemView.findViewById(R.id.tvCommentEmail);
            tvBody = itemView.findViewById(R.id.tvCommentBody);
        }
    }
}

