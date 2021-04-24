/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.scala;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.typesafe.zinc.*;
import org.gradle.api.internal.tasks.SimpleWorkResult;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.internal.*;
import org.gradle.internal.Factory;
import org.gradle.internal.SystemProperties;
import org.gradle.internal.nativeintegration.services.NativeServices;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.scopes.GlobalScopeServices;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.api.internal.tasks.compile.JavaCompilerArgumentsBuilder;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.jvm.Jvm;
import scala.Option;
import xsbti.F0;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;

    private static class ZincCompilerServices extends DefaultServiceRegistry {
        private static ZincCompilerServices instance;

        private ZincCompilerServices(File gradleUserHome) {
            super(NativeServices.getInstance());

            addProvider(new GlobalScopeServices(true));
            addProvider(new CacheRepositoryServices(gradleUserHome, null));
        }

        public static ZincCompilerServices getInstance(File gradleUserHome) {
            if (instance == null) {
                NativeServices.initialize(gradleUserHome);
                instance = new ZincCompilerServices(gradleUserHome);
            }
            return instance;
        }
    }
