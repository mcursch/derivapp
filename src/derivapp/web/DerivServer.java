package derivapp.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import derivapp.CompilerComponentFactory;
import derivapp.Parser;
import derivapp.ast.Program;
import derivapp.runtime.DerivativeVisitor;
import derivapp.runtime.Evaluator;
import derivapp.runtime.PrintVisitor;
import derivapp.runtime.SimplificationVisitor;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class DerivServer {

    // -----------------------------------------------------------------------
    // Request / response POJOs (public fields for Jackson)
    // -----------------------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComputeRequest {
        public String expression;
        public double x;
    }

    public static class ComputeResponse {
        public double evaluated;
        public String derivative;
        public double derivativeValue;

        public ComputeResponse(double evaluated, String derivative, double derivativeValue) {
            this.evaluated = evaluated;
            this.derivative = derivative;
            this.derivativeValue = derivativeValue;
        }
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    // -----------------------------------------------------------------------
    // Server entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(7070);

        app.post("/api/compute", ctx -> {
            try {
                ComputeRequest req = ctx.bodyAsClass(ComputeRequest.class);

                // 1. Parse expression
                Program program = (Program) CompilerComponentFactory.getParser(req.expression).parse();

                // 2. Evaluate f(x)
                double evaluated = new Evaluator(Map.of("x", req.x)).evaluate(program);

                // 3. Differentiate
                Program derivProgram = new DerivativeVisitor("x").differentiate(program);

                // 4. Simplify derivative
                Program simpProgram = new SimplificationVisitor().simplify(derivProgram);

                // 5. Print derivative as string
                String derivative = new PrintVisitor().print(simpProgram);

                // 6. Evaluate f'(x)
                double derivativeValue = new Evaluator(Map.of("x", req.x)).evaluate(simpProgram);

                ctx.json(new ComputeResponse(evaluated, derivative, derivativeValue));

            } catch (Exception e) {
                ctx.status(400).json(new ErrorResponse(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        });
    }
}
