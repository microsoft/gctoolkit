// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum;

import com.microsoft.censum.aggregator.Aggregation;
import com.microsoft.censum.io.DataSource;
import com.microsoft.censum.jvm.JavaVirtualMachine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Censum {

    private static Logger LOGGER = Logger.getLogger(Censum.class.getName());

    private static JavaVirtualMachine loadJavaVirtualMachine() {
        try {
            // TODO: property for to allow override of default implementation.
            Class<?> clazz =
                    Class.forName("com.microsoft.censum.vertx.jvm.DefaultJavaVirtualMachine", true, Thread.currentThread().getContextClassLoader());
            Constructor<?> constructor = clazz.getConstructor();
            JavaVirtualMachine javaVirtualMachine = (JavaVirtualMachine) constructor.newInstance();
            return javaVirtualMachine;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Cannot load \"com.microsoft.censum.vertx.jvm.DefaultJavaVirtualMachine\"",e);
        }
        return null;
    }

    private static final Set<Class<? extends Aggregation>> aggregationsFromServiceLoader;

    static {
        aggregationsFromServiceLoader = new HashSet<>();
        ServiceLoader<Aggregation> serviceLoader = ServiceLoader.load(Aggregation.class);
        serviceLoader.stream()
                .map(ServiceLoader.Provider::get)
                .map(Aggregation::getClass)
                .forEach(aggregationsFromServiceLoader::add);
    }

    private final Set<Class<? extends Aggregation>> registeredAggregations;
    public Censum() {
        // Allow for adding aggregations from source code,
        // but don't corrupt the ones loaded by the service loader
        this.registeredAggregations = new HashSet<>(aggregationsFromServiceLoader);
    }

    public void registerAggregation(Class<? extends Aggregation> aggregationClass) {
        registeredAggregations.add(aggregationClass);
    }

    public JavaVirtualMachine analyze(DataSource<?> dataSource) {
        // Potential NPE, but would have logged if there was trouble creating the instance.
        JavaVirtualMachine javaVirtualMachine = loadJavaVirtualMachine();
        try {
            Method analyze = javaVirtualMachine.getClass().getMethod("analyze", Set.class, DataSource.class);
            analyze.invoke(javaVirtualMachine, this.registeredAggregations, dataSource);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Cannot invoke analyze method",e);
        }
        return javaVirtualMachine;
    }
}
