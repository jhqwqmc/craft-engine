tasks.withType<JavaCompile> {
    dependsOn(tasks.clean)
}