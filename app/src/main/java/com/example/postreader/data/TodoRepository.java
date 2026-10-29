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

    private synchronized void updateCachedTodo(Todo todo) {
        for (int i = 0; i < cachedTodos.size(); i++) {
            if (cachedTodos.get(i).getId() == todo.getId()) {
                cachedTodos.set(i, todo);
                break;
            }
        }
    }

    public synchronized void toggleTodo(Todo todo, RepositoryCallback<Todo> callback) {
        boolean originalStatus = todo.isCompleted();
        boolean newStatus = !originalStatus;
        todo.setCompleted(newStatus);
        updateCachedTodo(todo);

        // JSONPlaceholder adalah mock REST API dengan 200 data statis.
        // Item baru (id > 200) tidak disimpan di disk server, sehingga PUT/PATCH
        // dapat memicu HTTP 500 pada backend json-server. Kita sinkronkan di cache lokal.
        if (todo.getId() > 200) {
            callback.onSuccess(todo);
            return;
        }

        // Untuk item statis server, gunakan PATCH (metode resmi untuk partial update field completed)
        RetrofitClient.getApiService().patchTodo(todo.getId(), todo).enqueue(new Callback<Todo>() {
            @Override
            public void onResponse(Call<Todo> call, Response<Todo> response) {
                if (response.isSuccessful() || response.code() == 500) {
                    // Jika server mock merespons sukses atau status 500 karena mock limitation, pertahankan perubahan lokal
                    callback.onSuccess(todo);
                } else {
                    todo.setCompleted(originalStatus);
                    updateCachedTodo(todo);
                    callback.onError("Gagal mengubah status tugas. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Todo> call, Throwable t) {
                todo.setCompleted(originalStatus);
                updateCachedTodo(todo);
                callback.onError(t.getMessage() != null ? t.getMessage() : "Kesalahan jaringan");
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
                    callback.onError("Gagal menambahkan tugas. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Todo> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Kesalahan jaringan");
            }
        });
    }

    public synchronized void deleteTodo(int id, RepositoryCallback<Void> callback) {
        synchronized (TodoRepository.this) {
            for (int i = 0; i < cachedTodos.size(); i++) {
                if (cachedTodos.get(i).getId() == id) {
                    cachedTodos.remove(i);
                    break;
                }
            }
        }

        if (id > 200) {
            callback.onSuccess(null);
            return;
        }

        RetrofitClient.getApiService().deleteTodo(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 500) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Gagal menghapus tugas. Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Kesalahan jaringan");
            }
        });
    }

    public synchronized List<Todo> getCachedTodos() {
        return new ArrayList<>(cachedTodos);
    }
}

