# 🌲 Maven Dependency Tree [![Demo](https://github.com/SimonMarquis/Maven-Dependency-Tree/actions/workflows/demo.yml/badge.svg)](https://github.com/SimonMarquis/Maven-Dependency-Tree/actions/workflows/demo.yml)

> Kotlin script to list transitive dependencies of a Maven artifact.

### Usage

```bash
$ kotlin tree.main.kts --help
```

```
Usage: tree.main.kts [ARGUMENTS] [OPTIONS]
Arguments: 
    artifact [groupId:artifactId:version] -> Name of the artifact to explore (optional)
Options: 
    --help, -h          -> Usage info
    --quiet, -q [false] -> Hide debug message
    --no-legend [false] -> Hide the legend
    --repository=<URL>  -> Maven repository (vararg) defaults to Maven Central
```

### Simple dependency on Maven Central

```bash
$ kotlin tree.main.kts org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2 --repository=https://repo1.maven.org/maven2
```

```
🌲 Maven Dependency Tree

⚙️ Arguments: [org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2, --repository=https://repo1.maven.org/maven2]
📡 Crawling POM files…
✅ https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core/1.6.2/kotlinx-coroutines-core-1.6.2.pom in 423.921752ms
✅ https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core-jvm/1.6.2/kotlinx-coroutines-core-jvm-1.6.2.pom in 3.886530ms
✅ https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib-jdk8/1.6.21/kotlin-stdlib-jdk8-1.6.21.pom in 4.248533ms
✅ https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib-common/1.6.21/kotlin-stdlib-common-1.6.21.pom in 2.873122ms
✅ https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.6.21/kotlin-stdlib-1.6.21.pom in 6.473750ms
✅ https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib-jdk7/1.6.21/kotlin-stdlib-jdk7-1.6.21.pom in 3.954230ms
✅ https://repo1.maven.org/maven2/org/jetbrains/annotations/13.0/annotations-13.0.pom in 6.219047ms
⏱ Total duration: 666.792915ms

org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2
└─ org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.2 (compile)
   ├─ org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21 (compile)
   │  ├─ org.jetbrains.kotlin:kotlin-stdlib:1.6.21 (compile)
   │  │  ├─ org.jetbrains.kotlin:kotlin-stdlib-common:1.6.21 (compile)
   │  │  └─ org.jetbrains:annotations:13.0 (compile)
   │  └─ org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.21 (compile)
   │     └─ org.jetbrains.kotlin:kotlin-stdlib:1.6.21 (compile) ↩️
   └─ org.jetbrains.kotlin:kotlin-stdlib-common:1.6.21 (compile) ↩️

🔣 Legend:
- 🔁 Cyclic dependency
- ↩️ Already printed
- ⛔ Non resolvable
- 💀 Failed to resolve
```

### More complex dependency tree on multiple repositories

```bash
$ kotlin tree.main.kts androidx.core:core:1.8.0 --repository=https://dl.google.com/android/maven2 --repository=https://repo1.maven.org/maven2
```

```
🌲 Maven Dependency Tree

⚙️ Arguments: [androidx.core:core:1.8.0, --repository=https://dl.google.com/android/maven2, --repository=https://repo1.maven.org/maven2]
📡 Crawling POM files…
✅ https://dl.google.com/android/maven2/androidx/core/core/1.8.0/core-1.8.0.pom in 509.684913ms
✅ https://dl.google.com/android/maven2/androidx/annotation/annotation/1.2.0/annotation-1.2.0.pom in 18.624724ms
✅ https://dl.google.com/android/maven2/androidx/annotation/annotation-experimental/1.1.0/annotation-experimental-1.1.0.pom in 62.313017ms
✅ https://dl.google.com/android/maven2/androidx/collection/collection/1.0.0/collection-1.0.0.pom in 20.174835ms
✅ https://dl.google.com/android/maven2/androidx/concurrent/concurrent-futures/1.0.0/concurrent-futures-1.0.0.pom in 52.146949ms
✅ https://dl.google.com/android/maven2/androidx/lifecycle/lifecycle-runtime/2.3.1/lifecycle-runtime-2.3.1.pom in 28.580292ms
✅ https://dl.google.com/android/maven2/androidx/versionedparcelable/versionedparcelable/1.1.1/versionedparcelable-1.1.1.pom in 46.431111ms
❌ https://dl.google.com/android/maven2/junit/junit/4.12/junit-4.12.pom FileNotFoundException in 28.122588ms
✅ https://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.pom in 54.531166ms
✅ https://dl.google.com/android/maven2/androidx/annotation/annotation/1.0.0/annotation-1.0.0.pom in 55.419471ms
❌ https://dl.google.com/android/maven2/com/google/guava/listenablefuture/1.0/listenablefuture-1.0.pom FileNotFoundException in 48.115922ms
✅ https://repo1.maven.org/maven2/com/google/guava/listenablefuture/1.0/listenablefuture-1.0.pom in 4.110027ms
✅ https://dl.google.com/android/maven2/androidx/annotation/annotation/1.1.0/annotation-1.1.0.pom in 57.136983ms
✅ https://dl.google.com/android/maven2/androidx/lifecycle/lifecycle-common/2.3.1/lifecycle-common-2.3.1.pom in 55.194970ms
✅ https://dl.google.com/android/maven2/androidx/arch/core/core-common/2.1.0/core-common-2.1.0.pom in 23.480057ms
✅ https://dl.google.com/android/maven2/androidx/arch/core/core-runtime/2.1.0/core-runtime-2.1.0.pom in 55.992375ms
❌ https://dl.google.com/android/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.pom FileNotFoundException in 17.272216ms
✅ https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.pom in 2.422317ms
⏱ Total duration: 1.303770532s

androidx.core:core:1.8.0
├─ androidx.annotation:annotation:1.2.0 (compile)
├─ androidx.annotation:annotation-experimental:1.1.0 (compile)
├─ androidx.collection:collection:1.0.0 (runtime)
│  ├─ junit:junit:4.12 (test)
│  │  └─ org.hamcrest:hamcrest-core:1.3
│  └─ androidx.annotation:annotation:1.0.0 (compile)
├─ androidx.concurrent:concurrent-futures:1.0.0 (runtime)
│  ├─ com.google.guava:listenablefuture:1.0 (compile)
│  └─ androidx.annotation:annotation:1.1.0 (compile)
├─ androidx.lifecycle:lifecycle-runtime:2.3.1 (compile)
│  ├─ androidx.lifecycle:lifecycle-common:2.3.1 (compile)
│  │  └─ androidx.annotation:annotation:1.1.0 (compile) ↩️
│  ├─ androidx.arch.core:core-common:2.1.0 (compile)
│  │  └─ androidx.annotation:annotation:1.1.0 (compile) ↩️
│  ├─ androidx.annotation:annotation:1.1.0 (compile) ↩️
│  └─ androidx.arch.core:core-runtime:2.1.0 (runtime)
│     ├─ androidx.annotation:annotation:1.1.0 (compile) ↩️
│     └─ androidx.arch.core:core-common:[2.1.0] (compile) ⛔
└─ androidx.versionedparcelable:versionedparcelable:1.1.1 (compile)
   ├─ androidx.annotation:annotation:1.1.0 (compile) ↩️
   └─ androidx.collection:collection:1.0.0 (compile)
      ├─ junit:junit:4.12 (test) ↩️
      └─ androidx.annotation:annotation:1.0.0 (compile) ↩️

🔣 Legend:
- 🔁 Cyclic dependency
- ↩️ Already printed
- ⛔ Non resolvable
- 💀 Failed to resolve
```

### Credits

- [@martinbonnin](https://github.com/martinbonnin) for [Xoxo](https://github.com/martinbonnin/Xoxo) 
  > A wrapper around org.w3c.dom with nicer Kotlin APIs.

### License

```
Copyright 2022 Simon Marquis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
