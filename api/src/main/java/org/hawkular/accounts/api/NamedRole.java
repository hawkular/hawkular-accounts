/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.accounts.api;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * CDI qualifier used for injecting the {@link org.hawkular.accounts.api.model.Role} bean related to the name
 * specified on this annotation.
 * <p>
 * For instance, a managed bean might include the following code in order to get a
 * {@link org.hawkular.accounts.api.model.Role} whose name is "auditor":
 * <p>
 * <pre>
 *     &#64;Inject &#64;NamedRole("auditor")
 *     Role auditor;
 * </pre>
 *
 * @author Juraci Paixão Kröhling
 */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, METHOD})
public @interface NamedRole {

    @Nonbinding
    String value() default "";
}
