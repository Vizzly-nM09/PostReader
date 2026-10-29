package com.example.postreader.data;

import com.example.postreader.Model.Todo;
import com.example.postreader.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TodoRepository {

    private static volatile TodoRepository instance;
    private final List<Todo> cachedTodos = new ArrayList<>();
    private int customIdCounter = 201;

    private TodoRepository() {
    }

    public static synchronized TodoRepository getInstance() {
        if (instance == null) {
            instance = new TodoRepository();
        }
        return instance;
    }

    public synchronized void fetchTodos(boolean forceRefresh, RepositoryCallback<List<Todo>> callback) {
        if (!forceRefresh && !cachedTodos.isEmpty()) {
            callback.onSuccess(new ArrayList<>(cachedTodos));
            return;
        }

        RetrofitClient.getApiService().getTodos().enqueue(new Callback<List<Todo>>() {
            @Override
            public void onResponse(Call<List<Todo>> call, Response<List<Todo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    synchronized (TodoRepository.this) {
                        cachedTodos.clear();
                        cachedTodos.addAll(response.body());
                    }
                    callback.onSuccess(new ArrayList<>(cachedTodos));
                } else {
                    callback.onError("Server returned status " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Todo>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized void toggleTodo(Todo todo, RepositoryCallback<Todo> callback) {
        boolean originalStatus = todo.isCompleted();
        todo.setCompleted(!originalStatus);

        RetrofitClient.getApiService().updateTodo(todo.getId(), todo).enqueue(new Callback<Todo>() {
            @Override
            public void onResponse(Call<Todo> call, Response<Todo> response) {
                if (response.isSuccessful()) {
                    synchronized (TodoRepository.this) {
                        for (int i = 0; i < cachedTodos.size(); i++) {
                            if (cachedTodos.get(i).getId() == todo.getId()) {
                                cachedTodos.set(i, todo);
                                break;
                            }
                        }
                    }
                    callback.onSuccess(todo);
                } else {
                    todo.setCompleted(originalStatus);
                    callback.onError("Failed to toggle task. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Todo> call, Throwable t) {
                todo.setCompleted(originalStatus);
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized void addTodo(Todo todo, RepositoryCallback<Todo> callback) {
        RetrofitClient.getApiService().createTodo(todo).enqueue(new Callback<Todo>() {
            @Override
            public void onResponse(Call<Todo> call, Response<Todo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Todo created = response.body();
                    synchronized (TodoRepository.this) {
                        int assignedId = customIdCounter++;
                        created.setId(assignedId);
                        cachedTodos.add(0, created);
                    }
                    callback.onSuccess(created);
                } else {
                    callback.onError("Failed to add task. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Todo> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized void deleteTodo(int id, RepositoryCallback<Void> callback) {
        RetrofitClient.getApiService().deleteTodo(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    synchronized (TodoRepository.this) {
                        for (int i = 0; i < cachedTodos.size(); i++) {
                            if (cachedTodos.get(i).getId() == id) {
                                cachedTodos.remove(i);
                                break;
                            }
                        }
                    }
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed to delete task. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public synchronized List<Todo> getCachedTodos() {
        return new ArrayList<>(cachedTodos);
    }
}

