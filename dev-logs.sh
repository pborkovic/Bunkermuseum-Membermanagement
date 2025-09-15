#!/bin/bash

echo "Viewing Development Environment Logs"
echo "======================================="
echo "Press Ctrl+C to stop viewing logs"
echo ""

# Follow development container logs
docker compose -f docker-compose.dev.yml logs -f