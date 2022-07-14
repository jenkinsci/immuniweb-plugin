package io.jenkins.plugins.immuniweb;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.DataOutputStream;
import java.util.Base64;
import java.util.Objects;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScannerBuilder extends Builder implements SimpleBuildStep {
    private final Secret apikey;
    private final String target;

    @DataBoundConstructor
    public ScannerBuilder(Secret apikey, String target) {
        this.apikey = apikey;
        this.target = target;
    }

    public String getApikey() {
        return apikey.getPlainText();
    }

    public String getTarget() {
        return target;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        // make POST request
        String urlParameters = "target_url=" + target;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        String request = "https://portal.immuniweb.com/client/project/neuron/startstop/";
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        String authString = Base64.getEncoder().encodeToString(("neuron:" + apikey.getPlainText()).getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + authString);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }

        // fetch response
        InputStream responseStream = conn.getInputStream();

        // json to class
        ObjectMapper mapper = new ObjectMapper();
        IWApiResponse resp = mapper.readValue(responseStream, IWApiResponse.class);

        // parse JSON response
        if (Objects.equals(resp.status, "ok")) {
            listener.getLogger().println("Scan scheduled: " + resp.id);
        } else {
            listener.getLogger().println("Error: " + resp.message);
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public FormValidation doCheckApikey(@QueryParameter Secret value) throws IOException, ServletException {
            if (value.getPlainText().length() == 0) {
                return FormValidation.error(Messages.ScannerBuilder_DescriptorImpl_errors_missingApikey());
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.ScannerBuilder_DescriptorImpl_DisplayName();
        }
    }
}

class IWApiResponse {
    public String status;
    public String message;
    public String id;
    public Float time;
}
