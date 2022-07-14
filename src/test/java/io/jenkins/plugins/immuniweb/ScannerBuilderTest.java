package io.jenkins.plugins.immuniweb;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ScannerBuilderTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String apikey = "test1234test";
    final String target = "http://dev.stage.example.com";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new ScannerBuilder(apikey, target));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new ScannerBuilder(apikey, target), project.getBuildersList().get(0));
    }

    /*
    @Test
    public void testConfigRoundtripFrench() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ScannerBuilder builder = new ScannerBuilder(apikey, target);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        ScannerBuilder lhs = new ScannerBuilder(apikey, target);
        lhs.setUseFrench(true);
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ScannerBuilder builder = new ScannerBuilder(apikey, target);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Hello, " + apikey, build);
    }

    @Test
    public void testBuildFrench() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ScannerBuilder builder = new ScannerBuilder(apikey, target);
        builder.setUseFrench(true);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Bonjour, " + apikey, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {\n" + "  greet '" + apikey + "'\n" + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Hello, " + apikey + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
    */
}
