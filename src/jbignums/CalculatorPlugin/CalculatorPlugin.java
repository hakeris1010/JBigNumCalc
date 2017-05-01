package jbignums.CalculatorPlugin;

/**
 * Basic interface for all calculator plugins.
 * An interested class can use CalcPlugins in many ways:
 * - Using Directly (Passing Query type queries and getting Result type results (inheritable))
 * - Using through a CalcState - a multithreaded worker to schedule tasks and get results asynchronously.
 */
public interface CalculatorPlugin {
    /**
     * Base class for Calculation Query and Result.
     * The inner workings are plugin-defined.
     * Basic stuff might be added.
     */
    class Query{ }
    class Result{ }

    Result startCalculation(Query query);
    void assignQuery(Query query);

    Query getLastQuery();
    Result getLastResult();
}
