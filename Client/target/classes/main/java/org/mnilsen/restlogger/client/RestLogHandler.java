/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mnilsen.restlogger.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author michaeln
 */
public class RestLogHandler extends Handler {

    private String logUrl = null;
    private URL url = null;
    private HttpURLConnection conn = null;
    private boolean valid = false;

    public RestLogHandler(String logUrl) {
        this.logUrl = logUrl;
    }
    
    private void setup()
    {
        try {
            this.url = new URL(logUrl);
        } catch (MalformedURLException ex) {
            this.getErrorManager().error("Bad URL in RestLogHandler: " + this.logUrl, ex, ErrorManager.OPEN_FAILURE);
            return;
        }
        this.valid = true;
    }

    @Override
    public void publish(LogRecord record) {
        try {
            if(!this.isLoggable(record)) return;
            if(!this.valid) this.setup();
            JSONObject jobj = new JSONObject();
            jobj.append("message", record.getMessage());
            jobj.append("level", record.getLevel().toString());
            jobj.append("systemId", record.getLoggerName());
            jobj.append("timestamp", record.getMillis());
            
            this.writeToREST(jobj.toString());
        } catch (IOException ex) {
            this.getErrorManager().error("Message write failed", ex, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public void flush() {
        //  no op
    }

    @Override
    public void close() throws SecurityException {
        this.url = null;
    }

    public String getLogUrl() {
        return logUrl;
    }

    private void writeToREST(String json) throws MalformedURLException, IOException {
        conn = (HttpURLConnection) this.url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            this.getErrorManager().error(String.format("Bad response from Logging server: %s",conn.getResponseCode()), null, ErrorManager.WRITE_FAILURE);
        }
        
        conn.disconnect();
    }
}
