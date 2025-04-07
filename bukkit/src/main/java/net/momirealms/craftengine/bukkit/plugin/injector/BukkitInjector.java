package net.momirealms.craftengine.bukkit.plugin.injector;

import com.mojang.datafixers.util.Pair;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.block.BukkitBlockShape;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.recipe.BukkitRecipeManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.NoteBlockChainUpdateUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.block.BlockKeys;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.StatePredicate;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CustomCookingRecipe;
import net.momirealms.craftengine.core.item.recipe.OptimizedIDItem;
import net.momirealms.craftengine.core.item.recipe.RecipeTypes;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.SectionPosUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.InjectedPalettedContainerHolder;
import net.momirealms.craftengine.shared.ObjectHolder;
import net.momirealms.craftengine.shared.block.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

public class BukkitInjector {
    private static final ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V17);
    private static final BukkitBlockShape STONE_SHAPE = new BukkitBlockShape(Reflections.instance$Blocks$STONE$defaultState);

    private static Class<?> clazz$InjectedPalettedContainer;

    private static VarHandle varHandle$InjectedPalettedContainer$target;

    private static Class<?> clazz$OptimizedItemDisplay;
    private static Constructor<?> constructor$OptimizedItemDisplay;

    private static Class<?> clazz$OptimizedItemDisplayFatory;
    private static Object instance$OptimizedItemDisplayFactory;

    private static Class<?> clazz$CraftEngineBlock;
    private static MethodHandle constructor$CraftEngineBlock;
    private static Field field$CraftEngineBlock$behavior;
    private static Field field$CraftEngineBlock$shape;
    private static Field field$CraftEngineBlock$isNoteBlock;

    private static Class<?> clazz$InjectedCacheChecker;

    private static InternalFieldAccessor internalFieldAccessor;

    public static void init() {
        try {
            // Paletted Container
            clazz$InjectedPalettedContainer = byteBuddy
                    .subclass(Reflections.clazz$PalettedContainer)
                    .name("net.minecraft.world.level.chunk.InjectedPalettedContainer")
                    .implement(InjectedPalettedContainerHolder.class)
                    .defineField("target", Reflections.clazz$PalettedContainer, Visibility.PRIVATE)
                    .defineField("cesection", CESection.class, Visibility.PRIVATE)
                    .defineField("ceworld", CEWorld.class, Visibility.PRIVATE)
                    .defineField("cepos", SectionPos.class, Visibility.PRIVATE)
                    .method(ElementMatchers.any()
                            .and(ElementMatchers.not(ElementMatchers.is(Reflections.method$PalettedContainer$getAndSet)))
                            .and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                            // TODO Requires Paper Patch
                            //.and(ElementMatchers.not(ElementMatchers.named("get").and(ElementMatchers.takesArguments(int.class)).and(ElementMatchers.returns(Object.class))))
                    )
                    .intercept(MethodDelegation.toField("target"))
                    .method(ElementMatchers.is(Reflections.method$PalettedContainer$getAndSet))
                    .intercept(MethodDelegation.to(GetAndSetInterceptor.INSTANCE))
                    .method(ElementMatchers.named("target"))
                    .intercept(FieldAccessor.ofField("target"))
                    .method(ElementMatchers.named("ceSection"))
                    .intercept(FieldAccessor.ofField("cesection"))
                    .method(ElementMatchers.named("ceWorld"))
                    .intercept(FieldAccessor.ofField("ceworld"))
                    .method(ElementMatchers.named("cePos"))
                    .intercept(FieldAccessor.ofField("cepos"))
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();

            varHandle$InjectedPalettedContainer$target = Objects.requireNonNull(ReflectionUtils.findVarHandle(clazz$InjectedPalettedContainer, "target", Reflections.clazz$PalettedContainer));

            // State Predicate
            DynamicType.Unloaded<?> alwaysTrue = byteBuddy
                    .subclass(Reflections.clazz$StatePredicate)
                    .method(ElementMatchers.named("test"))
                    .intercept(FixedValue.value(true))
                    .make();
            Class<?> alwaysTrueClass = alwaysTrue.load(BukkitInjector.class.getClassLoader()).getLoaded();
            DynamicType.Unloaded<?> alwaysFalse = byteBuddy
                    .subclass(Reflections.clazz$StatePredicate)
                    .method(ElementMatchers.named("test"))
                    .intercept(FixedValue.value(false))
                    .make();
            Class<?> alwaysFalseClass = alwaysFalse.load(BukkitInjector.class.getClassLoader()).getLoaded();
            StatePredicate.init(alwaysTrueClass.getDeclaredConstructor().newInstance(), alwaysFalseClass.getDeclaredConstructor().newInstance());
            // Optimized Item Display
            clazz$OptimizedItemDisplay = byteBuddy
                    .subclass(Reflections.clazz$Display$ItemDisplay, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name("net.minecraft.world.entity.OptimizedItemDisplay")
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();
            constructor$OptimizedItemDisplay = ReflectionUtils.getConstructor(clazz$OptimizedItemDisplay, Reflections.clazz$EntityType, Reflections.clazz$Level);
            clazz$OptimizedItemDisplayFatory = byteBuddy
                    .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name("net.momirealms.craftengine.bukkit.entity.OptimizedItemDisplayFactory")
                    .implement(Reflections.clazz$EntityType$EntityFactory)
                    .method(ElementMatchers.named("create"))
                    .intercept(MethodDelegation.to(OptimizedItemDisplayMethodInterceptor.INSTANCE))
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();
            instance$OptimizedItemDisplayFactory = Objects.requireNonNull(ReflectionUtils.getConstructor(clazz$OptimizedItemDisplayFatory, 0)).newInstance();

            // InternalFieldAccessor Interface
            Class<?> internalFieldAccessorInterface = new ByteBuddy()
                    .makeInterface()
                    .name("net.momirealms.craftengine.bukkit.plugin.injector.InternalFieldAccessor")
                    .defineMethod("field$ClientboundMoveEntityPacket$entityId", int.class, Modifier.PUBLIC)
                    .withParameter(Object.class, "packet")
                    .withoutCode()
                    .make()
                    .load(Reflections.clazz$ClientboundMoveEntityPacket.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();

            // Internal field accessor
            FieldDescription moveEntityIdFieldDesc = new FieldDescription.ForLoadedField(Reflections.field$ClientboundMoveEntityPacket$entityId);
            Class<?> clazz$InternalFieldAccessor = byteBuddy
                    .subclass(Object.class)
                    .name("net.minecraft.network.protocol.game.CraftEngineInternalFieldAccessor")
                    .implement(internalFieldAccessorInterface)
                    .method(ElementMatchers.named("field$ClientboundMoveEntityPacket$entityId"))
                    .intercept(new Implementation.Simple(
                            MethodVariableAccess.REFERENCE.loadFrom(1),
                            TypeCasting.to(TypeDescription.ForLoadedType.of(Reflections.clazz$ClientboundMoveEntityPacket)),
                            FieldAccess.forField(moveEntityIdFieldDesc).read(),
                            MethodReturn.INTEGER
                    ))
                    .make()
                    .load(Reflections.clazz$ClientboundMoveEntityPacket.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
            internalFieldAccessor = (InternalFieldAccessor) clazz$InternalFieldAccessor.getConstructor().newInstance();

            // CraftEngine Blocks
            String packageWithName = BukkitInjector.class.getName();
            String generatedClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + ".CraftEngineBlock";
            DynamicType.Builder<?> builder = byteBuddy
                    .subclass(Reflections.clazz$Block, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name(generatedClassName)
                    .defineField("behaviorHolder", ObjectHolder.class, Visibility.PUBLIC)
                    .defineField("shapeHolder", ObjectHolder.class, Visibility.PUBLIC)
                    .defineField("isClientSideNoteBlock", boolean.class, Visibility.PUBLIC)
                    // should always implement this interface
                    .implement(Reflections.clazz$Fallable)
                    .implement(Reflections.clazz$BonemealableBlock)
                    // TODO .implement(Reflections.clazz$SimpleWaterloggedBlock)
                    // internal interfaces
                    .implement(BehaviorHolder.class)
                    .implement(ShapeHolder.class)
                    .implement(NoteBlockIndicator.class)
                    .method(ElementMatchers.named("getBehaviorHolder"))
                    .intercept(FieldAccessor.ofField("behaviorHolder"))
                    .method(ElementMatchers.named("getShapeHolder"))
                    .intercept(FieldAccessor.ofField("shapeHolder"))
                    .method(ElementMatchers.named("isNoteBlock"))
                    .intercept(FieldAccessor.ofField("isClientSideNoteBlock"))
                    // getShape
                    .method(ElementMatchers.is(Reflections.method$BlockBehaviour$getShape))
                    .intercept(MethodDelegation.to(GetShapeInterceptor.INSTANCE))
                    // mirror
                    .method(ElementMatchers.is(Reflections.method$BlockBehaviour$mirror))
                    .intercept(MethodDelegation.to(MirrorInterceptor.INSTANCE))
                    // rotate
                    .method(ElementMatchers.is(Reflections.method$BlockBehaviour$rotate))
                    .intercept(MethodDelegation.to(RotateInterceptor.INSTANCE))
                    // tick
                    .method(ElementMatchers.is(Reflections.method$BlockBehaviour$tick))
                    .intercept(MethodDelegation.to(TickInterceptor.INSTANCE))
                    // isValidBoneMealTarget
                    .method(ElementMatchers.is(Reflections.method$BonemealableBlock$isValidBonemealTarget))
                    .intercept(MethodDelegation.to(IsValidBoneMealTargetInterceptor.INSTANCE))
                    // isBoneMealSuccess
                    .method(ElementMatchers.is(Reflections.method$BonemealableBlock$isBonemealSuccess))
                    .intercept(MethodDelegation.to(IsBoneMealSuccessInterceptor.INSTANCE))
                    // performBoneMeal
                    .method(ElementMatchers.is(Reflections.method$BonemealableBlock$performBonemeal))
                    .intercept(MethodDelegation.to(PerformBoneMealInterceptor.INSTANCE))
//                    // pickupBlock
//                    .method(ElementMatchers.is(Reflections.method$SimpleWaterloggedBlock$pickupBlock))
//                    .intercept(MethodDelegation.to(PickUpBlockInterceptor.INSTANCE))
//                    // placeLiquid
//                    .method(ElementMatchers.is(Reflections.method$SimpleWaterloggedBlock$placeLiquid))
//                    .intercept(MethodDelegation.to(PlaceLiquidInterceptor.INSTANCE))
//                    // canPlaceLiquid
//                    .method(ElementMatchers.is(Reflections.method$SimpleWaterloggedBlock$canPlaceLiquid))
//                    .intercept(MethodDelegation.to(CanPlaceLiquidInterceptor.INSTANCE))
                    // random tick
                    .method(ElementMatchers.is(Reflections.method$BlockBehaviour$randomTick))
                    .intercept(MethodDelegation.to(RandomTickInterceptor.INSTANCE))
                    // onPlace
                    .method(ElementMatchers.takesArguments(5)
                            .and(ElementMatchers.takesArgument(0, Reflections.clazz$BlockState))
                            .and(ElementMatchers.takesArgument(1, Reflections.clazz$Level))
                            .and(ElementMatchers.takesArgument(2, Reflections.clazz$BlockPos))
                            .and(ElementMatchers.takesArgument(3, Reflections.clazz$BlockState))
                            .and(ElementMatchers.takesArgument(4, boolean.class))
                            .and(ElementMatchers.named("onPlace").or(ElementMatchers.named("a")))
                    )
                    .intercept(MethodDelegation.to(OnPlaceInterceptor.INSTANCE))
                    // onBrokenAfterFall
                    .method(ElementMatchers.takesArguments(3)
                            .and(ElementMatchers.takesArgument(0, Reflections.clazz$Level))
                            .and(ElementMatchers.takesArgument(1, Reflections.clazz$BlockPos))
                            .and(ElementMatchers.takesArgument(2, Reflections.clazz$FallingBlockEntity))
                    )
                    .intercept(MethodDelegation.to(OnBrokenAfterFallInterceptor.INSTANCE))
                    // onLand
                    .method(ElementMatchers.takesArguments(5)
                            .and(ElementMatchers.takesArgument(0, Reflections.clazz$Level))
                            .and(ElementMatchers.takesArgument(1, Reflections.clazz$BlockPos))
                            .and(ElementMatchers.takesArgument(2, Reflections.clazz$BlockState))
                            .and(ElementMatchers.takesArgument(3, Reflections.clazz$BlockState))
                            .and(ElementMatchers.takesArgument(4, Reflections.clazz$FallingBlockEntity))
                    )
                    .intercept(MethodDelegation.to(OnLandInterceptor.INSTANCE))
                    // canSurvive
                    .method(ElementMatchers.takesArguments(3)
                            .and(ElementMatchers.takesArgument(0, Reflections.clazz$BlockState))
                            .and(ElementMatchers.takesArgument(1, Reflections.clazz$LevelReader))
                            .and(ElementMatchers.takesArgument(2, Reflections.clazz$BlockPos))
                    )
                    .intercept(MethodDelegation.to(CanSurviveInterceptor.INSTANCE))
                    // updateShape
                    .method(ElementMatchers.returns(Reflections.clazz$BlockState)
                            .and(ElementMatchers.takesArgument(0, Reflections.clazz$BlockState))
                            // LevelReader 1.21.3+                                                     // 1.20-1.12.2
                            .and(ElementMatchers.takesArgument(1, Reflections.clazz$LevelReader).or(ElementMatchers.takesArgument(1, Reflections.clazz$Direction)))
                            .and(ElementMatchers.named("updateShape").or(ElementMatchers.named("a"))))
                    .intercept(MethodDelegation.to(UpdateShapeInterceptor.INSTANCE))
//                    // getFluidState
//                    .method(ElementMatchers.returns(Reflections.clazz$FluidState)
//                            .and(ElementMatchers.takesArgument(0, Reflections.clazz$BlockState)))
//                    .intercept(MethodDelegation.to(FluidStateInterceptor.INSTANCE))
                    ;
            clazz$CraftEngineBlock = builder.make().load(BukkitInjector.class.getClassLoader()).getLoaded();

            constructor$CraftEngineBlock = MethodHandles.publicLookup().in(clazz$CraftEngineBlock)
                    .findConstructor(clazz$CraftEngineBlock, MethodType.methodType(void.class, Reflections.clazz$BlockBehaviour$Properties))
                    .asType(MethodType.methodType(Reflections.clazz$Block, Reflections.clazz$BlockBehaviour$Properties));

            field$CraftEngineBlock$behavior = clazz$CraftEngineBlock.getField("behaviorHolder");
            field$CraftEngineBlock$shape = clazz$CraftEngineBlock.getField("shapeHolder");
            field$CraftEngineBlock$isNoteBlock = clazz$CraftEngineBlock.getField("isClientSideNoteBlock");

            clazz$InjectedCacheChecker = byteBuddy
                    .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                    .name("net.momirealms.craftengine.bukkit.entity.InjectedCacheChecker")
                    .implement(Reflections.clazz$RecipeManager$CachedCheck)
                    .implement(InjectedCacheCheck.class)
                    .defineField("recipeType", Object.class, Visibility.PUBLIC)
                    .method(ElementMatchers.named("recipeType"))
                    .intercept(FieldAccessor.ofField("recipeType"))
                    .defineField("lastRecipe", Object.class, Visibility.PUBLIC)
                    .method(ElementMatchers.named("lastRecipe"))
                    .intercept(FieldAccessor.ofField("lastRecipe"))
                    .method(ElementMatchers.named("setLastRecipe"))
                    .intercept(FieldAccessor.ofField("lastRecipe"))
                    .defineField("lastCustomRecipe", Key.class, Visibility.PUBLIC)
                    .method(ElementMatchers.named("lastCustomRecipe"))
                    .intercept(FieldAccessor.ofField("lastCustomRecipe"))
                    .method(ElementMatchers.named("getRecipeFor").or(ElementMatchers.named("a")))
                    .intercept(MethodDelegation.to(
                            VersionHelper.isVersionNewerThan1_21_2() ?
                                    GetRecipeForMethodInterceptor1_21_2.INSTANCE :
                                    (VersionHelper.isVersionNewerThan1_21() ?
                                            GetRecipeForMethodInterceptor1_21.INSTANCE :
                                            VersionHelper.isVersionNewerThan1_20_5() ?
                                                    GetRecipeForMethodInterceptor1_20_5.INSTANCE :
                                                    GetRecipeForMethodInterceptor1_20.INSTANCE)
                    ))
                    .make()
                    .load(BukkitInjector.class.getClassLoader())
                    .getLoaded();
        } catch (Throwable e) {
            CraftEngine.instance().logger().severe("Failed to init injector", e);
        }
    }

    public static InternalFieldAccessor internalFieldAccessor() {
        return internalFieldAccessor;
    }

    public static void injectCookingBlockEntity(Object entity) throws ReflectiveOperationException {
        if (Reflections.clazz$AbstractFurnaceBlockEntity.isInstance(entity)) {
            Object quickCheck = Reflections.field$AbstractFurnaceBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            Object recipeType = FastNMS.INSTANCE.field$AbstractFurnaceBlockEntity$recipeType(entity);
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) Reflections.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            injectedChecker.recipeType(recipeType);
            Reflections.field$AbstractFurnaceBlockEntity$quickCheck.set(entity, injectedChecker);
        } else if (!VersionHelper.isVersionNewerThan1_21_2() && Reflections.clazz$CampfireBlockEntity.isInstance(entity)) {
            Object quickCheck = Reflections.field$CampfireBlockEntity$quickCheck.get(entity);
            if (clazz$InjectedCacheChecker.isInstance(quickCheck)) return; // already injected
            InjectedCacheCheck injectedChecker = (InjectedCacheCheck) Reflections.UNSAFE.allocateInstance(clazz$InjectedCacheChecker);
            injectedChecker.recipeType(Reflections.instance$RecipeType$CAMPFIRE_COOKING);
            Reflections.field$CampfireBlockEntity$quickCheck.set(entity, injectedChecker);
        }
    }

    public static Object getOptimizedItemDisplayFactory() {
        return instance$OptimizedItemDisplayFactory;
    }

    public static class OptimizedItemDisplayMethodInterceptor {
        public static final OptimizedItemDisplayMethodInterceptor INSTANCE = new OptimizedItemDisplayMethodInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) throws Exception {
            return constructor$OptimizedItemDisplay.newInstance(args[0], args[1]);
        }
    }

    public static void injectLevelChunkSection(Object targetSection, CESection ceSection, CEWorld ceWorld, SectionPos pos) {
        try {
            Object container = FastNMS.INSTANCE.field$LevelChunkSection$states(targetSection);
            if (!clazz$InjectedPalettedContainer.isInstance(container)) {
                InjectedPalettedContainerHolder injectedObject = (InjectedPalettedContainerHolder) Reflections.UNSAFE.allocateInstance(clazz$InjectedPalettedContainer);
                varHandle$InjectedPalettedContainer$target.set(injectedObject, container);
                injectedObject.ceSection(ceSection);
                injectedObject.ceWorld(ceWorld);
                injectedObject.cePos(pos);
                Reflections.varHandle$PalettedContainer$data.setVolatile(injectedObject, Reflections.varHandle$PalettedContainer$data.get(container));
                Reflections.field$LevelChunkSection$states.set(targetSection, injectedObject);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().severe("Failed to inject chunk section", e);
        }
    }

    public static void uninjectLevelChunkSection(Object section) {
        try {
            Object states = FastNMS.INSTANCE.field$LevelChunkSection$states(section);
            if (states instanceof InjectedPalettedContainerHolder holder) {
                Reflections.field$LevelChunkSection$states.set(section, holder.target());
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().severe("Failed to inject chunk section", e);
        }
    }

    public static class GetRecipeForMethodInterceptor1_20 {
        public static final GetRecipeForMethodInterceptor1_20 INSTANCE = new GetRecipeForMethodInterceptor1_20();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Pair<Object, Object>> optionalRecipe = (Optional<Pair<Object, Object>>) Reflections.method$RecipeManager$getRecipeFor0.invoke(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Pair<Object, Object> pair = optionalRecipe.get();
                Object resourceLocation = pair.getFirst();
                Key recipeId = Key.of(resourceLocation.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();

                ItemStack itemStack;
                List<Object> items;
                if (type == Reflections.instance$RecipeType$CAMPFIRE_COOKING) {
                    items = (List<Object>) Reflections.field$SimpleContainer$items.get(args[0]);
                } else {
                    items = (List<Object>) Reflections.field$AbstractFurnaceBlockEntity$items.get(args[0]);
                }
                itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(items.get(0));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(resourceLocation);
                    return Optional.of(pair.getSecond());
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == Reflections.instance$RecipeType$SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$CAMPFIRE_COOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.CAMPFIRE_COOKING, input, lastCustomRecipe);
                } else  {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(resourceLocation);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(pair.getSecond()));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class GetRecipeForMethodInterceptor1_20_5 {
        public static final GetRecipeForMethodInterceptor1_20_5 INSTANCE = new GetRecipeForMethodInterceptor1_20_5();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) Reflections.method$RecipeManager$getRecipeFor0.invoke(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Object holder = optionalRecipe.get();
                Object id = FastNMS.INSTANCE.field$RecipeHolder$id(holder);
                Key recipeId = Key.of(id.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();

                ItemStack itemStack;
                List<Object> items;
                if (type == Reflections.instance$RecipeType$CAMPFIRE_COOKING) {
                    items = (List<Object>) Reflections.field$SimpleContainer$items.get(args[0]);
                } else {
                    items = (List<Object>) Reflections.field$AbstractFurnaceBlockEntity$items.get(args[0]);
                }
                itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(items.get(0));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(id);
                    return optionalRecipe;
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == Reflections.instance$RecipeType$SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$CAMPFIRE_COOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.CAMPFIRE_COOKING, input, lastCustomRecipe);
                } else  {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(id);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(holder));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class GetRecipeForMethodInterceptor1_21 {
        public static final GetRecipeForMethodInterceptor1_21 INSTANCE = new GetRecipeForMethodInterceptor1_21();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) Reflections.method$RecipeManager$getRecipeFor0.invoke(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Object holder = optionalRecipe.get();
                Object id = FastNMS.INSTANCE.field$RecipeHolder$id(holder);
                Key recipeId = Key.of(id.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
                ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(Reflections.field$SingleRecipeInput$item.get(args[0]));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(id);
                    return optionalRecipe;
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == Reflections.instance$RecipeType$SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$CAMPFIRE_COOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.CAMPFIRE_COOKING, input, lastCustomRecipe);
                } else  {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(id);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(holder));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class GetRecipeForMethodInterceptor1_21_2 {
        public static final GetRecipeForMethodInterceptor1_21_2 INSTANCE = new GetRecipeForMethodInterceptor1_21_2();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) throws Exception {
            Object mcRecipeManager = BukkitRecipeManager.nmsRecipeManager();
            InjectedCacheCheck injectedCacheCheck = (InjectedCacheCheck) thisObj;
            Object type = injectedCacheCheck.recipeType();
            Object lastRecipe = injectedCacheCheck.lastRecipe();
            Optional<Object> optionalRecipe = (Optional<Object>) Reflections.method$RecipeManager$getRecipeFor1.invoke(mcRecipeManager, type, args[0], args[1], lastRecipe);
            if (optionalRecipe.isPresent()) {
                Object holder = optionalRecipe.get();
                Object id = FastNMS.INSTANCE.field$RecipeHolder$id(holder);
                Object resourceLocation = FastNMS.INSTANCE.field$ResourceKey$location(id);
                Key recipeId = Key.of(resourceLocation.toString());
                BukkitRecipeManager recipeManager = BukkitRecipeManager.instance();
                ItemStack itemStack = FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(Reflections.field$SingleRecipeInput$item.get(args[0]));

                // it's a recipe from other plugins
                boolean isCustom = recipeManager.isCustomRecipe(recipeId);
                if (!isCustom) {
                    injectedCacheCheck.lastRecipe(id);
                    return optionalRecipe;
                }

                Item<ItemStack> wrappedItem = BukkitItemManager.instance().wrap(itemStack);
                Optional<Holder.Reference<Key>> idHolder = BuiltInRegistries.OPTIMIZED_ITEM_ID.get(wrappedItem.id());
                if (idHolder.isEmpty()) {
                    return Optional.empty();
                }

                SingleItemInput<ItemStack> input = new SingleItemInput<>(new OptimizedIDItem<>(idHolder.get(), itemStack));
                CustomCookingRecipe<ItemStack> ceRecipe;
                Key lastCustomRecipe = injectedCacheCheck.lastCustomRecipe();
                if (type == Reflections.instance$RecipeType$SMELTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMELTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$BLASTING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.BLASTING, input, lastCustomRecipe);
                } else if (type == Reflections.instance$RecipeType$SMOKING) {
                    ceRecipe = (CustomCookingRecipe<ItemStack>) recipeManager.recipeByInput(RecipeTypes.SMOKING, input, lastCustomRecipe);
                } else {
                    return Optional.empty();
                }
                if (ceRecipe == null) {
                    return Optional.empty();
                }

                // Cache recipes, it might be incorrect on reloading
                injectedCacheCheck.lastCustomRecipe(ceRecipe.id());
                // It doesn't matter at all
                injectedCacheCheck.lastRecipe(id);
                return Optional.of(Optional.ofNullable(recipeManager.nmsRecipeHolderByRecipe(ceRecipe)).orElse(holder));
            } else {
                return Optional.empty();
            }
        }
    }

    public static class GetAndSetInterceptor {
        public static final GetAndSetInterceptor INSTANCE = new GetAndSetInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            InjectedPalettedContainerHolder holder = (InjectedPalettedContainerHolder) thisObj;
            Object targetStates = holder.target();
            int x = (int) args[0];
            int y = (int) args[1];
            int z = (int) args[2];
            Object previousState = FastNMS.INSTANCE.method$PalettedContainer$getAndSet(targetStates, x, y, z, args[3]);
            try {
                Object newState = args[3];
                int stateId = BlockStateUtils.blockStateToId(newState);
                CESection section = holder.ceSection();
                if (BlockStateUtils.isVanillaBlock(stateId)) {
                    section.setBlockState(x, y, z, EmptyBlock.INSTANCE.defaultState());
                    if (Config.enableLightSystem() && Config.forceUpdateLight()) {
                        updateLightIfChanged(holder, previousState, newState, null, y, z, x);
                    }
                } else {
                    ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockStateUnsafe(stateId);
                    section.setBlockState(x, y, z, immutableBlockState);
                    if (!immutableBlockState.isEmpty()) {
                        if (Config.enableLightSystem()) {
                            updateLightIfChanged(holder, previousState, newState, immutableBlockState.vanillaBlockState().handle(), y, z, x);
                        }
                    }
                }
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Failed to intercept setBlockState", e);
            }
            return previousState;
        }

        private void updateLightIfChanged(@This InjectedPalettedContainerHolder thisObj, Object previousBlockState, Object newState, @Nullable Object clientSideNewState, int y, int z, int x) throws ReflectiveOperationException {
            int previousLight = BlockStateUtils.getLightEmission(previousBlockState);
            int newLight = BlockStateUtils.getLightEmission(newState);
            if (previousLight != newLight || (clientSideNewState != null && (BlockStateUtils.isOcclude(newState) != BlockStateUtils.isOcclude(clientSideNewState)))) {
                CEWorld world = thisObj.ceWorld();
                SectionPos sectionPos = thisObj.cePos();
                Set<SectionPos> posSet = SectionPosUtils.calculateAffectedRegions((sectionPos.x() << 4) + x, (sectionPos.y() << 4) + y, (sectionPos.z() << 4) + z, Math.max(newLight, previousLight));
                world.sectionLightUpdated(posSet);
            }
        }
    }

    public static Object generateBlock(Key replacedBlock, Object ownerBlock, Object properties) throws Throwable {
        Object ownerProperties = Reflections.field$BlockBehaviour$properties.get(ownerBlock);
        Reflections.field$BlockBehaviour$Properties$hasCollision.set(properties, Reflections.field$BlockBehaviour$Properties$hasCollision.get(ownerProperties));

        ObjectHolder<BlockBehavior> behaviorHolder = new ObjectHolder<>(EmptyBlockBehavior.INSTANCE);
        ObjectHolder<BlockShape> shapeHolder = new ObjectHolder<>(STONE_SHAPE);

        Object newBlockInstance = constructor$CraftEngineBlock.invoke(properties);
        field$CraftEngineBlock$behavior.set(newBlockInstance, behaviorHolder);
        field$CraftEngineBlock$shape.set(newBlockInstance, shapeHolder);
        field$CraftEngineBlock$isNoteBlock.set(newBlockInstance, replacedBlock.equals(BlockKeys.NOTE_BLOCK));
        return newBlockInstance;
    }

//
//    public static class FluidStateInterceptor {
//        public static final FluidStateInterceptor INSTANCE = new FluidStateInterceptor();
//
//        @RuntimeType
//        public Object intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
//            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
//            try {
//                return holder.value().getFluidState(thisObj, args, superMethod);
//            } catch (Exception e) {
//                CraftEngine.instance().logger().severe("Failed to run getFluidState", e);
//                return args[0];
//            }
//        }
//    }

    public static class UpdateShapeInterceptor {
        public static final UpdateShapeInterceptor INSTANCE = new UpdateShapeInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) throws Exception {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            if (((NoteBlockIndicator) thisObj).isNoteBlock()) {
                startNoteBlockChain(args);
            }
            try {
                return holder.value().updateShape(thisObj, args, superMethod);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run updateShape", e);
                return args[0];
            }
        }
    }

    private static void startNoteBlockChain(Object[] args) throws ReflectiveOperationException {
        Object direction;
        Object serverLevel;
        Object blockPos;
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            direction = args[4];
            serverLevel = args[1];
            blockPos = args[3];
        } else {
            direction = args[1];
            serverLevel = args[3];
            blockPos = args[4];
        }
        int id = (int) Reflections.field$Direction$data3d.get(direction);
        // Y axis
        if (id == 0 || id == 1) {
            Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(serverLevel);
            FastNMS.INSTANCE.method$ServerChunkCache$blockChanged(chunkSource, blockPos);
            if (id == 1) {
                NoteBlockChainUpdateUtils.noteBlockChainUpdate(serverLevel, chunkSource, Reflections.instance$Direction$DOWN, blockPos, 0);
            } else {
                NoteBlockChainUpdateUtils.noteBlockChainUpdate(serverLevel, chunkSource, Reflections.instance$Direction$UP, blockPos, 0);
            }
        }
    }

    public static class GetShapeInterceptor {
        public static final GetShapeInterceptor INSTANCE = new GetShapeInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) throws Exception {
            ObjectHolder<BlockShape> holder = ((ShapeHolder) thisObj).getShapeHolder();
            try {
                return holder.value().getShape(thisObj, args);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run getShape", e);
                return superMethod.call();
            }
        }
    }

    public static class MirrorInterceptor {
        public static final MirrorInterceptor INSTANCE = new MirrorInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) throws Exception {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                return holder.value().mirror(thisObj, args, superMethod);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run mirror", e);
                return superMethod.call();
            }
        }
    }

    public static class RotateInterceptor {
        public static final RotateInterceptor INSTANCE = new RotateInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) throws Exception {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                return holder.value().rotate(thisObj, args, superMethod);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run rotate", e);
                return superMethod.call();
            }
        }
    }

    public static class RandomTickInterceptor {
        public static final RandomTickInterceptor INSTANCE = new RandomTickInterceptor();

        @RuntimeType
        public void intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                holder.value().randomTick(thisObj, args, superMethod);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run randomTick", e);
            }
        }
    }

    public static class TickInterceptor {
        public static final TickInterceptor INSTANCE = new TickInterceptor();

        @RuntimeType
        public void intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                holder.value().tick(thisObj, args, superMethod);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run tick", e);
            }
        }
    }

    public static class OnPlaceInterceptor {
        public static final OnPlaceInterceptor INSTANCE = new OnPlaceInterceptor();

        @RuntimeType
        public void intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                holder.value().onPlace(thisObj, args, superMethod);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run onPlace", e);
            }
        }
    }

    public static class OnLandInterceptor {
        public static final OnLandInterceptor INSTANCE = new OnLandInterceptor();

        @RuntimeType
        public void intercept(@This Object thisObj, @AllArguments Object[] args) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                holder.value().onLand(thisObj, args);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run onLand", e);
            }
        }
    }

    public static class OnBrokenAfterFallInterceptor {
        public static final OnBrokenAfterFallInterceptor INSTANCE = new OnBrokenAfterFallInterceptor();

        @RuntimeType
        public void intercept(@This Object thisObj, @AllArguments Object[] args) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                holder.value().onBrokenAfterFall(thisObj, args);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run onBrokenAfterFall", e);
            }
        }
    }

    public static class CanSurviveInterceptor {
        public static final CanSurviveInterceptor INSTANCE = new CanSurviveInterceptor();

        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                return holder.value().canSurvive(thisObj, args, superMethod);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run canSurvive", e);
                return true;
            }
        }
    }

    public static class IsBoneMealSuccessInterceptor {
        public static final IsBoneMealSuccessInterceptor INSTANCE = new IsBoneMealSuccessInterceptor();

        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                return holder.value().isBoneMealSuccess(thisObj, args);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run isBoneMealSuccess", e);
                return true;
            }
        }
    }

    public static class IsValidBoneMealTargetInterceptor {
        public static final IsValidBoneMealTargetInterceptor INSTANCE = new IsValidBoneMealTargetInterceptor();

        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                return holder.value().isValidBoneMealTarget(thisObj, args);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run isValidBoneMealTarget", e);
                return true;
            }
        }
    }

    public static class PerformBoneMealInterceptor {
        public static final PerformBoneMealInterceptor INSTANCE = new PerformBoneMealInterceptor();

        @RuntimeType
        public void intercept(@This Object thisObj, @AllArguments Object[] args) {
            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
            try {
                holder.value().performBoneMeal(thisObj, args);
            } catch (Exception e) {
                CraftEngine.instance().logger().severe("Failed to run performBoneMeal", e);
            }
        }
    }
//
//    public static class PickUpBlockInterceptor {
//        public static final PickUpBlockInterceptor INSTANCE = new PickUpBlockInterceptor();
//
//        @RuntimeType
//        public void intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
//            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
//            try {
//                holder.value().pickupBlock(thisObj, args, superMethod);
//            } catch (Exception e) {
//                CraftEngine.instance().logger().severe("Failed to run pickupBlock", e);
//            }
//        }
//    }
//
//    public static class PlaceLiquidInterceptor {
//        public static final PlaceLiquidInterceptor INSTANCE = new PlaceLiquidInterceptor();
//
//        @RuntimeType
//        public void intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
//            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
//            try {
//                holder.value().placeLiquid(thisObj, args, superMethod);
//            } catch (Exception e) {
//                CraftEngine.instance().logger().severe("Failed to run placeLiquid", e);
//            }
//        }
//    }
//
//    public static class CanPlaceLiquidInterceptor {
//        public static final CanPlaceLiquidInterceptor INSTANCE = new CanPlaceLiquidInterceptor();
//
//        @RuntimeType
//        public void intercept(@This Object thisObj, @AllArguments Object[] args, @SuperCall Callable<Object> superMethod) {
//            ObjectHolder<BlockBehavior> holder = ((BehaviorHolder) thisObj).getBehaviorHolder();
//            try {
//                holder.value().canPlaceLiquid(thisObj, args, superMethod);
//            } catch (Exception e) {
//                CraftEngine.instance().logger().severe("Failed to run canPlaceLiquid", e);
//            }
//        }
//    }
}
