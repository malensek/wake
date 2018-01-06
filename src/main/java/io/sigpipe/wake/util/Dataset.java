package io.sigpipe.wake.util;

import java.util.HashMap;
import java.util.Map;

public class Dataset extends HashMap<Object, Object> {

    private static final long serialVersionUID = -3527314157809372281L;

    public Dataset() {
        super();
    }

    public Dataset(Map<?, ?> map) {
        super(map);
    }

    public boolean parseBoolean(String parameterName, boolean defaultValue) {
        boolean value = defaultValue;

        Object o = this.get(parameterName);
        if (o == null) {
            return value;
        }

        value = Boolean.parseBoolean((String) o);

        return value;
    }

    public int parseInt(String parameterName, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt((String) this.get(parameterName));
        } catch (NumberFormatException e) { }
        return value;
    }

}
