package io.gloop.tasks.model;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 06.10.17.
 */

public class Task extends GloopObject {

    private String title;
    private String content;
    private TaskGroup taskGroup;
    private boolean privateTask = false;
    private boolean freezeTask = false;
    private int color;

    public Task() {}

    public boolean isPrivateTask() {
        return privateTask;
    }

    public void setPrivateTask(boolean privateTask) {
        this.privateTask = privateTask;
    }

    public boolean isFreezeTask() {
        return freezeTask;
    }

    public void setFreezeTask(boolean freezeTask) {
        this.freezeTask = freezeTask;
    }


    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TaskGroup getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
    }
}
