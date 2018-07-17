package org.jboss.windup.tooling.vertx;

import java.util.logging.LogRecord;

import org.jboss.windup.tooling.IProgressMonitorAdapter;
import org.jboss.windup.tooling.WindupToolingProgressMonitor;

public class ProgressMonitorAdapter implements IProgressMonitorAdapter, WindupToolingProgressMonitor {

    //private static Logger LOG = Logger.getLogger(ProgressMonitorAdapter.class.getName());

    private boolean isCancelled;

    @Override
    public void beginTask(String task, int totalWork) {
        System.out.println("beginTask");
        //LOG.info("beginTask: " + task + "totalWork: " + totalWork);
    }

    @Override
	public void done() {
        System.out.println("done");
       //LOG.info("done");
        //send("done", true);
        //dispose();
    }

    @Override
	public boolean isCancelled() { 
        System.out.println("isCancelled");
        return isCancelled; // this.isCancelled;
    }
    
    @Override
    public void setCancelled(boolean value) {
        System.out.println("setCancelled");
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
        System.out.println("setTaskName");
        //LOG.info("setTaskName: " + name);
        //send("taskName", name);
    }

    @Override
	public void subTask(String name) {
        System.out.println("subTask");
        //LOG.info("subTask: " + name);
        //send("subTask", name);
    }

    @Override
	public void worked(int work) {
        System.out.println("worked: " + work);
        //LOG.info("worked: " + work);
        //send("worked", work);
    }

    @Override
	public void logMessage(LogRecord logRecord) {
        System.out.println("logMessage: " + logRecord.getMessage());
        //LOG.info("logMessage: " + logRecord.getMessage());
        //send("logMessage", logRecord.getMessage());
    }
}