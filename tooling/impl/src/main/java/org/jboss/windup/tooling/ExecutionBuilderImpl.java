package org.jboss.windup.tooling;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Lists;
import org.jboss.windup.config.SkipReportsRenderingOption;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ExecutionBuilderImpl implements ExecutionBuilder, ExecutionBuilderSetInput, ExecutionBuilderSetOutput, ExecutionBuilderSetOptions,
            ExecutionBuilderSetOptionsAndProgressMonitor
{
    @Inject
    private GraphContextFactory graphContextFactory;

    @Inject
    private ToolingXMLService toolingXMLService;

    @Inject
    private WindupProcessor processor;

    private Path windupHome;
    private WindupToolingProgressMonitor progressMonitor;
    private Path input;
    private Path output;
    private Set<String> ignorePathPatterns = new HashSet<>();
    private Set<String> includePackagePrefixSet = new HashSet<>();
    private Set<String> excludePackagePrefixSet = new HashSet<>();
    private Set<String> userRulesPathSet = new HashSet<>();
    private Map<String, Object> options = new HashMap<>();
    private boolean skipReportsRendering;

    /**
     * Is the option to skip Report preparing and generation set?
     * 
     * @return the skipReportsRendering
     */
    public boolean isSkipReportsRendering()
    {
        return skipReportsRendering;
    }

    /**
     * Sets the option to skip Report preparing and generation
     * 
     * @param skipReportsRendering the skipReportsRendering to set
     */
    public void setSkipReportsRendering(boolean skipReportsRendering)
    {
        this.skipReportsRendering = skipReportsRendering;
    }

    @Override
    public ExecutionBuilderSetInput begin(Path windupHome)
    {
        this.windupHome = windupHome;
        return this;
    }

    @Override
    public ExecutionBuilderSetOutput setInput(Path input)
    {
        this.input = input;
        return this;
    }

    @Override
    public ExecutionBuilderSetOptionsAndProgressMonitor setOutput(Path output)
    {
        this.output = output;
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions ignore(String ignorePattern)
    {
        this.ignorePathPatterns.add(ignorePattern);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions includePackage(String packagePrefix)
    {
        this.includePackagePrefixSet.add(packagePrefix);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions includePackages(Collection<String> includePackagePrefixes)
    {
        if (includePackagePrefixes != null)
            this.includePackagePrefixSet.addAll(includePackagePrefixes);

        return this;
    }

    @Override
    public ExecutionBuilderSetOptions excludePackage(String packagePrefix)
    {
        this.excludePackagePrefixSet.add(packagePrefix);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions excludePackages(Collection<String> excludePackagePrefixes)
    {
        if (excludePackagePrefixes != null)
            this.excludePackagePrefixSet.addAll(excludePackagePrefixes);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions setProgressMonitor(WindupToolingProgressMonitor monitor)
    {
        this.progressMonitor = monitor;
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions sourceOnlyMode()
    {
        options.put(SourceModeOption.NAME, true);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions skipReportGeneration()
    {
        options.put(SkipReportsRenderingOption.NAME, true);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions addUserRulesPath(Path rulesPath)
    {
        if (rulesPath == null)
            return this;

        String pathString = rulesPath.normalize().toAbsolutePath().toString();
        this.userRulesPathSet.add(pathString);

        return this;
    }

    @Override
    public ExecutionBuilderSetOptions addUserRulesPaths(Iterable<Path> rulesPaths)
    {
        if (rulesPaths == null)
            return this;

        for (Path rulesPath : rulesPaths) {
            this.addUserRulesPath(rulesPath);
        }

        return this;
    }

    @Override
    public ExecutionBuilderSetOptions setOption(String name, Object value)
    {
        this.options.put(name, value);
        return this;
    }

    @Override
    public ExecutionResults execute()
    {
        PathUtil.setWindupHome(this.windupHome);
        WindupConfiguration windupConfiguration = new WindupConfiguration();
        try
        {
            windupConfiguration.useDefaultDirectories();
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to configure windup due to: " + e.getMessage(), e);
        }
        ToolingProgressMonitorAdapter progressMonitorAdapter = new ToolingProgressMonitorAdapter(this.progressMonitor);

        windupConfiguration.addInputPath(this.input);
        windupConfiguration.setOutputDirectory(this.output);
        windupConfiguration.setProgressMonitor(progressMonitorAdapter);
        windupConfiguration.setOptionValue(SkipReportsRenderingOption.NAME, skipReportsRendering);

        Path graphPath = output.resolve(GraphContextFactory.DEFAULT_GRAPH_SUBDIRECTORY);

        Logger globalLogger = Logger.getLogger("");
        WindupProgressLoggingHandler loggingHandler = null;
        if (progressMonitor instanceof WindupToolingProgressMonitor) {
            loggingHandler = new WindupProgressLoggingHandler((WindupToolingProgressMonitor)progressMonitor);
            globalLogger.addHandler(loggingHandler);
        }

        try (final GraphContext graphContext = graphContextFactory.create(graphPath))
        {

            GraphService<IgnoredFileRegexModel> graphService = new GraphService<>(graphContext, IgnoredFileRegexModel.class);
            for (String ignorePattern : this.ignorePathPatterns)
            {
                IgnoredFileRegexModel ignored = graphService.create();
                ignored.setRegex(ignorePattern);

                WindupJavaConfigurationModel javaCfg = WindupJavaConfigurationService.getJavaConfigurationModel(graphContext);
                javaCfg.addIgnoredFileRegex(ignored);
            }

            windupConfiguration.setOptionValue(ScanPackagesOption.NAME, Lists.toList(this.includePackagePrefixSet));
            windupConfiguration.setOptionValue(ExcludePackagesOption.NAME, Lists.toList(this.excludePackagePrefixSet));

            for (Map.Entry<String, Object> option : options.entrySet())
            {
                windupConfiguration.setOptionValue(option.getKey(), option.getValue());
            }
            
            windupConfiguration.setGraphContext(graphContext);
            
            processor.execute(windupConfiguration);

            return new ExecutionResultsImpl(graphContext, toolingXMLService);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to instantiate graph due to: " + e.getMessage(), e);
        } finally
        {
            if (loggingHandler != null)
                globalLogger.removeHandler(loggingHandler);
        }
    }

    private class WindupProgressLoggingHandler extends Handler
    {
        private final WindupToolingProgressMonitor monitor;

        public WindupProgressLoggingHandler(WindupToolingProgressMonitor monitor)
        {
            this.monitor = monitor;
        }

        @Override
        public void publish(LogRecord record)
        {
            if (this.monitor == null)
                return;

            this.monitor.logMessage(record);
        }

        @Override
        public void flush()
        {

        }

        @Override
        public void close() throws SecurityException
        {

        }
    }
}
