package io.gloop.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.exceptions.GloopLoadException;
import io.gloop.permissions.GloopUser;
import io.gloop.tasks.dialogs.BoardInfoDialog;
import io.gloop.tasks.model.Task;
import io.gloop.tasks.model.UserInfo;

public class ListFragment extends Fragment {

    private final static String SELECTED = "selected";
    private final static String NOT_SELECTED = "notSelected";

    private Context context;
    private GloopUser owner;
    private TaskAdapter taskAdapter;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static final int VIEW_OPEN_TASKS = 0;
    public static final int VIEW_CLOSED_TASKS = 1;

    private int operation;
    private UserInfo userInfo;

    public static ListFragment newInstance(int operation, UserInfo userinfo, GloopUser owner) {
        ListFragment f = new ListFragment();
        Bundle args = new Bundle();
        args.putInt("operation", operation);
        args.putSerializable("userinfo", userinfo);
        args.putSerializable("owner", owner);
        f.setArguments(args);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout rv = (RelativeLayout) inflater.inflate(R.layout.fragment_list, container, false);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        operation = args.getInt("operation", 0);
        userInfo = (UserInfo) args.getSerializable("userinfo");
        this.owner = (GloopUser) args.getSerializable("owner");

        recyclerView = (RecyclerView) rv.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    ((TaskListActivity) getActivity()).setFABVisibility(View.INVISIBLE);
                } else {
                    ((TaskListActivity) getActivity()).setFABVisibility(View.VISIBLE);
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        this.context = getContext();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rv.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setupRecyclerView();
            }
        });

        return rv;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                taskAdapter.filter(s);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                setupRecyclerView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadTasksTask extends AsyncTask<Void, Integer, GloopList<Task>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mSwipeRefreshLayout == null) {
                if (getView() != null) {
                    mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
                    mSwipeRefreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6);
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }

        }

        @Override
        protected GloopList<Task> doInBackground(Void... urls) {
            GloopList<Task> all = null;
            if (operation == VIEW_OPEN_TASKS)
                all = Gloop.all(Task.class).where().equalsTo("done", false).all();
            else
                all = Gloop.all(Task.class).where().equalsTo("done", true).all();

            all.load();
            return all;
        }


        @Override
        protected void onPostExecute(GloopList<Task> tasks) {
            super.onPostExecute(tasks);
            try {
                taskAdapter = new TaskAdapter(tasks);
                recyclerView.setAdapter(taskAdapter);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void setupRecyclerView() {
        new LoadTasksTask().execute();
    }

    public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.BoardViewHolder> {

        private ArrayList<Task> list;
        private final GloopList<Task> originalList;

        TaskAdapter(final GloopList<Task> tasks) {
            originalList = tasks;
            list = (ArrayList<Task>) tasks.getLocalCopy();
            Collections.sort(list, Collections.reverseOrder(new Comparator<Task>() {
                @Override
                public int compare(Task left, Task right) {
                    return Long.compare(left.getTimestamp(), right.getTimestamp());
                }
            }));
        }

        @Override
        public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new BoardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final BoardViewHolder holder, int position) {
            final Task task = list.get(position);

            holder.mContentView.setText(task.getTitle());
            final int color = task.getColor();

            holder.mImage.setBackgroundColor(color);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, TaskDetailActivity.class);
                    intent.putExtra(TaskDetailFragment.ARG_BOARD, task);
                    intent.putExtra(TaskDetailFragment.ARG_USER_INFO, userInfo);
                    context.startActivity(intent);
                }
            });

            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    int mWidth = getResources().getDisplayMetrics().widthPixels;
                    int mHeight = getResources().getDisplayMetrics().heightPixels;


                    new BoardInfoDialog(context, owner, task, userInfo, mHeight / 2, mWidth / 2);
                    setupRecyclerView();
                    return true;
                }
            });

            if (task.isDone()) {
                holder.mTaskDone.setColorFilter(getContext().getResources().getColor(R.color.Yellow));
                holder.mTaskDone.setTag(SELECTED);
            } else {
                holder.mTaskDone.setColorFilter(getContext().getResources().getColor(R.color.Gray));
                holder.mTaskDone.setTag(NOT_SELECTED);
            }
            holder.mTaskDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.mTaskDone.getTag().equals(NOT_SELECTED)) {
                        holder.mTaskDone.setColorFilter(getContext().getResources().getColor(R.color.Yellow));
                        holder.mTaskDone.setTag(SELECTED);
                        task.setDone(true);
                    } else {
                        holder.mTaskDone.setColorFilter(getContext().getResources().getColor(R.color.Gray));
                        holder.mTaskDone.setTag(NOT_SELECTED);
                        task.setDone(false);
                    }
                    task.save();
                    setupRecyclerView();
                    userInfo.saveInBackground();
                }
            });

        }

        @Override
        public int getItemCount() {
            try {
                if (list != null)
                    return list.size();
                else
                    return 0;
            } catch (GloopLoadException e) {
                e.printStackTrace();
                return 0;
            }
        }

        void filter(String s) {
            if (s.equals("")) {
                list = (ArrayList<Task>) originalList.getLocalCopy();
            } else {
                String search = s.toLowerCase();
                for (Task task : originalList) {
                    if (!task.getTitle().toLowerCase().startsWith(search))
                        list.remove(task);
                }
            }

            notifyDataSetChanged();
        }

        class BoardViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mContentView;
            //            final TextView mLines;
            final ImageView mImage;
            final ImageView mTaskDone;

            final List<CircleImageView> memberImages = new ArrayList<>();


            BoardViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.card_view);
                mContentView = (TextView) view.findViewById(R.id.board_name);
                mImage = (ImageView) view.findViewById(R.id.avatar);
                mTaskDone = (ImageView) view.findViewById(R.id.task_done);

                memberImages.add((CircleImageView) view.findViewById(R.id.user_image1));
                memberImages.add((CircleImageView) view.findViewById(R.id.user_image2));
                memberImages.add((CircleImageView) view.findViewById(R.id.user_image3));
                memberImages.add((CircleImageView) view.findViewById(R.id.user_image4));
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}