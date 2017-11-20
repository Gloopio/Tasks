package io.gloop.tasks.deeplink;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import io.gloop.tasks.R;

/**
 * Created by Alex Untertrifaller on 09.05.17.
 */
public class DeepLinkActivity extends Activity {

    public static final String BASE_DEEP_LINK = "app://tasks.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            if (segments.size() >= 1) {
                String parameter1 = segments.get(0);
                showPopup(parameter1);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    // opens a dialog on long press on the list item
    private void showPopup(final String boardName) {

        final Dialog dialog = new Dialog(DeepLinkActivity.this, R.style.AppTheme_PopupTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_invire_request);

        TextView tvBoardName = (TextView) dialog.findViewById(R.id.dialog_invite_request_text);
        tvBoardName.setText(String.format("Do you want to access the board %s?", boardName));

        Button buttonDeny = (Button) dialog.findViewById(R.id.dialog_invite_request_deny);
        buttonDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                DeepLinkActivity.this.finish();
            }
        });

        final Context context = getApplicationContext();

        Button acceptButton = (Button) dialog.findViewById(R.id.dialog_invite_request_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

//                new AsyncTask<Void, Void, Task>() {
//
//                    private ProgressDialog progress;
//                    private UserInfo userInfo;
//                    private String errorMessage;
//
//                    @Override
//                    protected void onPreExecute() {
//                        super.onPreExecute();
//                        progress = new ProgressDialog(DeepLinkActivity.this);
//                        progress.setTitle("Loading");
//                        progress.setMessage("Wait while loading lines.");
//                        progress.setCancelable(false);
//                        progress.show();
//                    }
//
//                    @Override
//                    protected Task doInBackground(Void... voids) {
//                        SharedPreferencesStore.setContext(context);
//
//                        Gloop.initialize(DeepLinkActivity.this);
//
//                        if (Gloop.login(SharedPreferencesStore.getEmail(), SharedPreferencesStore.getPassword())) {
//
//                            userInfo = Gloop.allLocal(UserInfo.class)
//                                    .where()
//                                    .equalsTo("email", Gloop.getOwner().getName())
//                                    .first();
//
//                            Task task = Gloop.all(Task.class).where().equalsTo("name", boardName).first();
//                            if (task != null && !task.isPrivateTask()) {
//                                GloopGroup group = Gloop
//                                        .all(GloopGroup.class)
//                                        .where()
//                                        .equalsTo("objectId", task.getOwner())
//                                        .first();
//
//                                if (group != null) {
//                                    GloopLogger.i("GloopGroup found add myself to group and save");
//                                    group.addMember(Gloop.getOwner().getUserId());
//                                    group.save();
//                                } else {
//                                    GloopLogger.e("GloopGroup not found!");
//                                }
//                                task.save();
//                                return Gloop.all(Task.class).where().equalsTo("objectId", task.getBoardId()).first();
//                            } else {
//                                // if the board is not public check the PrivateTaskRequest objects.
//
//                                PrivateTaskRequest privateBoard = Gloop
//                                        .all(PrivateTaskRequest.class)
//                                        .where()
//                                        .equalsTo("boardName", boardName)
//                                        .first();
//
//                                if (privateBoard != null) {
//                                    // request access to private board with the TaskAccessRequest object.
//                                    TaskAccessRequest request = new TaskAccessRequest();
//                                    request.setUser(privateBoard.getBoardCreator(), PUBLIC | READ | WRITE);
//                                    request.setBoardName(boardName);
//                                    request.setBoardCreator(privateBoard.getBoardCreator());
//                                    request.setUserId(Gloop.getOwner().getUserId());
//                                    request.setBoardGroupId(privateBoard.getGroupId());
//                                    if (userInfo.getImageURL() != null)
//                                        request.setUserImageUri(userInfo.getImageURL().toString());
//                                    request.save();
//                                } else {
//                                    GloopLogger.i("Could not find public board with name: " + boardName);
//                                }
//                            }
//                            errorMessage = "Request to access private board is send.";
//                            return null;
//
//                        } else {
//                            errorMessage = "Could not find the board.";
//                            return null;
//                        }
//                    }
//
//                    @Override
//                    protected void onPostExecute(Task task) {
//                        super.onPostExecute(task);
//
//                        if (task != null) {
//                            Intent intent = new Intent(getApplicationContext(), TaskDetailActivity.class);
//                            intent.putExtra(TaskDetailFragment.ARG_BOARD, task);
//                            intent.putExtra(TaskDetailFragment.ARG_USER_INFO, userInfo);
//                            startActivity(intent);
//
//                            Toast.makeText(getApplicationContext(), "Task added to your list.", Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
//                        }
//
//                        progress.dismiss();
//
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
//                        dialog.dismiss();
//                        DeepLinkActivity.this.finish();
////                            }
////                        });
//                    }
//                }.execute();


            }
        });

        dialog.show();

    }
}