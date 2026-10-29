package com.example.postreader.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.postreader.Model.Todo;
import com.example.postreader.R;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    public enum FilterStatus {
        ALL,
        PENDING,
        COMPLETED
    }

    public interface OnTodoActionListener {
        void onToggle(Todo todo);
        void onDelete(Todo todo);
    }

    private final List<Todo> fullList = new ArrayList<>();
    private final List<Todo> displayList = new ArrayList<>();
    private final Context context;
    private final OnTodoActionListener listener;
    private FilterStatus currentFilter = FilterStatus.ALL;
    private String currentQuery = "";

    public TodoAdapter(List<Todo> todos, Context context, OnTodoActionListener listener) {
        this.context = context;
        this.listener = listener;
        setTodos(todos);
    }

    public void setTodos(List<Todo> todos) {
        fullList.clear();
        if (todos != null) {
            fullList.addAll(todos);
        }
        applyFilters();
    }

    public void setFilter(FilterStatus filter) {
        this.currentFilter = filter;
        applyFilters();
    }

    public void filter(String query) {
        this.currentQuery = query != null ? query.trim().toLowerCase() : "";
        applyFilters();
    }

    private void applyFilters() {
        displayList.clear();
        for (Todo todo : fullList) {
            boolean matchesStatus = false;
            switch (currentFilter) {
                case ALL:
                    matchesStatus = true;
                    break;
                case PENDING:
                    matchesStatus = !todo.isCompleted();
                    break;
                case COMPLETED:
                    matchesStatus = todo.isCompleted();
                    break;
            }

            boolean matchesQuery = currentQuery.isEmpty() ||
                    (todo.getTitle() != null && todo.getTitle().toLowerCase().contains(currentQuery));

            if (matchesStatus && matchesQuery) {
                displayList.add(todo);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Todo todo = displayList.get(position);

        holder.tvTitle.setText(todo.getTitle());

        // Temporarily detach listener to avoid unwanted callbacks during recycling
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(todo.isCompleted());

        if (todo.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.ink_muted));
            holder.tvStatusBadge.setText(R.string.filter_completed);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_completed));
            holder.tvStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_completed_container));
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.ink));
            holder.tvStatusBadge.setText(R.string.filter_pending);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
            holder.tvStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending_container));
        }

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggle(todo);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(todo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCheckBox cbCompleted;
        final TextView tvTitle;
        final TextView tvStatusBadge;
        final ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTitle = itemView.findViewById(R.id.tvTodoTitle);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnDelete = itemView.findViewById(R.id.btnDeleteTodo);
        }
    }
}

