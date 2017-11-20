package io.gloop.tasks.model;

import java.util.HashMap;
import java.util.Map;

import io.gloop.GloopObject;

/**
 * Created by Alex Untertrifaller on 20.11.17.
 */

public class TaskGroup extends GloopObject {

    private String name;
    private Map<String, String> members = new HashMap<>();


    public Map<String, String> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }

    public void addMember(String userId, String userImageUri) {
        members.put(userId, userImageUri);

    }
}
