package Spider.RateSpiderExceptions;

import org.json.JSONException;

/**
 * Created by Jayvee on 2014/9/24.
 */
public class SpiderJsonException extends JSONException {
    /**
     * Constructs a JSONException with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */

    public SpiderJsonException(String message) {
        super(message);
    }
}
