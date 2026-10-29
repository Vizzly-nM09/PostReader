package com.example.postreader.network;

import com.example.postreader.Model.Comment;
import com.example.postreader.Model.Post;
import com.example.postreader.Model.Todo;
import com.example.postreader.Model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @GET("posts")
    Call<List<Post>> getPosts();

    @GET("posts/{id}")
    Call<Post> getPostById(@Path("id") int id);

    @POST("posts")
    Call<Post> createPost(@Body Post post);

    @PUT("posts/{id}")
    Call<Post> updatePost(@Path("id") int id, @Body Post post);

    @PATCH("posts/{id}")
    Call<Post> patchPost(@Path("id") int id, @Body Post post);

    @DELETE("posts/{id}")
    Call<Void> deletePost(@Path("id") int id);

    @GET("users/{id}")
    Call<User> getUserById(@Path("id") int id);

    @GET("posts/{id}/comments")
    Call<List<Comment>> getCommentsByPostId(@Path("id") int postId);

    @GET("todos")
    Call<List<Todo>> getTodos();

    @POST("todos")
    Call<Todo> createTodo(@Body Todo todo);

    @PUT("todos/{id}")
    Call<Todo> updateTodo(@Path("id") int id, @Body Todo todo);

    @PATCH("todos/{id}")
    Call<Todo> patchTodo(@Path("id") int id, @Body Todo todo);

    @DELETE("todos/{id}")
    Call<Void> deleteTodo(@Path("id") int id);
}
