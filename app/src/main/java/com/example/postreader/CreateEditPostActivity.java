package com.example.postreader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.postreader.Model.Post;
import com.example.postreader.data.PostRepository;
import com.example.postreader.data.RepositoryCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateEditPostActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_POST_ID = "extra_post_id";
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_BODY = "extra_body";

    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;

    private int mode = MODE_CREATE;
    private int postId = 0;
    private int userId = 1;

    private TextInputLayout tilTitle;
    private TextInputEditText etTitle;
    private TextInputLayout tilBody;
    private TextInputEditText etBody;
    private MaterialButton btnSubmit;

    private final PostRepository postRepository = PostRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_edit_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        readIntentData();
    }

    private void initViews() {
        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView tvScreenTitle = findViewById(R.id.tvScreenTitle);
        btnSubmit = findViewById(R.id.btnSubmit);
        tilTitle = findViewById(R.id.tilTitle);
        etTitle = findViewById(R.id.etTitle);
        tilBody = findViewById(R.id.tilBody);
        etBody = findViewById(R.id.etBody);

        btnClose.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitForm());
    }

    private void readIntentData() {
        Intent intent = getIntent();
        mode = intent.getIntExtra(EXTRA_MODE, MODE_CREATE);

        TextView tvScreenTitle = findViewById(R.id.tvScreenTitle);

        if (mode == MODE_EDIT) {
            tvScreenTitle.setText(R.string.title_edit_post);
            btnSubmit.setText(R.string.action_save);

            postId = intent.getIntExtra(EXTRA_POST_ID, 0);
            userId = intent.getIntExtra(EXTRA_USER_ID, 1);
            String title = intent.getStringExtra(EXTRA_TITLE);
            String body = intent.getStringExtra(EXTRA_BODY);

            if (title != null) {
                etTitle.setText(title);
            }
            if (body != null) {
                etBody.setText(body);
            }
        } else {
            tvScreenTitle.setText(R.string.title_create_post);
            btnSubmit.setText(R.string.action_publish);
        }
    }

    private void submitForm() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String body = etBody.getText() != null ? etBody.getText().toString().trim() : "";

        tilTitle.setError(null);
        tilBody.setError(null);

        boolean isValid = true;
        if (title.isEmpty()) {
            tilTitle.setError(getString(R.string.error_empty_title));
            isValid = false;
        }
        if (body.isEmpty()) {
            tilBody.setError(getString(R.string.error_empty_body));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        btnSubmit.setEnabled(false);

        if (mode == MODE_CREATE) {
            Post newPost = new Post(userId, 0, title, body);
            postRepository.addPost(newPost, new RepositoryCallback<Post>() {
                @Override
                public void onSuccess(Post result) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateEditPostActivity.this, R.string.post_created_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(CreateEditPostActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Post updatedPost = new Post(userId, postId, title, body);
            postRepository.updatePost(updatedPost, new RepositoryCallback<Post>() {
                @Override
                public void onSuccess(Post result) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateEditPostActivity.this, R.string.post_updated_success, Toast.LENGTH_SHORT).show();
                        Intent data = new Intent();
                        data.putExtra(EXTRA_TITLE, title);
                        data.putExtra(EXTRA_BODY, body);
                        setResult(RESULT_OK, data);
                        finish();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(CreateEditPostActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
}

