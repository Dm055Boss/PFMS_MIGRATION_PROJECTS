package com.intech.cpsms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cpsms")
public class CpsmsProps {

    private Paths paths = new Paths();
    private Dsc dsc = new Dsc();
    private File file = new File();
    private Response response = new Response();

    public Paths getPaths() { return paths; }
    public void setPaths(Paths paths) { this.paths = paths; }

    public Dsc getDsc() { return dsc; }
    public void setDsc(Dsc dsc) { this.dsc = dsc; }

    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }

    public Response getResponse() { return response; }
    public void setResponse(Response response) { this.response = response; }

    // ---------- nested classes ----------

    public static class Paths {
        private String req;
        private String res;
        private String err;

        public String getReq() { return req; }
        public void setReq(String req) { this.req = req; }

        public String getRes() { return res; }
        public void setRes(String res) { this.res = res; }

        public String getErr() { return err; }
        public void setErr(String err) { this.err = err; }
    }

    public static class Dsc {
        private String publicKeyPath;
        private String publicKeyName;

        public String getPublicKeyPath() { return publicKeyPath; }
        public void setPublicKeyPath(String publicKeyPath) { this.publicKeyPath = publicKeyPath; }

        public String getPublicKeyName() { return publicKeyName; }
        public void setPublicKeyName(String publicKeyName) { this.publicKeyName = publicKeyName; }
    }

    public static class File {
        /** Regex or glob you handle in code (e.g., .*PAYREQ.*\.xml or *PAYREQ*.xml) */
        private String pattern;

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
    }

    public static class Response {
        /** We always insert ACK after PAYREQ in the filename (success or failure). */
        private boolean alwaysAckName = true;

        public boolean isAlwaysAckName() { return alwaysAckName; }
        public void setAlwaysAckName(boolean alwaysAckName) { this.alwaysAckName = alwaysAckName; }
    }
}
