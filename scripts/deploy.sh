#!/bin/bash

# Production Deployment Script for Bunkermuseum Member Management Platform
# This script handles the deployment of the application to production

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Configuration
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

# Functions
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    # Check if Docker Compose is installed
    if ! command -v docker compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    # Check if .env.prod exists
    if [ ! -f "$PROJECT_ROOT/$ENV_FILE" ]; then
        print_error "$ENV_FILE not found!"
        print_info "Please copy .env.prod.template to .env.prod and configure it."
        exit 1
    fi

    print_success "Prerequisites check passed"
}

# Create required directories
create_directories() {
    print_info "Creating required directories..."

    mkdir -p "$PROJECT_ROOT/backups/postgres"
    mkdir -p "$PROJECT_ROOT/backups/minio"
    mkdir -p "$PROJECT_ROOT/nginx/ssl"

    print_success "Directories created"
}

# Check SSL certificates
check_ssl() {
    if [ ! -f "$PROJECT_ROOT/nginx/ssl/fullchain.pem" ] || [ ! -f "$PROJECT_ROOT/nginx/ssl/privkey.pem" ]; then
        print_warning "SSL certificates not found in nginx/ssl/"
        print_warning "Application will run on HTTP only"
        print_info "To enable HTTPS:"
        print_info "  1. Place SSL certificates in nginx/ssl/"
        print_info "  2. Update nginx/nginx.prod.conf (uncomment HTTPS server block)"
        print_info "  3. Set ENABLE_SSL=true in .env.prod"
        echo ""
        read -p "Continue without SSL? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        print_success "SSL certificates found"
    fi
}

# Build Docker images
build_images() {
    print_info "Building Docker images..."

    cd "$PROJECT_ROOT"
    docker compose -f "$COMPOSE_FILE" build --no-cache

    print_success "Docker images built successfully"
}

# Pull latest images
pull_images() {
    print_info "Pulling latest dependency images..."

    cd "$PROJECT_ROOT"
    docker compose -f "$COMPOSE_FILE" pull postgres minio

    print_success "Images pulled successfully"
}

# Start services
start_services() {
    print_info "Starting services..."

    cd "$PROJECT_ROOT"
    docker compose -f "$COMPOSE_FILE" up -d

    print_success "Services started"
}

# Check service health
check_health() {
    print_info "Waiting for services to be healthy..."

    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))

        # Check if all services are healthy
        local unhealthy=$(docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" ps --format json | jq -r 'select(.Health != "healthy" and .Health != "") | .Service' 2>/dev/null)

        if [ -z "$unhealthy" ]; then
            print_success "All services are healthy"
            return 0
        fi

        print_info "Attempt $attempt/$max_attempts - Waiting for services to be healthy..."
        sleep 10
    done

    print_error "Some services are not healthy after $max_attempts attempts"
    print_info "Checking service status..."
    cd "$PROJECT_ROOT"
    docker compose -f "$COMPOSE_FILE" ps
    return 1
}

# Display service URLs
display_urls() {
    print_success "Deployment completed successfully!"
    echo ""
    print_info "Service URLs:"
    echo "  Application:    http://localhost (or your configured domain)"
    echo "  MinIO Console:  http://localhost:9001"
    echo "  MinIO API:      http://localhost:9000"
    echo ""
    print_info "To view logs:"
    echo "  docker compose -f $COMPOSE_FILE logs -f"
    echo ""
    print_info "To stop services:"
    echo "  docker compose -f $COMPOSE_FILE down"
    echo ""
    print_info "To view service status:"
    echo "  docker compose -f $COMPOSE_FILE ps"
}

# Main deployment flow
main() {
    echo ""
    print_info "=== Bunkermuseum Production Deployment ==="
    echo ""

    check_prerequisites
    create_directories
    check_ssl

    # Ask for confirmation
    read -p "Ready to deploy to production? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Deployment cancelled"
        exit 0
    fi

    pull_images
    build_images
    start_services
    check_health

    display_urls
}

# Run main function
main
