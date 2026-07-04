#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
echo "Starting Recka..."
echo "This launcher uses Maven so JavaFX is loaded automatically."
echo "Do NOT start target/recka-1.0.0.jar directly."
mvn clean javafx:run
