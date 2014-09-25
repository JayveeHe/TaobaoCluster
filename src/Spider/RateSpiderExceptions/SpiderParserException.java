package Spider.RateSpiderExceptions;

import java.io.IOException;

/**
 * Created by Jayvee on 2014/9/24.
 */
public class SpiderParserException extends IOException {
    private String msg;

    public SpiderParserException(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
