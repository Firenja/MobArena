package com.garbagemule.MobArena.signs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

class SignFile {

    List<String> lines() throws IOException {
        return Collections.emptyList();
    }

    void append(String line) throws IOException {
        System.out.println("[SignFile] append(String)");
    }

    void erase(String line) throws IOException {
        System.out.println("[SignFile] erase(String)");
    }

}
