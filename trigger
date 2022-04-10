#!/bin/bash

COOKIEJAR="$(mktemp)"
SERVER=http://localhost:8080/jenkins
CRUMB=$(curl -s --cookie-jar "$COOKIEJAR" "$SERVER/crumbIssuer/api/json" | jq -r .crumb)

curl -s --cookie "$COOKIEJAR" -H "Jenkins-Crumb: $CRUMB" -X POST $SERVER/job/zzz/build?delay=0sec
rm "$COOKIEJAR"
