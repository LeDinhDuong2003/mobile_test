// app/src/main/java/com/example/mobileproject/adapter/CommentAdapter.java
package com.example.mobileproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.model.Comment;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        if (holder.username != null) {
            holder.username.setText(comment.getUser() != null ? comment.getUser().getFullName() : "Anonymous");
        }
        if (holder.role != null) {
            holder.role.setText(comment.getUser() != null ? comment.getUser().getRole() : "");
        }
        if (holder.content != null) {
            holder.content.setText(comment.getComment() != null ? comment.getComment() : "");
        }
        if (holder.time != null) {
            holder.time.setText(comment.getCreatedAt() != null ?
                    comment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        }
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, role, content, time;

        public ViewHolder(View view) {
            super(view);
            username = view.findViewById(R.id.txt_username);
            role = view.findViewById(R.id.txt_role);
            content = view.findViewById(R.id.txt_content);
            time = view.findViewById(R.id.txt_time);
        }
    }
}