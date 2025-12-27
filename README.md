# 🏦 BankX - Laboratorio de Microservicios Reactivos (CI/CD)

Este repositorio implementa una arquitectura de microservicios orientada a eventos utilizando **Java 17, Spring Boot 3, Kafka, Redis y MongoDB**. 

El proyecto destaca por su estrategia de **CI/CD con Jenkins**, utilizando un patrón de **Monorepo** que permite gestionar la infraestructura y los microservicios en un mismo repositorio, pero desplegándolos de manera independiente mediante filtros inteligentes.

---

## 📋 Requisitos Previos

Asegúrate de tener instalado y configurado lo siguiente:
* **Docker Desktop** (configurado para contenedores Linux).
* **Git**.
* **ngrok** (Para exponer tu Jenkins local a GitHub Webhooks).
* **Postman** (Para pruebas de integración).

---

## ⚙️ 1. Configuración del Entorno

### 1.1 Clonar el Repositorio
```bash
git clone [https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git](https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git)
cd banxk-laboraotrio-02
```

### 1.2 Configurar Variables de Entorno (.env)
El sistema utiliza un archivo centralizado para la configuración. Crea un archivo llamado .env dentro de la carpeta app/ (donde se encuentra el docker-compose.yml de Jenkins) y pega el siguiente contenido:

⚠️ Importante: Actualiza JENKINS_URL con tu dirección de ngrok cada vez que lo reinicies.


```ini
# ==========================================
# 🔑 CONFIGURACIÓN DE GITHUB
# ==========================================
GITHUB_USER=EduardoAzaldegui
GITHUB_TOKEN=TU_GITHUB_TOKEN_AQUI

# ==========================================
# 📦 REPOSITORIOS (Estrategia Monorepo)
# ==========================================
# Apuntan al mismo repo, Jenkins filtrará por carpeta
GITHUB_REPO_URL_INFRA=[https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git](https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git)
GITHUB_BRANCH_INFRA=main
JENKINS_WEBHOOK_TOKEN_INFRA=token-jenkins-infraestructura

GITHUB_REPO_URL_PRODUCER=[https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git](https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git)
GITHUB_BRANCH_PRODUCER=main
JENKINS_WEBHOOK_TOKEN_PRODUCER=token-jenkins-producer

GITHUB_REPO_URL_CONSUMER=[https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git](https://github.com/EduardoAzaldegui/banxk-laboraotrio-02.git)
GITHUB_BRANCH_CONSUMER=main
JENKINS_WEBHOOK_TOKEN_CONSUMER=token-jenkins-consumer

# ==========================================
# 🔧 CONFIGURACIÓN DE JENKINS
# ==========================================
JENKINS_PORT=8071           
JENKINS_AGENT_PORT=50001            
JENKINS_ADMIN_USER=admin
JENKINS_ADMIN_PASSWORD=MiPasswordSuperSeguro123!
# URL pública de ngrok (ej: [https://xxxx.ngrok-free.app](https://xxxx.ngrok-free.app))
JENKINS_URL=[https://TU-URL-NGROK.ngrok-free.app](https://TU-URL-NGROK.ngrok-free.app)

# ==========================================
# 🏗️ INFRAESTRUCTURA (Red Interna Docker)
# ==========================================
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
SCHEMA_REGISTRY_URL=http://schema-registry:8081
REDIS_HOST=redis
MONGODB_URI=mongodb://root:root@mongodb:27018/bank_db?authSource=admin

# ==========================================
# 🚀 CONFIGURACIÓN DE APPS
# ==========================================
APP_PORT=8082
APP_COMPOSE_FILE=docker-compose.yml
JAVA_VERSION=17
```

### 1.3 Levantar Jenkins
Ejecuta el siguiente comando para iniciar Jenkins con la configuración como código (JCasC):
```bash
cd app
docker-compose up -d
```


## 🔄 2. Estrategia CI/CD: Monorepo con Filtros Regex
Para evitar desplegar todo el sistema ante cualquier cambio menor, hemos implementado un patrón de filtrado por rutas utilizando el plugin Generic Webhook Trigger de Jenkins.

¿Cómo funciona?
Aunque todo el código vive en un solo repositorio, Jenkins analiza qué archivos fueron modificados en cada push y decide qué pipeline ejecutar basándose en expresiones regulares (Regex).


Ejemplo: Si modificas un archivo en card-ops-producer/src/..., Jenkins detectará el cambio mediante la regex y solo disparará el job deploy-card-ops-producer, ignorando la infraestructura y el consumidor.



## 🚀 3. Flujo de Despliegue (Paso a Paso)
Para garantizar el funcionamiento correcto, despliega en este orden estricto:

Desplegar Infraestructura (deploy-infrastructure):

Este job levanta Kafka, Zookeeper, Redis y MongoDB.

Es fundamental que este paso termine exitosamente ("Healthy") antes de continuar.

Desplegar Producer (deploy-card-ops-producer):

Levanta el servicio Spring Boot en el puerto 8085.

Desplegar Consumer (deploy-dispatch-consumer):

Levanta el servicio Spring Boot en el puerto 8082.


## 🧪 4. Pruebas de Integración
Puedes importar el archivo JSON proporcionado (postman/collection.json) en Postman para probar los endpoints.

Endpoints Disponibles
Producer (Generar Evento): POST http://localhost:8085/api/card-replacements

Consumer (Consultar Estado): GET http://localhost:8082/api/events?requestId={ID}

Health Check: http://localhost:8085/actuator/health



#### 📂 Estructura del Proyecto

```plaintext
/
├── bank-infrastructure/      # Docker Compose de servicios base (Kafka, Redis, Mongo)
├── card-ops-producer/        # Microservicio Java (Producer)
├── dispatch-consumer/        # Microservicio Java (Consumer)
├── app/                      # Configuración de Jenkins (JCasC y docker-compose)
└── README.md                 # Documentación del proyecto
```