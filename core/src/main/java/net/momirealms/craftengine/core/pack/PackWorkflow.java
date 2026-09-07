package net.momirealms.craftengine.core.pack;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public record PackWorkflow(String name, List<Step> steps) {
    public static final String RELOAD_PACK = "reload_pack";

    public PackWorkflow {
        steps = List.copyOf(steps);
    }

    public enum Type { GENERATE, VALIDATE, OPTIMIZE, MAP_COMPATIBILITY, OBFUSCATE, UPLOAD, SEND_PACK }

    public record Step(Type type, String path, String host) {}

    public static PackWorkflow fromConfig(String name, ConfigValue value, Set<String> hostIds) {
        List<Step> steps = value.getAsSection().getList("steps", entry -> {
            ConfigSection section = entry.value() instanceof String
                    ? ConfigSection.of(entry.path(), java.util.Map.of("type", entry.getAsString())) : entry.getAsSection();
            Type type = Type.valueOf(section.getNonEmptyString("type").replace('-', '_').toUpperCase(Locale.ROOT));
            return new Step(type, section.getString("path"), section.getString("pack", section.getString("host")));
        });
        PackWorkflow workflow = new PackWorkflow(name, steps);
        workflow.validate(hostIds);
        return workflow;
    }

    public void validate(Set<String> hostIds) {
        if (!this.name.matches("[A-Za-z0-9_-]+") || this.steps.isEmpty()) {
            throw new IllegalArgumentException("Invalid or empty resource pack workflow: " + this.name);
        }
        boolean generated = false;
        boolean obfuscated = false;
        for (Step step : this.steps) {
            switch (step.type()) {
                case GENERATE -> {
                    if (generated) throw new IllegalArgumentException("Workflow " + this.name + " contains more than one generate step");
                    generated = true;
                }
                case VALIDATE, OPTIMIZE, OBFUSCATE -> {
                    if (!generated || obfuscated) throw new IllegalArgumentException(step.type() + " requires generated assets before obfuscation");
                    obfuscated = step.type() == Type.OBFUSCATE;
                }
                case MAP_COMPATIBILITY -> {
                    if (!generated || obfuscated || step.path() == null || step.path().isBlank()) {
                        throw new IllegalArgumentException("map_compatibility requires generated assets before obfuscation and an output path");
                    }
                }
                case UPLOAD -> {
                    if (step.host() == null || !hostIds.contains(step.host())) throw new IllegalArgumentException("Unknown resource pack host: " + step.host());
                    if (step.path() == null || step.path().isBlank()) {
                        throw new IllegalArgumentException("upload requires an explicit file path");
                    }
                }
                case SEND_PACK -> {
                    if (step.host() == null || !hostIds.contains(step.host())) throw new IllegalArgumentException("Unknown resource pack: " + step.host());
                }
            }
        }
    }

    public void execute(StepExecutor executor) throws Exception {
        for (Step step : this.steps) {
            executor.execute(step);
        }
    }

    public static void trigger(ConfigSection workflows, String event, WorkflowExecutor executor) throws Exception {
        for (String name : workflows.keySet()) {
            if (event.equals(workflows.getValue(name).getAsSection().getString("trigger"))) {
                executor.execute(name);
            }
        }
    }

    @FunctionalInterface
    public interface WorkflowExecutor {
        void execute(String name) throws Exception;
    }

    @FunctionalInterface
    public interface StepExecutor {
        void execute(Step step) throws Exception;
    }
}
