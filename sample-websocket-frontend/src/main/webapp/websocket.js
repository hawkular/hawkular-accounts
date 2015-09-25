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
var wsUri = "ws://"
    + document.location.hostname
    + ":"
    + document.location.port
    + "/hawkular-accounts-sample-websocket-backend/socket";

var websocket = new WebSocket(wsUri);
var messageBox = document.getElementById("messageBox");
var errorBox = document.getElementById("errorBox");

websocket.onopen = function(event) {
    messageBox.innerHTML = "<li>Opened connection to "+wsUri+".</li>";
    var loginSuccessful = false;
    websocket.onmessage = function(event) {
        console.log("Response from the token login: " + event.data);
        loginSuccessful = true;
        messageBox.innerHTML += "<li>" + event.data + "</li>";
        websocket.onmessage = function(event) {
            console.log("onMessage: " + event.data);
            messageBox.innerHTML += "<li>" + event.data + "</li>";
        };
    };
};

websocket.onclose = function(event) {
    messageBox.innerHTML += "<li>Socket closed: " + event.code + " - " + event.reason + "</li>";
};

websocket.onerror = function(event) {
    console.debug(event);
    errorBox.innerHTML += "<li>" + event.data + "</li>";
};

function sendEchoMessage() {
    keycloak.updateToken(5).success(function () {
        console.log("Keycloak token to send on the payload: " + keycloak.token);
        var payload = {"message": "Hello World!"};

        //payload.authentication = {
        //    "token": keycloak.token,
        //    "persona": keycloak.subject
        //};
        payload.authentication = {
            "login": {
                "username": "jdoe",
                "password": "password"
            }
        };

        var message = JSON.stringify(payload);
        console.debug("Sending this message: " + message);
        websocket.send(message);
    }).error(function (e) {
        console.log("An error occurred while updating the token.");
        console.log(e);
    });
}