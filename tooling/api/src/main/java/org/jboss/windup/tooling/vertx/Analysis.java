package org.jboss.windup.tooling.vertx;

import java.util.Set;

import java.util.logging.LogRecord;

import org.jboss.windup.tooling.ExecutionBuilder;
import org.jboss.windup.tooling.ExecutionResults;
import org.jboss.windup.tooling.IOptionKeys;
import org.jboss.windup.tooling.IProgressMonitorAdapter;
import org.jboss.windup.tooling.WindupToolingProgressMonitor;
import org.jboss.windup.util.PathUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class Analysis extends AbstractVerticle implements IProgressMonitorAdapter {

    private boolean isCancelled;
    //private final String id;
    //private final String path;
    private final String client;
    //private MessageConsumer<Void> consumer;
    private final ExecutionBuilder executionBuilder;
    private IProgressMonitorAdapter progressMonitor;

    private boolean done; 
    private boolean cancelled;

    public Analysis(String client, ExecutionBuilder executionBuilder) {
        this.executionBuilder = executionBuilder;
        this.client = client;
        this.progressMonitor = new ProgressMonitorAdapter(this);
    }

    public void analyze(Set<String> input, String output) {
        vertx.executeBlocking(f -> {
            System.out.println("Starting analysis...");
            vertx.eventBus().send(client, new JsonObject().put("state", "Starting Analysis..."));
            try {
                System.out.println("Setting up executionBuilder.");
                executionBuilder.clear();
                executionBuilder.setInput(input);
                System.out.println("Setting up executionBuilder.1");
                executionBuilder.setOutput(output);
                System.out.println("Setting up executionBuilder.2");
                executionBuilder.setProgressMonitor((WindupToolingProgressMonitor)progressMonitor);
                System.out.println("Setting up executionBuilder.3");
                executionBuilder.setWindupHome(PathUtil.getWindupHome().toString());
                System.out.println("Setting up executionBuilder.4");
                executionBuilder.setOption(IOptionKeys.sourceModeOption, true);
                System.out.println("Setting up executionBuilder.5");
                executionBuilder.setOption(IOptionKeys.skipReportsRenderingOption, true);
                System.out.println("Setting up executionBuilder.6");
                executionBuilder.ignore("\\.class$");
                System.out.println("about to run analysis.");
                ExecutionResults results = executionBuilder.execute();
                vertx.eventBus().send(client, new JsonObject().put("state", "done"));
                System.out.println("executionBuilder returned.");
                JsonObject data = new JsonObject();
                data.put("status", "analyzed");
                data.put("report", "/Users/johnsteele/Desktop/demos/demo/reports/index.html");
                data.put("hintCount", results.getHints().size());
                data.put("classificationCount", results.getClassifications().size());
                System.out.println("analysis complete.");
                System.out.println(data);
            }
            catch (Exception e) {
                System.err.println("Error while running analysis.");
                System.err.println(e.getMessage());
                System.err.println("Server `RemoteException` error while performing analysis.");
                //data.put("server error", e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("analysis complete.");
                //dispose();
            }
            f.complete();
        }, (t) -> {
            System.out.println("confirmation: " + t.toString());
        });
    }

    private void send(String op, Object value) {
        System.out.println("attempting to send client data: " + value);
        JsonObject load = new JsonObject();
        load.put("op", op);
        load.put("value", value);
        send(load);
    }

    private void send(JsonObject data) {
        vertx.eventBus().send(client, data);
    }

    @Override
    public void beginTask(String task, int totalWork) {
        System.out.println("beginTask");
        JsonObject load = new JsonObject();
        load.put("op", "beginTask");
        load.put("task", task);
        load.put("totalWork", totalWork);
        send(load);
    }

    @Override
    public void done() {
        send("done", true);
        this.done = true;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
    
    @Override
    public void setCancelled(boolean value) {
        send("setCancelled", value);
        this.isCancelled = value;
    }

    @Override
    public void setTaskName(String name) {
        send("setTaskName", name);
    }

    @Override
    public void subTask(String name) {
        send("subTask", name);
    }

    @Override
    public void logMessage(LogRecord logRecord) {
        send("logMessage", logRecord.getMessage());
    }
    @Override
    public void worked(int work) {
        send("worked", work);
    }
}