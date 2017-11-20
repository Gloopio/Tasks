/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gloop.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.gloop.Gloop;
import io.gloop.GloopList;
import io.gloop.exceptions.GloopException;
import io.gloop.exceptions.GloopLoadException;
import io.gloop.permissions.GloopUser;
import io.gloop.tasks.dialogs.AcceptBoardAccessDialog;
import io.gloop.tasks.dialogs.BoardInfoDialog;
import io.gloop.tasks.model.TaskAccessRequest;
import io.gloop.tasks.model.Task;
import io.gloop.tasks.model.TaskGroup;
import io.gloop.tasks.model.UserInfo;

public class ListFragment extends Fragment {

    private final static String SELECTED = "selected";
    private final static String NOT_SELECTED = "notSelected";

    private Context context;
    private GloopUser owner;
    private TaskAdapter taskAdapter;

    private TextView infoText;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static final int VIEW_MY_TASKS = 0;
    public static final int VIEW_GROUPS = 1;

    private int operation;
    private UserInfo userInfo;

    public static ListFragment newInstance(int operation, UserInfo userinfo, GloopUser owner) {
        ListFragment f = new ListFragment();
        // Supply index input as an argument.
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

        infoText = (TextView) rv.findViewById(R.id.fragment_list_info_text);

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
                update();

            }
        });

        return rv;
    }

    private boolean running = false;

    private void update() {
        if (!running)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    running = true;
                    Gloop.sync();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupRecyclerView();
                            checkForPrivateBoardAccessRequests();
                            running = false;
                        }
                    });

                }
            }).start();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
        checkForPrivateBoardAccessRequests();
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
                update();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkForPrivateBoardAccessRequests() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final GloopList<TaskAccessRequest> accessRequests = Gloop
                            .all(TaskAccessRequest.class)
                            .where()
                            .equalsTo("boardCreator", owner.getUserId())
                            .all();
                    if (accessRequests != null) {
                        for (final TaskAccessRequest accessRequest : accessRequests) {
                            final FragmentActivity activity = getActivity();
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            new AcceptBoardAccessDialog(activity, accessRequest).show();
                                        }
                                    }
                            );
                        }
                    }
                } catch (GloopException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // TODO impl
//    private class LoadGroupsTask extends AsyncTask<Void, Integer, GloopList<TaskGroup>> {
//
//        private String info = null;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mSwipeRefreshLayout.setRefreshing(true);
//        }
//
//        @Override
//        protected GloopList<TaskGroup> doInBackground(Void... urls) {
//            return Gloop.all(TaskGroup.class);
//        }
//
//        @Override
//        protected void onPostExecute(GloopList<TaskGroup> tasks) {
//            super.onPostExecute(tasks);
//            try {
//                taskAdapter = new TaskAdapter(tasks);
//                recyclerView.setAdapter(taskAdapter);
//                mSwipeRefreshLayout.setRefreshing(false);
//
//                if (info != null) {
//                    infoText.setText(info);
//                } else {
//                    infoText.setText("");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


    private class LoadTasksTask extends AsyncTask<Void, Integer, GloopList<Task>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected GloopList<Task> doInBackground(Void... urls) {
            GloopList<Task> all = Gloop.all(Task.class);
            all.load();
            return all;
        }


        @Override
        protected void onPostExecute(GloopList<Task> tasks) {
            super.onPostExecute(tasks);
            try {
                taskAdapter = new TaskAdapter(tasks);
                recyclerView.setAdapter(taskAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class LoadGroupsTask extends AsyncTask<Void, Integer, GloopList<TaskGroup>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected GloopList<TaskGroup> doInBackground(Void... urls) {
            GloopList<TaskGroup> all = Gloop.all(TaskGroup.class);
            all.load();
            return all;
        }


        @Override
        protected void onPostExecute(GloopList<TaskGroup> groups) {
            super.onPostExecute(groups);
            try {
                GroupAdapter groupAdapter = new GroupAdapter(groups);
                recyclerView.setAdapter(groupAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupRecyclerView() {
        if (operation == VIEW_MY_TASKS)
            new LoadTasksTask().execute();
        else
            new LoadGroupsTask().execute();
    }

    public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.BoardViewHolder> {

        private ArrayList<Task> list;
        private final GloopList<Task> originalList;

        TaskAdapter(final GloopList<Task> tasks) {
            originalList = tasks;
//                if (tasks.size() != 0) {
            list = (ArrayList<Task>) tasks.getLocalCopy();
            Collections.sort(list, Collections.reverseOrder(new Comparator<Task>() {
                @Override
                public int compare(Task left, Task right) {
                    return Long.compare(left.getTimestamp(), right.getTimestamp());
                }
            }));
//                }

//            originalList.addOnChangeListener(new GloopOnChangeListener() {
//                @Override
//                public void onChange() {
//                    list = (ArrayList<Task>) boards.getLocalCopy();
//                    notifyDataSetChanged();
//                }
//            });
        }

        @Override
        public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new BoardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final BoardViewHolder holder, int position) {
            final Task boardInfo = list.get(position);

            holder.mContentView.setText(boardInfo.getTitle());
            final int color = boardInfo.getColor();

            holder.mImage.setBackgroundColor(color);

//                holder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(final View view) {
//                        holder.mView.setClickable(false);
//                        holder.mView.setEnabled(false);
//
//                        new AsyncTask<Void, Void, Task>() {
//
//                            private ProgressDialog progress;
//
//                            @Override
//                            protected void onPreExecute() {
//                                super.onPreExecute();
//                                progress = new ProgressDialog(context);
//                                progress.setTitle(getString(R.string.loading));
//                                progress.setMessage(getString(R.string.wait_while_loading_lines));
//                                progress.setCancelable(false);
//                                progress.show();
//                            }
//
//                            @Override
//                            protected Task doInBackground(Void... voids) {
//                                return Gloop.all(Task.class)
//                                        .where()
//                                        .equalsTo(Constants.OBJECT_ID, boardInfo.getBoardId())
//                                        .first();
//                            }
//
//                            @Override
//                            protected void onPostExecute(Task task) {
//                                super.onPostExecute(task);
//                                Context context = view.getContext();
//                                Intent intent = new Intent(context, TaskDetailActivity.class);
//                                intent.putExtra(TaskDetailFragment.ARG_BOARD, task);
//                                intent.putExtra(TaskDetailFragment.ARG_USER_INFO, userInfo);
//
//                                context.startActivity(intent);
//
//                                progress.dismiss();
//                                holder.mView.setClickable(true);
//                                holder.mView.setEnabled(true);
//                            }
//                        }.execute();
//                    }
//                });
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new BoardInfoDialog(context, owner, boardInfo, userInfo, 100.0, 100.0);
                    setupRecyclerView();
                    return true;
                }
            });

//                if (userInfo.getFavoritesBoardId().contains(boardInfo.getBoardId())) {
//                    holder.mFavorite.setImageResource(R.drawable.ic_star_black_24dp);
//                    holder.mFavorite.setTag(SELECTED);
//                }
//                holder.mFavorite.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (holder.mFavorite.getTag().equals(NOT_SELECTED)) {
//                            holder.mFavorite.setImageResource(R.drawable.ic_star_black_24dp);
//                            holder.mFavorite.setTag(SELECTED);
//                            userInfo.addFavoriteBoardId(boardInfo.getBoardId());
//                        } else {
//                            holder.mFavorite.setImageResource(R.drawable.ic_star_border_black_24dp);
//                            holder.mFavorite.setTag(NOT_SELECTED);
//                            userInfo.removeFavoriteBoardId(boardInfo.getBoardId());
//                        }
//                        userInfo.saveInBackground();
//                    }
//                });

            // TODO impl
//                setMemberImages(boardInfo, holder);
        }

        private void setMemberImages(Task board, BoardViewHolder holder) {
            int count = 0;
            for (Map.Entry<String, String> entry : board.getTaskGroup().getMembers().entrySet()) {
                if (entry.getValue() != null)
                    Picasso.with(context)
                            .load(Uri.parse(entry.getValue()))
                            .into(holder.memberImages.get(count++));
                else {
                    holder.memberImages.get(count++).setImageResource(R.drawable.user_with_background);
                }
                if (count >= 4)
                    break;
            }
            if (count < 4) {
                for (int i = count; i < 4; i++) {
                    holder.memberImages.get(i).setVisibility(View.GONE);
                }
            }
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
            final TextView mLines;
            final ImageView mImage;
            final ImageView mFavorite;

            final List<CircleImageView> memberImages = new ArrayList<>();


            BoardViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.card_view);
                mContentView = (TextView) view.findViewById(R.id.board_name);
                mLines = (TextView) view.findViewById(R.id.lines);
                mImage = (ImageView) view.findViewById(R.id.avatar);
                mFavorite = (ImageView) view.findViewById(R.id.board_favorite);

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

    public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

        private ArrayList<TaskGroup> list;
        private final GloopList<TaskGroup> originalList;

        GroupAdapter(final GloopList<TaskGroup> groups) {
            originalList = groups;
            list = (ArrayList<TaskGroup>) groups.getLocalCopy();
//            Collections.sort(list, Collections.reverseOrder(new Comparator<Task>() {
//                @Override
//                public int compare(Task left, Task right) {
//                    return Long.compare(left.getTimestamp(), right.getTimestamp());
//                }
//            }));
        }

        @Override
        public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final GroupViewHolder holder, int position) {
            final TaskGroup group = list.get(position);

            holder.mContentView.setText(group.getName());
//            final int color = group.getColor();

//            holder.mImage.setBackgroundColor(color);
//            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    new BoardInfoDialog(context, owner, group, userInfo, 100.0, 100.0);
//                    setupRecyclerView();
//                    return true;
//                }
//            });

            // TODO impl
//                setMemberImages(boardInfo, holder);
        }

        private void setMemberImages(Task board, GroupViewHolder holder) {
            int count = 0;
            for (Map.Entry<String, String> entry : board.getTaskGroup().getMembers().entrySet()) {
                if (entry.getValue() != null)
                    Picasso.with(context)
                            .load(Uri.parse(entry.getValue()))
                            .into(holder.memberImages.get(count++));
                else {
                    holder.memberImages.get(count++).setImageResource(R.drawable.user_with_background);
                }
                if (count >= 4)
                    break;
            }
            if (count < 4) {
                for (int i = count; i < 4; i++) {
                    holder.memberImages.get(i).setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            try {
                if (list != null)
                    return list.size();
                else
                    return 0;
            } catch (GloopLoadException e) {
//                e.printStackTrace();
                return 0;
            }
        }

        void filter(String s) {
            if (s.equals("")) {
                list = (ArrayList<TaskGroup>) originalList.getLocalCopy();
            } else {
                String search = s.toLowerCase();
                for (TaskGroup task : originalList) {
                    if (!task.getName().toLowerCase().startsWith(search))
                        list.remove(task);
                }
            }

            notifyDataSetChanged();
        }

        class GroupViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mContentView;
            final TextView mLines;
            final ImageView mImage;
            final ImageView mFavorite;

            final List<CircleImageView> memberImages = new ArrayList<>();


            GroupViewHolder(View view) {
                super(view);
                mView = view.findViewById(R.id.card_view);
                mContentView = (TextView) view.findViewById(R.id.board_name);
                mLines = (TextView) view.findViewById(R.id.lines);
                mImage = (ImageView) view.findViewById(R.id.avatar);
                mFavorite = (ImageView) view.findViewById(R.id.board_favorite);

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