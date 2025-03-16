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

                        <div class="footer">
                            <p>Sistema Roteiro &copy; 2023 - Todos os direitos reservados</p>
                        </div>
                    </div>
                </body>
                </html>
                """;

        return ResponseEntity.ok(htmlContent);
    }
}