package com.example.postreader;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.postreader.Helpers.NetworkUtils;
import com.example.postreader.Model.Post;
import com.example.postreader.Model.Todo;
import com.example.postreader.adapter.PostAdapter;
import com.example.postreader.adapter.TodoAdapter;
import com.example.postreader.data.PostRepository;
import com.example.postreader.data.RepositoryCallback;
import com.example.postreader.data.TodoRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private enum Tab {
        POSTS,
        TODOS
    }

    private Tab currentTab = Tab.POSTS;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvPosts;
    private RecyclerView rvTodos;
    private View loadingGroup;
    private TextView tvLoading;
    private View stateGroup;
    private TextView tvStateTitle;
    private TextView tvStateMessage;
    private EditText etSearch;
    private ImageButton btnClearSearch;
    private ChipGroup chipGroupTodos;
    private FloatingActionButton fabAction;
    private BottomNavigationView bottomNav;

    private PostAdapter postAdapter;
    private TodoAdapter todoAdapter;

    private final PostRepository postRepository = PostRepository.getInstance();
    private final TodoRepository todoRepository = TodoRepository.getInstance();

    private final ActivityResultLauncher<Intent> createPostLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshPosts(false);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupRecyclerViews();
        setupSearch();
        setupBottomNav();
        setupFilterChips();
        setupFab();

        swipeRefresh.setOnRefreshListener(() -> {
            if (currentTab == Tab.POSTS) {
                refreshPosts(true);
            } else {
                refreshTodos(true);
            }
        });

        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            if (currentTab == Tab.POSTS) {
                refreshPosts(true);
            } else {
                refreshTodos(true);
            }
        });

        refreshPosts(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTab == Tab.POSTS && postAdapter != null) {
            postAdapter.setPosts(postRepository.getCachedPosts());
        }
    }

    private void initViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvPosts = findViewById(R.id.rvPosts);
        rvTodos = findViewById(R.id.rvTodos);
        loadingGroup = findViewById(R.id.loadingGroup);
        tvLoading = findViewById(R.id.tvLoading);
        stateGroup = findViewById(R.id.stateGroup);
        tvStateTitle = findViewById(R.id.tvStateTitle);
        tvStateMessage = findViewById(R.id.tvStateMessage);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        chipGroupTodos = findViewById(R.id.chipGroupTodos);
        fabAction = findViewById(R.id.fabAction);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupRecyclerViews() {
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setHasFixedSize(true);
        postAdapter = new PostAdapter(postRepository.getCachedPosts(), this);
        rvPosts.setAdapter(postAdapter);

        rvTodos.setLayoutManager(new LinearLayoutManager(this));
        rvTodos.setHasFixedSize(true);
        todoAdapter = new TodoAdapter(todoRepository.getCachedTodos(), this, new TodoAdapter.OnTodoActionListener() {
            @Override
            public void onToggle(Todo todo) {
                todoRepository.toggleTodo(todo, new RepositoryCallback<Todo>() {
                    @Override
                    public void onSuccess(Todo result) {
                        todoAdapter.setTodos(todoRepository.getCachedTodos());
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onDelete(Todo todo) {
                todoRepository.deleteTodo(todo.getId(), new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        todoAdapter.setTodos(todoRepository.getCachedTodos());
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
        rvTodos.setAdapter(todoAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                if (currentTab == Tab.POSTS) {
                    postAdapter.filter(query);
                    checkFilteredEmpty(postAdapter.getItemCount());
                } else {
                    todoAdapter.filter(query);
                    checkFilteredEmpty(todoAdapter.getItemCount());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void checkFilteredEmpty(int count) {
        if (count == 0 && (loadingGroup.getVisibility() != View.VISIBLE)) {
            showState(getString(R.string.empty_posts), getString(R.string.empty_posts_description));
        } else if (count > 0 && stateGroup.getVisibility() == View.VISIBLE) {
            showContent();
        }
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_posts) {
                switchToPosts();
                return true;
            } else if (itemId == R.id.menu_todos) {
                switchToTodos();
                return true;
            }
            return false;
        });
    }

    private void switchToPosts() {
        currentTab = Tab.POSTS;
        chipGroupTodos.setVisibility(View.GONE);
        rvTodos.setVisibility(View.GONE);
        fabAction.setContentDescription(getString(R.string.action_create_post));

        postAdapter.filter(etSearch.getText().toString());
        if (postRepository.getCachedPosts().isEmpty()) {
            refreshPosts(false);
        } else {
            showContent();
        }
    }

    private void switchToTodos() {
        currentTab = Tab.TODOS;
        chipGroupTodos.setVisibility(View.VISIBLE);
        rvPosts.setVisibility(View.GONE);
        fabAction.setContentDescription(getString(R.string.action_add_task));

        todoAdapter.filter(etSearch.getText().toString());
        if (todoRepository.getCachedTodos().isEmpty()) {
            refreshTodos(false);
        } else {
            showContent();
        }
    }

    private void setupFilterChips() {
        chipGroupTodos.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                todoAdapter.setFilter(TodoAdapter.FilterStatus.ALL);
            } else if (checkedId == R.id.chipPending) {
                todoAdapter.setFilter(TodoAdapter.FilterStatus.PENDING);
            } else if (checkedId == R.id.chipCompleted) {
                todoAdapter.setFilter(TodoAdapter.FilterStatus.COMPLETED);
            }
            checkFilteredEmpty(todoAdapter.getItemCount());
        });
    }

    private void setupFab() {
        fabAction.setOnClickListener(v -> {
            if (currentTab == Tab.POSTS) {
                Intent intent = new Intent(MainActivity.this, CreateEditPostActivity.class);
                intent.putExtra(CreateEditPostActivity.EXTRA_MODE, CreateEditPostActivity.MODE_CREATE);
                createPostLauncher.launch(intent);
            } else {
                showAddTodoDialog();
            }
        });
    }

    private void showAddTodoDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_todo, null);
        TextInputEditText etTodoTitle = dialogView.findViewById(R.id.etTodoTitle);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_add_todo_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_add_task, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = etTodoTitle.getText() != null ? etTodoTitle.getText().toString().trim() : "";
                if (title.isEmpty()) {
                    etTodoTitle.setError(getString(R.string.error_empty_todo));
                    return;
                }

                Todo newTodo = new Todo(1, 0, title, false);
                todoRepository.addTodo(newTodo, new RepositoryCallback<Todo>() {
                    @Override
                    public void onSuccess(Todo result) {
                        runOnUiThread(() -> {
                            todoAdapter.setTodos(todoRepository.getCachedTodos());
                            showContent();
                            Toast.makeText(MainActivity.this, R.string.todo_added_success, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                    }
                });
            });
        });

        dialog.show();
    }

    private void refreshPosts(boolean forceRefresh) {
        if (!NetworkUtils.isOnline(this) && postRepository.getCachedPosts().isEmpty()) {
            swipeRefresh.setRefreshing(false);
            showState(getString(R.string.error_offline), getString(R.string.error_network));
            return;
        }

        if (postRepository.getCachedPosts().isEmpty()) {
            showLoading(getString(R.string.loading_posts));
        }

        postRepository.fetchPosts(forceRefresh, new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    postAdapter.setPosts(result);
                    if (result.isEmpty()) {
                        showState(getString(R.string.empty_posts), getString(R.string.empty_posts_description));
                    } else {
                        showContent();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    if (postRepository.getCachedPosts().isEmpty()) {
                        showState(getString(R.string.error_network), message);
                    } else {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void refreshTodos(boolean forceRefresh) {
        if (!NetworkUtils.isOnline(this) && todoRepository.getCachedTodos().isEmpty()) {
            swipeRefresh.setRefreshing(false);
            showState(getString(R.string.error_offline), getString(R.string.error_network));
            return;
        }

        if (todoRepository.getCachedTodos().isEmpty()) {
            showLoading(getString(R.string.loading_todos));
        }

        todoRepository.fetchTodos(forceRefresh, new RepositoryCallback<List<Todo>>() {
            @Override
            public void onSuccess(List<Todo> result) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    todoAdapter.setTodos(result);
                    if (result.isEmpty()) {
                        showState(getString(R.string.empty_todos), getString(R.string.empty_todos_description));
                    } else {
                        showContent();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    if (todoRepository.getCachedTodos().isEmpty()) {
                        showState(getString(R.string.error_network), message);
                    } else {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showLoading(String message) {
        tvLoading.setText(message);
        loadingGroup.setVisibility(View.VISIBLE);
        rvPosts.setVisibility(View.GONE);
        rvTodos.setVisibility(View.GONE);
        stateGroup.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingGroup.setVisibility(View.GONE);
        stateGroup.setVisibility(View.GONE);
        if (currentTab == Tab.POSTS) {
            rvPosts.setVisibility(View.VISIBLE);
            rvTodos.setVisibility(View.GONE);
        } else {
            rvPosts.setVisibility(View.GONE);
            rvTodos.setVisibility(View.VISIBLE);
        }
    }

    private void showState(String title, String message) {
        loadingGroup.setVisibility(View.GONE);
        rvPosts.setVisibility(View.GONE);
        rvTodos.setVisibility(View.GONE);
        stateGroup.setVisibility(View.VISIBLE);
        tvStateTitle.setText(title);
        tvStateMessage.setText(message);
    }
}
