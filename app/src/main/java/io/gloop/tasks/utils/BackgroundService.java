package io.gloop.tasks.utils;

import com.stanfy.enroscar.goro.Goro;

import java.util.concurrent.Callable;

/**
 * Created by Alex Untertrifaller on 02.10.17.
 */

public class BackgroundService {

    private static Goro goro;

    public static void init() {
        goro = Goro.create();
    }

    public static void schedule(Callable callable) {
        goro.schedule(callable);
    }

}
