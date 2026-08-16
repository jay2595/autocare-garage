#!/usr/bin/env bash
# Starts all three services on your machine without Docker.
# Usage:  ./run-local.sh          (Ctrl-C stops everything)
set -e

echo "Building all modules..."
mvn -q clean package

echo "Starting customers-service on :8081"
java -jar customers-service/target/customers-service-1.0.0.jar > /tmp/customers.log 2>&1 &
CUSTOMERS_PID=$!

echo "Starting workshop-service on :8082"
java -jar workshop-service/target/workshop-service-1.0.0.jar > /tmp/workshop.log 2>&1 &
WORKSHOP_PID=$!

sleep 12

echo "Starting web-ui on :8080"
java -jar web-ui/target/web-ui-1.0.0.jar > /tmp/webui.log 2>&1 &
WEBUI_PID=$!

trap "kill $CUSTOMERS_PID $WORKSHOP_PID $WEBUI_PID 2>/dev/null" EXIT

echo ""
echo "  web-ui             http://localhost:8080"
echo "  customers-service  http://localhost:8081/api/customers"
echo "  workshop-service   http://localhost:8082/api/workshop/jobs"
echo ""
echo "Logs: /tmp/customers.log /tmp/workshop.log /tmp/webui.log"
echo "Ctrl-C to stop."
wait
