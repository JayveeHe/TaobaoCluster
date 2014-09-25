package Spider.RateSpiderExceptions;

import java.io.IOException;

/**
 * Created by Jayvee on 2014/9/26.
 */
public class SpiderParseException extends IOException {
    private String msg;

    public SpiderParseException(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
