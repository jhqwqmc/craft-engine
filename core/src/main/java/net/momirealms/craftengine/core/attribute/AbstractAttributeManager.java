package net.momirealms.craftengine.core.attribute;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.attribute.formula.CauseToFormula;
import net.momirealms.craftengine.core.attribute.formula.VictimToFormula;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.ConcurrentChainedUUID2ReferenceHashTable;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractAttributeManager implements AttributeManager {
    protected final CraftEngine plugin;
    // API 注册
    protected final Map<Key, Attribute> apiAttributes = new HashMap<>();
    protected final Map<Key, AttributeOperation> apiOperations = new HashMap<>();
    // 配置注册
    protected final Map<Key, Attribute> configAttributes = new HashMap<>();
    protected final Map<Key, AttributeOperation> configOperations = new HashMap<>();
    // 运行中的实体属性容器
    protected final ConcurrentChainedUUID2ReferenceHashTable<AttributeGetter> containers = ConcurrentChainedUUID2ReferenceHashTable.createWithCapacity(128);
    private final OperationParser operationParser = new OperationParser();
    private final AttributeParser attributeParser = new AttributeParser();
    private final DamageFormulaParser damageFormulaParser = new DamageFormulaParser();
    // API配置合并
    protected Map<Key, Attribute> mergedAttributes = Map.of();
    // 运算管线快照
    protected volatile List<AttributeOperation> sortedOperations = List.of();
    private CauseToFormula causeToFormula;

    protected AbstractAttributeManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.apiOperations.put(AttributeOperations.ADD_VALUE_ID, AttributeOperations.ADD_VALUE);
        this.apiOperations.put(AttributeOperations.ADD_MULTIPLIED_BASE_ID, AttributeOperations.ADD_MULTIPLIED_BASE);
        this.apiOperations.put(AttributeOperations.ADD_MULTIPLIED_TOTAL_ID, AttributeOperations.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void unload() {
        this.configAttributes.clear();
        this.configOperations.clear();
    }

    protected void rebuildSortedOperations() {
        Map<Key, AttributeOperation> merged = new LinkedHashMap<>(this.apiOperations);
        merged.putAll(this.configOperations);
        List<AttributeOperation> all = new ArrayList<>(merged.values());
        all.sort(Comparator.comparingInt(AttributeOperation::order));
        this.sortedOperations = List.copyOf(all);
    }

    @Override
    public Optional<Attribute> getAttribute(Key id) {
        return Optional.ofNullable(this.mergedAttributes.get(id));
    }

    @Override
    public double getAttributeValue(Entity entity, Attribute attribute) {
        AttributeGetter attributeGetter = getOrCreateContainer(entity);
        if (attributeGetter == null) {
            return attribute.defaultValue(entity);
        }
        return attributeGetter.getAttributeValue(attribute);
    }

    @Override
    public void removeContainer(UUID uuid) {
        this.containers.remove(uuid);
    }

    public AttributeGetter getOrCreateContainer(Entity entity) {
        if (Config.applyAttributeToAll() || entity instanceof Player) {
            return this.containers.computeIfAbsent(entity.uuid(), k -> new AttributeContainer(this, entity));
        }
        return this.containers.get(entity.uuid());
    }

    @Override
    public List<AttributeOperation> sortedOperations() {
        return this.sortedOperations;
    }

    @Override
    public ConfigParser[] parsers() {
        return new ConfigParser[]{this.attributeParser, this.operationParser, this.damageFormulaParser};
    }

    @Override
    public DamageFormula findFormula(DamageEvent event) {
        if (this.causeToFormula == null) return null;
        return this.causeToFormula.getFormula(event);
    }

    @Override
    public void processDamageEvent(DamageEvent event) {
        DamageFormula formula = findFormula(event);
        if (formula == null) {
            return;
        }
        double newDamage = formula.getValue(event);
        if (event.damage() != newDamage) {
            if (newDamage < 0) {
                event.setDamage(0);
                return;
            }
            event.setDamage(newDamage);
        }
    }

    protected abstract List<Key> resolveEntities(Key tag);

    private final class DamageFormulaParser extends SectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"damage_rules", "damage-rules"};

        @Override
        protected void parseSection(Pack pack, Path path, ConfigSection section) {
            Map<Key, VictimToFormula> causeToFormulas = new HashMap<>();
            for (String damageSourceType : section.keySet()) {
                Key source = Key.of(damageSourceType);
                List<ConfigSection> sections = section.getList(damageSourceType, ConfigValue::getAsSection);
                Map<Key, DamageFormula> formulas = new HashMap<>();
                DamageFormula defaultFormula = null;
                for (int i = section.size() - 1; i >= 0; i--) {
                    ConfigSection configSection = sections.get(i);
                    List<String> targets = configSection.getStringList("target");
                    DamageFormula formula = configSection.getNonNullValue("formula", ConfigConstants.ARGUMENT_STRING, v -> compile(v.path(), v.getAsString()));
                    if (!targets.isEmpty()) {
                        for (String target : targets) {
                            if (target.isEmpty()) continue;
                            if (target.charAt(0) == '#') {
                                for (Key entity : resolveEntities(Key.of(target.substring(1)))) {
                                    formulas.put(entity, formula);
                                }
                            } else {
                                formulas.put(Key.of(target), formula);
                            }
                        }
                    } else {
                        defaultFormula = formula;
                    }
                }
                VictimToFormula victimToFormula = new VictimToFormula(defaultFormula, formulas);
                causeToFormulas.put(source, victimToFormula);
            }
            AbstractAttributeManager.this.causeToFormula = new CauseToFormula(causeToFormulas);
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.ATTRIBUTE_RULES;
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of(LoadingStages.ATTRIBUTE);
        }

        private DamageFormula compile(String path, String formula) {
            Expression expression = new Expression(formula);
            Set<String> usedVariables;
            try {
                usedVariables = expression.getUsedVariables();
            } catch (ParseException e) {
                throw new KnownResourceException("TODO", path, formula);
            }
            List<DamageFormula.VariableBinding> bindings = new ArrayList<>();
            for (String variable : usedVariables) {
                switch (variable) {
                    case "damage" ->
                            bindings.add(DamageFormula.VariableBinding.field(variable, DamageFormula.VariableBinding.FIELD_DAMAGE));
                    case "is_critical" ->
                            bindings.add(DamageFormula.VariableBinding.field(variable, DamageFormula.VariableBinding.FIELD_IS_CRITICAL));
                    default -> {
                        if (variable.startsWith(DamageFormula.ATTACKER_PREFIX)) {
                            bindings.add(DamageFormula.VariableBinding.attribute(variable, AttributeSide.ATTACKER, getAttribute(Key.of(variable.substring(DamageFormula.ATTACKER_PREFIX.length()))).orElseThrow(() -> new KnownResourceException("TODO", path, formula, variable))));
                        } else if (variable.startsWith(DamageFormula.VICTIM_PREFIX)) {
                            bindings.add(DamageFormula.VariableBinding.attribute(variable, AttributeSide.VICTIM, getAttribute(Key.of(variable.substring(DamageFormula.VICTIM_PREFIX.length()))).orElseThrow(() -> new KnownResourceException("TODO", path, formula, variable))));
                        } else {
                            throw new KnownResourceException("attribute.formula.unknown_variable", formula, variable);
                        }
                    }
                }
            }
            return new DamageFormula(formula, expression, bindings);
        }
    }

    private final class AttributeParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"attributes", "attribute"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.ATTRIBUTE;
        }

        @Override
        public int count() {
            return AbstractAttributeManager.this.configAttributes.size();
        }

        @Override
        public void postProcess() {
            Map<Key, Attribute> attributes = new HashMap<>();
            attributes.putAll(AbstractAttributeManager.this.apiAttributes);
            attributes.putAll(AbstractAttributeManager.this.configAttributes);
            AbstractAttributeManager.this.mergedAttributes = attributes;
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            double defaultValue = section.getDouble("base", 0d);
            ConfigSection constraintSection = section.getSection("constraint");
            ValueConstraint constraint = ValueConstraint.noLimit();
            if (constraintSection != null) {
                constraint = ValueConstraint.clamp(
                        constraintSection.getDouble("min", Double.MIN_VALUE),
                        constraintSection.getDouble("max", Double.MAX_VALUE)
                );
            }
            VanillaAttributeSync sync = null;
            ConfigSection syncSection = section.getSection("sync");
            if (syncSection != null) {
                sync = new VanillaAttributeSync(
                        syncSection.getNonNullKey("target"),
                        new Expression(section.getString("value", "value"))
                );
            }
            Attribute attribute = new Attribute(id, defaultValue, constraint, sync);
            AbstractAttributeManager.this.configAttributes.put(id, attribute);
        }
    }

    private final class OperationParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"attribute_operations", "attribute-operations"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.ATTRIBUTE_OPERATION;
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of(LoadingStages.ATTRIBUTE);
        }

        @Override
        public int count() {
            return AbstractAttributeManager.this.configOperations.size();
        }

        @Override
        public void postProcess() {
            AbstractAttributeManager.this.rebuildSortedOperations();
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            int order = section.getNonNullInt("order");
            String expression = section.getNonNullString("expression");
            AbstractAttributeManager.this.configOperations.put(id, AttributeOperation.expression(id, order, expression));
        }
    }
}
