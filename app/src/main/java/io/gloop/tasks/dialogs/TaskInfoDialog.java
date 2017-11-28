package io.gloop.tasks.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.gloop.Gloop;
import io.gloop.permissions.GloopGroup;
import io.gloop.permissions.GloopUser;
import io.gloop.tasks.R;
import io.gloop.tasks.model.Task;
import io.gloop.tasks.model.UserInfo;

/**
 * Created by Alex Untertrifaller on 14.06.17.
 */

public class TaskInfoDialog {

    public TaskInfoDialog(final @NonNull Context context, final GloopUser owner, final Task task, final UserInfo userInfo, final double x, final double y) {
        final View dialogView = View.inflate(context, R.layout.dialog_info, null);

        final Dialog dialog = new Dialog(context, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);


        RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.pop_stat_view);
        layout.setBackgroundColor(task.getColor());

        TextView tvBoardName = (TextView) dialog.findViewById(R.id.dialog_info_board_name);
        tvBoardName.setText(task.getTitle());

        Button membersButton = (Button) dialog.findViewById(R.id.dialog_info_btn_add_member);

        membersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TaskMembersDialog(context, userInfo, task);

                revealShow(dialogView, false, dialog, x, y);
            }
        });


        Button deleteButton = (Button) dialog.findViewById(R.id.dialog_info_btn_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupId = task.getOwner();

                task.delete();

                GloopGroup group = Gloop.all(GloopGroup.class)
                        .where()
                        .equalsTo("objectId", groupId)
                        .first();
                if (group != null)
                    group.delete();

                revealShow(dialogView, false, dialog, x, y);
            }
        });

        ImageView imageView = (ImageView) dialog.findViewById(R.id.pop_info_board_closeDialogImg);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog, x, y);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                revealShow(dialogView, true, null, x, y);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {

                    revealShow(dialogView, false, dialog, x, y);
                    return true;
                }

                return false;
            }
        });

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }

    private void revealShow(View dialogView, boolean b, final Dialog dialog, double x, double y) {

        final View view = dialogView.findViewById(R.id.pop_stat_view);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int cx = (int) x;
        int cy = (int) y + 250;

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