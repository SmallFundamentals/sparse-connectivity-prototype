package rsync.client.uploader;

import java.util.ArrayList;
import java.util.List;

public class ChecksumResult {

    private List<Long> rolling;
    private List<String> md5;

    public List<Long> getRolling() {
        return this.rolling;
    }

    public void setRolling(ArrayList<Long> rolling) {
        this.rolling = rolling;
    }

    public List<String> getMd5() {
        return this.md5;
    }

    public void setMd5(ArrayList<String> md5) {
        this.md5 = md5;
    }
}
