package com.example.postreader.data;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}

