# Sistema de Pedidos com Notificações Assíncronas

API REST para criação e consulta de pedidos com arquitetura hexagonal, persistência em PostgreSQL e mensageria via Kafka ou RabbitMQ.

## Funcionalidades

- **API REST** para criação e consulta de pedidos
- **Arquitetura Hexagonal** com separação clara de domínio, aplicação e infraestrutura
- **Persistência** com JPA + PostgreSQL
- **Mensageria** com Kafka ou RabbitMQ (configurável)
- **Notificações** via AWS SES (e-mail) e SNS (push)
- **Testes Unitários** com JUnit 5 e Mockito
- **Testcontainers** para testes de integração

## Arquitetura Hexagonal

```
src/main/java/com/order/service/
├── domain/                 # Núcleo do negócio (sem dependências externas)
│   ├── model/              # Entidades e Value Objects
│   ├── event/              # Eventos de domínio
│   ├── exception/          # Exceções customizadas
│   └── repository/         # Interfaces de repositório (ports)
│
├── application/            # Casos de uso
│   ├── port/
│   │   ├── inbound/        # Interfaces de entrada (use cases)
│   │   └── outbound/       # Interfaces de saída (serviços externos)
│   ├── service/            # Implementações dos use cases
│   └── dto/                # Objetos de transferência
│
└── infrastructure/         # Implementações concretas
    ├── adapter/
    │   ├── in/web/         # Controladores REST
    │   └── out/            # Adaptadores externos
    │       ├── persistence/  # JPA Repository
    │       ├── messaging/  # Kafka/RabbitMQ
    │       └── notification/ # AWS SES/SNS
    └── config/             # Configurações Spring
```

### Princípios da Arquitetura

| Camada             | Responsabilidade                                       |
| ------------------ | ------------------------------------------------------ |
| **Domain**         | Regras de negócio puras, sem dependências de framework |
| **Application**    | Orquestra casos de uso, depende apenas do domain       |
| **Infrastructure** | Implementações concretas (frameworks, DB, APIs)        |

**Fluxo de Dependência:**

```
Infrastructure → Application → Domain
     ↑                              |
     └──────────────────────────────┘
        (Domain não conhece ninguém)
```

## Tecnologias

- **Java 17**
- **Spring Boot 3.5.13**
- **Spring Data JPA**
- **Spring Kafka**
- **Spring AMQP (RabbitMQ)**
- **PostgreSQL**
- **AWS SDK (SES, SNS)**
- **Lombok**
- **JUnit 5 + Mockito**
- **Testcontainers**

## Configuração

### 1. Iniciar dependências com Docker Compose

```bash
docker-compose up -d
```

Isso iniciará:

- PostgreSQL (porta 5432)
- Kafka (porta 9092)
- Zookeeper (porta 2181)
- RabbitMQ (porta 5672, console 15672)

### 2. Configurar aplicação

Editar `src/main/resources/application.properties`:

```properties
# Escolher mensageria: kafka ou rabbitmq
messaging.type=kafka

# Configurar AWS (opcional, para notificações)
aws.region=us-east-1
aws.sns.topic.arn=arn:aws:sns:us-east-1:123456789:orders-topic
```

### 3. Executar aplicação

```bash
./mvnw spring-boot:run
```

## API Endpoints

### Criar Pedido

```bash
POST /api/v1/orders
Content-Type: application/json

{
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "street": "Rua A, 123",
  "city": "São Paulo",
  "state": "SP",
  "zipCode": "01000-000",
  "country": "BR",
  "items": [
    {
      "productId": "PROD-001",
      "productName": "Produto Teste",
      "quantity": 2,
      "unitPrice": 50.00
    }
  ]
}
```

**Resposta (201 Created):**

```json
{
  "id": "uuid-gerado",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "customerAddress": "Rua A, 123, São Paulo, SP 01000-000, BR",
  "items": [
    {
      "id": "item-uuid",
      "productId": "PROD-001",
      "productName": "Produto Teste",
      "quantity": 2,
      "unitPrice": 50.0,
      "totalPrice": 100.0
    }
  ],
  "totalAmount": 100.0,
  "status": "PENDING",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### Buscar Pedido por ID

```bash
GET /api/v1/orders/{id}
```

### Listar Todos os Pedidos

```bash
GET /api/v1/orders
```

### Listar Pedidos por Status

```bash
GET /api/v1/orders?status=PENDING
```

## Eventos e Mensageria

### Eventos Publicados

| Evento                    | Descrição                           |
| ------------------------- | ----------------------------------- |
| `OrderCreatedEvent`       | Disparado quando um pedido é criado |
| `OrderStatusUpdatedEvent` | Disparado quando o status muda      |

### Fluxo de Mensageria

1. **API** recebe requisição POST `/api/v1/orders`
2. **CreateOrderService** cria o pedido e salva no banco
3. **EventPublisherPort** publica `OrderCreatedEvent` no Kafka/RabbitMQ
4. **Consumer** (Kafka/RabbitMQ) recebe o evento
5. **NotificationPort** envia e-mail (SES) ou notificação push (SNS)

## Testes

### Executar todos os testes

```bash
./mvnw test
```

### Estrutura de Testes

```
src/test/java/com/order/service/
├── unit/                      # Testes unitários
│   ├── domain/
│   │   ├── model/             # Testes de entidades
│   │   └── vo/                # Testes de Value Objects
│   └── application/
│       └── service/           # Testes de casos de uso
│
└── integration/               # Testes de integração
```

### Exemplo de Teste Unitário

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        var request = createValidRequest();

        // Act
        var response = createOrderService.execute(request);

        // Assert
        assertNotNull(response);
        verify(orderRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publish(any());
    }
}
```

## Conceitos e Boas Práticas

### Domain-Driven Design

- **Entidades** têm identidade única (ex: Order)
- **Value Objects** são imutáveis e comparados por valor (ex: Money, Email)
- **Aggregates** encapsulam consistência de dados
- **Domain Events** representam fatos ocorridos no domínio

### Streams e Lambdas

O projeto faz uso extensivo de Streams e Lambdas:

```java
// Cálculo de total usando streams
public Money getTotalAmount() {
    return items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(Money::add)
            .orElse(Money.zero());
}

// Mapeamento de itens
private List<OrderItem> createOrderItems(OrderRequest request) {
    return request.getItems().stream()
            .map(item -> OrderItem.create(...))
            .collect(Collectors.toList());
}
```

### Exceções Customizadas

Hierarquia de exceções:

```
DomainException (abstract)
├── OrderNotFoundException
├── InvalidOrderException
└── InsufficientStockException
```

### Portas e Adaptadores

**Inbound Ports (Use Cases):**

```java
public interface CreateOrderUseCase {
    OrderResponse execute(OrderRequest request);
}
```

**Outbound Ports (Serviços Externos):**

```java
public interface EventPublisherPort {
    void publish(DomainEvent event);
}
```

**Adaptadores** implementam as portas usando tecnologias específicas.

## Banco de Dados

### Schema

**Tabela `orders`:**

- id (PK)
- customer_name, customer_email
- street, city, state, zip_code, country
- total_amount
- status (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- created_at, updated_at

**Tabela `order_items`:**

- id (PK)
- order_id (FK)
- product_id, product_name
- quantity, unit_price, total_price

## Próximos Passos

- [x] Implementar confirmação de pedido (POST /orders/{id}/confirm)
- [x] Implementar cancelamento de pedido
- [ ] Adicionar pagamento com integração Stripe
- [ ] Implementar saga pattern para consistência distribuída
- [x] Adicionar cache com Redis
- [ ] Implementar rate limiting
- [ ] adicionar observabilidade (logs estruturados, métricas, tracing)
