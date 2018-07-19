package org.jboss.windup.tooling.vertx;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;
import java.util.logging.LogRecord;

import org.jboss.windup.tooling.ExecutionBuilder;
import org.jboss.windup.tooling.ExecutionResults;
import org.jboss.windup.tooling.IOptionKeys;
import org.jboss.windup.tooling.IProgressMonitorAdapter;
import org.jboss.windup.tooling.WindupToolingProgressMonitor;
import org.jboss.windup.util.PathUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class Analysis extends AbstractVerticle implements IProgressMonitorAdapter, Handler<Message<Void>> {

    private boolean isCancelled;
    private final String id;
    private final String path;
    private MessageConsumer<Void> consumer;
    private final ExecutionBuilder executionBuilder;
    private IProgressMonitorAdapter progressMonitor;

    public Analysis(ExecutionBuilder executionBuilder, String id) {
        this.executionBuilder = executionBuilder;
        this.id = id;
        this.path = VertxServer.SERVER_BUS+"."+id;
        this.progressMonitor = new ProgressMonitorAdapter(this);
    }

    @Override
    public void start() throws Exception {
        System.out.println("analysis verticle start().");
        this.consumer = vertx.eventBus().consumer(path, this);
    }

    public boolean isAnalysis(String id) {
        return Objects.equal(this.id, id);
    }

    public void analyze() {
        System.out.println("Starting analysis...");
        Set<String> input = new HashSet<String>();
		input.add("/Users/johnsteele/Desktop/demos/demo");
        
		try {
            System.out.println("Setting up executionBuilder.");

            executionBuilder.setInput(input);
            System.out.println("Setting up executionBuilder.1");
            executionBuilder.setOutput("/Users/johnsteele/Desktop/demos/demo/out");
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
            dispose();
        }
    }

    private void send(String key, Object value) {
        JsonObject payload = new JsonObject();
        payload.put(key, value);
        send(payload);
    }

    public void send(Object data) {
        System.out.println("eb sending: " + data);
        vertx.eventBus().send(path, data);
    }

    public void dispose() {
        System.out.println("disposing...");
        if (consumer.isRegistered()) {
            System.out.println("unregistering event consumer...");
            System.out.println("unregistering");
            consumer.unregister(((e) -> {
                System.out.println("event consumer unregistered...");
                send("consumer unregistered", id);
            }));
        }
        if (context != null) {
            System.out.println("undeploying analysis worker");
            vertx.undeploy(deploymentID(), (e) -> {
                System.out.println("verticle undeployed...");
            });
            System.out.println("unregistering event consumer...");
        }
    }

    @Override
    public void handle(Message<Void> event) {
        event.reply(null);
    }


    @Override
    public void beginTask(String task, int totalWork) {
        System.out.println("Analysis::beginTask");
        send("beginTask: " + task, totalWork);
        //LOG.info("beginTask: " + task + "totalWork: " + totalWork);
    }

    @Override
    public void done() {
        System.out.println("Analysis::done");
        send("done", true);
        //LOG.info("done");
        //send("done", true);
        //dispose();
    }

    @Override
    public boolean isCancelled() { 
        System.out.println("Analysis::isCancelled");
        return isCancelled;
    }
    
    @Override
    public void setCancelled(boolean value) {
        System.out.println("Analysis::setCancelled");
        send("receivedCancelEvent", value);
        this.isCancelled = value;
        //this.isCancelled = value;
        //LOG.info("cancelled: " + value);
        //send("cancelled", value);
        ////if (isCancelled) {
        //    dispose();
        //}
    }

    @Override
    public void setTaskName(String name) {
        System.out.println("Analysis::setTaskName");
        send("setTaskName", name);
        //LOG.info("setTaskName: " + name);
        //send("taskName", name);
    }

    @Override
    public void subTask(String name) {
        System.out.println("Analysis::subTask");
        send("subTask", name);
        //LOG.info("subTask: " + name);
        //send("subTask", name);
    }

    @Override
    public void logMessage(LogRecord logRecord) {
        System.out.println("Analysis::logMessage: " + logRecord.getMessage());
        send("logMessage", logRecord.getMessage());
        //LOG.info("logMessage: " + logRecord.getMessage());
        //send("logMessage", logRecord.getMessage());
    }
    @Override
    public void worked(int work) {
        System.out.println("Analysis::worked: " + work);
        send("worked", work);
    }
}