package dev.luisoliveira.roteiro.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/docs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentationController {

    private static final String SYSTEM_ARCHITECTURE_MERMAID = """
            flowchart TB
                Client(Cliente) --> ContentController

                subgraph Controllers
                    ContentController[ContentGenerationController]
                end

                subgraph Services
                    EventBus[EventBusService]
                    ProcessTracking[ProcessTrackingService]
                    TitleGeneration[TitleGenerationService]
                    OracaoGeneration[OracaoGenerationService]
                    ShortGeneration[ShortGenerationService]
                    DescriptionGeneration[DescriptionGenerationService]
                    ImagePromptGeneration[ImagePromptGenerationService]
                    ContentCompilation[ContentCompilationService]
                    OpenAI[OpenAIService]
                    FileStorage[FileStorageService]
                end

                subgraph Events
                    ContentInitiated[ContentInitiatedEvent]
                    TitlesGenerated[TitlesGeneratedEvent]
                    TitleSelected[TitleSelectedEvent]
                    OracaoGenerated[OracaoGeneratedEvent]
                    ShortGenerated[ShortGeneratedEvent]
                    DescriptionGenerated[DescriptionGeneratedEvent]
                    ImagePromptGenerated[ImagePromptGeneratedEvent]
                    ContentCompleted[ContentCompletedEvent]
                end

                subgraph External
                    OpenAIAPI[OpenAI API]
                end

                ContentController --> EventBus
                ContentController --> ProcessTracking

                EventBus --> ContentInitiated
                ContentInitiated --> TitleGeneration

                TitleGeneration --> TitlesGenerated
                TitlesGenerated --> ProcessTracking

                ContentController --> TitleSelected
                TitleSelected --> OracaoGeneration

                OracaoGeneration --> OracaoGenerated
                OracaoGenerated --> ShortGeneration

                ShortGeneration --> ShortGenerated
                ShortGenerated --> DescriptionGeneration

                DescriptionGeneration --> DescriptionGenerated
                DescriptionGenerated --> ImagePromptGeneration
                DescriptionGenerated --> ContentCompilation

                ImagePromptGeneration --> ImagePromptGenerated

                ContentCompilation --> ContentCompleted
                ContentCompleted --> ProcessTracking

                TitleGeneration --> OpenAI
                OracaoGeneration --> OpenAI
                ShortGeneration --> OpenAI
                DescriptionGeneration --> OpenAI
                ImagePromptGeneration --> OpenAI

                OpenAI --> OpenAIAPI

                ContentCompilation --> FileStorage
            """;

    /**
     * Retorna o diagrama de arquitetura do sistema em formato Mermaid
     * Este endpoint pode ser usado com visualizadores Mermaid como o plugin
     * do GitHub ou outros sites que suportam a sintaxe Mermaid
     */
    @GetMapping(value = "/architecture", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getArchitectureDiagram() {
        return ResponseEntity.ok(SYSTEM_ARCHITECTURE_MERMAID);
    }

    /**
     * Retorna uma página HTML que renderiza o diagrama usando a biblioteca Mermaid
     */
    @GetMapping(value = "/architecture/view", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getArchitectureDiagramView() {
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>Arquitetura do Sistema - Roteiro</title>
                    <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        h1 { color: #333; }
                        .mermaid {
                            background-color: white;
                            padding: 20px;
                            border-radius: 5px;
                            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                        }
                    </style>
                </head>
                <body>
                    <h1>Diagrama de Arquitetura do Sistema</h1>
                    <div class="mermaid">
                """ + SYSTEM_ARCHITECTURE_MERMAID + """
                    </div>

                    <script>
                        mermaid.initialize({
                            startOnLoad: true,
                            theme: 'default',
                            securityLevel: 'loose',
                            flowchart: {
                                useMaxWidth: true,
                                htmlLabels: true
                            }
                        });
                    </script>
                </body>
                </html>
                """;

        return ResponseEntity.ok(htmlContent);
    }

    /**
     * Retorna a documentação completa da API em formato HTML
     */
    @GetMapping(value = "/api", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getApiDocumentation() {
        String htmlContent = """
                                                <!DOCTYPE html>
                                                <html>
                                                <head>
                                                    <meta charset="utf-8">
                                                    <title>Documentação da API - Roteiro</title>
                                                    <style>
                                                        body {
                                                            font-family: Arial, sans-serif;
                                                            line-height: 1.6;
                                                            margin: 0;
                                                            padding: 20px;
                                                            color: #333;
                                                        }
                                                        .container {
                                                            max-width: 1200px;
                                                            margin: 0 auto;
                                                        }
                                                        h1 {
                                                            color: #2c3e50;
                                                            border-bottom: 2px solid #3498db;
                                                            padding-bottom: 10px;
                                                        }
                                                        h2 {
                                                            color: #2980b9;
                                                            margin-top: 30px;
                                                        }
                                                        h3 {
                                                            color: #3498db;
                                                            margin-top: 20px;
                                                        }
                                                        .endpoint {
                                                            background-color: #f8f9fa;
                                                            border-left: 4px solid #3498db;
                                                            padding: 15px;
                                                            margin-bottom: 20px;
                                                            border-radius: 0 4px 4px 0;
                                                        }
                                                        .method {
                                                            display: inline-block;
                                                            padding: 5px 10px;
                                                            border-radius: 4px;
                                                            font-weight: bold;
                                                            margin-right: 10px;
                                                        }
                                                        .get {
                                                            background-color: #61affe;
                                                            color: white;
                                                        }
                                                        .post {
                                                            background-color: #49cc90;
                                                            color: white;
                                                        }
                                                        .put {
                                                            background-color: #fca130;
                                                            color: white;
                                                        }
                                                        .delete {
                                                            background-color: #f93e3e;
                                                            color: white;
                                                        }
                                                        .path {
                                                            font-family: monospace;
                                                            font-size: 1.1em;
                                                        }
                                                        .description {
                                                            margin-top: 10px;
                                                        }
                                                        .params, .response {
                                                            margin-top: 15px;
                                                        }
                                                        table {
                                                            width: 100%;
                                                            border-collapse: collapse;
                                                            margin-top: 10px;
                                                        }
                                                        th, td {
                                                            border: 1px solid #ddd;
                                                            padding: 8px;
                                                            text-align: left;
                                                        }
                                                        th {
                                                            background-color: #f2f2f2;
                                                        }
                                                        code {
                                                            background-color: #f8f9fa;
                                                            padding: 2px 5px;
                                                            border-radius: 3px;
                                                            font-family: monospace;
                                                        }
                                                        pre {
                                                            background-color: #f8f9fa;
                                                            padding: 15px;
                                                            border-radius: 4px;
                                                            overflow-x: auto;
                                                        }
                                                        .note {
                                                            background-color: #fff3cd;
                                                            border-left: 4px solid #ffc107;
                                                            padding: 15px;
                                                            margin: 15px 0;
                                                            border-radius: 0 4px 4px 0;
                                                        }
                                                    </style>
                                                </head>
                                                <body>
                                                    <div class="container">
                                                        <h1>Documentação da API - Roteiro</h1>
                                                        <p>
                                                            Esta documentação descreve os endpoints disponíveis na API do sistema Roteiro,
                                                            uma plataforma para geração de conteúdo de orações utilizando inteligência artificial.
                                                        </p>

                                                        <h2>Base URL</h2>
                                                        <p>Todos os endpoints são relativos à URL base: <code>/api</code></p>

                                                        <h2>Autenticação</h2>
                                                        <p>
                                                            Atualmente, a API não requer autenticação para acesso aos endpoints.
                                                            Isso poderá mudar em versões futuras.
                                                        </p>

                                                        <h2>Endpoints</h2>

                                                        <h3>Geração de Conteúdo</h3>

                                                        <div class="endpoint">
                                                            <span class="method post">POST</span>
                                                            <span class="path">/content/generate</span>
                                                            <div class="description">
                                                                Inicia o processo de geração de conteúdo para uma oração.
                                                            </div>
                                                            <div class="params">
                                                                <h4>Parâmetros (JSON):</h4>
                                                                <table>
                                                                    <tr>
                                                                        <th>Nome</th>
                                                                        <th>Tipo</th>
                                                                        <th>Descrição</th>
                                                                        <th>Obrigatório</th>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>theme</td>
                                                                        <td>String</td>
                                                                        <td>Tema da oração a ser gerada</td>
                                                                        <td>Sim</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>style</td>
                                                                        <td>String</td>
                                                                        <td>Estilo da oração (ex: "formal", "poético")</td>
                                                                        <td>Não</td>
                                                                    </tr>
                                                                </table>
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                                  "processId": "550e8400-e29b-41d4-a716-446655440000",
                                  "status": "INITIATED",
                                  "message": "Processo de geração iniciado com sucesso"
                                }</pre>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/content/status/{processId}</span>
                                                            <div class="description">
                                                                Verifica o status de um processo de geração de conteúdo.
                                                            </div>
                                                            <div class="params">
                                                                <h4>Parâmetros de Path:</h4>
                                                                <table>
                                                                    <tr>
                                                                        <th>Nome</th>
                                                                        <th>Tipo</th>
                                                                        <th>Descrição</th>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>processId</td>
                                                                        <td>String</td>
                                                                        <td>ID do processo de geração</td>
                                                                    </tr>
                                                                </table>
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                                  "processId": "550e8400-e29b-41d4-a716-446655440000",
                                  "status": "TITLES_GENERATED",
                                  "progress": 20,
                                  "titles": ["Título 1", "Título 2", "Título 3"],
                                  "message": "Títulos gerados com sucesso"
                                }</pre>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method post">POST</span>
                                                            <span class="path">/content/select-title</span>
                                                            <div class="description">
                                                                Seleciona um título para continuar o processo de geração.
                                                            </div>
                                                            <div class="params">
                                                                <h4>Parâmetros (JSON):</h4>
                                                                <table>
                                                                    <tr>
                                                                        <th>Nome</th>
                                                                        <th>Tipo</th>
                                                                        <th>Descrição</th>
                                                                        <th>Obrigatório</th>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>processId</td>
                                                                        <td>String</td>
                                                                        <td>ID do processo de geração</td>
                                                                        <td>Sim</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>selectedTitle</td>
                                                                        <td>String</td>
                                                                        <td>Título selecionado</td>
                                                                        <td>Sim</td>
                                                                    </tr>
                                                                </table>
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                                  "processId": "550e8400-e29b-41d4-a716-446655440000",
                                  "status": "TITLE_SELECTED",
                                  "message": "Título selecionado com sucesso"
                                }</pre>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/content/download/{processId}</span>
                                                            <div class="description">
                                                                Faz o download do conteúdo gerado.
                                                            </div>
                                                            <div class="params">
                                                                <h4>Parâmetros de Path:</h4>
                                                                <table>
                                                                    <tr>
                                                                        <th>Nome</th>
                                                                        <th>Tipo</th>
                                                                        <th>Descrição</th>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>processId</td>
                                                                        <td>String</td>
                                                                        <td>ID do processo de geração</td>
                                                                    </tr>
                                                                </table>
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <p>Arquivo de texto com o conteúdo gerado</p>
                                                            </div>
                                                        </div>

                                                        <h3>Configuração do Sistema</h3>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/config</span>
                                                            <div class="description">
                                                                Obtém as configurações atuais do sistema.
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                                  "outputPath": "./gerados",
                                  "createDirectoryIfNotExists": true
                                }</pre>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method post">POST</span>
                                                            <span class="path">/config/update-output-path</span>
                                                            <div class="description">
                                                                Atualiza o caminho de saída para os arquivos gerados.
                                                            </div>
                                                            <div class="params">
                                                                <h4>Parâmetros (JSON):</h4>
                                                                <table>
                                                                    <tr>
                                                                        <th>Nome</th>
                                                                        <th>Tipo</th>
                                                                        <th>Descrição</th>
                                                                        <th>Obrigatório</th>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>outputPath</td>
                                                                        <td>String</td>
                                                                        <td>Novo caminho para os arquivos gerados</td>
                                                                        <td>Sim</td>
                                                                    </tr>
                                                                </table>
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                                  "outputPath": "/novo/caminho",
                                  "createDirectoryIfNotExists": true,
                                  "message": "Caminho de saída atualizado com sucesso"
                                }</pre>
                                                            </div>
                                                        </div>

                                                        <h3>Documentação</h3>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/docs/architecture</span>
                                                            <div class="description">
                                                                Retorna o diagrama de arquitetura do sistema em formato Mermaid.
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <p>Texto com a sintaxe Mermaid para o diagrama de arquitetura</p>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/docs/architecture/view</span>
                                                            <div class="description">
                                                                Retorna uma página HTML que renderiza o diagrama de arquitetura.
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <p>Página HTML com o diagrama de arquitetura renderizado</p>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/docs/api</span>
                                                            <div class="description">
                                                                Retorna esta documentação da API.
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <p>Página HTML com a documentação da API</p>
                                                            </div>
                                                        </div>

                                                        <h3>WebSocket</h3>

                                                        <div class="endpoint">
                                                            <span class="method post">POST</span>
                                                            <span class="path">/ws</span>
                                                            <div class="description">
                                                                Endpoint para conexão WebSocket para receber atualizações em tempo real.
                                                            </div>
                                                            <div class="note">
                                                                <p>
                                                                    Para se conectar ao WebSocket, use o caminho <code>/api/ws</code> e os seguintes tópicos:
                                                                </p>
                                                                <ul>
                                                                    <li><code>/topic/process/{processId}</code> - Atualizações específicas de um processo</li>
                                                                    <li><code>/topic/notifications</code> - Notificações gerais do sistema</li>
                                                                </ul>
                                                            </div>
                                                        </div>

                                                        <h2>Códigos de Status</h2>
                                                        <table>
                                                            <tr>
                                                                <th>Código</th>
                                                                <th>Descrição</th>
                                                            </tr>
                                                            <tr>
                                                                <td>200 OK</td>
                                                                <td>A requisição foi bem-sucedida</td>
                                                            </tr>
                                                            <tr>
                                                                <td>201 Created</td>
                                                                <td>Um novo recurso foi criado com sucesso</td>
                                                            </tr>
                                                            <tr>
                                                                <td>400 Bad Request</td>
                                                                <td>A requisição contém parâmetros inválidos</td>
                                                            </tr>
                                                            <tr>
                                                                <td>404 Not Found</td>
                                                                <td>O recurso solicitado não foi encontrado</td>
                                                            </tr>
                                                            <tr>
                                                                <td>500 Internal Server Error</td>
                                                                <td>Ocorreu um erro no servidor</td>
                                                            </tr>
                                                        </table>

                                                        <h2>Modelos de Dados</h2>

                                                        <h3>ProcessStatus</h3>
                                                        <p>Possíveis valores para o status de um processo:</p>
                                                        <ul>
                                                            <li><code>INITIATED</code> - Processo iniciado</li>
                                                            <li><code>TITLES_GENERATED</code> - Títulos gerados</li>
                                                            <li><code>TITLE_SELECTED</code> - Título selecionado</li>
                                                            <li><code>ORACAO_GENERATED</code> - Oração gerada</li>
                                                            <li><code>SHORT_GENERATED</code> - Versão curta gerada</li>
                                                            <li><code>DESCRIPTION_GENERATED</code> - Descrição gerada</li>
                                                            <li><code>IMAGE_PROMPT_GENERATED</code> - Prompt para imagem gerado</li>
                                                            <li><code>COMPLETED</code> - Processo concluído</li>
                                                            <li><code>ERROR</code> - Erro no processo</li>
                                                        </ul>

                                                        <div class="note">
                                                            <p>
                                                                <strong>Nota:</strong> Esta documentação está sujeita a alterações à medida que a API evolui.
                                                                Verifique regularmente para obter as informações mais atualizadas.
                                                            </p>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/status</span>
                                                            <div class="description">
                                                                Retorna o status de todos os processos ativos.
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                  "activeProcesses": 2,
                  "processes": [
                    {
                      "processId": "550e8400-e29b-41d4-a716-446655440000",
                      "currentStage": "TITLES_GENERATED",
                      "progressPercentage": 20,
                      "startTime": "2023-10-15T14:30:45",
                      "lastUpdated": "2023-10-15T14:31:12",
                      "completed": false,
                      "tema": "Gratidão",
                      "titulo": "Oração de Gratidão pela Vida",
                      "hasAudio": false
                    },
                    {
                      "processId": "550e8400-e29b-41d4-a716-446655440001",
                      "currentStage": "COMPLETED",
                      "progressPercentage": 100,
                      "startTime": "2023-10-15T13:20:30",
                      "lastUpdated": "2023-10-15T13:25:45",
                      "completed": true,
                      "tema": "Proteção",
                      "titulo": "Oração de Proteção para a Família",
                      "hasAudio": true
                    }
                  ]
                }</pre>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/status/{processId}</span>
                                                            <div class="description">
                                                                Retorna o status detalhado de um processo específico.
                                                            </div>
                                                            <div class="params">
                                                                <h4>Parâmetros de Path:</h4>
                                                                <table>
                                                                    <tr>
                                                                        <th>Nome</th>
                                                                        <th>Tipo</th>
                                                                        <th>Descrição</th>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>processId</td>
                                                                        <td>String</td>
                                                                        <td>ID do processo</td>
                                                                    </tr>
                                                                </table>
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                  "processId": "550e8400-e29b-41d4-a716-446655440000",
                  "status": {
                    "processId": "550e8400-e29b-41d4-a716-446655440000",
                    "currentStage": "TITLES_GENERATED",
                    "progressPercentage": 20,
                    "startTime": "2023-10-15T14:30:45",
                    "lastUpdated": "2023-10-15T14:31:12",
                    "completed": false
                  },
                  "info": {
                    "tema": "Gratidão",
                    "estiloOracao": "Poético",
                    "duracao": "Média",
                    "tipoOracao": "Agradecimento",
                    "idioma": "Português",
                    "titulo": "Oração de Gratidão pela Vida",
                    "observacoes": "Incluir menção à natureza",
                    "gerarVersaoShort": true,
                    "gerarAudio": true,
                    "fullAudioId": null,
                    "shortAudioId": null
                  },
                  "titles": [
                    "Oração de Gratidão pela Vida",
                    "Agradecimento por Todas as Bênçãos",
                    "Gratidão Eterna pelo Dom da Vida"
                  ]
                }</pre>
                                                            </div>
                                                        </div>

                                                        <div class="endpoint">
                                                            <span class="method get">GET</span>
                                                            <span class="path">/status/stats</span>
                                                            <div class="description">
                                                                Retorna estatísticas gerais sobre os eventos do sistema.
                                                            </div>
                                                            <div class="response">
                                                                <h4>Resposta:</h4>
                                                                <pre>{
                  "totalProcesses": 10,
                  "completedProcesses": 7,
                  "activeProcesses": 3,
                  "statusDistribution": {
                    "INITIATED": 1,
                    "TITLES_GENERATED": 1,
                    "ORACAO_GENERATED": 1,
                    "COMPLETED": 7
                  },
                  "processesLast24Hours": 5
                }</pre>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </body>
                                                </html>
                                                """;

        return ResponseEntity.ok(htmlContent);
    }

    /**
     * Retorna a página inicial da documentação
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getDocumentationIndex() {
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>Documentação - Roteiro</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            margin: 0;
                            padding: 20px;
                            color: #333;
                            background-color: #f5f5f5;
                        }
                        .container {
                            max-width: 800px;
                            margin: 0 auto;
                            background-color: white;
                            padding: 30px;
                            border-radius: 8px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        h1 {
                            color: #2c3e50;
                            border-bottom: 2px solid #3498db;
                            padding-bottom: 10px;
                        }
                        h2 {
                            color: #2980b9;
                            margin-top: 30px;
                        }
                        .card {
                            background-color: #f8f9fa;
                            border-left: 4px solid #3498db;
                            padding: 20px;
                            margin-bottom: 20px;
                            border-radius: 0 4px 4px 0;
                            transition: transform 0.3s ease, box-shadow 0.3s ease;
                        }
                        .card:hover {
                            transform: translateY(-5px);
                            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
                        }
                        a {
                            color: #3498db;
                            text-decoration: none;
                        }
                        a:hover {
                            text-decoration: underline;
                        }
                        .btn {
                            display: inline-block;
                            background-color: #3498db;
                            color: white;
                            padding: 10px 15px;
                            border-radius: 4px;
                            text-decoration: none;
                            margin-top: 10px;
                            transition: background-color 0.3s ease;
                        }
                        .btn:hover {
                            background-color: #2980b9;
                            text-decoration: none;
                        }
                        .footer {
                            margin-top: 40px;
                            text-align: center;
                            color: #7f8c8d;
                            font-size: 0.9em;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Documentação do Sistema Roteiro</h1>
                        <p>
                            Bem-vindo à documentação do Sistema Roteiro, uma plataforma para geração de conteúdo
                            de orações utilizando inteligência artificial. Esta documentação fornece informações
                            sobre a API, arquitetura e funcionamento do sistema.
                        </p>

                        <div class="card">
                            <h2>Documentação da API</h2>
                            <p>
                                Documentação completa dos endpoints disponíveis na API, incluindo parâmetros,
                                respostas e exemplos de uso.
                            </p>
                            <a href="/api/docs/api" class="btn">Ver Documentação da API</a>
                        </div>

                        <div class="card">
                            <h2>Arquitetura do Sistema</h2>
                            <p>
                                Diagrama visual da arquitetura do sistema, mostrando os componentes, serviços
                                e fluxo de dados.
                            </p>
                            <a href="/api/docs/architecture/view" class="btn">Ver Diagrama de Arquitetura</a>
                        </div>

                        <div class="card">
                            <h2>Código-fonte Mermaid</h2>
                            <p>
                                Código-fonte do diagrama de arquitetura em formato Mermaid, para uso em
                                outras ferramentas ou documentações.
                            </p>
                            <a href="/api/docs/architecture" class="btn">Ver Código Mermaid</a>
                        </div>

                        <div class="card">
                            <h2>Documentação Interativa (Swagger)</h2>
                            <p>
                                Interface interativa para explorar e testar os endpoints da API diretamente
                                no navegador.
                            </p>
                            <a href="/api/swagger-ui/index.html" class="btn">Abrir Swagger UI</a>
                        </div>

                        <div class="card">
                            <h2>Eventos WebSocket</h2>
                            <p>
                                Documentação dos eventos WebSocket disponíveis para comunicação em tempo real.
                            </p>
                            <a href="/api/docs/websocket-events" class="btn">Ver Eventos WebSocket</a>
                        </div>

                        <div class="card">
                            <h2>Mapeamento Frontend-Backend</h2>
                            <p>
                                Tabela de mapeamento entre componentes React e controllers/services Java.
                            </p>
                            <a href="/api/docs/frontend-mapping" class="btn">Ver Mapeamento</a>
                        </div>

                        <div class="card">
                            <h2>Exemplos de Fluxos Completos</h2>
                            <p>
                                Exemplos detalhados de fluxos completos desde o frontend até o backend.
                            </p>
                            <a href="/api/docs/workflow-examples" class="btn">Ver Exemplos de Fluxos</a>
                        </div>

                        <div class="card">
                            <h2>Interfaces TypeScript</h2>
                            <p>
                                Documentação sobre a geração automática de interfaces TypeScript a partir dos DTOs Java,
                                facilitando a integração entre o backend e o frontend.
                            </p>
                            <a href="/api/docs/typescript-interfaces" class="btn">Ver Documentação</a>
                        </div>

                        <div class="footer">
                            <p>Sistema Roteiro &copy; 2023 - Todos os direitos reservados</p>
                        </div>
                    </div>
                </body>
                </html>
                """;

        return ResponseEntity.ok(htmlContent);
    }

    /**
     * Retorna o mapeamento entre componentes React e controllers/services Java
     */
    @GetMapping(value = "/frontend-mapping", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getFrontendBackendMapping() {
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Mapeamento Frontend-Backend</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; }
                    </style>
                </head>
                <body>
                    <h1>Mapeamento de Componentes Frontend-Backend</h1>
                    <table>
                        <tr>
                            <th>Componente React</th>
                            <th>Controller/Service Java</th>
                            <th>Descrição</th>
                        </tr>
                        <tr>
                            <td>GenerationForm.tsx</td>
                            <td>ContentGenerationController.startGeneration()</td>
                            <td>Formulário de geração de conteúdo e início do processo</td>
                        </tr>
                        <!-- Mais mapeamentos aqui -->
                    </table>
                </body>
                </html>
                """;
        return ResponseEntity.ok(htmlContent);
    }

    /**
     * Retorna a documentação dos eventos WebSocket
     */
    @GetMapping(value = "/websocket-events", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getWebSocketEventsDocumentation() {
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Documentação de Eventos WebSocket</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; }
                    </style>
                </head>
                <body>
                    <h1>Documentação de Eventos WebSocket</h1>
                    <table>
                        <tr>
                            <th>Evento</th>
                            <th>Descrição</th>
                            <th>Formato de Mensagem</th>
                        </tr>
                        <tr>
                            <td>processUpdate</td>
                            <td>Atualizações sobre o status do processo</td>
                            <td>{ "processId": "string", "status": "string", "progress": "int" }</td>
                        </tr>
                        <tr>
                            <td>notification</td>
                            <td>Notificações gerais do sistema</td>
                            <td>{ "type": "string", "message": "string" }</td>
                        </tr>
                        <!-- Mais eventos aqui -->
                    </table>
                </body>
                </html>
                """;
        return ResponseEntity.ok(htmlContent);
    }

    /**
     * Retorna exemplos de fluxos completos desde o frontend até o backend
     */
    @GetMapping(value = "/workflow-examples", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getWorkflowExamples() {
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Exemplos de Fluxos Completos</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                        .workflow {
                            background-color: #f8f9fa;
                            border-left: 4px solid #3498db;
                            padding: 15px;
                            margin-bottom: 30px;
                            border-radius: 0 4px 4px 0;
                        }
                        .step {
                            margin-bottom: 15px;
                            padding-left: 20px;
                            position: relative;
                        }
                        .step:before {
                            content: "";
                            position: absolute;
                            left: 0;
                            top: 8px;
                            width: 10px;
                            height: 10px;
                            border-radius: 50%;
                            background-color: #3498db;
                        }
                        .step:after {
                            content: "";
                            position: absolute;
                            left: 4px;
                            top: 18px;
                            width: 2px;
                            height: calc(100% - 8px);
                            background-color: #3498db;
                        }
                        .step:last-child:after {
                            display: none;
                        }
                        .frontend { color: #e74c3c; }
                        .backend { color: #2ecc71; }
                        .code {
                            background-color: #f1f1f1;
                            padding: 2px 5px;
                            border-radius: 3px;
                            font-family: monospace;
                        }
                        h3 { color: #3498db; }
                    </style>
                </head>
                <body>
                    <h1>Exemplos de Fluxos Completos</h1>
                    <p>
                        Esta página demonstra fluxos completos de interação entre o frontend e o backend,
                        mostrando como os componentes se comunicam durante processos típicos do sistema.
                    </p>

                    <div class="workflow">
                        <h3>Fluxo 1: Geração de Conteúdo de Oração</h3>

                        <div class="step">
                            <h4>1. Iniciar Geração de Conteúdo</h4>
                            <p><span class="frontend">Frontend</span>: O usuário preenche o formulário com o tema da oração e clica em "Gerar".</p>
                            <p><span class="code">GenerationForm.tsx</span> envia uma requisição POST para <span class="code">/api/content/generate</span></p>
                            <p><span class="backend">Backend</span>: <span class="code">ContentGenerationController.startGeneration()</span> processa a requisição e inicia o processo.</p>
                            <p>Retorna: <span class="code">{ processId: "uuid", status: "INITIATED" }</span></p>
                        </div>

                        <div class="step">
                            <h4>2. Receber Atualizações via WebSocket</h4>
                            <p><span class="frontend">Frontend</span>: <span class="code">ProcessTracker.tsx</span> se conecta ao WebSocket.</p>
                            <p><span class="code">socket.subscribe('/topic/process/{processId}')</span></p>
                            <p><span class="backend">Backend</span>: <span class="code">WebSocketService</span> envia atualizações quando os títulos são gerados.</p>
                            <p>Mensagem: <span class="code">{ processId: "uuid", status: "TITLES_GENERATED", titles: ["Título 1", "Título 2", "Título 3"] }</span></p>
                        </div>

                        <div class="step">
                            <h4>3. Selecionar um Título</h4>
                            <p><span class="frontend">Frontend</span>: O usuário seleciona um dos títulos gerados.</p>
                            <p><span class="code">TitleSelector.tsx</span> envia uma requisição POST para <span class="code">/api/content/select-title</span></p>
                            <p><span class="backend">Backend</span>: <span class="code">ContentGenerationController.selectTitle()</span> processa a seleção e continua o processo.</p>
                            <p>Retorna: <span class="code">{ processId: "uuid", status: "TITLE_SELECTED" }</span></p>
                        </div>

                        <div class="step">
                            <h4>4. Receber Notificação de Conclusão</h4>
                            <p><span class="frontend">Frontend</span>: <span class="code">ProcessTracker.tsx</span> continua ouvindo o WebSocket.</p>
                            <p><span class="backend">Backend</span>: Após completar todas as etapas (geração de oração, versão curta, descrição), o <span class="code">WebSocketService</span> envia uma notificação de conclusão.</p>
                            <p>Mensagem: <span class="code">{ processId: "uuid", status: "COMPLETED" }</span></p>
                        </div>

                        <div class="step">
                            <h4>5. Baixar o Conteúdo Gerado</h4>
                            <p><span class="frontend">Frontend</span>: <span class="code">DownloadButton.tsx</span> é ativado e o usuário clica para baixar.</p>
                            <p>Faz uma requisição GET para <span class="code">/api/content/download/{processId}</span></p>
                            <p><span class="backend">Backend</span>: <span class="code">ContentGenerationController.downloadContent()</span> retorna o arquivo gerado.</p>
                        </div>
                    </div>

                    <div class="workflow">
                        <h3>Fluxo 2: Configuração do Sistema</h3>

                        <div class="step">
                            <h4>1. Visualizar Configurações Atuais</h4>
                            <p><span class="frontend">Frontend</span>: <span class="code">ConfigPanel.tsx</span> faz uma requisição GET para <span class="code">/api/config</span></p>
                            <p><span class="backend">Backend</span>: <span class="code">SystemConfigController.getConfig()</span> retorna as configurações atuais.</p>
                            <p>Retorna: <span class="code">{ outputPath: "./gerados", createDirectoryIfNotExists: true }</span></p>
                        </div>

                        <div class="step">
                            <h4>2. Atualizar Caminho de Saída</h4>
                            <p><span class="frontend">Frontend</span>: O usuário edita o caminho e salva.</p>
                            <p><span class="code">ConfigForm.tsx</span> envia uma requisição POST para <span class="code">/api/config/update-output-path</span></p>
                            <p><span class="backend">Backend</span>: <span class="code">SystemConfigController.updateOutputPath()</span> atualiza a configuração.</p>
                            <p>Retorna: <span class="code">{ outputPath: "/novo/caminho", createDirectoryIfNotExists: true, message: "Caminho atualizado com sucesso" }</span></p>
                        </div>
                    </div>
                </body>
                </html>
                """;
        return ResponseEntity.ok(htmlContent);
    }

    /**
     * Retorna a documentação sobre a Geração Automática de Interfaces TypeScript
     */
    @GetMapping(value = "/typescript-interfaces", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getTypeScriptInterfacesDocumentation() {
        String htmlContent = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta charset="utf-8">
                                    <title>Interfaces TypeScript Geradas Automaticamente - Roteiro</title>
                                    <style>
                                        body {
                                            font-family: Arial, sans-serif;
                                            line-height: 1.6;
                                            margin: 0;
                                            padding: 20px;
                                            color: #333;
                                        }
                                        .container {
                                            max-width: 1000px;
                                            margin: 0 auto;
                                            background-color: white;
                                            padding: 30px;
                                            border-radius: 8px;
                                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                                        }
                                        h1 {
                                            color: #2c3e50;
                                            border-bottom: 2px solid #3498db;
                                            padding-bottom: 10px;
                                        }
                                        h2 {
                                            color: #2980b9;
                                            margin-top: 30px;
                                        }
                                        h3 {
                                            color: #3498db;
                                            margin-top: 20px;
                                        }
                                        pre {
                                            background-color: #f8f9fa;
                                            padding: 15px;
                                            border-radius: 4px;
                                            overflow-x: auto;
                                            border-left: 4px solid #3498db;
                                        }
                                        code {
                                            font-family: Consolas, Monaco, 'Andale Mono', monospace;
                                            background-color: #f1f1f1;
                                            padding: 2px 5px;
                                            border-radius: 3px;
                                        }
                                        .note {
                                            background-color: #fff3cd;
                                            border-left: 4px solid #ffc107;
                                            padding: 15px;
                                            margin: 15px 0;
                                            border-radius: 0 4px 4px 0;
                                        }
                                        .step {
                                            background-color: #f8f9fa;
                                            padding: 15px;
                                            margin-bottom: 20px;
                                            border-radius: 4px;
                                            counter-increment: step-counter;
                                            position: relative;
                                            padding-left: 50px;
                                        }
                                        .step:before {
                                            content: counter(step-counter);
                                            position: absolute;
                                            left: 15px;
                                            top: 15px;
                                            background-color: #3498db;
                                            color: white;
                                            width: 25px;
                                            height: 25px;
                                            border-radius: 50%;
                                            text-align: center;
                                            line-height: 25px;
                                            font-weight: bold;
                                        }
                                        table {
                                            width: 100%;
                                            border-collapse: collapse;
                                            margin: 20px 0;
                                        }
                                        th, td {
                                            border: 1px solid #ddd;
                                            padding: 8px;
                                            text-align: left;
                                        }
                                        th {
                                            background-color: #f2f2f2;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <h1>Interfaces TypeScript Geradas Automaticamente</h1>

                                        <p>
                                            O Sistema Roteiro utiliza a geração automática de interfaces TypeScript a partir dos DTOs Java,
                                            facilitando a integração entre o backend e o frontend. Isso garante que os tipos de dados
                                            sejam consistentes em toda a aplicação e reduz erros de integração.
                                        </p>

                                        <div class="note">
                                            <strong>Nota:</strong> As interfaces TypeScript são geradas automaticamente durante o build do projeto
                                            usando o plugin <code>typescript-generator-maven-plugin</code>.
                                        </div>

                                        <h2>Como Funciona</h2>

                                        <p>
                                            O processo de geração de interfaces TypeScript é automatizado e ocorre durante a fase
                                            <code>process-classes</code> do ciclo de vida do Maven. O plugin analisa as classes Java
                                            especificadas na configuração e gera interfaces TypeScript correspondentes.
                                        </p>

                                        <h3>Configuração no pom.xml</h3>

                                        <pre>
                &lt;plugin&gt;
                    &lt;groupId&gt;cz.habarta.typescript-generator&lt;/groupId&gt;
                    &lt;artifactId&gt;typescript-generator-maven-plugin&lt;/artifactId&gt;
                    &lt;version&gt;3.2.1263&lt;/version&gt;
                    &lt;executions&gt;
                        &lt;execution&gt;
                            &lt;id&gt;generate&lt;/id&gt;
                            &lt;goals&gt;
                                &lt;goal&gt;generate&lt;/goal&gt;
                            &lt;/goals&gt;
                            &lt;phase&gt;process-classes&lt;/phase&gt;
                        &lt;/execution&gt;
                    &lt;/executions&gt;
                    &lt;configuration&gt;
                        &lt;jsonLibrary&gt;jackson2&lt;/jsonLibrary&gt;
                        &lt;classes&gt;
                            &lt;class&gt;dev.luisoliveira.roteiro.dto.ProcessStatusDTO&lt;/class&gt;
                            &lt;class&gt;dev.luisoliveira.roteiro.dto.ContentGenerationRequestDTO&lt;/class&gt;
                            &lt;class&gt;dev.luisoliveira.roteiro.dto.TitleSelectionRequestDTO&lt;/class&gt;
                            &lt;class&gt;dev.luisoliveira.roteiro.dto.SystemConfigDTO&lt;/class&gt;
                        &lt;/classes&gt;
                        &lt;outputKind&gt;module&lt;/outputKind&gt;
                        &lt;outputFile&gt;target/generated-sources/typescript/api-models.ts&lt;/outputFile&gt;
                        &lt;mapEnum&gt;asEnum&lt;/mapEnum&gt;
                        &lt;optionalProperties&gt;useLibraryDefinition&lt;/optionalProperties&gt;
                        &lt;mapClasses&gt;asInterfaces&lt;/mapClasses&gt;
                        &lt;generateNpmPackageJson&gt;true&lt;/generateNpmPackageJson&gt;
                        &lt;npmName&gt;@roteiro/api-models&lt;/npmName&gt;
                        &lt;npmVersion&gt;1.0.0&lt;/npmVersion&gt;
                    &lt;/configuration&gt;
                &lt;/plugin&gt;
                                        </pre>

                                        <h2>DTOs Mapeados para TypeScript</h2>

                                        <p>
                                            Os seguintes DTOs Java são mapeados para interfaces TypeScript:
                                        </p>

                                        <table>
                                            <tr>
                                                <th>DTO Java</th>
                                                <th>Interface TypeScript</th>
                                                <th>Descrição</th>
                                            </tr>
                                            <tr>
                                                <td>ProcessStatusDTO</td>
                                                <td>ProcessStatusDTO</td>
                                                <td>Representa o status de um processo de geração de conteúdo</td>
                                            </tr>
                                            <tr>
                                                <td>ContentGenerationRequestDTO</td>
                                                <td>ContentGenerationRequestDTO</td>
                                                <td>Requisição para iniciar a geração de conteúdo</td>
                                            </tr>
                                            <tr>
                                                <td>TitleSelectionRequestDTO</td>
                                                <td>TitleSelectionRequestDTO</td>
                                                <td>Requisição para selecionar um título</td>
                                            </tr>
                                            <tr>
                                                <td>SystemConfigDTO</td>
                                                <td>SystemConfigDTO</td>
                                                <td>Configurações do sistema</td>
                                            </tr>
                                        </table>

                                        <h2>Como Usar no Frontend</h2>

                                        <div class="step">
                                            <h3>Passo 1: Gerar as Interfaces TypeScript</h3>
                                            <p>
                                                Execute o comando Maven para gerar as interfaces TypeScript:
                                            </p>
                                            <pre>mvn clean compile</pre>
                                            <p>
                                                As interfaces serão geradas em <code>target/generated-sources/typescript/api-models.ts</code>
                                            </p>
                                        </div>

                                        <div class="step">
                                            <h3>Passo 2: Copiar para o Projeto Frontend</h3>
                                            <p>
                                                Copie o arquivo <code>api-models.ts</code> para o seu projeto frontend, por exemplo:
                                            </p>
                                            <pre>cp target/generated-sources/typescript/api-models.ts ../frontend/src/types/</pre>
                                        </div>

                                        <div class="step">
                                            <h3>Passo 3: Importar e Usar as Interfaces</h3>
                                            <p>
                                                Importe as interfaces em seus componentes React:
                                            </p>
                                            <pre>
                import { ContentGenerationRequestDTO, ProcessStatusDTO } from '../types/api-models';

                // Exemplo de uso em um componente React
                const GenerationForm: React.FC = () => {
                    const [request, setRequest] = useState&lt;ContentGenerationRequestDTO&gt;({
                        theme: '',
                        style: 'formal'
                    });

                    const handleSubmit = async () => {
                        const response = await fetch('/api/content/generate', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify(request)
                        });

                        const result: ProcessStatusDTO = await response.json();
                        // Processar o resultado...
                    };

                    return (
                        // JSX do formulário...
                    );
                };
                                            </pre>
                                        </div>

                                        <h2>Alternativa: Usar como Pacote NPM</h2>

                                        <p>
                                            O plugin também gera um arquivo <code>package.json</code>, permitindo que você publique
                                            as interfaces como um pacote NPM privado. Isso é útil para projetos maiores com múltiplos
                                            repositórios frontend.
                                        </p>

                                        <pre>
                // Publicar o pacote NPM
                cd target/generated-sources/typescript
                npm publish

                // Instalar no projeto frontend
                npm install @roteiro/api-models
                                        </pre>

                                        <h2>Benefícios</h2>

                                        <ul>
                                            <li>Garantia de tipo entre frontend e backend</li>
                                            <li>Redução de erros de integração</li>
                                            <li>Autocompletar e verificação de tipo no IDE</li>
                                            <li>Documentação automática dos tipos de dados</li>
                                            <li>Facilidade de manutenção quando os DTOs mudam</li>
                                        </ul>

                                        <div class="note">
                                            <strong>Dica:</strong> Configure seu pipeline de CI/CD para gerar as interfaces TypeScript
                                            automaticamente e publicá-las como um pacote NPM a cada build bem-sucedido.
                                        </div>
                                    </div>
                                </body>
                                </html>
                                """;
        return ResponseEntity.ok(htmlContent);
    }
}