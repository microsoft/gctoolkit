package com.microsoft.gctoolkit.message;

/**
 * Interface defining the DataSource Channel. This must be implemented by a provider
 * and made available via the module service provider API.
 */
public interface DataSourceChannel extends Channel<String,DataSourceParser> {}
