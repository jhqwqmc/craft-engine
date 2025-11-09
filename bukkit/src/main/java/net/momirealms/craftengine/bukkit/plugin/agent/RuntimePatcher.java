package net.momirealms.craftengine.bukkit.plugin.agent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.Opcodes;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RuntimePatcher {

    public static void patch(BukkitCraftEngine plugin) throws ReflectiveOperationException {
        Class<?> holderClass = new ByteBuddy()
                .subclass(Object.class)
                .name("net.momirealms.craftengine.bukkit.plugin.agent.PluginHolder")
                .defineField("plugin", Object.class, Modifier.PUBLIC | Modifier.STATIC)
                .defineMethod("setPlugin", void.class, Modifier.PUBLIC | Modifier.STATIC)
                .withParameters(Object.class)
                .intercept(new Implementation() {
                    @Override
                    public @NotNull InstrumentedType prepare(@NotNull InstrumentedType instrumentedType) {
                        return instrumentedType;
                    }

                    @Override
                    public @NotNull ByteCodeAppender appender(@NotNull Target implementationTarget) {
                        return (methodVisitor, implementationContext, instrumentedMethod) -> {
                            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                            methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC,
                                    "net/momirealms/craftengine/bukkit/plugin/agent/PluginHolder",
                                    "plugin",
                                    "Ljava/lang/Object;");
                            methodVisitor.visitInsn(Opcodes.RETURN);
                            return new ByteCodeAppender.Size(1, 1);
                        };
                    }
                })
                .make()
                .load(Bukkit.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        Method setPlugin = holderClass.getMethod("setPlugin", Object.class);
        setPlugin.invoke(null, plugin);
        Instrumentation inst = ByteBuddyAgent.install();
        BlocksAgent.agentmain(null, inst);
    }
}
