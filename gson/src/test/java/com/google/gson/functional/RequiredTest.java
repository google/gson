package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Required;
import junit.framework.TestCase;

/**
 * Created by Алексей Анисов on 04/08/15.
 * © TomskSoft
 */
public class RequiredTest extends TestCase {
    private Gson gson;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gson = new Gson();
    }


    private class Server {
        @Required
        String ip;
        @Required
        String port;
        String log_level;
    }

    public void testRequiredAnnotation() throws Exception {
        String json = "{\"ip\":\"127.0.0.1\",\"port\":\"2378\"}";
        Server server = gson.fromJson(json, Server.class);
        assertNotNull(server.ip);
        assertNotNull(server.port);
        assertNull(server.log_level);
    }

    public void testRequiredAnnotation2() throws Exception {
        String json = "{\"ip\":\"127.0.0.1\",\"log_level\":\"info\"}";
        try {
            gson.fromJson(json, Server.class);
            assert false;
        }
        catch (JsonParseException e) {
            // ignore
        }
    }
}
