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
var keycloak = Keycloak();
keycloak.init({ onLoad: 'login-required' })
    .success(function(authenticated) {
        if (authenticated) {
            console.debug("User is now logged in");
            document.getElementById("usersName").innerHTML = keycloak.tokenParsed.name + "("+ keycloak.subject +")";
        } else {
            console.debug("User is NOT authenticated");
        }
    })
    .error(function() {
        alert('Something wrong happened during the auth');
    });
