package com.example.postreader.data;

import com.example.postreader.Model.Post;
import com.example.postreader.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {

    private static volatile PostRepository instance;
    private final List<Post> cachedPosts = new ArrayList<>();
    private int customIdCounter = 101;

    private PostRepository() {
    }

    public static synchronized PostRepository getInstance() {
        if (instance == null) {
            instance = new PostRepository();
        }
        return instance;
    }

    public synchronized void fetchPosts(boolean forceRefresh, RepositoryCallback<List<Post>> callback) {
        if (!forceRefresh && !cachedPosts.isEmpty()) {
            callback.onSuccess(new ArrayList<>(cachedPosts));
            return;
        }

        RetrofitClient.getApiService().getPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    synchronized (PostRepository.this) {
                        cachedPosts.clear();
                        cachedPosts.addAll(response.body());
                    }
                    callback.onSuccess(new ArrayList<>(cachedPosts));
                } else {
                    callback.onError("Server returned status " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized void addPost(Post post, RepositoryCallback<Post> callback) {
        RetrofitClient.getApiService().createPost(post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post created = response.body();
                    synchronized (PostRepository.this) {
                        int assignedId = customIdCounter++;
                        created.setId(assignedId);
                        cachedPosts.add(0, created);
                    }
                    callback.onSuccess(created);
                } else {
                    callback.onError("Failed to create post. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized void updatePost(Post post, RepositoryCallback<Post> callback) {
        RetrofitClient.getApiService().updatePost(post.getId(), post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    synchronized (PostRepository.this) {
                        for (int i = 0; i < cachedPosts.size(); i++) {
                            if (cachedPosts.get(i).getId() == post.getId()) {
                                cachedPosts.set(i, post);
                                break;
                            }
                        }
                    }
                    callback.onSuccess(post);
                } else {
                    callback.onError("Failed to update post. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized void deletePost(int id, RepositoryCallback<Void> callback) {
        RetrofitClient.getApiService().deletePost(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    synchronized (PostRepository.this) {
                        for (int i = 0; i < cachedPosts.size(); i++) {
                            if (cachedPosts.get(i).getId() == id) {
                                cachedPosts.remove(i);
                                break;
                            }
                        }
                    }
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete post. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized Post getPostById(int id) {
        for (Post p : cachedPosts) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public synchronized List<Post> getCachedPosts() {
        return new ArrayList<>(cachedPosts);
    }
}

