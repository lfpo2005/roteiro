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
}