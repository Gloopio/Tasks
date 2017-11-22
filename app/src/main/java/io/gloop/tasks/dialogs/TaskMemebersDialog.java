package io.gloop.tasks.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.permissions.GloopGroup;
import io.gloop.tasks.R;
import io.gloop.tasks.model.Task;
import io.gloop.tasks.model.UserInfo;

/**
 * Created by Alex Untertrifaller on 21.09.17.
 */

public class TaskMemebersDialog {

    private Activity activity;
    private MyRecyclerViewAdapter adapter;

    public TaskMemebersDialog(Activity activity, UserInfo userInfo, Task task) {
        this.activity = activity;
        final View dialogView = View.inflate(activity, R.layout.dialog_task_members, null);

        final Dialog dialog = new Dialog(activity, R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.closeDialogImg);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog);
            }
        });

        // load members of task
        String groupId = task.getGloopUser();
        final GloopGroup group = Gloop.all(GloopGroup.class).where().equalsTo("objectId", groupId).first();
        List<String> members = group.getMembers();


        // set up the RecyclerView
        final RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.member_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new MyRecyclerViewAdapter(activity, members, group);
        recyclerView.setAdapter(adapter);


        final EditText newMember = (EditText) dialog.findViewById(R.id.member_new);
        Button addMember = (Button) dialog.findViewById(R.id.member_add);
        addMember.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String memberId = newMember.getText().toString();
                group.addMember(memberId);
                group.save();
                adapter.notifyDataSetChanged();
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

    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<String> mData = Collections.emptyList();
        private LayoutInflater mInflater;
        private GloopGroup group;

        // data is passed into the constructor
        public MyRecyclerViewAdapter(Context context, List<String> data, GloopGroup group) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
            this.group = group;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_member, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the textview in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final String memberId = mData.get(position);
            holder.myTextView.setText(memberId);
            holder.removeMember.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    group.getMembers().remove(memberId);
                    group.save();
                    adapter.notifyDataSetChanged();
                }
            });
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }


        // stores and recycles views as they are scrolled off screen
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView myTextView;
            ImageView removeMember;

            ViewHolder(View itemView) {
                super(itemView);
                myTextView = (TextView) itemView.findViewById(R.id.tvAnimalName);
                removeMember = (ImageView) itemView.findViewById(R.id.member_remove);
            }
        }

        public String getItem(int id) {
            return mData.get(id);
        }
    }

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {

        final View view = dialogView.findViewById(R.id.dialog);

        int w = view.getWidth();
        int h = view.getHeight();

        int endRadius = (int) Math.hypot(w, h);

        int mWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int mHeight = activity.getResources().getDisplayMetrics().heightPixels;

        int cx = mWidth / 2;
        int cy = mHeight / 2;


        if (b) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);

            view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(700);
            revealAnimator.start();

        } else {

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);

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

    private static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        GloopLogger.e("Was not able to restart application, mStartActivity null");
                    }
                } else {
                    GloopLogger.e("Was not able to restart application, PM null");
                }
            } else {
                GloopLogger.e("Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            GloopLogger.e("Was not able to restart application");
        }
    }
}
