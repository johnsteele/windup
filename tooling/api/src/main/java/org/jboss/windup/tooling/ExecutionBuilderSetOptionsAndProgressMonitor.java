package org.jboss.windup.tooling;

/**
 * Allows setting windup options, including the {@link WindupProgressMonitor}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetOptionsAndProgressMonitor
{
    /**
     * Sets a pattern of file paths to ignore during processing.
     */
    ExecutionBuilderSetOptions ignore(String ignorePattern);

    /**
     * Sets the package name prefixes to scan (the default is to scan all packages).
     */
    ExecutionBuilderSetOptions includePackage(String includePackagePrefix);

    /**
     * Sets the package name prefixes to ignore.
     */
    ExecutionBuilderSetOptions excludePackage(String excludePackagePrefix);

    /**
     * Sets the callback that will be used for monitoring progress.
     */
    ExecutionBuilderSetOptions setProgressMonitor(WindupToolingProgressMonitor monitor);

    /**
     * Sets the option with the specified name to the specified value. Option names can be found in static variables on {@link ConfigurationOption}
     * implementations.
     */
    ExecutionBuilderSetOptions setOption(String name, Object value);

    /**
     * Execute windup.
     */
    ExecutionResults execute();
}
