#!/bin/bash

echo "Starting Bunkermuseum Development Environment"
echo "================================================"

# Stop production containers if running
echo "Stopping production containers..."
docker compose -f docker-compose.yml down 2>/dev/null || true

# Start development environment
echo "Starting development containers with hot reload..."
docker compose -f docker-compose.dev.yml up --build

echo ""
echo "Development environment started!"
echo "Access your application at:"
echo "   - Application: http://localhost:8080"