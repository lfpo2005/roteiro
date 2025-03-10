package dev.luisoliveira.roteiro.util;


public class PromptBuilder {

    // Constantes com dados da análise
    private static final String[] PALAVRAS_CHAVE = {
            "PODEROSA", "PROTECCIÓN", "BENDICIONES", "TRANSFORMAR", "RENOVAR"
    };

    private static final String[] TEMAS_EFICAZES = {
            "protección familiar", "mensajes diarios", "renovación financiera", "transformación personal"
    };

    public static String buildTitlePrompt(String tema, String estilo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie 5 títulos em espanhol para uma oração sobre \"")
                .append(tema)
                .append("\" no estilo \"")
                .append(estilo)
                .append("\".\n\n");

        prompt.append("Use esta fórmula de título que tem o melhor CTR (5,75%):\n");
        prompt.append("[PODEROSA ORACIÓN] para [tema específico] [benefício específico] #oración #hashtag\n\n");

        prompt.append("Diretrizes importantes baseadas em análise de dados:\n");
        prompt.append("1. Use 5-8 palavras no título (CTR médio: 3,07%)\n");
        prompt.append("2. SEMPRE inclua hashtags no título (dobram visualizações)\n");
        prompt.append("3. Use MAIÚSCULAS para palavras-chave importantes\n");
        prompt.append("4. Inclua 1-2 emojis estratégicos no final\n");
        prompt.append("5. Use palavras de poder como: PODEROSA, PROTECCIÓN, BENDICIONES\n\n");

        prompt.append("Exemplos de títulos de sucesso:\n");
        prompt.append("- PODEROSA ORACION POR LOS HIJOS POR LA PROTECCIÓN #oración #salmo91\n");
        prompt.append("- Dios Tiene un Mensaje Para Ti Hoy! Escucha Esta Oración Matutina #Oración\n");
        prompt.append("- ORACIÓN PODEROSA para RENOVAR TUS FINANZAS #feyesperanza\n\n");

        prompt.append("Forneça apenas a lista de 5 títulos, sem comentários adicionais.");

        return prompt.toString();
    }

    public static String buildOracaoPrompt(String tema, String estilo, String duracao, String titulo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie uma oração em espanhol com o título: \"").append(titulo).append("\"\n\n");

        prompt.append("Tema: ").append(tema).append("\n");
        prompt.append("Estilo: ").append(estilo).append("\n");
        prompt.append("Duração: ").append(duracao).append("\n\n");

        prompt.append("A oração deve seguir esta estrutura:\n");
        prompt.append("1. Versículo inicial relevante (\"Versículo del día:\")\n");
        prompt.append("2. Introdução com saudação e relevância do tema (6-8 linhas)\n");
        prompt.append("3. Reflexão bíblica com passagem relevante (6-8 linhas)\n");
        prompt.append("4. Oração principal com:\n");
        prompt.append("   - Invocação inicial\n");
        prompt.append("   - Desenvolvimento do tema\n");
        prompt.append("   - Aplicação prática\n");
        prompt.append("   - Pedidos específicos\n");
        prompt.append("   - Declaração de fé\n");
        prompt.append("5. Encerramento com:\n");
        prompt.append("   - Recapitulação\n");
        prompt.append("   - Versículo final (diferente do inicial)\n");
        prompt.append("   - Bênção final\n");
        prompt.append("   - Convite para inscrição\n");
        prompt.append("   - \"En el nombre de Jesús, Amén\"\n\n");

        // Ajustar tamanho baseado na duração
        if (duracao.contains("Short1")) {
            prompt.append("Tamanho total: 300-500 caracteres (30-60 segundos)");
        } else if (duracao.contains("Short2")) {
            prompt.append("Tamanho total: 500-800 caracteres (60-90 segundos)");
        } else if (duracao.contains("Mini")) {
            prompt.append("Tamanho total: 800-1.200 caracteres (2-3 minutos)");
        } else if (duracao.contains("Padrão")) {
            prompt.append("Tamanho total: 1.800-2.200 caracteres (5 minutos)");
        } else if (duracao.contains("Completa")) {
            prompt.append("Tamanho total: 3.500-4.000 caracteres (8-10 minutos)");
        } else if (duracao.contains("Expandida")) {
            prompt.append("Tamanho total: 6.000-12.000 caracteres (15+ minutos)");
        } else {
            // Formato ideal baseado na análise
            prompt.append("Tamanho total: 5.000-6.000 caracteres (15-30 minutos)");
        }

        return prompt.toString();
    }

    // Métodos adicionais para outros tipos de prompts...
}