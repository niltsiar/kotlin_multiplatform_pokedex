#!/bin/bash
# Runs tests and generates coverage report
# Usage: ./test-coverage.sh [feature-name]

FEATURE=$1

if [ -n "$FEATURE" ]; then
    echo "Running tests for feature: $FEATURE"
    ./gradlew :features:$FEATURE:test --continue
else
    echo "Running all tests"
    ./gradlew test --continue
fi

# Generate coverage report if Jacoco is available
./gradlew tasks | grep -q "jacoco" && ./gradlew jacocoTestReport
