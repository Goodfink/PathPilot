package org.example.pathpilot.helpers;

import java.nio.file.Path;

public class PathHelpers {

    public String getFileName(Path path) {
        String p = path.toString();
        return p.substring(p.lastIndexOf('/') + 1, p.lastIndexOf('.'));
    }

    public String getFolderName(Path path) {
        String f = path.toString();
        return f.substring(f.lastIndexOf('/'));
    }

    public String getFileExtension(Path path) {
        String p = path.toString();
        return p.substring(p.lastIndexOf(".") + 1);
    }
}
