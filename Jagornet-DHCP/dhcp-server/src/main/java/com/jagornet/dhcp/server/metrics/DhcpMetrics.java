package com.jagornet.dhcp.server.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Central metrics manager for Jagornet DHCP server backed by Micrometer & Prometheus.
 */
public class DhcpMetrics {

    private static final DhcpMetrics INSTANCE = new DhcpMetrics();

    private final PrometheusMeterRegistry registry;
    private final ConcurrentMap<String, Counter> packetCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> packetTimers = new ConcurrentHashMap<>();

    private DhcpMetrics() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        
        // Bind JVM & system metrics
        try {
            new ClassLoaderMetrics().bindTo(registry);
            new JvmMemoryMetrics().bindTo(registry);
            new JvmGcMetrics().bindTo(registry);
            new JvmThreadMetrics().bindTo(registry);
            new ProcessorMetrics().bindTo(registry);
        } catch (Exception e) {
            // Ignore optional metrics bind failures if running on restricted environments
        }
    }

    public static DhcpMetrics getInstance() {
        return INSTANCE;
    }

    public PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    public String scrape() {
        return registry.scrape();
    }

    public void incrementPacketCounter(String version, String msgType, String result) {
        String key = version + ":" + msgType + ":" + result;
        packetCounters.computeIfAbsent(key, k -> 
            Counter.builder("dhcp_packets_total")
                   .description("Total DHCP packets processed")
                   .tag("version", version)
                   .tag("type", msgType)
                   .tag("result", result)
                   .register(registry)
        ).increment();
    }

    public void recordPacketLatency(String version, String msgType, long durationNanos) {
        String key = version + ":" + msgType;
        packetTimers.computeIfAbsent(key, k ->
            Timer.builder("dhcp_request_duration_seconds")
                 .description("DHCP request processing duration in seconds")
                 .tag("version", version)
                 .tag("type", msgType)
                 .register(registry)
        ).record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
