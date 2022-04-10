#!/bin/bash
set -x

COOKIEJAR="$(mktemp)"
SERVER=http://localhost:8080/jenkins
CRUMB=$(curl 2>/dev/null --cookie-jar "$COOKIEJAR" "$SERVER/crumbIssuer/api/json" | jq -r .crumb)

curl 2>/dev/null --cookie "$COOKIEJAR" -H "Jenkins-Crumb: $CRUMB" -X POST $SERVER/job/zzz/build?delay=0sec
rm "$COOKIEJAR"
