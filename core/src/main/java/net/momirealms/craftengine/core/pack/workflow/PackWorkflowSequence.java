package net.momirealms.craftengine.core.pack.workflow;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;

import java.util.List;
import java.util.Map;

public record PackWorkflowSequence(String name, List<PackWorkflow> workflows) {
    public static final String RELOAD_PACK = "reload_pack";

    public PackWorkflowSequence {
        workflows = List.copyOf(workflows);
        if (!name.matches("[A-Za-z0-9_-]+") || workflows.isEmpty()) {
            throw new IllegalArgumentException("Invalid or empty resource pack workflow: " + name);
        }
    }

    public static PackWorkflowSequence fromConfig(String name, ConfigValue value) {
        List<PackWorkflow> workflows = value.getAsSection().getList("steps", entry -> PackWorkflows.fromConfig(
                entry.value() instanceof String ? ConfigSection.of(entry.path(), Map.of("type", entry.getAsString())) : entry.getAsSection()));
        return new PackWorkflowSequence(name, workflows);
    }

    public void validate(PackWorkflowValidation validation) {
        for (PackWorkflow workflow : this.workflows) workflow.validate(validation);
    }

    public void execute(PackWorkflowContext context) throws Exception {
        for (PackWorkflow workflow : this.workflows) {
            context.plugin().logger().info("Resource pack workflow " + this.name + ": " + workflow.type().id().asMinimalString());
            workflow.execute(context);
        }
    }

    public static void trigger(ConfigSection workflows, String event, Executor executor) throws Exception {
        for (String name : workflows.keySet()) {
            if (event.equals(workflows.getValue(name).getAsSection().getString("trigger"))) executor.execute(name);
        }
    }

    @FunctionalInterface
    public interface Executor {
        void execute(String name) throws Exception;
    }
}
