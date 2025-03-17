# Roteiro - Backend

Sistema de geração de conteúdo de orações com integração MongoDB e ElevenLabs.

## Requisitos do Sistema

- Java 17 ou superior
- Maven 3.6 ou superior
- Docker e Docker Compose
- MongoDB 6.0 ou superior

## Configuração do Ambiente

### 1. Clonar o Repositório

```bash
git clone [URL_DO_REPOSITORIO]
cd roteiro
```

### 2. Configurar Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
# MongoDB
MONGODB_URI=mongodb://localhost:27017/roteiro
MONGODB_DATABASE=roteiro

# ElevenLabs
ELEVENLABS_API_KEY=sua_chave_api_aqui
ELEVENLABS_VOICE_ID=id_da_voz_aqui

# OpenAI
OPENAI_API_KEY=sua_chave_api_aqui
```

### 3. Iniciar MongoDB com Docker

```bash
docker-compose up -d mongodb
```

### 4. Compilar o Projeto

```bash
mvn clean install
```

### 5. Executar a Aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

## Estrutura do Projeto

```
roteiro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/luisoliveira/roteiro/
│   │   │       ├── config/         # Configurações do Spring
│   │   │       ├── controller/     # Controladores REST
│   │   │       ├── model/          # Modelos de dados
│   │   │       ├── repository/     # Repositórios MongoDB
│   │   │       ├── service/        # Serviços de negócio
│   │   │       └── dto/            # Objetos de transferência de dados
│   │   └── resources/
│   │       └── application.yml     # Configurações da aplicação
│   └── test/                       # Testes unitários e de integração
├── docker-compose.yml              # Configuração do Docker
└── pom.xml                         # Dependências e configurações do Maven
```

## Endpoints da API

### Geração de Conteúdo

#### Iniciar Geração de Conteúdo
```
POST /api/content/generate
Content-Type: application/json

{
    "tema": "string",
    "estiloOracao": "string",
    "duracao": "string",
    "tipoOracao": "string",
    "idioma": "string",
    "titulo": "string",
    "observacoes": "string",
    "gerarVersaoShort": boolean,
    "gerarAudio": boolean
}
```

#### Verificar Status do Processo
```
GET /api/content/status/{processId}
```

#### Download de Arquivo
```
GET /api/content/download/{processId}/{filename}
```

### Diagnóstico e Monitoramento

#### Verificar Status do Banco de Dados
```
GET /api/content/db-status
```

#### Verificar Variáveis de Ambiente
```
GET /api/content/env-check
```

#### Testar Repositório
```
POST /api/content/test-repository
```

#### Testar Manipulação de IDs
```
POST /api/content/test-id
```

#### Criar Documento de Teste
```
POST /api/content/test-document
```

#### Buscar Documento de Teste
```
GET /api/content/test-document/{id}
```

### Busca de Orações

As seguintes rotas estão disponíveis através do PrayerContentRepository:

#### Buscar Orações por Título
```
GET /api/prayers/title/{title}
```

#### Buscar Orações por Tema
```
GET /api/prayers/theme/{theme}
```

#### Buscar Orações por Data de Criação
```
GET /api/prayers/created-between?start={startDate}&end={endDate}
```

#### Buscar Orações por Conteúdo
```
GET /api/prayers/content/{text}
```

#### Buscar Orações por ID do Processo
```
GET /api/prayers/process/{processId}
```

#### Buscar Orações por Idioma
```
GET /api/prayers/language/{language}
```

#### Buscar Orações por Estilo
```
GET /api/prayers/style/{style}
```

## Desenvolvimento

### Tecnologias Utilizadas

- Spring Boot 3.2.3
- Spring Data MongoDB
- Spring Web
- Spring Security
- Lombok
- OpenAI API
- ElevenLabs API
- Docker
- MongoDB

### Fluxo de Desenvolvimento

1. Crie uma branch para sua feature
2. Faça as alterações necessárias
3. Execute os testes
4. Faça commit das alterações
5. Crie um Pull Request

### Testes

Para executar os testes:

```bash
mvn test
```

## Troubleshooting

### Problemas com MongoDB

1. Verifique se o MongoDB está rodando:
```bash
docker ps
```

2. Verifique os logs do MongoDB:
```bash
docker logs mongodb
```

3. Verifique a conexão:
```bash
mongo mongodb://localhost:27017/roteiro
```

### Problemas com a API

1. Verifique os logs da aplicação
2. Confirme se todas as variáveis de ambiente estão configuradas
3. Verifique se as APIs externas (OpenAI, ElevenLabs) estão acessíveis

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes. 