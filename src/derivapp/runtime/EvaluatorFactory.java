package derivapp.runtime;

import java.util.Map;

/**
 * Factory for creating Evaluator instances, following the same pattern
 * as {@link derivapp.CompilerComponentFactory}.
 */
public class EvaluatorFactory {

    /**
     * Returns a new Evaluator bound to the given variable environment.
     *
     * @param env variable bindings, e.g. {"x": 3.0}
     * @return a ready-to-use Evaluator
     */
    public static Evaluator getEvaluator(Map<String, Double> env) {
        return new Evaluator(env);
    }
}
