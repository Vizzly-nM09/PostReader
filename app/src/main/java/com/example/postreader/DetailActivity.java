package com.example.postreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.postreader.Helpers.NetworkUtils;
import com.example.postreader.Model.Comment;
import com.example.postreader.Model.Post;
import com.example.postreader.Model.User;
import com.example.postreader.adapter.CommentAdapter;
import com.example.postreader.data.PostRepository;
import com.example.postreader.data.RepositoryCallback;
import com.example.postreader.network.RetrofitClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private int postId;
    private Post currentPost;

    private View scrollContent;
    private View loadingGroup;
    private View stateGroup;
    private TextView tvStateMessage;
    private View btnRetry;

    private View cardAuthor;
    private TextView tvAuthorInitials;
    private TextView tvAuthorName;
    private TextView tvAuthorMeta;

    private TextView tvTitle;
    private TextView tvBody;

    private TextView tvCommentsHeader;
    private ProgressBar pbComments;
    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;

    private final PostRepository postRepository = PostRepository.getInstance();

    private final ActivityResultLauncher<Intent> editPostLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String updatedTitle = result.getData().getStringExtra(CreateEditPostActivity.EXTRA_TITLE);
                    String updatedBody = result.getData().getStringExtra(CreateEditPostActivity.EXTRA_BODY);
                    if (currentPost != null) {
                        currentPost.setTitle(updatedTitle);
                        currentPost.setBody(updatedBody);
                        bindPost(currentPost);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        postId = getIntent().getIntExtra(EXTRA_POST_ID, 0);
        if (postId <= 0) {
            btnRetry.setVisibility(View.GONE);
            showState(getString(R.string.error_missing_post));
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.post_number, postId));
        }

        btnRetry.setOnClickListener(v -> loadPostData());

        loadPostData();
    }

    private void initViews() {
        scrollContent = findViewById(R.id.scrollContent);
        loadingGroup = findViewById(R.id.loadingGroup);
        stateGroup = findViewById(R.id.stateGroup);
        tvStateMessage = findViewById(R.id.tvStateMessage);
        btnRetry = findViewById(R.id.btnRetry);

        cardAuthor = findViewById(R.id.cardAuthor);
        tvAuthorInitials = findViewById(R.id.tvAuthorInitials);
        tvAuthorName = findViewById(R.id.tvAuthorName);
        tvAuthorMeta = findViewById(R.id.tvAuthorMeta);

        tvTitle = findViewById(R.id.tvTitle);
        tvBody = findViewById(R.id.tvBody);

        tvCommentsHeader = findViewById(R.id.tvCommentsHeader);
        pbComments = findViewById(R.id.pbComments);
        rvComments = findViewById(R.id.rvComments);

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(null);
        rvComments.setAdapter(commentAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            if (currentPost != null) {
                Intent intent = new Intent(this, CreateEditPostActivity.class);
                intent.putExtra(CreateEditPostActivity.EXTRA_MODE, CreateEditPostActivity.MODE_EDIT);
                intent.putExtra(CreateEditPostActivity.EXTRA_POST_ID, currentPost.getId());
                intent.putExtra(CreateEditPostActivity.EXTRA_USER_ID, currentPost.getUserId());
                intent.putExtra(CreateEditPostActivity.EXTRA_TITLE, currentPost.getTitle());
                intent.putExtra(CreateEditPostActivity.EXTRA_BODY, currentPost.getBody());
                editPostLauncher.launch(intent);
            }
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deletePost())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void deletePost() {
        postRepository.deletePost(postId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, getString(R.string.post_deleted_success, postId), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadPostData() {
        currentPost = postRepository.getPostById(postId);
        if (currentPost != null) {
            bindPost(currentPost);
            showContent();
            loadAuthor(currentPost.getUserId());
            loadComments(currentPost.getId());
            return;
        }

        if (!NetworkUtils.isOnline(this)) {
            showState(getString(R.string.error_offline));
            return;
        }

        showLoading();

        RetrofitClient.getApiService().getPostById(postId).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                runOnUiThread(() -> {
                    if (!response.isSuccessful() || response.body() == null) {
                        showState(getString(R.string.error_http, response.code()));
                        return;
                    }
                    currentPost = response.body();
                    bindPost(currentPost);
                    showContent();
                    loadAuthor(currentPost.getUserId());
                    loadComments(currentPost.getId());
                });
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                runOnUiThread(() -> showState(getString(R.string.error_network)));
            }
        });
    }

    private void bindPost(Post post) {
        tvTitle.setText(post.getTitle());
        tvBody.setText(post.getBody());
    }

    private void loadAuthor(int userId) {
        int targetUserId = userId > 0 ? userId : 1;
        RetrofitClient.getApiService().getUserById(targetUserId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    runOnUiThread(() -> {
                        cardAuthor.setVisibility(View.VISIBLE);
                        tvAuthorInitials.setText(user.getInitials());
                        tvAuthorName.setText(user.getName());

                        String companyPart = user.getCompany() != null && !user.getCompany().getName().isEmpty()
                                ? " · " + user.getCompany().getName()
                                : "";
                        tvAuthorMeta.setText(getString(R.string.author_handle, user.getUsername()) + companyPart);
                    });
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // If author fails to load, gracefully omit the card without blocking article reading
            }
        });
    }

    private void loadComments(int postId) {
        pbComments.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getCommentsByPostId(postId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                runOnUiThread(() -> {
                    pbComments.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Comment> comments = response.body();
                        tvCommentsHeader.setText(getString(R.string.section_discussion, comments.size()));
                        commentAdapter.setComments(comments);
                    } else {
                        tvCommentsHeader.setText(getString(R.string.section_discussion, 0));
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                runOnUiThread(() -> {
                    pbComments.setVisibility(View.GONE);
                    tvCommentsHeader.setText(getString(R.string.section_discussion, 0));
                });
            }
        });
    }

    private void showLoading() {
        loadingGroup.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);
        stateGroup.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingGroup.setVisibility(View.GONE);
        stateGroup.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
    }

    private void showState(String message) {
        loadingGroup.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        stateGroup.setVisibility(View.VISIBLE);
        tvStateMessage.setText(message);
    }
}
