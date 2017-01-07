package com.github.bingoohuang.westcache.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.File;
import java.io.IOException;

import static com.github.bingoohuang.westcache.utils.Snapshots.USER_HOME;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@UtilityClass
public class Diamonds {
    @SneakyThrows
    public void writeDiamondJSON(String group, String dataid, Object content) {
        File file = getDiamondFile(group, dataid);
        writeDiamondJSON(file, content);
    }

    @SneakyThrows
    public void writeDiamond(String group, String dataid, String content) {
        File file = getDiamondFile(group, dataid);
        writeDiamond(file, content);
    }

    public File getDiamondFile(String group, String dataid) {
        val configDataDir = new File(USER_HOME, ".diamond-client/config-data");
        val groupDir = new File(configDataDir, group);
        groupDir.mkdirs();

        return new File(groupDir, dataid + ".diamond");
    }

    public void writeDiamondJSON(File diamondFile, Object content) throws IOException {
        String json = FastJsons.json(content);
        Files.write(json, diamondFile, Charsets.UTF_8);
    }

    public void writeDiamond(File diamondFile, String content) throws IOException {
        Files.write(content, diamondFile, Charsets.UTF_8);
    }


}
