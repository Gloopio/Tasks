package io.gloop.tasks;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import io.gloop.Gloop;
import io.gloop.permissions.GloopGroup;
import io.gloop.permissions.GloopPermission;
import io.gloop.tasks.dialogs.TaskMembersDialog;
import io.gloop.tasks.model.Task;
import io.gloop.tasks.model.UserInfo;
import io.gloop.tasks.utils.ColorUtil;
import io.gloop.tasks.utils.NameUtil;

public class TaskDetailFragment extends Fragment {

    public static final String ARG_BOARD = "board";
    public static final String ARG_USER_INFO = "userInfo";

    private Task task;
    private UserInfo userInfo;

    private EditText title;
    private EditText text;

    public TaskDetailFragment() {
        // Mandatory empty constructor for the fragment manager to instantiate the fragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userInfo = (UserInfo) getArguments().getSerializable(ARG_USER_INFO);

        if (getArguments().containsKey(TaskDetailFragment.ARG_BOARD))
            task = (Task) getArguments().getSerializable(ARG_BOARD);
        else {
            // create a group
            GloopGroup group = new GloopGroup();
            if (userInfo != null)
                group.addMember(userInfo.getEmail());
            else
                group.addMember(Gloop.getOwner().getUserId());
            group.save();

            // create new task
            task = new Task();
            task.setUser(group.getObjectId());
            task.setColor(ColorUtil.getColorByName(getContext(), NameUtil.randomObject(getContext())));
            task.save();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task, container, false);
        setHasOptionsMenu(true);

        title = (EditText) rootView.findViewById(R.id.title);
        title.setText(task.getTitle());

        text = (EditText) rootView.findViewById(R.id.text);
        text.setText(task.getContent());

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:

                task.setTitle(title.getText().toString());
                task.setContent(text.getText().toString());
                task.save();

                getActivity().finish();
                break;

            case R.id.action_members:
                new TaskMembersDialog(getActivity(), userInfo, task);
                break;

            case R.id.action_delete:
                if (task.getPermission() != GloopPermission.WRITE)
                    Snackbar.make(getActivity().findViewById(R.id.item_detail_root), "Your not allowed to delete the task.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                else {
                    task.delete();
                    getActivity().finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
