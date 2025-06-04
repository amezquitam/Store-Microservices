#!/bin/bash

# Función para compilar un servicio
build_service() {
  local service_dir=$1
  echo "==============================="
  echo "Compilando $service_dir..."
  (
    cd "$service_dir" || exit 1
    mvn package -DskipTests
  )
}

# Verifica si se proporcionó un argumento
if [ -z "$1" ]; then
  echo "Uso: ./build.sh [servicio|all]"
  echo "Servicios disponibles: gateway, inventory, order, product, payment, eureka, all"
  exit 1
fi

# Nombre normalizado
service=$(echo "$1" | tr '[:upper:]' '[:lower:]')

# Compilar todos
if [ "$service" = "all" ]; then
  build_service gateway &
  build_service inventory-service &
  build_service order-service &
  build_service product-service &
  build_service payment-service &
  build_service eureka-server &

  # Esperar a que todos los procesos terminen
  wait
  echo "✅ Todos los servicios compilados."
  exit 0
fi

# Compilar uno específico
case "$service" in
  gateway)
    build_service gateway
    ;;
  inventory)
    build_service inventory-service
    ;;
  order)
    build_service order-service
    ;;
  product)
    build_service product-service
    ;;
  payment)
    build_service payment-service
    ;;
  eureka)
    build_service eureka-server
    ;;
  *)
    echo "Servicio no reconocido: $1"
    echo "Servicios disponibles: gateway, inventory, order, product, payment, eureka, all"
    exit 1
    ;;
esac
