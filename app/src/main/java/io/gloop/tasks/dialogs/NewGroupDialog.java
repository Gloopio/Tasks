package io.gloop.tasks.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionMenu;

import java.util.Objects;

import io.gloop.permissions.GloopUser;
import io.gloop.tasks.R;
import io.gloop.tasks.model.TaskGroup;
import io.gloop.tasks.model.UserInfo;
import io.gloop.tasks.utils.NameUtil;

/**
 * Created by Alex Untertrifaller on 09.06.17.
 */
public class NewGroupDialog {

    private FloatingActionMenu fab;

    public NewGroupDialog(@NonNull final Context context, final GloopUser owner, final View view, final FragmentManager fragmentManager, FloatingActionMenu fab, final UserInfo userInfo) {
        this.fab = fab;

        final View dialogView = View.inflate(context, R.layout.dialog_new_board, null);

        final Dialog dialog = new Dialog(context, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);


        final String colorName = NameUtil.randomColor(context);

        final EditText etBoardName = (EditText) dialog.findViewById(R.id.dialog_new_board_board_name);
        etBoardName.getBackground().setColorFilter(context.getResources().getColor(R.color.edit_text_color), PorterDuff.Mode.SRC_IN);

        Button generateButton = (Button) dialog.findViewById(R.id.dialog_new_board_btn_generate_name);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String boardName = NameUtil.randomAdjective(context) + colorName + NameUtil.randomObject(context);
                etBoardName.setText(boardName);
            }
        });

        Button closeButton = (Button) dialog.findViewById(R.id.dialog_new_board_btn_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revealShow(dialogView, false, dialog);
            }
        });

        Button saveButton = (Button) dialog.findViewById(R.id.dialog_new_board_btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncTask<Void, Void, TaskGroup>() {

                    private ProgressDialog progress;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        progress = new ProgressDialog(context);
                        progress.setTitle("Creating new task");
                        progress.setMessage("Wait while loading...");
                        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                        progress.show();
                    }

                    @Override
                    protected TaskGroup doInBackground(Void... voids) {
                        final TaskGroup group = new TaskGroup();

                        if (!Objects.equals(etBoardName.getText().toString(), ""))
                            group.setName(etBoardName.getText().toString());
                        else
                            group.setName(NameUtil.randomAdjective(context) + colorName + NameUtil.randomObject(context));

                        group.save();

                        return group;
                    }

                    @Override
                    protected void onPostExecute(TaskGroup task) {
                        super.onPostExecute(task);
                        Context context = view.getContext();
//                        Intent intent = new Intent(context, TaskDetailActivity.class);
//                        intent.putExtra(TaskDetailFragment.ARG_BOARD, task);
//                        intent.putExtra(TaskDetailFragment.ARG_USER_INFO, userInfo);

//                        context.startActivity(intent);

                        progress.dismiss();

                        // close popup
                        dialog.dismiss();
                    }
                }.execute();
            }
        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_new_board_closeDialogImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                revealShow(dialogView, true, null);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {

                    revealShow(dialogView, false, dialog);
                    return true;
                }

                return false;
            }
        });

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.pop_new_board);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = fab.getRight() - 100;
        int cy = fab.getBottom() - 300;

        if (b) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);

            view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(700);
            revealAnimator.start();

        } else {

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    view.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(700);
            anim.start();
        }
    }
}
