package io.gloop.tasks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import io.gloop.tasks.dialogs.TaskMemebersDialog;
import io.gloop.tasks.model.Task;
import io.gloop.tasks.model.UserInfo;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;


public class TaskDetailFragment extends Fragment implements BottomNavigation.OnMenuItemSelectionListener {

    public static final String ARG_BOARD = "board";
    public static final String ARG_USER_INFO = "userInfo";

    private String currentColor = "#FF000000";
//    private BottomNavigation navigation;

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

        if (getArguments().containsKey(ARG_BOARD)) {
            task = (Task) getArguments().getSerializable(ARG_BOARD);
            userInfo = (UserInfo) getArguments().getSerializable(ARG_USER_INFO);
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

        // set default color
//        rootView.setBackgroundColor(Color.parseColor(currentColor));

//        navigation = (BottomNavigation) rootView.findViewById(R.id.BottomNavigation);
//        navigation.setOnMenuItemClickListener(this);
//        navigation.setSelectedIndex(2, true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        final MenuItem searchItem = menu.findItem(R.id.action_search);
//        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                taskAdapter.filter(s);
//                return false;
//            }
//        });
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
//                getFragmentManager().popBackStackImmediate();
                break;
            case R.id.action_members:

                new TaskMemebersDialog(getActivity(), userInfo, task);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuItemSelect(final int itemId, final int position, final boolean fromUser) {
        onDrawingMenuSelected(itemId, position, fromUser);
    }

    @Override
    public void onMenuItemReselect(final int itemId, final int position, final boolean fromUser) {
        onDrawingMenuSelected(itemId, position, fromUser);
    }

    public void onDrawingMenuSelected(final int itemId, final int position, final boolean fromUser) {
        switch (itemId) {
//            case R.id.nav_darwing_clear:
//                new ClearBoardDialog(BoardDetailFragment.this.getContext(), drawView, navigation);
//                break;
//            case R.id.nav_darwing_brush:
//                drawView.setErase(false);
//                break;
//            case R.id.nav_darwing_delete_line:
//                drawView.setErase(true);
//                break;
//            case R.id.nav_darwing_line_thickness:
//                new LineThicknessChooserDialog(BoardDetailFragment.this.getContext(), drawView, navigation);
//                break;
//            case R.id.nav_drawing_color:
//                new ColorChooserDialog(getContext(), BoardDetailFragment.this, navigation);
//                break;
        }
    }
}
