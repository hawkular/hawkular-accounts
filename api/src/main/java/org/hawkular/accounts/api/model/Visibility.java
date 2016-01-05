/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.accounts.api.model;

/**
 * Simple enum with visibility values. The original intention is to mark the visibility of an organization, but can
 * be reused where it makes sense (semantically).
 *
 * APPLY means that users can apply to this organization and that the organization might be used in directory listings.
 * PRIVATE means that users cannot apply to it, and it's not to be used in any directory listings.
 *
 * @author Juraci Paixão Kröhling
 */
public enum Visibility {
    APPLY, PRIVATE
}
