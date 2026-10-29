package com.example.postreader.Model;

public class Comment {
    private int postId;
    private int id;
    private String name;
    private String email;
    private String body;

    public Comment() {
    }

    public Comment(int postId, int id, String name, String email, String body) {
        this.postId = postId;
        this.id = id;
        this.name = name;
        this.email = email;
        this.body = body;
    }

    public int getPostId() {
        return postId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getBody() {
        return body != null ? body : "";
    }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            if (email != null && !email.isEmpty()) {
                return email.substring(0, 1).toUpperCase();
            }
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}

