package com.poorld.badget;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

        List<String> apps = new ArrayList<>();
        apps.add("aaa");
        apps.add("bbb");
        apps.add("ccc");

        Optional<String> opt = apps.stream().filter(app -> app.equals("ab")).findAny();
        opt.ifPresent(System.out::println);

    }
}