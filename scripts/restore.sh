#!/bin/bash

# Restore Script for Bunkermuseum Member Management Platform
# This script restores PostgreSQL database and MinIO storage from backups

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Configuration
COMPOSE_FILE="docker-compose.prod.yml"
BACKUP_DIR="$PROJECT_ROOT/backups"

# Load environment variables
if [ -f "$PROJECT_ROOT/.env.prod" ]; then
    source "$PROJECT_ROOT/.env.prod"
else
    echo -e "${RED}[ERROR]${NC} .env.prod not found!"
    exit 1
fi

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

# List available PostgreSQL backups
list_postgres_backups() {
    print_info "Available PostgreSQL backups:"
    local backups=($(ls -t "$BACKUP_DIR/postgres"/*.sql.gz 2>/dev/null))

    if [ ${#backups[@]} -eq 0 ]; then
        print_error "No PostgreSQL backups found!"
        return 1
    fi

    for i in "${!backups[@]}"; do
        local backup="${backups[$i]}"
        local size=$(du -h "$backup" | cut -f1)
        local date=$(basename "$backup" | sed 's/bunkermuseum_\(.*\)\.sql\.gz/\1/')
        echo "  $((i+1)). $date ($size)"
    done

    echo "${backups[@]}"
}

# List available MinIO backups
list_minio_backups() {
    print_info "Available MinIO backups:"
    local backups=($(ls -t "$BACKUP_DIR/minio"/*.tar.gz 2>/dev/null))

    if [ ${#backups[@]} -eq 0 ]; then
        print_error "No MinIO backups found!"
        return 1
    fi

    for i in "${!backups[@]}"; do
        local backup="${backups[$i]}"
        local size=$(du -h "$backup" | cut -f1)
        local date=$(basename "$backup" | sed 's/minio_\(.*\)\.tar\.gz/\1/')
        echo "  $((i+1)). $date ($size)"
    done

    echo "${backups[@]}"
}

# Restore PostgreSQL database
restore_postgres() {
    local backup_file="$1"

    print_warning "This will DROP and RECREATE the database!"
    read -p "Are you sure you want to restore the database? (yes/no) " -r
    echo

    if [ "$REPLY" != "yes" ]; then
        print_info "Database restore cancelled"
        return 1
    fi

    print_info "Restoring PostgreSQL database from: $backup_file"

    # Drop and recreate database
    docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" exec -T postgres \
        psql -U "$DB_USERNAME" -d postgres -c "DROP DATABASE IF EXISTS $DB_NAME;"

    docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" exec -T postgres \
        psql -U "$DB_USERNAME" -d postgres -c "CREATE DATABASE $DB_NAME;"

    # Restore backup
    gunzip -c "$backup_file" | docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" exec -T postgres \
        psql -U "$DB_USERNAME" -d "$DB_NAME"

    if [ $? -eq 0 ]; then
        print_success "Database restored successfully!"
    else
        print_error "Database restore failed!"
        return 1
    fi
}

# Restore MinIO data
restore_minio() {
    local backup_file="$1"

    print_warning "This will REPLACE all MinIO data!"
    read -p "Are you sure you want to restore MinIO data? (yes/no) " -r
    echo

    if [ "$REPLY" != "yes" ]; then
        print_info "MinIO restore cancelled"
        return 1
    fi

    print_info "Restoring MinIO data from: $backup_file"

    # Stop MinIO service
    docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" stop minio

    # Restore backup
    cat "$backup_file" | docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" run --rm -T minio \
        tar xzf - -C /

    if [ $? -eq 0 ]; then
        print_success "MinIO data restored successfully!"
    else
        print_error "MinIO restore failed!"
        docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" start minio
        return 1
    fi

    # Start MinIO service
    docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" start minio
}

# Main restore flow
main() {
    echo ""
    print_info "=== Bunkermuseum Restore ==="
    echo ""

    # Check if services are running
    if ! docker compose -f "$PROJECT_ROOT/$COMPOSE_FILE" ps | grep -q "Up"; then
        print_error "Services are not running. Please start them first."
        exit 1
    fi

    # Menu
    echo "What would you like to restore?"
    echo "  1. PostgreSQL database only"
    echo "  2. MinIO storage only"
    echo "  3. Both PostgreSQL and MinIO"
    echo "  4. Cancel"
    echo ""
    read -p "Select option (1-4): " option

    case $option in
        1)
            backups=($(list_postgres_backups))
            if [ $? -eq 0 ]; then
                read -p "Select backup number: " num
                restore_postgres "${backups[$((num-1))]}"
            fi
            ;;
        2)
            backups=($(list_minio_backups))
            if [ $? -eq 0 ]; then
                read -p "Select backup number: " num
                restore_minio "${backups[$((num-1))]}"
            fi
            ;;
        3)
            pg_backups=($(list_postgres_backups))
            if [ $? -ne 0 ]; then exit 1; fi
            read -p "Select PostgreSQL backup number: " pg_num

            minio_backups=($(list_minio_backups))
            if [ $? -ne 0 ]; then exit 1; fi
            read -p "Select MinIO backup number: " minio_num

            restore_postgres "${pg_backups[$((pg_num-1))]}"
            restore_minio "${minio_backups[$((minio_num-1))]}"
            ;;
        *)
            print_info "Restore cancelled"
            exit 0
            ;;
    esac

    echo ""
    print_success "Restore completed!"
}

# Run main function
main
